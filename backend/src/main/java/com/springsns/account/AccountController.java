package com.springsns.account;

import com.springsns.domain.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/account")
    public ResponseEntity signUpSubmit(@Validated @RequestBody SignUpForm signUpForm, BindingResult bindingResult) {
        log.info("AccountController.Post./account");

        signUpFormDuplicateCheck(bindingResult,signUpForm.getEmail(),signUpForm.getNickname());

        if(bindingResult.hasErrors()){
            log.info("sign up submit error : {}",bindingResult);
            return new ResponseEntity(bindingResult.getAllErrors(),HttpStatus.BAD_REQUEST);
        }

        Account account = accountService.processSignUpAccount(signUpForm);

        HashMap<String,Object> resultMap = new HashMap<>();
        resultMap.put("user",new AccountResponseDto(account));

        return new ResponseEntity(resultMap,HttpStatus.OK);
    }

    @PostMapping("/account/sign-in")
    public ResponseEntity signInSubmit(@Validated @RequestBody SignInForm signInForm, BindingResult bindingResult) {
        log.info("AccountController.Post./sign-in");

        signInFormMismatchCheck(bindingResult,signInForm.getEmail(),signInForm.getPassword());

        if(bindingResult.hasErrors()){
            log.info("sign in submit error : {}",bindingResult);
            return new ResponseEntity(bindingResult.getAllErrors(),HttpStatus.BAD_REQUEST);
        }

        String jwt = accountService.processSignInAccount(signInForm.getEmail());

        HashMap<String,String> resultMap = new HashMap<>();
        resultMap.put("jwt",jwt);

        return new ResponseEntity(resultMap, HttpStatus.OK);
    }

    //쿼리 파라미터를 모델어트리뷰트에 담을 수 있다.
    @GetMapping("/account/check-email-token")
    public ResponseEntity checkEmailToken(@ModelAttribute EmailCheckForm emailCheckForm,BindingResult bindingResult) {
        log.info("AccountController.Get./account/check-email-token");
        String email = emailCheckForm.getEmail();
        String token = emailCheckForm.getToken();

        emailTokenInvalidCheck(bindingResult,email,token);

        if(bindingResult.hasErrors()){
            log.info("check email token invalid");
            return new ResponseEntity(bindingResult.getAllErrors(),HttpStatus.BAD_REQUEST);
        }

        Account account = accountService.verifyEmailToken(email);

        HashMap<String,Object> resultMap = new HashMap<>();
        resultMap.put("user",new AccountResponseDto(account));

        return new ResponseEntity(resultMap, HttpStatus.OK);
    }

    @GetMapping("/account/resend-email-token")
    public ResponseEntity resendEmail(Principal principal){
        log.info("AccountController.Get./account/resend-email-token");
        String email = principal.getName();

        Map<String,Object> resultMap = new HashMap<>();
        if(isAlreadyVerified(email)){
            log.info("resend email token error : already verified");
            resultMap.put("error","already verified");
            return new ResponseEntity(resultMap,HttpStatus.BAD_REQUEST);
        }

        Account account = accountService.resendEmail(email);

        resultMap.put("user",new AccountResponseDto(account));

        return new ResponseEntity(resultMap,HttpStatus.OK);
    }

    @PatchMapping("/account")
    public ResponseEntity changePassword(@Validated @RequestBody ChangePasswordForm changePasswordForm,BindingResult bindingResult,Principal principal){
        log.info("AccountController.Patch./account");

        if(bindingResult.hasErrors()){
            log.info("change password error : {}",bindingResult);
            return new ResponseEntity(bindingResult.getAllErrors(),HttpStatus.BAD_REQUEST);
        }

        String email = principal.getName();

        Account account = accountService.changePassword(email,changePasswordForm.getPassword());

        HashMap<String,Object> resultMap = new HashMap<>();
        resultMap.put("user",new AccountResponseDto(account));

        return new ResponseEntity(resultMap,HttpStatus.OK);
    }

    @DeleteMapping("/account")
    public ResponseEntity deleteAccount(Principal principal){
        log.info("AccountController.Delete./account");

        String email = principal.getName();
        accountService.processDeleteAccount(email);

        return new ResponseEntity(HttpStatus.OK);
    }

    private void signUpFormDuplicateCheck(BindingResult bindingResult, String email, String nickname) {
        //회원가입시 중복 이메일인지 확인
        if (accountRepository.existsByEmail(email)) {
            bindingResult.rejectValue("email","duplicate");
        }

        //회원가입시 중복 닉네임인지 확인
        if (accountRepository.existsByNickname(nickname)) {
            bindingResult.rejectValue("nickname","duplicate");
        }
    }

    private void signInFormMismatchCheck(BindingResult bindingResult, String email, String password) {
        //회원 탈퇴된 계정은 로그인 불가이므로 활성화된 계정에서만 찾음.
        Account account = accountRepository.findActivateAccountByEmail(email);

        if(account==null || !passwordEncoder.matches(password,account.getPassword())){
            bindingResult.reject("mismatch");
        }
    }

    private void emailTokenInvalidCheck(BindingResult bindingResult, String email, String token) {
        Account account = accountRepository.findByEmail(email);

        if(account==null || !account.isValidToken(token)){
            bindingResult.reject("invalid");
        }
    }

    private boolean isAlreadyVerified(String email) {
        Account account = accountRepository.findByEmail(email);

        return account.isEmailVerified();
    }

}
