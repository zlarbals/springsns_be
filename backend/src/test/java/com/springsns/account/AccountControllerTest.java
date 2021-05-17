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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    EmailService emailService;

    @DisplayName("인증 메일 확인 - 입력값 오류")
    @Test
    void checkEmailToken_with_wrong_input() throws Exception {
        mockMvc.perform(get("/check-email-token")
                .param("token", "sdfjslwfs")
                .param("email", "email@email.com"))
                .andExpect(status().is4xxClientError())
                .andExpect(unauthenticated());
    }

    @DisplayName("인증 메일 확인 - 입력값 정상")
    @Test
    void checkEmailToken() throws Exception {
        Account account = Account.builder()
                .email("test@email.com")
                .password("12345678")
                .nickname("kyumin")
                .build();
        Account newAccount = accountRepository.save(account);
        newAccount.generateEmailCheckToken();

        mockMvc.perform(get("/check-email-token")
                .param("token", newAccount.getEmailCheckToken())
                .param("email", newAccount.getEmail()))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername("kyumin"));

    }

    @DisplayName("회원 가입 처리 - 입력값 정상")
    @Test
    void signUpSubmit_with_correct_input() throws Exception {

        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setEmail("zlarbals@gmail.com");
        signUpForm.setNickname("kimkim11");
        signUpForm.setPassword("dskljasdf32423");

        String json = objectMapper.writeValueAsString(signUpForm);

        mockMvc.perform(post("/sign-up").contentType(MediaType.APPLICATION_JSON).content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername("kimkim11"));
        //.andExpect(jsonPath("email").value("zlarbals@gmail.com"));

        //Account account = accountRepository.findByEmail("zlarbals@gmail.com").orElseThrow(()->new IllegalArgumentException());
        Account account = accountRepository.findByEmail("zlarbals@gmail.com");
        assertNotNull(account);
        assertNotEquals(account.getPassword(), "dskljasdf32423");
        assertNotNull(account.getEmailCheckToken());

        then(emailService).should().sendEmail(ArgumentMatchers.any(EmailMessage.class));
    }

    @DisplayName("회원 가입 처리 - 입력값 오류")
    @Test
    void signUpSubmit_with_wrong_input() throws Exception {

        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setEmail("zlarbals...");
        signUpForm.setNickname("kimkim11");
        signUpForm.setPassword("12345");

        String json = objectMapper.writeValueAsString(signUpForm);

        //.with(csrf())를 통해 테스트에서 csrf토큰을 설정해줄 수 있다.
//        mockMvc.perform(post("/sign-up").contentType(MediaType.APPLICATION_JSON).content(json).with(csrf()))
//                .andDo(print())
//                .andExpect(status().is4xxClientError());

        mockMvc.perform(post("/sign-up").contentType(MediaType.APPLICATION_JSON).content(json))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(unauthenticated());
    }

}