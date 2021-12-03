package com.springsns.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springsns.domain.Account;
import com.springsns.mail.EmailMessage;
import com.springsns.mail.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @MockBean
    EmailService emailService;

    @DisplayName("인증 메일 확인 - 계정 없음")
    @Test
    void checkEmailTokenWithNoAccount() throws Exception {
        mockMvc.perform(get("/check-email-token")
                .param("token", "sdfjslwfs")
                .param("email", "email@email.com"))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @DisplayName("인증 메일 확인 - 올바른 계정, 올바른 토큰")
    @Transactional
    @Test
    void checkEmailTokenWithCorrectInput() throws Exception {
        registerAccount("email1","email1@email.com","12345678");

        Account account = accountRepository.findByEmail("email1@email.com");

        mockMvc.perform(get("/check-email-token")
                .param("token", account.getEmailCheckToken())
                .param("email", account.getEmail()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.emailVerified").value(true))
                .andExpect(jsonPath("$.user.email").value("email1@email.com"));

    }

    @DisplayName("인증 메일 확인 - 올바른 계정, 잘못된 토큰")
    @Transactional
    @Test
    void checkEmailTokenWithCorrectAccountWrongToken() throws Exception{
        registerAccount("email2","email2@email.com","12345678");

        Account account = accountRepository.findByEmail("email2@email.com");

        mockMvc.perform(get("/check-email-token")
                        .param("token", "WrongToken")
                        .param("email", account.getEmail()))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @DisplayName("회원 가입 처리 - 입력값 정상")
    @Test
    void signUpSubmitWithCorrectInput() throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setEmail("signup1@email.com");
        signUpForm.setNickname("signup1");
        signUpForm.setPassword("12345678");

        String json = objectMapper.writeValueAsString(signUpForm);

        //.with(csrf())를 통해 테스트에서 csrf토큰을 설정해줄 수 있다.
//        mockMvc.perform(post("/sign-up").contentType(MediaType.APPLICATION_JSON).content(json).with(csrf()))
//                .andDo(print())
//                .andExpect(status().is4xxClientError());

        mockMvc.perform(post("/sign-up").contentType(MediaType.APPLICATION_JSON).content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("email").value("signup1@email.com"))
                .andExpect(jsonPath("emailVerified").value(false));

        Account account = accountRepository.findByEmail("signup1@email.com");
        assertNotNull(account);
        assertNotEquals(account.getPassword(), "dskljasdf32423");
        assertNotNull(account.getEmailCheckToken());

        then(emailService).should().sendEmail(ArgumentMatchers.any(EmailMessage.class));
    }

    @DisplayName("회원 가입 처리 - 이메일 중복")
    @Test
    void signUpSubmitWithDuplicateEmail() throws Exception{
        registerAccount("signup2","signup2@email.com","12345678");

        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setEmail("signup2@email.com");
        signUpForm.setNickname("signupduplicate");
        signUpForm.setPassword("12345678");

        String json = objectMapper.writeValueAsString(signUpForm);

        mockMvc.perform(post("/sign-up").contentType(MediaType.APPLICATION_JSON).content(json))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        Account account = accountRepository.findByEmail("signup2@email.com");
        assertNotNull(account);
        assertEquals(account.getNickname(),"signup2");
    }

    @DisplayName("회원 가입 처리 - 닉네임 중복")
    @Test
    void signUpSubmitWithDuplicateNickname() throws Exception{
        registerAccount("signup3","signup3@email.com","12345678");

        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setEmail("nicknameduplicate@email.com");
        signUpForm.setNickname("signup3");
        signUpForm.setPassword("12345678");

        String json = objectMapper.writeValueAsString(signUpForm);

        mockMvc.perform(post("/sign-up").contentType(MediaType.APPLICATION_JSON).content(json))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        Account account = accountRepository.findByEmail("nicknameduplicate@email.com");
        assertNull(account);
    }

    @DisplayName("회원 가입 처리 - 이메일 입력 오류")
    @Test
    void signUpSubmitWithWrongEmail() throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setEmail("signup4...");
        signUpForm.setNickname("signup4");
        signUpForm.setPassword("12345678");

        String json = objectMapper.writeValueAsString(signUpForm);

        mockMvc.perform(post("/sign-up").contentType(MediaType.APPLICATION_JSON).content(json))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        Account account = accountRepository.findByEmail("signup4...");
        assertNull(account);
    }

    @DisplayName("회원 가입 처리 - 닉네임 입력 오류")
    @Test
    void signUpSubmitWithWrongNickname() throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setEmail("signup5@email.com");
        signUpForm.setNickname("signup5#");
        signUpForm.setPassword("12345678");

        String json = objectMapper.writeValueAsString(signUpForm);

        mockMvc.perform(post("/sign-up").contentType(MediaType.APPLICATION_JSON).content(json))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        Account account = accountRepository.findByEmail("signup5@email.com");
        assertNull(account);
    }

    @DisplayName("회원 가입 처리 - 비밀번호 입력 오류")
    @Test
    void signUpSubmitWithWrongPassword() throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setEmail("signup6@email.com");
        signUpForm.setNickname("signup6");
        signUpForm.setPassword("11");

        String json = objectMapper.writeValueAsString(signUpForm);

        mockMvc.perform(post("/sign-up").contentType(MediaType.APPLICATION_JSON).content(json))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        Account account = accountRepository.findByEmail("signup6@email.com");
        assertNull(account);
    }

    @DisplayName("로그인 처리 - 입력값 정상")
    @Test
    void signInWithCorrectInput() throws Exception{
        registerAccount("signin1","signin1@email.com","12345678");

        SignInForm signInForm = new SignInForm();
        signInForm.setEmail("signin1@email.com");
        signInForm.setPassword("12345678");

        String json = objectMapper.writeValueAsString(signInForm);
        mockMvc.perform(post("/users/signin").contentType(MediaType.APPLICATION_JSON).content(json))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("로그인 처리 - 잘못된 패스워드")
    @Test
    void signInWithWrongPassword() throws Exception{
        registerAccount("signin2","signin2@email.com","12345678");

        SignInForm signInForm = new SignInForm();
        signInForm.setEmail("signin21@email.com");
        signInForm.setPassword("87654321");

        String json = objectMapper.writeValueAsString(signInForm);
        mockMvc.perform(post("/users/signin").contentType(MediaType.APPLICATION_JSON).content(json))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @DisplayName("로그인 처리 - 없는 계정")
    @Test
    void signInWithWrongEmail() throws Exception{
        SignInForm signInForm = new SignInForm();
        signInForm.setEmail("signin3@email.com");
        signInForm.setPassword("12345678");

        String json = objectMapper.writeValueAsString(signInForm);
        mockMvc.perform(post("/users/signin").contentType(MediaType.APPLICATION_JSON).content(json))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        Account account = accountRepository.findByEmail("signin3@email.com");

        assertNull(account);
    }

    private void registerAccount(String nickname, String email, String password) {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname(nickname);
        signUpForm.setEmail(email);
        signUpForm.setPassword(password);
        accountService.processNewAccount(signUpForm);
    }

}