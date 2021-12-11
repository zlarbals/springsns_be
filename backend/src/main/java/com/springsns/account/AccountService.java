package com.springsns.account;

import com.springsns.config.AppProperties;
import com.springsns.domain.Account;
import com.springsns.mail.EmailMessage;
import com.springsns.mail.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    //@transactional을 붙여서 범위안에 넣어놔야 해당 객체는 persist상태가 유지된다.
    //persist상태의 객체는 transaction이 종료될 때 상태를 db에 싱크한다.
    @Transactional
    public AccountResponseDto processNewAccount(SignUpForm signUpForm) {
        //signUpForm으로 newAccount 생성하고 저장.
        Account newAccount = saveNewAccount(signUpForm);
        //email check token 생성.
        newAccount.generateEmailCheckToken();//토큰 값 생성. uuid 사용해서 랜덤하게 생성하자.
        //확인 email 보내기.
        sendSignUpConfirmEmail(newAccount);

        AccountResponseDto accountResponseDto = new AccountResponseDto(newAccount);

        return accountResponseDto;
    }

    public boolean resendEmail(String email){
        Account account = accountRepository.findByEmail(email);

        if(account.isEmailVerified()){
            return false;
        }

        sendSignUpConfirmEmail(account);

        //AccountResponseDto accountResponseDto = new AccountResponseDto(account);

        return true;
    }

    //회원 가입 처리
    private Account saveNewAccount(SignUpForm signUpForm) {
        //new account 만들기
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .likes(new ArrayList<>())
                .posts(new ArrayList<>())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword()))   //패스워드 encoding.
                .roles(Collections.singletonList("ROLE_USER")) //최초 가입시 USER로 설정
                .build();
        //회원 저장
        return accountRepository.save(account);

        //save 메서드 안에서는 transactional처리가 되었기 때문에 persist상태이다.
        //하지만 save메서드를 벗어나면 transaction범위를 벗어났으므로 deteched상태가 된다.
    }

    private void sendSignUpConfirmEmail(Account newAccount) {
        Context context = new Context(); // 모델이라고 생각하면 된다.
        context.setVariable("link","/account/check-email-token?token=" + newAccount.getEmailCheckToken() + "&email=" + newAccount.getEmail());
        context.setVariable("nickname",newAccount.getNickname());
        context.setVariable("linkName","이메일 인증하기");
        context.setVariable("message","Spring SNS 서비스를 사용하려면 링크를 클릭하세요.");
        context.setVariable("host",appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("SpringSNS, 회원 가입 인증")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    public boolean isValidPassword(String password, String encodedPassword) {
        return passwordEncoder.matches(password, encodedPassword);
    }

    @Transactional
    public AccountResponseDto completeSignUp(Account account) {
        account.setEmailVerified(true);
        account.setEmailVerifiedDate(LocalDateTime.now());

        AccountResponseDto accountResponseDto = new AccountResponseDto(account);

        return accountResponseDto;
    }


    public Map<String, Object> createJWTToken(SignInForm signInForm) {
        Account account = accountRepository.findByEmail(signInForm.getEmail());
        AccountResponseDto accountResponseDto = new AccountResponseDto(account);

        //jwt 토큰 생성
        String jwtToken = jwtTokenProvider.createToken(account.getUsername(),account.getRoles());

        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("jwtToken",jwtToken);
        resultMap.put("user",accountResponseDto);

        return resultMap;
    }
}
