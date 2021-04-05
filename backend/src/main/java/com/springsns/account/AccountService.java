package com.springsns.account;

import com.springsns.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;

    //@transactional을 붙여서 범위안에 넣어놔야 해당 객체는 persist상태가 유지된다.
    //persist상태의 객체는 transaction이 종료될 때 상태를 db에 싱크한다.
    @Transactional
    public Account processNewAccount(SignUpForm signUpForm) {
        //signUpForm으로 newAccount 생성하고 저장.
        Account newAccount = saveNewAccount(signUpForm);
        //email check token 생성.
        newAccount.generateEmailCheckToken();//토큰 값 생성. uuid 사용해서 랜덤하게 생성하자.
        //확인 email 보내기.
        sendSignUpConfirmEmail(newAccount);

        return newAccount;
    }

    //회원 가입 처리
    private Account saveNewAccount(SignUpForm signUpForm) {
        //new account 만들기
        Account account = Account.builder()
                .email(signUpForm.getEmail())
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
        //email 보내기.
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());//받는 사람
        mailMessage.setSubject("SpringSns, 회원 가입 인증");  //메일 제목
        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() + "&email=" + newAccount.getEmail());  // 메일의 본문
        javaMailSender.send(mailMessage);//메일 보내기.
    }

    public boolean isValidPassword(String password, String encodedPassword) {
        return passwordEncoder.matches(password, encodedPassword);
    }

    @Transactional
    public void completeSignUp(Account account) {
        account.setEmailVerified(true);
        account.setJoinedAt(LocalDateTime.now());
    }
}
