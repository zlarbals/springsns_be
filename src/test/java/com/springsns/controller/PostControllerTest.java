package com.springsns.controller;

import com.springsns.controller.dto.PostForm;
import com.springsns.repository.AccountRepository;
import com.springsns.service.AccountService;
import com.springsns.controller.dto.SignUpForm;
import com.springsns.repository.CommentRepository;
import com.springsns.domain.Account;
import com.springsns.domain.Comment;
import com.springsns.domain.Post;
import com.springsns.service.LikeService;
import com.springsns.repository.PostRepository;
import com.springsns.service.PostService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private LikeService likeService;

    @Autowired
    private CommentRepository commentRepository;

    private static final String ORIGINAL_FILE_NAME = "testpicture.PNG";
    private static final String PATH="./src/test/resources/testpicture.PNG";
    private static int registerAccountCount=0;

    @AfterEach
    public void clearRepository() {
        postRepository.deleteAll();
        accountRepository.deleteAll();
        registerAccountCount=0;
    }

    @DisplayName("사진 없는 게시글 등록 - 등록 안된 사용자")
    @Test
    void registerPostWithNoPictureWithUnregisteredUser() throws Exception{
        //given

        //when
        ResultActions resultActions = mockMvc.perform(post("/post")
                        .param("content", "hello"))
                .andDo(print());

        //then
        resultActions.andExpect(status().isUnauthorized());
    }

    @DisplayName("사진 있는 게시글 등록 - 등록 안된 사용자")
    @Test
    void registerPostWithPictureWithUnregisteredUser() throws Exception{

        //given
        MockMultipartFile image = makeMockImageFile(ORIGINAL_FILE_NAME, PATH);

        ///when
        ResultActions resultActions = mockMvc.perform(
                        multipart("/post")
                                .file(image)
                                .param("content", "hello"))
                .andDo(print());

        //then
        resultActions.andExpect(status().isUnauthorized());
    }

    @DisplayName("사진 없는 게시글 등록 - 이메일 인증 안된 사용자")
    @Test
    void registerPostWithNoPictureWithUnauthenticatedUser() throws Exception{

        //given
        Account account = registerAccount();
        String jwt = getJWT(account.getEmail());

        //when
        ResultActions resultActions = mockMvc.perform(post("/post")
                        .header("Authorization", jwt)
                        .param("content", "hello"))
                .andDo(print());

        //then
        resultActions.andExpect(status().isForbidden());
    }

    @DisplayName("사진 있는 게시글 등록 - 이메일 인증 안된 사용자")
    @Test
    void registerPostWithPictureWithUnauthenticatedUser() throws Exception{

        //given
        Account account = registerAccount();
        String jwt = getJWT(account.getEmail());

        MockMultipartFile image = makeMockImageFile(ORIGINAL_FILE_NAME, PATH);

        //when
        ResultActions resultActions = mockMvc.perform(
                        multipart("/post")
                                .file(image)
                                .param("content", "hello")
                                .header("Authorization", jwt))
                .andDo(print());

        //then
        resultActions.andExpect(status().isForbidden());
    }

    @DisplayName("사진 없는 게시글 등록 - 잘못된 입력, 인증된 사용자")
    @Test
    void registerInvalidPostWithNoPictureWithAuthenticatedUser() throws Exception{

        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail(),account.getEmailCheckToken());
        String jwt = getJWT(account.getEmail());

        //when
        ResultActions resultActions = mockMvc.perform(post("/post")
                        .header("Authorization", jwt)
                        .param("content", ""))
                .andDo(print());

        //then
        resultActions.andExpect(status().isBadRequest());

    }


    @DisplayName("사진 없는 게시글 등록 - 인증된 사용자")
    @Test
    void registerPostWithNoPictureWithAuthenticatedUser() throws Exception{

        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail(),account.getEmailCheckToken());
        String jwt = getJWT(account.getEmail());

        //when
        ResultActions resultActions = mockMvc.perform(post("/post")
                        .header("Authorization", jwt)
                        .param("content", "hello"))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.response.image").isEmpty());

    }

    @DisplayName("사진 있는 게시글 등록  - 인증된 사용자")
    @Test
    void registerPostWithPictureWithAuthenticatedUser() throws Exception{

        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail(),account.getEmailCheckToken());
        String jwt = getJWT(account.getEmail());

        MockMultipartFile image = makeMockImageFile(ORIGINAL_FILE_NAME, PATH);

        //when
        ResultActions resultActions = mockMvc.perform(
                        multipart("/post")
                                .file(image)
                                .param("content", "hello")
                                .header("Authorization", jwt))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.response.image").isNotEmpty());

    }

    @DisplayName("게시글 가져오기(좋아요 표시) - 로그인한 사용자")
    @Test
    void getPostWithRegisteredUser() throws Exception{

        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail(),account.getEmailCheckToken());
        String jwt = getJWT(account.getEmail());
        List<Post> registeredPostList = registerPost(account);
        registerLikeToPost(registeredPostList,account);

        //when
        ResultActions resultActions = mockMvc.perform(get("/post")
                        .header("Authorization", jwt))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response",hasSize(5)))
                .andExpect(jsonPath("$.response.[0].like").value(true));

    }

    @DisplayName("게시글 가져오기(좋아요 표시) - 등록 안된 사용자")
    @Test
    void getPostWithUnregisteredUser() throws Exception{

        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail(),account.getEmailCheckToken());
        List<Post> registeredPostList = registerPost(account);
        registerLikeToPost(registeredPostList,account);

        //when
        ResultActions resultActions = mockMvc.perform(get("/post"))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response",hasSize(5)))
                .andExpect(jsonPath("$.response.[0].like").value(false))
                .andExpect(jsonPath("$.response.[2].like").value(false));
    }

    @DisplayName("특정 유저의 게시글 가져오기 - 등록된 사용자")
    @Test
    void getPostFromSpecificUserWithRegisteredUser() throws Exception{

        //given
        Account postingAccount = registerAccount();
        authenticateEmail(postingAccount.getEmail(),postingAccount.getEmailCheckToken());
        registerPost(postingAccount);

        Account requestingAccount = registerAccount();
        authenticateEmail(requestingAccount.getEmail(),requestingAccount.getEmailCheckToken());
        String requestingAccountJWT = getJWT(requestingAccount.getEmail());
        registerPost(requestingAccount);

        //when
        ResultActions resultActions = mockMvc.perform(get("/post/account/" + postingAccount.getNickname())
                        .header("Authorization", requestingAccountJWT))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response",hasSize(7)))
                .andExpect(jsonPath("$.response.[0].authorNickname").value(postingAccount.getNickname()))
                .andExpect(jsonPath("$.response.[4].authorNickname").value(postingAccount.getNickname()));

    }

    @DisplayName("특정 유저의 게시글 가져오기 - 등록 안된 사용자")
    @Test
    void getPostFromSpecificUserWithUnregisteredUser() throws Exception{

        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail(),account.getEmailCheckToken());
        registerPost(account);

        //when
        ResultActions resultActions = mockMvc.perform(get("/post/account/" + account.getNickname()))
                .andDo(print());

        //then
        resultActions.andExpect(status().isUnauthorized());
    }

    @DisplayName("게시글 검색 - 등록 안된 사용자")
    @Test
    void searchPostWithUnregisteredUser() throws Exception{
        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail(),account.getEmailCheckToken());
        registerPost(account);

        String keyword = "like";

        //when
        ResultActions resultActions = mockMvc.perform(get("/post/search/" + keyword))
                .andDo(print());

        //then
        resultActions.andExpect(status().isUnauthorized());
    }

    @DisplayName("게시글 검색 - 등록된 사용자")
    @Test
    void searchPostWithRegisteredUser() throws Exception{
        //given
        Account postingAccount = registerAccount();
        authenticateEmail(postingAccount.getEmail(),postingAccount.getEmailCheckToken());
        registerPost(postingAccount);

        Account requestingAccount = registerAccount();
        String requestingAccountJWT = getJWT(requestingAccount.getEmail());

        String keyword = "like";

        //when
        ResultActions resultActions = mockMvc.perform(get("/post/search/" + keyword)
                        .header("Authorization", requestingAccountJWT))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response",hasSize(2)));
    }

    @DisplayName("게시글 삭제 - 인증된 사용자, 등록 안된 게시글 요청")
    @Test
    void deleteUnregisteredPostWithAuthenticatedUser() throws Exception{
        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail(),account.getEmailCheckToken());
        String jwt = getJWT(account.getEmail());
        registerPost(account);

        //when
        ResultActions resultActions = mockMvc.perform(delete("/post/11111111")
                        .header("Authorization", jwt))
                .andDo(print());

        //then
        resultActions.andExpect(status().isNotFound());
    }

    @DisplayName("게시글 삭제 - 인증된 사용자, 본인이 작성하지 않은 게시글 요청")
    @Test
    void deleteNotOwnedPostWithAuthenticatedUser() throws Exception{
        //given
        Account postingAccount = registerAccount();
        authenticateEmail(postingAccount.getEmail(),postingAccount.getEmailCheckToken());
        List<Post> registeredPostList = registerPost(postingAccount);

        Account requestingAccount = registerAccount();
        authenticateEmail(requestingAccount.getEmail(),requestingAccount.getEmailCheckToken());
        String requestingAccountJWT = getJWT(requestingAccount.getEmail());

        //when
        ResultActions resultActions = mockMvc.perform(delete("/post/" + registeredPostList.get(0).getId())
                        .header("Authorization", requestingAccountJWT))
                .andDo(print());

        //then
        resultActions.andExpect(status().isBadRequest());
    }

    @DisplayName("게시글 삭제 - 인증된 사용자, 본인이 작성하고 게시글에 좋아요 존재하는 경우")
    @Test
    void deletePostWithLikeWithAuthenticatedUser() throws Exception{
        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail(),account.getEmailCheckToken());
        String jwt = getJWT(account.getEmail());
        List<Post> registeredPostList = registerPost(account);
        registerLikeToPost(registeredPostList,account);

        //when
        ResultActions resultActions = mockMvc.perform(delete("/post/" + registeredPostList.get(registeredPostList.size() - 1).getId()) //like 마킹된 마지막 게시글
                        .header("Authorization", jwt))
                .andDo(print());

        //then
        resultActions.andExpect(status().isBadRequest());
    }

    @DisplayName("게시글 삭제 - 인증된 사용자, 본인이 작성하고 게시글에 댓글 존재하는 경우")
    @Test
    void deletePostWithCommentWithAuthenticatedUser() throws Exception{
        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail(),account.getEmailCheckToken());
        String jwt = getJWT(account.getEmail());
        List<Post> registeredPostList = registerPost(account);
        registerCommentToPost(registeredPostList,account);

        //when
        ResultActions resultActions = mockMvc.perform(delete("/post/" + registeredPostList.get(0).getId())
                        .header("Authorization", jwt))
                .andDo(print());

        //then
        resultActions.andExpect(status().isBadRequest());
    }

    @DisplayName("게시글 삭제 - 인증된 사용자, 본인이 작성하고 게시글에 좋아요/댓글 없는 경우")
    @Test
    void deletePostWithAuthenticatedUser() throws Exception{
        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail(),account.getEmailCheckToken());
        String jwt = getJWT(account.getEmail());
        List<Post> registeredPostList = registerPost(account);
        Long deletePostId = registeredPostList.get(0).getId();

        //when
        ResultActions resultActions = mockMvc.perform(delete("/post/" + deletePostId)
                        .header("Authorization", jwt))
                .andDo(print());

        //then
        resultActions.andExpect(status().isNoContent());

        assertFalse(postRepository.findById(deletePostId).isPresent());
    }

    @DisplayName("게시글 삭제(이미지 존재) - 인증된 사용자, 본인이 작성하고 게시글에 좋아요/댓글 없는 경우")
    @Test
    void deleteImagePostWithAuthenticatedUser() throws Exception{
        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail(),account.getEmailCheckToken());
        String jwt = getJWT(account.getEmail());

        Post deletePost = registerPostWithImage(account);
        Long deletePostId = deletePost.getId();
        String imageName = deletePost.getPostImage().getOriginalFileName();

        //when
        ResultActions resultActions = mockMvc.perform(delete("/post/" + deletePostId)
                        .header("Authorization", jwt))
                .andDo(print());

        //then
        resultActions.andExpect(status().isNoContent());

        assertFalse(postRepository.findById(deletePostId).isPresent());
        File image = new File("./src/main/resources/images/"+imageName);
        assertFalse(image.exists());
    }

    @DisplayName("이미지 다운로드 - 정상적인 요청")
    @Test
    void downloadImage() throws Exception{
        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail(),account.getEmailCheckToken());

        Post post = registerPostWithImage(account);
        String imageName = post.getPostImage().getOriginalFileName();

        //when
        ResultActions resultActions = mockMvc.perform(get("/post/image/"+imageName))
                .andDo(print());

        //then
        resultActions.andExpect(status().isOk());
    }

    @DisplayName("이미지 다운로드 - 존재하지 않는 이미지를 요청한 경우")
    @Test
    void downloadInvalidImage() throws Exception{
        //given

        //when
        ResultActions resultActions = mockMvc.perform(get("/post/image/"+"invalidImageName"))
                .andDo(print());

        //then
        resultActions.andExpect(status().isNotFound());
    }

    @DisplayName("좋아요 한 게시글 가져오기 - 등록 안된 사용자")
    @Test
    void getLikedPostWithUnregisteredUser() throws Exception{

        //given

        //when
        ResultActions resultActions = mockMvc.perform(get("/post/like"))
                .andDo(print());

        //then
        resultActions.andExpect(status().isUnauthorized());

    }

    @DisplayName("좋아요 한 게시글 가져오기 - 등록된 사용자")
    @Test
    void getLikedPostWithRegisteredUser() throws Exception{

        //given
        Account postingAccount = registerAccount();
        authenticateEmail(postingAccount.getEmail(),postingAccount.getEmailCheckToken());
        List<Post> registeredPostList = registerPost(postingAccount);

        Account likingAccount = registerAccount();
        String likingAccountJWT = getJWT(likingAccount.getEmail());
        registerLikeToPost(registeredPostList,likingAccount);

        //when
        ResultActions resultActions = mockMvc.perform(get("/post/like")
                        .header("Authorization", likingAccountJWT))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response",hasSize(2)));
    }

    private Post registerPostWithImage(Account account) throws IOException {
        MockMultipartFile image = makeMockImageFile(ORIGINAL_FILE_NAME, PATH);
        PostForm postForm = getPostForm(image);

        Post post = postService.processRegisterPost(postForm, account.getEmail());

        return post;
    }

    private PostForm getPostForm(MockMultipartFile image){
        PostForm postForm = new PostForm();
        postForm.setImage(image);
        postForm.setContent("hello");

        return postForm;
    }

    private Account registerAccount() {
        String nickname = "register"+registerAccountCount;
        String email = nickname+"@email.com";
        String password = "12345678";
        SignUpForm signUpForm = new SignUpForm(nickname, email, password);

        registerAccountCount++;

        return accountService.processSignUpAccount(signUpForm);
    }

    private void authenticateEmail(String email,String token) {
        accountService.verifyEmailToken(email,token);
    }

    private String getJWT(String email) {
        String jwt = accountService.processSignInAccount(email,"12345678");
        return jwt;
    }

    private MockMultipartFile makeMockImageFile(String originalFileName, String path) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(new File(path));

        //public ResponseEntity registerPost(@RequestPart(required = false) MultipartFile file, @RequestParam String content, Principal principal) throws IOException{}
        //MockMultipartFile의 첫번쨰 인자의 경우 controller에서 받는 변수명으로 써야 한다.
        return new MockMultipartFile("image", originalFileName,"image/png", fileInputStream);
    }

    private List<Post> registerPost(Account account) {
        //게시글, 좋아요의 주체는 account
        List<Post> registerPostsList = new ArrayList<>();

        //게시글 생성
        for(int i=0;i<7;i++){
            String content = "post content"+i;
            if(i>=5){
                content = "post like content"+i;
            }
            Post post = Post.builder()
                    .account(account)
                    .content(content)
                    .postImage(null)
                    .build();

            registerPostsList.add(postRepository.save(post));
        }

        return registerPostsList;
    }

    private void registerLikeToPost(List<Post> registeredPostList,Account account){
        int length = registeredPostList.size();
        int likePostCount = 2;
        for(int i=length-1;i>=length-likePostCount;i--){
            likeService.processAddAndDeleteLike(account.getEmail(),registeredPostList.get(i).getId());
        }
    }

    private void registerCommentToPost(List<Post> registeredPostList,Account account){
        int commentPostCount = 2;
        for(int i=0;i<commentPostCount;i++){
            String content = "comment"+i;
            Comment comment = Comment.builder()
                    .account(account)
                    .post(registeredPostList.get(i))
                    .content(content)
                    .build();

            commentRepository.save(comment);
        }
    }

}