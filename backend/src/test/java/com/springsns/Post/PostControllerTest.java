package com.springsns.Post;

import com.springsns.account.AccountRepository;
import com.springsns.account.AccountService;
import com.springsns.account.SignInForm;
import com.springsns.account.SignUpForm;
import com.springsns.domain.Account;
import com.springsns.domain.Post;
import com.springsns.like.LikeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private LikeService likeService;

    private static final String ORIGINAL_FILE_NAME = "testpicture.PNG";
    private static final String PATH ="C:/Project/SpringSNS/backend/src/test/resources/testpicture.PNG";

    @DisplayName("사진 없는 게시글 등록 - 등록 안된 사용자")
    @Test
    void registerPostWithNoPictureWithUnregisteredUser() throws Exception{
        mockMvc.perform(post("/post").param("content","hello"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @DisplayName("사진 있는 게시글 등록 - 등록 안된 사용자")
    @Test
    void registerPostWithPictureWithUnregisteredUser() throws Exception{
        MockMultipartFile mockMultipartFile = makeMockImageFile(ORIGINAL_FILE_NAME, PATH);

        mockMvc.perform(
                        multipart("/post")
                                .file(mockMultipartFile)
                                .param("content","hello")
                                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @DisplayName("사진 없는 게시글 등록 - 이메일 인증 안된 사용자")
    @Test
    void registerPostWithNoPictureWithUnauthenticatedUser() throws Exception{
        String nickname = "postingUnAuthenticate1";
        String email = "postingUnAuthenticate1@email.com";
        String password = "12345678";
        registerAccount(nickname,email,password);

        Object jwt = getJWTToken(email,password);

        mockMvc.perform(post("/post").header("X-AUTH-TOKEN",jwt).param("content","hello"))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @DisplayName("사진 있는 게시글 등록 - 이메일 인증 안된 사용자")
    @Test
    void registerPostWithPictureWithUnauthenticatedUser() throws Exception{
        String nickname = "postingUnAuthenticate2";
        String email = "postingUnAuthenticate2@email.com";
        String password = "12345678";
        registerAccount(nickname,email,password);

        Object jwt = getJWTToken(email,password);

        MockMultipartFile mockMultipartFile = makeMockImageFile(ORIGINAL_FILE_NAME, PATH);

        mockMvc.perform(
                        multipart("/post")
                                .file(mockMultipartFile)
                                .param("content","hello")
                                .header("X-AUTH-TOKEN",jwt)
                                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }


    @DisplayName("사진 없는 게시글 등록 - 인증된 사용자")
    @Test
    void registerPostWithNoPictureWithAuthenticatedUser() throws Exception{
        String nickname = "postingAuthenticate1";
        String email = "postingAuthenticate1@email.com";
        String password = "12345678";
        registerAccount(nickname,email,password);

        authenticateEmail(email);

        Object jwt = getJWTToken(email,password);

        mockMvc.perform(post("/post").header("X-AUTH-TOKEN",jwt).param("content","hello"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("existFile").value(false));

    }

    @DisplayName("사진 있는 게시글 등록  - 인증된 사용자")
    @Test
    void registerPostWithPictureWithAuthenticatedUser() throws Exception{
        String nickname = "postingAuthenticate2";
        String email = "postingAuthenticate2@email.com";
        String password = "12345678";
        registerAccount(nickname,email,password);

        authenticateEmail(email);

        Object jwt = getJWTToken(email,password);

        MockMultipartFile mockMultipartFile = makeMockImageFile(ORIGINAL_FILE_NAME, PATH);

        mockMvc.perform(
                multipart("/post")
                        .file(mockMultipartFile)
                        .param("content","hello")
                        .header("X-AUTH-TOKEN",jwt)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("existFile").value(true));
    }

    @DisplayName("게시글 가져오기(좋아요 표시) - 로그인한 사용자")
    @Test
    void getPostWithRegisteredUser() throws Exception{
        //인증된 사용자 등록
        String nickname = "getPostTest1";
        String email = "getPostTest1@email.com";
        String password ="12345678";
        registerAccount(nickname,email,password);

        authenticateEmail(email);

        Object jwt = getJWTToken(email,password);

        Account account = accountRepository.findByEmail(email);

        //게시글을 생성
        Post post=null;
        for(int i=0;i<7;i++){
            String content = "GetPostSignIn"+i;
            post = Post.builder()
                    .account(account)
                    .content(content)
                    .postFile(null)
                    .build();

            postRepository.save(post);
        }

        //마지막 게시글 좋아요 표시
        likeService.addLike(email,post.getId());

        //가장 마지막 게시글 = post
        //가져온 첫 게시글이 post인지 확인
        mockMvc.perform(get("/post").header("X-AUTH-TOKEN",jwt))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[0].content").value(post.getContent()))
                .andExpect(jsonPath("$.content.[0].like").value(true));

    }

    @DisplayName("게시글 가져오기(좋아요 표시) - 등록 안된 사용자")
    @Test
    void getPostWithUnregisteredUser() throws Exception{
        //인증된 사용자 등록
        String nickname = "getPostTest2";
        String email = "getPostTest2@email.com";
        String password ="12345678";
        registerAccount(nickname,email,password);

        authenticateEmail(email);

        Account account = accountRepository.findByEmail(email);

        //게시글을 생성
        Post post=null;
        for(int i=0;i<7;i++){
            String content = "GetPostSignOut"+i;
            post = Post.builder()
                    .account(account)
                    .content(content)
                    .postFile(null)
                    .build();

            postRepository.save(post);
        }

        //마지막 게시글 좋아요 표시
        likeService.addLike(email,post.getId());

        //가장 마지막 게시글 = post
        //가져온 첫 게시글이 post인지 확인
        mockMvc.perform(get("/post"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[0].content").value(post.getContent()))
                .andExpect(jsonPath("$.content.[0].like").value(false));
    }

    @DisplayName("특정 유저의 게시글 가져오기 - 등록된 사용자")
    @Test
    void getPostFromSpecificUserWithRegisteredUser() throws Exception{
        String postingNickname = "getPostTest3";
        String postingEmail = "getPostTest3@email.com";
        String postingPassword = "12345678";

        registerAccount(postingNickname,postingEmail,postingPassword);

        authenticateEmail(postingEmail);

        Account account = accountRepository.findByEmail(postingEmail);

        Post post = null;
        for(int i=0;i<7;i++){
            String content="GetPostSpecificUser"+i;
            post = Post.builder()
                    .account(account)
                    .content(content)
                    .postFile(null)
                    .build();

            postRepository.save(post);
        }

        String nickname = "getPostTest4";
        String email = "getPostTest4@email.com";
        String password = "12345678";

        registerAccount(nickname,email,password);

        Object jwt = getJWTToken(email,password);

        mockMvc.perform(get("/post/account/"+postingNickname).header("X-AUTH-TOKEN",jwt))
                .andDo(print())
                .andExpect(jsonPath("$.[0].content").value(post.getContent()))
                .andExpect(jsonPath("$",hasSize(7)));
    }

    @DisplayName("특정 유저의 게시글 가져오기 - 등록 안된 사용자")
    @Test
    void getPostFromSpecificUserWithUnregisteredUser() throws Exception{
        String postingNickname = "getPostTest5";
        String postingEmail = "getPostTest5@email.com";
        String postingPassword = "12345678";

        registerAccount(postingNickname,postingEmail,postingPassword);

        authenticateEmail(postingEmail);

        Account account = accountRepository.findByEmail(postingEmail);

        Post post = null;
        for(int i=0;i<7;i++){
            String content="GetPostSpecificUser"+i;
            post = Post.builder()
                    .account(account)
                    .content(content)
                    .postFile(null)
                    .build();

            postRepository.save(post);
        }

        mockMvc.perform(get("/post/account/"+postingNickname))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @DisplayName("게시글 검색 - 등록 안된 사용자")
    @Test
    void searchPostWithUnregisteredUser() throws Exception{
        String postingNickname = "searchPostTest1";
        String postingEmail = "searchPostTest1@email.com";
        String postingPassword = "12345678";

        registerAccount(postingNickname,postingEmail,postingPassword);

        authenticateEmail(postingEmail);

        Account account = accountRepository.findByEmail(postingEmail);

        for(int i=0;i<7;i++){
            String content=null;
            if(i==1||i==2||i==4){
                content = "search hello post test"+i;
            }else{
                content = "search post test"+i;
            }

            Post post= Post.builder()
                    .account(account)
                    .content(content)
                    .postFile(null)
                    .build();

            postRepository.save(post);
        }

        String keyword = "hello";
        mockMvc.perform(get("/post/search/"+keyword))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @DisplayName("게시글 검색 - 이메일 인증 안된 사용자")
    @Test
    void searchPostWithUnauthenticatedUser() throws Exception{
        String postingNickname = "searchPostTest2";
        String postingEmail = "searchPostTest2@email.com";
        String postingPassword = "12345678";

        registerAccount(postingNickname,postingEmail,postingPassword);

        authenticateEmail(postingEmail);

        Account account = accountRepository.findByEmail(postingEmail);

        for(int i=0;i<7;i++){
            String content=null;
            if(i==1||i==2||i==4){
                content = "search hello post test"+i;
            }else{
                content = "search post test"+i;
            }

            Post post= Post.builder()
                    .account(account)
                    .content(content)
                    .postFile(null)
                    .build();

            postRepository.save(post);
        }

        String nickname = "searchPostTest3";
        String email = "searchPostTest3@email.com";
        String password = "12345678";

        registerAccount(nickname,email,password);

        Object jwt = getJWTToken(email,password);

        String keyword = "hello";
        mockMvc.perform(get("/post/search/"+keyword).header("X-AUTH-TOKEN",jwt))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @DisplayName("게시글 검색 - 인증된 사용자")
    @Test
    void searchPostWithAuthenticatedUser() throws Exception{
        String postingNickname = "searchPostTest4";
        String postingEmail = "searchPostTest4@email.com";
        String postingPassword = "12345678";

        registerAccount(postingNickname,postingEmail,postingPassword);

        authenticateEmail(postingEmail);

        Account account = accountRepository.findByEmail(postingEmail);

        for(int i=0;i<7;i++){
            String content=null;
            if(i==1||i==2||i==4){
                content = "search hello post test"+i;
            }else{
                content = "search post test"+i;
            }

            Post post= Post.builder()
                    .account(account)
                    .content(content)
                    .postFile(null)
                    .build();

            postRepository.save(post);
        }

        String nickname = "searchPostTest5";
        String email = "searchPostTest5@email.com";
        String password = "12345678";

        registerAccount(nickname,email,password);

        authenticateEmail(email);

        Object jwt = getJWTToken(email,password);

        String keyword = "hello";
        mockMvc.perform(get("/post/search/"+keyword).header("X-AUTH-TOKEN",jwt))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(3)));
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

    private MockMultipartFile makeMockImageFile(String originalFileName, String path) throws IOException {

        FileInputStream fileInputStream = new FileInputStream(new File(path));

        //public ResponseEntity registerPost(@RequestPart(required = false) MultipartFile file, @RequestParam String content, Principal principal) throws IOException{}
        //MockMultipartFile의 첫번쨰 인자의 경우 controller에서 받는 변수명으로 써야 한다.
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", originalFileName,"image/png", fileInputStream);

        return mockMultipartFile;
    }

}