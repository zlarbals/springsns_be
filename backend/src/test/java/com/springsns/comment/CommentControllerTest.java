package com.springsns.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springsns.Post.PostRepository;
import com.springsns.account.AccountRepository;
import com.springsns.account.AccountService;
import com.springsns.account.SignInForm;
import com.springsns.account.SignUpForm;
import com.springsns.domain.Account;
import com.springsns.domain.Comment;
import com.springsns.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

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

    @DisplayName("댓글 등록 - 등록 안된 사용자")
    @Test
    void registerCommentWithUnregisteredUser() throws Exception{
        //계정 만들기
        String postingNickname = "registerCommentTest1";
        String postingEmail = "registerCommentTest1@email.com";
        String postingPassword = "12345678";

        registerAccount(postingNickname,postingEmail,postingPassword);

        authenticateEmail(postingEmail);

        Account account = accountRepository.findByEmail(postingEmail);

        //게시글 등록
        Post post = Post.builder()
                .account(account)
                .content("registerCommentTest")
                .postFile(null)
                .build();

        postRepository.save(post);

        String commentContent = "RegisterCommentContent";
        CommentForm commentForm = new CommentForm();
        commentForm.setContent(commentContent);

        String body = objectMapper.writeValueAsString(commentForm);

        //게시글에 댓글 등록
        mockMvc.perform(post("/comment/post/"+post.getId()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @DisplayName("댓글 등록 - 이메일 인증 안된 사용자")
    @Test
    void registerCommentWithUnauthenticatedUser() throws Exception{
        //계정 만들기
        String postingNickname = "registerCommentTest2";
        String postingEmail = "registerCommentTest2@email.com";
        String postingPassword = "12345678";

        registerAccount(postingNickname,postingEmail,postingPassword);

        authenticateEmail(postingEmail);

        Account account = accountRepository.findByEmail(postingEmail);

        //게시글 등록
        Post post = Post.builder()
                .account(account)
                .content("registerCommentTest")
                .postFile(null)
                .build();

        postRepository.save(post);

        //댓글 작성할 계정 만들기
        String nickname = "registerCommentTest3";
        String email = "registerCommentTest3@email.com";
        String password = "12345678";

        registerAccount(nickname,email,password);

        Object jwt = getJWTToken(email,password);

        String commentContent = "RegisterCommentContent";
        CommentForm commentForm = new CommentForm();
        commentForm.setContent(commentContent);

        String body = objectMapper.writeValueAsString(commentForm);

        //게시글에 댓글 등록
        mockMvc.perform(post("/comment/post/"+post.getId()).header("X-AUTH-TOKEN",jwt).contentType(MediaType.APPLICATION_JSON).content(body))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @DisplayName("댓글 등록 - 인증된 사용자")
    @Test
    void registerCommentWithAuthenticatedUser() throws Exception{
        //계정 만들기
        String postingNickname = "registerCommentTest4";
        String postingEmail = "registerCommentTest4@email.com";
        String postingPassword = "12345678";

        registerAccount(postingNickname,postingEmail,postingPassword);

        authenticateEmail(postingEmail);

        Account account = accountRepository.findByEmail(postingEmail);

        //게시글 등록
        Post post = Post.builder()
                .account(account)
                .content("registerCommentTest")
                .postFile(null)
                .build();

        postRepository.save(post);

        //댓글 작성할 계정 만들기
        String nickname = "registerCommentTest5";
        String email = "registerCommentTest5@email.com";
        String password = "12345678";

        registerAccount(nickname,email,password);

        authenticateEmail(email);

        Object jwt = getJWTToken(email,password);

        String commentContent = "RegisterCommentContent";
        CommentForm commentForm = new CommentForm();
        commentForm.setContent(commentContent);

        String body = objectMapper.writeValueAsString(commentForm);

        //게시글에 댓글 등록
        mockMvc.perform(post("/comment/post/"+post.getId()).header("X-AUTH-TOKEN",jwt).contentType(MediaType.APPLICATION_JSON).content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(commentContent));
    }

    @DisplayName("댓글 가져오기 - 등록 안된 사용자")
    @Test
    void getCommentWithUnregisteredUser() throws Exception{
        //게시글 작성할 계정 만들기
        String postingNickname = "getCommentTest1";
        String postingEmail = "getCommentTest1@email.com";
        String postingPassword = "12345678";

        registerAccount(postingNickname,postingEmail,postingPassword);

        authenticateEmail(postingEmail);

        Account account = accountRepository.findByEmail(postingEmail);

        //게시글 등록
        Post post = Post.builder()
                .account(account)
                .content("GetCommentTest")
                .postFile(null)
                .build();

        postRepository.save(post);

        //댓글 등록
        for(int i=0;i<7;i++){
            String content = "GetCommentTest"+i;
            Comment comment = Comment.builder()
                    .account(account)
                    .post(post)
                    .content(content)
                    .build();

            commentRepository.save(comment);
        }

        mockMvc.perform(get("/comment/post/"+post.getId()))
                .andDo(print())
                .andExpect(status().isForbidden());

    }

    @DisplayName("댓글 가져오기 - 등록된 사용자")
    @Test
    void getCommentWithRegisteredUser() throws Exception{
        //게시글 작성할 계정 만들기
        String postingNickname = "getCommentTest2";
        String postingEmail = "getCommentTest2@email.com";
        String postingPassword = "12345678";

        registerAccount(postingNickname,postingEmail,postingPassword);

        authenticateEmail(postingEmail);

        Account account = accountRepository.findByEmail(postingEmail);

        //게시글 등록
        Post post = Post.builder()
                .account(account)
                .content("GetCommentTest")
                .postFile(null)
                .build();

        postRepository.save(post);

        //댓글 등록
        for(int i=0;i<7;i++){
            String content = "GetCommentTest"+i;
            Comment comment = Comment.builder()
                    .account(account)
                    .post(post)
                    .content(content)
                    .build();

            commentRepository.save(comment);
        }

        //댓글 가져올 계정 만들기
        String nickname = "getCommentTest3";
        String email = "getCommentTest3@email.com";
        String password = "12345678";

        registerAccount(nickname,email,password);

        Object jwt = getJWTToken(email,password);


        mockMvc.perform(get("/comment/post/"+post.getId()).header("X-AUTH-TOKEN",jwt))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments",hasSize(7)));

    }

    private void registerAccount(String nickname, String email, String password) {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname(nickname);
        signUpForm.setEmail(email);
        signUpForm.setPassword(password);
        accountService.processNewAccount(signUpForm);
    }

    private void authenticateEmail(String email) {
        Account account = accountRepository.findByEmail(email);
        accountService.completeSignUp(account);
    }

    private Object getJWTToken(String email, String password) {
        SignInForm signInForm = new SignInForm();
        signInForm.setEmail(email);
        signInForm.setPassword(password);
        Map<String, Object> data = accountService.createJWTToken(signInForm);
        return data.get("jwtToken");
    }

}