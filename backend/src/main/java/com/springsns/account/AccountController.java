package com.springsns.account;

import com.springsns.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @PostMapping("/users/signin")
    public ResponseEntity signInSubmit(@RequestBody SignInForm signInForm, Errors errors) {
        Account account = accountRepository.findByEmail(signInForm.getEmail()).orElseThrow(() -> new IllegalArgumentException("가입되지 않은 E-MAIL 입니다."));

        if (!accountService.isValidPassword(signInForm.getPassword(), account.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        //jwt 토큰 생성
        String jwtToken = jwtTokenProvider.createToken(account.getUsername(), account.getRoles());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("jwtToken", jwtToken); // jwt 토큰 저장.
        //resultMap.put("emailVerified", account.isEmailVerified());
        //account를 넘겨주면서 email verified 여부까지 넘어간다.
        resultMap.put("user",account);  //TODO 비밀번호까지 다 넘어가는데 이메일 닉네임 정도만 가도록 DTO 만들 것.

        return new ResponseEntity(resultMap, HttpStatus.OK);

    }

    @PostMapping("/sign-up")
    public ResponseEntity signUpSubmit(@Valid @RequestBody SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        //new account 생성 저장, email check token 생성, email 보내기.
        Account account = accountService.processNewAccount(signUpForm);

        return ResponseEntity.ok().body(account);
    }

    @GetMapping("/check-email-token")
    public ResponseEntity checkEmailToken(String token, String email) {

        System.out.println(token);
        System.out.println(email);

        Account account = accountRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("가입되지 않은 E-MAIL 입니다."));

        //토큰 비교해서 다른 경우.
        if (!account.isValidToken(token)) {
            return ResponseEntity.badRequest().build();
        }

        //emailVerified를 true로 만들고 등록날짜 설정.
        Account emailVerifiedAccount = accountService.completeSignUp(account);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("user", emailVerifiedAccount);

        return new ResponseEntity(resultMap, HttpStatus.OK);

    }


}
