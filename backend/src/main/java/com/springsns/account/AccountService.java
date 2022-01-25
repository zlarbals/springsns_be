package com.springsns.account;

import com.springsns.config.AppProperties;
import com.springsns.domain.Account;
import com.springsns.mail.EmailMessage;
import com.springsns.mail.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.ArrayList;

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
    public Account processSignUpAccount(SignUpForm signUpForm) {
        //signUpForm으로 Account 생성하고 저장.
        Account account = saveNewAccount(signUpForm);
        //email check token 생성.
        account.generateEmailCheckToken();//토큰 값 생성. uuid 사용해서 랜덤하게 생성하자.
        //확인 email 보내기.
        sendSignUpConfirmEmail(account);

        return account;
    }

    public String processSignInAccount(String email){
        Account account = accountRepository.findByEmail(email);

        //jwt 생성
        String jwt = createJWT(account);

        return jwt;
    }

    @Transactional
    public void processDeleteAccount(String email) {
        Account account = accountRepository.findByEmail(email);

        //계정 닉네임 변경
        account.setNickname("LeftUser");
        //계정 비활성화
        account.setActivate(false);
        //계정 삭제 확인 email 보내기.
        sendDeleteConfirmEmail(email);
    }

    public Account resendEmail(String email){
        Account account = accountRepository.findByEmail(email);

        //이메일 전송
        sendSignUpConfirmEmail(account);

        return account;
    }

    @Transactional
    public Account verifyEmailToken(String email) {

        Account account = accountRepository.findByEmail(email);

        account.setEmailVerified(true);
        account.setEmailVerifiedDate(LocalDateTime.now());

        return account;
    }

    @Transactional
    public Account changePassword(String email, String password) {
        Account account = accountRepository.findByEmail(email);

        account.setPassword(passwordEncoder.encode(password));

        return account;
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
                .isActivate(true)
                .build();
        //회원 저장
        return accountRepository.save(account);

        //save 메서드 안에서는 transactional처리가 되었기 때문에 persist상태이다.
        //하지만 save메서드를 벗어나면 transaction범위를 벗어났으므로 deteched상태가 된다.
    }

    private String createJWT(Account account) {
        //jwt 토큰 생성
        String jwt = jwtTokenProvider.createToken(account.getEmail());

        return jwt;
    }

    private void sendSignUpConfirmEmail(Account account) {
        Context context = new Context();
        context.setVariable("link","/account/check-email-token?token=" + account.getEmailCheckToken() + "&email=" + account.getEmail());
        context.setVariable("nickname",account.getNickname());
        context.setVariable("linkName","이메일 인증하기");
        context.setVariable("message","Spring SNS 서비스를 사용하려면 링크를 클릭하세요.");
        context.setVariable("host",appProperties.getHost());
        String message = templateEngine.process("mail/send-email-authentication-token", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("SpringSNS, 회원 가입 인증")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    private void sendDeleteConfirmEmail(String email) {
        Context context = new Context();
        context.setVariable("email",email);

        String message = templateEngine.process("mail/inform-delete",context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(email)
                .subject("SpringSNS, 회원 탈퇴 확인 메일")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }
}
