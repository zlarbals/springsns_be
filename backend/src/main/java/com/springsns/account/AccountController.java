package com.springsns.account;

import com.springsns.advice.Result;
import com.springsns.domain.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/account")
    public ResponseEntity<Result> signUpSubmit(@Validated @RequestBody SignUpForm signUpForm, BindingResult bindingResult) {
        log.info("AccountController.Post./account");

        if(bindingResult.hasErrors()){
            log.info("sign up submit error : {}",bindingResult);
            throw new IllegalArgumentException("잘못된 형식입니다.");
        }

        Account account = accountService.processSignUpAccount(signUpForm);

        AccountResponseDto accountResponseDto = new AccountResponseDto(account);

        return new ResponseEntity(new Result(HttpStatus.CREATED,accountResponseDto),HttpStatus.CREATED);
    }

    @PostMapping("/account/sign-in")
    public ResponseEntity<Result> signInSubmit(@Validated @RequestBody SignInForm signInForm, BindingResult bindingResult) {
        log.info("AccountController.Post./sign-in");

        if(bindingResult.hasErrors()){
            log.info("sign in submit error : {}",bindingResult);
            throw new IllegalArgumentException("잘못된 형식입니다.");
        }

        String bearerAuthJWT = accountService.processSignInAccount(signInForm.getEmail(),signInForm.getPassword());

        return new ResponseEntity(new Result(HttpStatus.OK,bearerAuthJWT), HttpStatus.OK);
    }

    @GetMapping("/account/check-email-token")
    public ResponseEntity<Result> checkEmailToken(@RequestParam String token,@RequestParam String email) {
        log.info("AccountController.Get./account/check-email-token");

        Account account = accountService.verifyEmailToken(email,token);

        AccountResponseDto accountResponseDto = new AccountResponseDto(account);

        return new ResponseEntity(new Result(HttpStatus.OK,accountResponseDto), HttpStatus.OK);
    }

    @GetMapping("/account/resend-email-token")
    public ResponseEntity resendEmail(HttpServletRequest request){
        log.info("AccountController.Get./account/resend-email-token");

        String email = (String) request.getAttribute("SignInAccountEmail");

        accountService.resendEmail(email);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/account")
    public ResponseEntity<Result> changePassword(@Validated @RequestBody ChangePasswordForm changePasswordForm,BindingResult bindingResult,HttpServletRequest request){
        log.info("AccountController.Patch./account");

        if(bindingResult.hasErrors()){
            log.info("change password error : {}",bindingResult);
            return new ResponseEntity(bindingResult.getAllErrors(),HttpStatus.BAD_REQUEST);
        }

        String email = (String) request.getAttribute("SignInAccountEmail");

        Account account = accountService.changePassword(email,changePasswordForm.getPassword());
        AccountResponseDto accountResponseDto = new AccountResponseDto(account);

        return new ResponseEntity(new Result(HttpStatus.OK,accountResponseDto),HttpStatus.OK);
    }

    @DeleteMapping("/account")
    public ResponseEntity deleteAccount(HttpServletRequest request){
        log.info("AccountController.Delete./account");

        String email = (String) request.getAttribute("SignInAccountEmail");
        accountService.processDeleteAccount(email);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

}
