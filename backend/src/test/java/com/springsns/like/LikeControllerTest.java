package com.springsns.like;

import com.springsns.post.PostRepository;
import com.springsns.account.AccountRepository;
import com.springsns.account.AccountService;
import com.springsns.account.SignUpForm;
import com.springsns.domain.Account;
import com.springsns.domain.Post;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class LikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private LikeService likeService;

    private static int registerAccountCount=0;

    @AfterEach
    public void clearRepository() {
        likeRepository.deleteAll();
        postRepository.deleteAll();
        accountRepository.deleteAll();
        registerAccountCount=0;
    }

    @DisplayName("좋아요 등록 - 등록 안된 사용자")
    @Test
    void registerLikeWithUnregisteredUser() throws Exception{

        //given
        Account postingAccount = registerAccount();
        authenticateEmail(postingAccount.getEmail());
        Post post = registerPost(postingAccount);

        //when
        ResultActions resultActions = mockMvc.perform(post("/like/" + post.getId()))
                .andDo(print());

        //then
        resultActions.andExpect(status().isUnauthorized());
    }

    @DisplayName("좋아요 등록 - 등록된 사용자")
    @Test
    void registerLikeWithRegisteredUser() throws Exception{

        //given
        Account postingAccount = registerAccount();
        authenticateEmail(postingAccount.getEmail());
        Post post = registerPost(postingAccount);

        Account likingAccount = registerAccount();
        String likingAccountJWT = getJWT(likingAccount.getEmail());

        //when
        ResultActions resultActions = mockMvc.perform(post("/like/" + post.getId())
                        .header("Authorization", likingAccountJWT))
                .andDo(print());

        //then
        resultActions.andExpect(status().isOk());

        assertTrue(likeRepository.existsByAccountAndPost(likingAccount,post));
    }

    @DisplayName("좋아요 등록 - 존재하지 않는 게시글, 등록된 사용자")
    @Test
    void registerLikeInNullPostWithRegisteredUser() throws Exception{

        //given
        Account likingAccount = registerAccount();
        String likingAccountJWT = getJWT(likingAccount.getEmail());

        //when
        ResultActions resultActions = mockMvc.perform(post("/like/" + 99999)
                        .header("Authorization", likingAccountJWT))
                .andDo(print());

        //then
        resultActions.andExpect(status().isBadRequest());
    }

    @DisplayName("좋아요 삭제 - 등록된 사용자")
    @Test
    void deleteLikeWithRegisteredUser() throws Exception{

        //given
        Account postingAccount = registerAccount();
        authenticateEmail(postingAccount.getEmail());
        Post post = registerPost(postingAccount);

        Account likingAccount = registerAccount();
        String likingAccountJWT = getJWT(likingAccount.getEmail());
        registerLikeToPost(post,likingAccount);

        //when
        ResultActions resultActions = mockMvc.perform(post("/like/" + post.getId())
                        .header("Authorization", likingAccountJWT))
                .andDo(print());

        //then
        resultActions.andExpect(status().isOk());

        assertFalse(likeRepository.existsByAccountAndPost(likingAccount,post));
    }

    @DisplayName("좋아요 한 게시글 가져오기 - 등록 안된 사용자")
    @Test
    void getLikedPostWithUnregisteredUser() throws Exception{

        //given

        //when
        ResultActions resultActions = mockMvc.perform(get("/like"))
                .andDo(print());

        //then
        resultActions.andExpect(status().isUnauthorized());

    }

    @DisplayName("좋아요 한 게시글 가져오기 - 등록된 사용자")
    @Test
    void getLikedPostWithRegisteredUser() throws Exception{

        //given
        Account postingAccount = registerAccount();
        authenticateEmail(postingAccount.getEmail());
        Post post = registerPost(postingAccount);

        Account likingAccount = registerAccount();
        String likingAccountJWT = getJWT(likingAccount.getEmail());
        registerLikeToPost(post,likingAccount);

        //when
        ResultActions resultActions = mockMvc.perform(get("/like")
                        .header("Authorization", likingAccountJWT))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(1)));
    }

    private Account registerAccount() {
        String nickname = "register"+registerAccountCount;
        String email = nickname+"@email.com";
        String password = "12345678";
        SignUpForm signUpForm = new SignUpForm(nickname, email, password);

        registerAccountCount++;

        return accountService.processSignUpAccount(signUpForm);
    }

    private void authenticateEmail(String email) {
        accountService.verifyEmailToken(email);
    }

    private String getJWT(String email) {
        String jwt = accountService.processSignInAccount(email);
        return "Bearer "+jwt;
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

    private void registerLikeToPost(Post post,Account account){
        likeService.processAddAndDeleteLike(account.getEmail(),post.getId());
    }

}