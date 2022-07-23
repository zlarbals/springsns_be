package com.springsns.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springsns.controller.dto.CommentForm;
import com.springsns.repository.CommentRepository;
import com.springsns.repository.PostRepository;
import com.springsns.repository.AccountRepository;
import com.springsns.service.AccountService;
import com.springsns.controller.dto.SignUpForm;
import com.springsns.domain.Account;
import com.springsns.domain.Comment;
import com.springsns.domain.Post;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    private static int registerAccountCount=0;

    @AfterEach
    public void clearRepository() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        accountRepository.deleteAll();
        registerAccountCount=0;
    }

    @DisplayName("댓글 등록 - 등록 안된 사용자")
    @Test
    void registerCommentWithUnregisteredUser() throws Exception{

        //given
        Account postingAccount = registerAccount();
        authenticateEmail(postingAccount.getEmail(),postingAccount.getEmailCheckToken());
        Post post = registerPost(postingAccount);

        CommentForm commentForm = getCommentForm();
        String commentFormToJson = objectMapper.writeValueAsString(commentForm);

        //when
        ResultActions resultActions = mockMvc.perform(post("/comment/post/" + post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentFormToJson))
                .andDo(print());

        //then
        resultActions.andExpect(status().isUnauthorized());
    }

    @DisplayName("댓글 등록 - 이메일 인증 안된 사용자")
    @Test
    void registerCommentWithUnauthenticatedUser() throws Exception{

        //given
        Account postingAccount = registerAccount();
        authenticateEmail(postingAccount.getEmail(),postingAccount.getEmailCheckToken());
        Post post = registerPost(postingAccount);

        Account commentingAccount = registerAccount();
        String commentingAccountJWT = getJWT(commentingAccount.getEmail());
        CommentForm commentForm = getCommentForm();
        String commentFormToJson = objectMapper.writeValueAsString(commentForm);

        //when
        ResultActions resultActions = mockMvc.perform(post("/comment/post/" + post.getId())
                        .header("Authorization", commentingAccountJWT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentFormToJson))
                .andDo(print());

        //then
        resultActions.andExpect(status().isForbidden());
    }

    @DisplayName("댓글 등록 - 인증된 사용자")
    @Test
    void registerCommentWithAuthenticatedUser() throws Exception{

        //given
        Account postingAccount = registerAccount();
        authenticateEmail(postingAccount.getEmail(),postingAccount.getEmailCheckToken());
        Post post = registerPost(postingAccount);

        Account commentingAccount = registerAccount();
        authenticateEmail(commentingAccount.getEmail(),commentingAccount.getEmailCheckToken());
        String commentingAccountJWT = getJWT(commentingAccount.getEmail());
        CommentForm commentForm = getCommentForm();
        String commentFormToJson = objectMapper.writeValueAsString(commentForm);

        //when
        ResultActions resultActions = mockMvc.perform(post("/comment/post/" + post.getId())
                        .header("Authorization", commentingAccountJWT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentFormToJson))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.response.content").value(commentForm.getContent()));
    }

    @DisplayName("댓글 등록 - 잘못된 입력, 인증된 사용자")
    @Test
    void registerInvalidCommentWithAuthenticatedUser() throws Exception{

        //given
        Account postingAccount = registerAccount();
        authenticateEmail(postingAccount.getEmail(),postingAccount.getEmailCheckToken());
        Post post = registerPost(postingAccount);

        Account commentingAccount = registerAccount();
        authenticateEmail(commentingAccount.getEmail(),commentingAccount.getEmailCheckToken());
        String commentingAccountJWT = getJWT(commentingAccount.getEmail());

        CommentForm commentForm = new CommentForm();
        String commentFormToJson = objectMapper.writeValueAsString(commentForm);

        //when
        ResultActions resultActions = mockMvc.perform(post("/comment/post/" + post.getId())
                        .header("Authorization", commentingAccountJWT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentFormToJson))
                .andDo(print());

        //then
        resultActions.andExpect(status().isBadRequest());
    }

    @DisplayName("댓글 등록 - 존재하지 않는 게시글, 인증된 사용자")
    @Test
    void registerCommentInNullPostWithAuthenticatedUser() throws Exception{

        //given
        Account commentingAccount = registerAccount();
        authenticateEmail(commentingAccount.getEmail(),commentingAccount.getEmailCheckToken());
        String commentingAccountJWT = getJWT(commentingAccount.getEmail());

        CommentForm commentForm = getCommentForm();
        String commentFormToJson = objectMapper.writeValueAsString(commentForm);

        //when
        ResultActions resultActions = mockMvc.perform(post("/comment/post/" + 99999)
                        .header("Authorization", commentingAccountJWT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentFormToJson))
                .andDo(print());

        //then
        resultActions.andExpect(status().isNotFound());
    }

    @DisplayName("댓글 가져오기 - 등록 안된 사용자")
    @Test
    void getCommentWithUnregisteredUser() throws Exception{

        //given
        Account postingAccount = registerAccount();
        authenticateEmail(postingAccount.getEmail(),postingAccount.getEmailCheckToken());
        Post post = registerPost(postingAccount);

        Account commentingAccount = registerAccount();
        authenticateEmail(commentingAccount.getEmail(),commentingAccount.getEmailCheckToken());
        registerCommentToPost(post,commentingAccount);

        //when
        ResultActions resultActions = mockMvc.perform(get("/comment/post/" + post.getId()))
                .andDo(print());

        //then
        resultActions.andExpect(status().isUnauthorized());

    }

    @DisplayName("댓글 가져오기 - 등록된 사용자")
    @Test
    void getCommentWithRegisteredUser() throws Exception{

        //given
        Account postingAccount = registerAccount();
        authenticateEmail(postingAccount.getEmail(),postingAccount.getEmailCheckToken());
        Post post = registerPost(postingAccount);

        Account commentingAccount = registerAccount();
        authenticateEmail(commentingAccount.getEmail(),commentingAccount.getEmailCheckToken());
        registerCommentToPost(post,commentingAccount);

        Account requestingAccount = registerAccount();
        String requestingAccountJWT = getJWT(requestingAccount.getEmail());

        //when
        ResultActions resultActions = mockMvc.perform(get("/comment/post/" + post.getId())
                        .header("Authorization", requestingAccountJWT))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response",hasSize(2)));

    }

    @DisplayName("댓글 가져오기 - 존재하지 않는 게시글, 등록된 사용자")
    @Test
    void getNullPostCommentWithRegisteredUser() throws Exception{

        //given
        Account requestingAccount = registerAccount();
        String requestingAccountJWT = getJWT(requestingAccount.getEmail());

        //when
        ResultActions resultActions = mockMvc.perform(get("/comment/post/" + 99999)
                        .header("Authorization", requestingAccountJWT))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isNotFound());

    }

    private Account registerAccount() {
        String nickname = "register"+registerAccountCount;
        String email = nickname+"@email.com";
        String password = "12345678";
        SignUpForm signUpForm = new SignUpForm(nickname, email, password);

        registerAccountCount++;

        return accountService.processSignUpAccount(signUpForm);
    }

    private Post registerPost(Account account) {
        String content = "post content";
        Post post = Post.builder()
                .account(account)
                .content(content)
                .postImage(null)
                .build();

        return postRepository.save(post);
    }

    private void authenticateEmail(String email,String token) {
        accountService.verifyEmailToken(email,token);
    }

    private String getJWT(String email) {
        String jwt = accountService.processSignInAccount(email,"12345678");
        return jwt;
    }

    private CommentForm getCommentForm() {
        String commentContent = "comment content";
        CommentForm commentForm = new CommentForm();
        commentForm.setContent(commentContent);

        return commentForm;
    }

    private void registerCommentToPost(Post post,Account account){
        int commentCount = 2;
        for(int i=0;i<commentCount;i++){
            String content = "comment content";
            Comment comment = Comment.builder()
                    .account(account)
                    .post(post)
                    .content(content)
                    .build();

            commentRepository.save(comment);
        }
    }

}