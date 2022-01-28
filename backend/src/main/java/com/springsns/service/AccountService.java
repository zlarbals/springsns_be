package com.springsns.service;

import com.springsns.util.jwt.JwtTokenProvider;
import com.springsns.controller.dto.SignUpForm;
import com.springsns.config.AppProperties;
import com.springsns.domain.Account;
import com.springsns.exception.AccountDuplicatedException;
import com.springsns.util.mail.EmailMessage;
import com.springsns.util.mail.EmailService;
import com.springsns.repository.AccountRepository;
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

        validateDuplicateAccount(signUpForm.getEmail(),signUpForm.getNickname());

        //signUpForm으로 Account 생성하고 저장.
        Account account = saveNewAccount(signUpForm);
        //email check token 생성.
        account.generateEmailCheckToken();
        //확인 email 보내기.
        sendSignUpConfirmEmail(account);

        return account;
    }

    public String processSignInAccount(String email,String password){
        Account account = accountRepository.findActivateAccountByEmail(email).orElseThrow(()->new IllegalArgumentException("로그인에 실패했습니다"));

        validateSignInMismatch(account.getPassword(),password);

        String jwt = createJWT(account);

        return "Bearer "+jwt;
    }

    @Transactional
    public void processDeleteAccount(String email) {
        Account account = accountRepository.findActivateAccountByEmail(email).orElseThrow(()->new IllegalArgumentException("존재하지 않는 계정입니다."));

        //계정 닉네임 변경
        account.setNickname("LeftUser");
        //계정 비활성화
        account.setActivate(false);
        //계정 삭제 확인 email 보내기.
        sendDeleteConfirmEmail(email);
    }

    public void resendEmail(String email){

        Account account = accountRepository.findActivateAccountByEmail(email).orElseThrow(()->new IllegalArgumentException("존재하지 않는 계정입니다."));

        //이미 인증했는지 확인
        if(account.isEmailVerified()){
            throw new IllegalStateException("이미 이메일 인증이 완료되었습니다.");
        }

        //이메일 전송
        sendSignUpConfirmEmail(account);
    }

    @Transactional
    public Account verifyEmailToken(String email,String token) {

        Account account = accountRepository.findActivateAccountByEmail(email).orElseThrow(()->new IllegalArgumentException("토큰이 일치하지 않습니다."));

        if(!account.isValidToken(token)){
            throw new IllegalArgumentException("토큰이 일치하지 않습니다");
        }

        account.setEmailVerified(true);
        account.setEmailVerifiedDate(LocalDateTime.now());

        return account;
    }

    @Transactional
    public Account changePassword(String email, String password) {
        Account account = accountRepository.findActivateAccountByEmail(email).orElseThrow(()->new IllegalArgumentException("존재하지 않는 계정입니다."));

        account.setPassword(passwordEncoder.encode(password));

        return account;
    }

    private void validateDuplicateAccount(String email, String nickname) {
        if(accountRepository.existsByEmail(email)){
            throw new AccountDuplicatedException("중복된 이메일 입니다.");
        }

        if(accountRepository.existsByNickname(nickname)){
            throw new AccountDuplicatedException("중복된 닉네임 입니다.");
        }
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

    private void validateSignInMismatch(String encryptedPassword,String password) {
        if(!passwordEncoder.matches(password,encryptedPassword)){
            throw new IllegalArgumentException("로그인에 실패했습니다");
        }
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
