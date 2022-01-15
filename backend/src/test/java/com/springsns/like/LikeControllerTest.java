package com.springsns.like;

import com.springsns.Post.PostRepository;
import com.springsns.account.AccountRepository;
import com.springsns.account.AccountService;
import com.springsns.account.SignUpForm;
import com.springsns.domain.Account;
import com.springsns.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @DisplayName("좋아요 등록 - 등록 안된 사용자")
    @Test
    void registerLikeWithUnregisteredUser() throws Exception{
        //게시글 등록
        String postingNickname = "registerLikeTest1";
        String postingEmail = "registerLikeTest1@email.com";
        String postingPassword = "12345678";

        registerAccount(postingNickname,postingEmail,postingPassword);

        authenticateEmail(postingEmail);

        Account account = accountRepository.findByEmail(postingEmail);

        String content = "registerLike";
        Post post = Post.builder()
                .account(account)
                .content(content)
                .postFile(null)
                .build();

        postRepository.save(post);

        mockMvc.perform(post("/like/"+post.getId()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @DisplayName("좋아요 등록 - 등록된 사용자")
    @Test
    void registerLikeWithRegisteredUser() throws Exception{
        String postingNickname = "registerLikeTest2";
        String postingEmail = "registerLikeTest2@email.com";
        String postingPassword = "12345678";

        registerAccount(postingNickname,postingEmail,postingPassword);

        authenticateEmail(postingEmail);

        Account account = accountRepository.findByEmail(postingEmail);

        String content = "registerLike";
        Post post = Post.builder()
                .account(account)
                .content(content)
                .postFile(null)
                .build();

        postRepository.save(post);

        String nickname = "registerLikeTest3";
        String email = "registerLikeTest3@email.com";
        String password = "12345678";

        registerAccount(nickname,email,password);

        String jwt = getJWT(email);

        mockMvc.perform(post("/like/"+post.getId()).header("X-AUTH-TOKEN",jwt))
                .andDo(print())
                .andExpect(status().isOk());

        Account likedAccount = accountRepository.findByEmail(email);

        assertTrue(likeRepository.existsByAccountAndPost(likedAccount,post));
    }

    @DisplayName("좋아요 삭제 - 등록된 사용자")
    @Test
    void deleteLikeWithRegisteredUser() throws Exception{
        String postingNickname = "deleteLikeTest1";
        String postingEmail = "deleteLikeTest1@email.com";
        String postingPassword = "12345678";

        registerAccount(postingNickname,postingEmail,postingPassword);

        authenticateEmail(postingEmail);

        Account account = accountRepository.findByEmail(postingEmail);

        String content = "deleteLike";
        Post post = Post.builder()
                .account(account)
                .content(content)
                .postFile(null)
                .build();

        postRepository.save(post);

        String nickname = "deleteLikeTest2";
        String email = "deleteLikeTest2@email.com";
        String password = "12345678";

        registerAccount(nickname,email,password);

        String jwt = getJWT(email);

        Account likedAccount = accountRepository.findByEmail(email);

        likeService.addLike(email,post.getId());

        assertTrue(likeRepository.existsByAccountAndPost(likedAccount,post));

        mockMvc.perform(post("/like/"+post.getId()).header("X-AUTH-TOKEN",jwt))
                .andDo(print())
                .andExpect(status().isOk());

        assertFalse(likeRepository.existsByAccountAndPost(likedAccount,post));
    }

    @DisplayName("좋아요 한 게시글 가져오기 - 등록 안된 사용자")
    @Test
    void getLikedPostWithUnregisteredUser() throws Exception{
        //게시글 등록
        String postingNickname = "getLikedPostTest1";
        String postingEmail = "getLikedPostTest1@email.com";
        String postingPassword = "12345678";

        registerAccount(postingNickname,postingEmail,postingPassword);

        authenticateEmail(postingEmail);

        Account account = accountRepository.findByEmail(postingEmail);

        Post post = null;
        for(int i=0;i<7;i++){
            String content="GetLikedPost"+i;
            post = Post.builder()
                    .account(account)
                    .content(content)
                    .postFile(null)
                    .build();

            postRepository.save(post);
        }

        List<Post> posts = postRepository.findAll();

        //좋아요 등록
        String nickname = "getLikedPostTest2";
        String email = "getLikedPostTest2@email.com";
        String password = "12345678";

        registerAccount(nickname,email,password);

        likeService.addLike(email,posts.get(0).getId());
        likeService.addLike(email,posts.get(3).getId());

        mockMvc.perform(get("/like"))
                .andDo(print())
                .andExpect(status().isForbidden());

    }

    @DisplayName("좋아요 한 게시글 가져오기 - 등록된 사용자")
    @Test
    void getLikedPostWithRegisteredUser() throws Exception{
        //게시글 등록
        String postingNickname = "getLikedPostTest3";
        String postingEmail = "getLikedPostTest3@email.com";
        String postingPassword = "12345678";

        registerAccount(postingNickname,postingEmail,postingPassword);

        authenticateEmail(postingEmail);

        Account account = accountRepository.findByEmail(postingEmail);

        Post post = null;
        for(int i=0;i<7;i++){
            String content="GetLikedPost"+i;
            post = Post.builder()
                    .account(account)
                    .content(content)
                    .postFile(null)
                    .build();

            postRepository.save(post);
        }

        List<Post> posts = postRepository.findAll();

        //좋아요 등록
        String nickname = "getLikedPostTest4";
        String email = "getLikedPostTest4@email.com";
        String password = "12345678";

        registerAccount(nickname,email,password);

        String jwt = getJWT(email);

        likeService.addLike(email,posts.get(0).getId());
        likeService.addLike(email,posts.get(3).getId());

        mockMvc.perform(get("/like").header("X-AUTH-TOKEN",jwt))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(2)));
    }

    private void registerAccount(String nickname, String email, String password) {
        SignUpForm signUpForm = new SignUpForm(nickname,email,password);
        accountService.processSignUpAccount(signUpForm);
    }

    private void authenticateEmail(String email) {
        accountService.verifyEmailToken(email);
    }

    private String getJWT(String email) {
        String jwt = accountService.processSignInAccount(email);
        return jwt;
    }

}