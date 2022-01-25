package com.springsns.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springsns.domain.Account;
import com.springsns.mail.EmailMessage;
import com.springsns.mail.EmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    EmailService emailService;

    @AfterEach
    public void clearRepository() {
        accountRepository.deleteAll();
    }

    @DisplayName("인증 메일 확인 - 계정 없음")
    @Test
    void checkEmailTokenWithNoAccount() throws Exception {

        //given
        String token = "WrongToken";
        String email = "email@email.com";

        //when
        ResultActions resultActions = mockMvc.perform(get("/account/check-email-token")
                        .param("token", token)
                        .param("email", email))
                .andDo(print());

        //then
        resultActions.andExpect(status().is4xxClientError());
    }

    @DisplayName("인증 메일 확인 - 올바른 계정, 올바른 토큰")
    @Transactional
    @Test
    void checkEmailTokenWithCorrectInput() throws Exception {
        //given
        Account account = registerAccount();

        //when
        ResultActions resultActions = mockMvc.perform(get("/account/check-email-token")
                        .param("token", account.getEmailCheckToken())
                        .param("email", account.getEmail()))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.emailVerified").value(true))
                .andExpect(jsonPath("$.user.email").value(account.getEmail()));

    }

    @DisplayName("인증 메일 확인 - 올바른 계정, 잘못된 토큰")
    @Transactional
    @Test
    void checkEmailTokenWithCorrectAccountWrongToken() throws Exception {

        //given
        Account account = registerAccount();

        //when
        ResultActions resultActions = mockMvc.perform(get("/account/check-email-token")
                        .param("token", "WrongToken")
                        .param("email", account.getEmail()))
                .andDo(print());

        //then
        resultActions.andExpect(status().is4xxClientError());
    }

    @DisplayName("인증 메일 재전송 - 잘못된 JWT")
    @Test
    void resendEmailTokenWithWrongJWT() throws Exception{

        //given
        registerAccount();

        //when
        ResultActions resultActions = mockMvc.perform(get("/account/resend-email-token").header("Authorization", "WrongToken"))
                .andDo(print());

        //then
        resultActions.andExpect(status().isUnauthorized());

        //Account 등록 - 1번
        then(emailService).should().sendEmail(ArgumentMatchers.any(EmailMessage.class));
    }

    @DisplayName("인증 메일 재전송 - 이미 인증된 사용자의 요청")
    @Test
    void resendEmailTokenWithAlreadyAuthenticatedUser() throws Exception{

        //given
        Account registeredAccount = registerAccount();
        accountService.verifyEmailToken(registeredAccount.getEmail());

        String registeredAccountJWT = getJWT(registeredAccount.getEmail());

        //when
        ResultActions resultActions = mockMvc.perform(get("/account/resend-email-token").header("Authorization", registeredAccountJWT))
                .andDo(print());

        //then
        resultActions.andExpect(status().isBadRequest());

        //Account 등록 - 1번
        then(emailService).should().sendEmail(ArgumentMatchers.any(EmailMessage.class));

    }

    @DisplayName("인증 메일 재전송 - 입력값 정상")
    @Test
    void resendEmailTokenWithCorrectInput() throws Exception{
        //given
        Account registeredAccount = registerAccount();

        String registeredAccountJWT = getJWT(registeredAccount.getEmail());

        //when
        ResultActions resultActions = mockMvc.perform(get("/account/resend-email-token").header("Authorization", registeredAccountJWT))
                .andDo(print());

        //then
        resultActions.andExpect(status().isOk());
        //Account 등록 - 1번 + 재전송 - 1번
        then(emailService).should(times(2)).sendEmail(ArgumentMatchers.any(EmailMessage.class));
    }

    @DisplayName("회원 가입 처리 - 입력값 정상")
    @Test
    void signUpSubmitWithCorrectInput() throws Exception {

        //given
        SignUpForm signUpForm = getSignUpForm();
        String signUpFormToJson = objectMapper.writeValueAsString(signUpForm);

        //when
        ResultActions resultActions = mockMvc.perform(post("/account").contentType(MediaType.APPLICATION_JSON).content(signUpFormToJson))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value(signUpForm.getEmail()))
                .andExpect(jsonPath("$.user.emailVerified").value(false));

        Account account = accountRepository.findByEmail(signUpForm.getEmail());
        assertNotNull(account);
        assertNotNull(account.getEmailCheckToken());

        then(emailService).should().sendEmail(ArgumentMatchers.any(EmailMessage.class));
    }

    @DisplayName("회원 가입 처리 - 이메일 중복")
    @Test
    void signUpSubmitWithDuplicateEmail() throws Exception {

        //given
        Account registeredAccount = registerAccount();
        SignUpForm signUpForm = new SignUpForm("nickname", registeredAccount.getEmail(), "12345678");
        String signUpFormToJson = objectMapper.writeValueAsString(signUpForm);


        //when
        ResultActions resultActions = mockMvc.perform(post("/account").contentType(MediaType.APPLICATION_JSON).content(signUpFormToJson))
                .andDo(print());

        //then
        resultActions.andExpect(status().is4xxClientError());

        Account account = accountRepository.findByEmail(signUpForm.getEmail());
        assertNotNull(account);
        assertEquals(account.getNickname(), registeredAccount.getNickname());
    }

    @DisplayName("회원 가입 처리 - 닉네임 중복")
    @Test
    void signUpSubmitWithDuplicateNickname() throws Exception {

        //given
        Account registeredAccount = registerAccount();
        SignUpForm signUpForm = new SignUpForm(registeredAccount.getNickname(), "email@email.com", "12345678");
        String signUpFormToJson = objectMapper.writeValueAsString(signUpForm);

        //when
        ResultActions resultActions = mockMvc.perform(post("/account").contentType(MediaType.APPLICATION_JSON).content(signUpFormToJson))
                .andDo(print());

        //then
        resultActions.andExpect(status().is4xxClientError());

        Account account = accountRepository.findByEmail(registeredAccount.getEmail());
        assertNotNull(account);
        assertEquals(account.getEmail(), registeredAccount.getEmail());
    }

    @DisplayName("회원 가입 처리 - 이메일 입력 오류")
    @Test
    void signUpSubmitWithWrongEmail() throws Exception {

        //given
        String email = "signup";
        SignUpForm signUpForm = new SignUpForm("signup", email, "12345678");
        String signUpFormToJson = objectMapper.writeValueAsString(signUpForm);

        //when
        ResultActions resultActions = mockMvc.perform(post("/account").contentType(MediaType.APPLICATION_JSON).content(signUpFormToJson))
                .andDo(print());

        //then
        resultActions.andExpect(status().is4xxClientError());

        Account account = accountRepository.findByEmail(email);
        assertNull(account);
    }

    @DisplayName("회원 가입 처리 - 닉네임 입력 오류")
    @Test
    void signUpSubmitWithWrongNickname() throws Exception {

        //given
        String email = "signup@email.com";
        String nickname = "signup#";
        SignUpForm signUpForm = new SignUpForm(nickname, email, "12345678");
        String signUpFormToJson = objectMapper.writeValueAsString(signUpForm);

        //when
        ResultActions resultActions = mockMvc.perform(post("/account").contentType(MediaType.APPLICATION_JSON).content(signUpFormToJson))
                .andDo(print());

        //then
        resultActions.andExpect(status().is4xxClientError());

        Account account = accountRepository.findByEmail(email);
        assertNull(account);
    }

    @DisplayName("회원 가입 처리 - 비밀번호 입력 오류")
    @Test
    void signUpSubmitWithWrongPassword() throws Exception {

        //given
        String email = "signup@email.com";
        String password = "11";
        SignUpForm signUpForm = new SignUpForm("signup", email, password);
        String signUpFormToJson = objectMapper.writeValueAsString(signUpForm);

        //when
        ResultActions resultActions = mockMvc.perform(post("/account").contentType(MediaType.APPLICATION_JSON).content(signUpFormToJson))
                .andDo(print());

        //then
        resultActions.andExpect(status().is4xxClientError());

        Account account = accountRepository.findByEmail(email);
        assertNull(account);
    }

    @DisplayName("로그인 처리 - 입력값 정상")
    @Test
    void signInWithCorrectInput() throws Exception {

        //given
        Account registeredAccount = registerAccount();
        //registeredAccount의 비밀번호는 암호화 되어 있어서 getter로 넘기면 안된다.
        String signInFormToJson = getSignInFormToJson(registeredAccount.getEmail(), "12345678");

        //when
        ResultActions resultActions = mockMvc.perform(post("/account/sign-in").contentType(MediaType.APPLICATION_JSON).content(signInFormToJson))
                .andDo(print());

        //then
        resultActions.andExpect(status().isOk());

    }

    @DisplayName("로그인 처리 - 잘못된 패스워드")
    @Test
    void signInWithWrongPassword() throws Exception {

        //given
        Account registeredAccount = registerAccount();
        //registeredAccount의 비밀번호는 암호화 되어 있어서 getter로 넘기면 안된다.
        String signInFormToJson = getSignInFormToJson(registeredAccount.getEmail(), "WrongPassword");

        //when
        ResultActions resultActions = mockMvc.perform(post("/account/sign-in").contentType(MediaType.APPLICATION_JSON).content(signInFormToJson))
                .andDo(print());

        //then
        resultActions.andExpect(status().is4xxClientError());
    }

    @DisplayName("로그인 처리 - 없는 계정")
    @Test
    void signInWithWrongEmail() throws Exception {

        //given
        String email = "email@email.com";
        String password = "12345678";
        String signInFormToJson = getSignInFormToJson(email, password);

        //when
        ResultActions resultActions = mockMvc.perform(post("/account/sign-in").contentType(MediaType.APPLICATION_JSON).content(signInFormToJson))
                .andDo(print());

        //then
        resultActions.andExpect(status().is4xxClientError());

        Account account = accountRepository.findByEmail(email);
        assertNull(account);
    }

    @DisplayName("패스워드 변경 - 잘못된 패스워드로 변경")
    @Test
    void changePasswordWithWrongPassword() throws Exception {

        //given
        Account registeredAccount = registerAccount();
        String registeredAccountJWT = getJWT(registeredAccount.getEmail());

        String passwordToChange = "11";
        String changePasswordFormToJson = getChangePasswordFormToJson(passwordToChange);

        //when
        ResultActions resultActions = mockMvc.perform(patch("/account").contentType(MediaType.APPLICATION_JSON).header("Authorization", registeredAccountJWT).content(changePasswordFormToJson))
                .andDo(print());

        //then
        resultActions.andExpect(status().is4xxClientError());

        Account account = accountRepository.findByEmail(registeredAccount.getEmail());
        assertFalse(passwordEncoder.matches(passwordToChange, account.getPassword()));

    }

    @DisplayName("패스워드 변경 - 정상적인 패스워드로 변경")
    @Test
    void changePasswordWithCorrectPassword() throws Exception {

        //given
        Account registeredAccount = registerAccount();
        String registeredAccountJWT = getJWT(registeredAccount.getEmail());

        String passwordToChange = "87654321";
        String changePasswordFormToJson = getChangePasswordFormToJson(passwordToChange);

        //when
        ResultActions resultActions = mockMvc.perform(patch("/account").header("Authorization", registeredAccountJWT).contentType(MediaType.APPLICATION_JSON).content(changePasswordFormToJson))
                .andDo(print());

        //then
        resultActions.andExpect(status().isOk());

        Account account = accountRepository.findByEmail(registeredAccount.getEmail());
        assertTrue(passwordEncoder.matches(passwordToChange, account.getPassword()));
    }

    @DisplayName("패스워드 변경 - 잘못된 JWT")
    @Test
    void changePasswordWithWrongJWT() throws Exception {

        //given
        Account registeredAccount = registerAccount();

        String passwordToChange = "87654321";
        String changePasswordFormToJson = getChangePasswordFormToJson(passwordToChange);

        //when
        ResultActions resultActions = mockMvc.perform(patch("/account").header("Authorization", "WrongJWT").content(changePasswordFormToJson))
                .andDo(print());

        //then
        resultActions.andExpect(status().isUnauthorized());

        Account account = accountRepository.findByEmail(registeredAccount.getEmail());
        assertFalse(passwordEncoder.matches(passwordToChange, account.getPassword()));
    }

    @DisplayName("계정 탈퇴 - 정상 JWT")
    @Test
    void deleteAccountWithCorrectJWT() throws Exception {

        //given
        Account registeredAccount = registerAccount();
        String registeredAccountJWT = getJWT(registeredAccount.getEmail());

        //when
        ResultActions resultActions = mockMvc.perform(delete("/account").header("Authorization", registeredAccountJWT))
                .andDo(print());

        //then
        resultActions.andExpect(status().isOk());

        assertFalse(accountRepository.findByEmail(registeredAccount.getEmail()).isActivate());
        assertNull(accountRepository.findActivateAccountByEmail(registeredAccount.getEmail()));
        //Account 등록 - 1번 + 계정 탈퇴 - 1번
        then(emailService).should(times(2)).sendEmail(ArgumentMatchers.any(EmailMessage.class));
    }

    @DisplayName("계정 탈퇴 - 잘못된 JWT")
    @Test
    void deleteAccountWithWrongJWT() throws Exception {

        //given
        Account registeredAccount = registerAccount();

        //when
        ResultActions resultActions = mockMvc.perform(delete("/account").header("Authorization", "WrongJWT"))
                .andDo(print());

        //then
        resultActions.andExpect(status().isUnauthorized());

        assertTrue(accountRepository.findByEmail(registeredAccount.getEmail()).isActivate());
        assertNotNull(accountRepository.findActivateAccountByEmail(registeredAccount.getEmail()));
    }

    private Account registerAccount() {
        String email = "register@email.com";
        String nickname = "register";
        String password = "12345678";
        SignUpForm signUpForm = new SignUpForm(nickname, email, password);

        Account account = accountService.processSignUpAccount(signUpForm);
        return account;
    }

    private SignUpForm getSignUpForm() {
        String email = "signup@email.com";
        String nickname = "signup";
        String password = "12345678";
        SignUpForm signUpForm = new SignUpForm(nickname, email, password);

        return signUpForm;
    }

    private String getSignInFormToJson(String email, String password) throws JsonProcessingException {
        SignInForm signInForm = new SignInForm(email, password);

        return objectMapper.writeValueAsString(signInForm);
    }

    private String getChangePasswordFormToJson(String changePassword) throws JsonProcessingException {
        ChangePasswordForm changePasswordForm = new ChangePasswordForm(changePassword);

        return objectMapper.writeValueAsString(changePasswordForm);
    }

    private String getJWT(String email) {
        String jwt = accountService.processSignInAccount(email);
        return "Bearer "+jwt;
    }

}