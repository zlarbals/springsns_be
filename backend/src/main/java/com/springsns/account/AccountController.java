package com.springsns.account;

import com.springsns.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final SignInFormValidator signInFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    //signUpForm이라는 데이터를 받을 때 바인더를 설정할 수 있다.
    @InitBinder("signUpForm")
    public void initBinderForSignUp(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @InitBinder("signInForm")
    public void initBinderForSignIn(WebDataBinder webDataBinder){
        webDataBinder.addValidators(signInFormValidator);
    }

    @PostMapping("/account")
    public ResponseEntity signUpSubmit(@Valid @RequestBody SignUpForm signUpForm, Errors errors) {
        System.out.println("post /account");
        if (errors.hasErrors()) {
            Map<String, Object> resultMap = new HashMap<>();

            List<ObjectError> allErrors = errors.getAllErrors();
            List<String> errorsMessage=new ArrayList<>();
            for(ObjectError error:allErrors){
                errorsMessage.add(error.getDefaultMessage());
            }

            resultMap.put("error",errorsMessage);
            return ResponseEntity.badRequest().body(resultMap);
        }

        //new account 생성 저장, email check token 생성, email 보내기.
        AccountResponseDto accountResponseDto = accountService.processNewAccount(signUpForm);

        return ResponseEntity.ok().body(accountResponseDto);
    }

    @PostMapping("/account/sign-in")
    public ResponseEntity signInSubmit(@Valid @RequestBody SignInForm signInForm, Errors errors) {
        System.out.println("post /account/sign-in");
        if(errors.hasErrors()){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        Account account = accountRepository.findByEmail(signInForm.getEmail());

        Map<String,Object> resultMap = new HashMap<>();
        //jwt토큰과 user 정보를 맵에 리턴.
        String jwt = accountService.createJWTToken(signInForm);

        resultMap.put("user",new AccountResponseDto(account));

        ResponseCookie responseCookie = ResponseCookie.from("jwt",jwt)
                        .httpOnly(true).build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,responseCookie.toString())
                .body(resultMap);
    }

    @GetMapping("/account/check-email-token")
    public ResponseEntity checkEmailToken(String token, String email) {
        System.out.println("get /account/check-email-token");

        //Account account = accountRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("가입되지 않은 E-MAIL 입니다."));
        Account account = accountRepository.findByEmail(email);

        //토큰 비교해서 다른 경우.
        if (account == null || !account.isValidToken(token)) {
            return ResponseEntity.badRequest().build();
        }

        //emailVerified를 true로 만들고 등록날짜 설정.
        AccountResponseDto accountResponseDto = accountService.completeSignUp(account);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("user", accountResponseDto);

        return new ResponseEntity(resultMap, HttpStatus.OK);
    }

    @GetMapping("/account/resend-email-token")
    public ResponseEntity sendEmail(Principal principal){
        System.out.println("get /account/resend-email-token");
        HashMap<String,Object> resultMap = new HashMap<>();
        String email = principal.getName();

        boolean isOk = accountService.resendEmail(email);

        if(isOk){
            return new ResponseEntity(HttpStatus.OK);
        }else{
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    //URI만 작성.
    //TODO 비밀번호 변경 구현.  TDD로 도전 해볼 것.
    @PatchMapping("/account")
    public ResponseEntity changePassword(@RequestBody ChangePasswordForm changePasswordForm,Principal principal){
        System.out.println("patch /account");
        String email = principal.getName();

        Account account = accountRepository.findByEmail(email);

        AccountResponseDto accountResponseDto = accountService.changePassword(changePasswordForm.getPassword(), email);

        return new ResponseEntity(accountResponseDto,HttpStatus.OK);
    }

    //URI만 작성.
    //TODO 회원탈퇴 구현.   TDD로 도전 해볼 것.
    @DeleteMapping("/account")
    public ResponseEntity deleteAccount(Principal principal){
        System.out.println("delete /account");

        String email = principal.getName();
        accountService.processDeleteAccount(email);

        return new ResponseEntity(HttpStatus.OK);
    }


}
