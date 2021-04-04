package com.springsns.account;

import com.springsns.domain.Account;
import com.sun.mail.iap.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final JwtTokenProvider jwtTokenProvider;

    //signUpForm이라는 데이터를 받을 때 바인더를 설정할 수 있다.
    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(signUpFormValidator);
    }

    @PostMapping("/users/signin")
    public ResponseEntity signInSubmit(@RequestBody SignInForm signInForm,Errors errors){
        Account account = accountRepository.findByEmail(signInForm.getEmail()).orElseThrow(()->new IllegalArgumentException("가입되지 않은 E-MAIL 입니다."));

        if(!accountService.isValidPassword(signInForm.getPassword(),account.getPassword())){
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        String jwtToken = jwtTokenProvider.createToken(account.getUsername(),account.getRoles());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("jwtToken",jwtToken);

        return new ResponseEntity(resultMap, HttpStatus.OK);

    }

    @PostMapping("/sign-up")
    public ResponseEntity signUpSubmit(@Valid @RequestBody SignUpForm signUpForm, Errors errors){
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().body(errors);
        }

        Account account = accountService.processNewAccount(signUpForm);
        //accountService.login(account);  // 리액트에서도 마찬가지로 바로 로그인 되게 설정해야한다.

        return ResponseEntity.ok().body(account); //TODO body에 뭘 넣어야 할지 고민을 해야 할 듯하다. 다른 rest api 사례를 보자.
        //return ResponseEntity.ok().body(newAccount);
   }

   @GetMapping("/check-email-token")
    public ResponseEntity checkEmailToken(String token,String email){
        Account account = accountRepository.findByEmail(email).orElseThrow(()->new IllegalArgumentException("가입되지 않은 E-MAIL 입니다."));

        //없는 email인 경우.
//        if(account==null){
//            return ResponseEntity.badRequest().build();
//        }

        //토큰 비교해서 다른 경우.
        if(!account.isValidToken(token)){
            return ResponseEntity.badRequest().build();
        }

        account.completeSignUp();
        //accountService.login(account); // 리액트에서도 바로 로그인되게 설정해야 한다. 해당 요청이 오면.

        //accountRepository.count()를 통해 몇번째 회원인지 리턴해준다.
        return ResponseEntity.ok().build();

   }

}
