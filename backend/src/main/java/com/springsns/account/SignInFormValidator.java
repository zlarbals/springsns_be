package com.springsns.account;

import com.springsns.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignInFormValidator implements Validator {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(SignInForm.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        SignInForm signInForm = (SignInForm) o;
        //Account account = accountRepository.findByEmail(signInForm.getEmail());

        //회원 탈퇴된 계정은 로그인 불가.
        Account account = accountRepository.findActivateAccountByEmail(signInForm.getEmail());

        if(account==null){
            errors.rejectValue("email","invalid.email",new Object[]{signInForm.getEmail()},"로그인에 실패하였습니다.");
        }else{
            if (!passwordEncoder.matches(signInForm.getPassword(),account.getPassword())){
                errors.rejectValue("password","invalid.password",new Object[]{signInForm.getEmail()},"로그인에 실패하였습니다." );
            }
        }
    }
}
