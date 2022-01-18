package com.springsns.post;

import com.springsns.account.AccountRepository;
import com.springsns.account.AccountService;
import com.springsns.account.SignUpForm;
import com.springsns.comment.CommentRepository;
import com.springsns.domain.Account;
import com.springsns.domain.Comment;
import com.springsns.domain.Post;
import com.springsns.like.LikeService;
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
        resultActions.andExpect(status().isForbidden());
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
        resultActions.andExpect(status().isForbidden());
    }

    @DisplayName("사진 없는 게시글 등록 - 이메일 인증 안된 사용자")
    @Test
    void registerPostWithNoPictureWithUnauthenticatedUser() throws Exception{

        //given
        Account account = registerAccount();
        String jwt = getJWT(account.getEmail());

        //when
        ResultActions resultActions = mockMvc.perform(post("/post")
                        .header("X-AUTH-TOKEN", jwt)
                        .param("content", "hello"))
                .andDo(print());

        //then
        resultActions.andExpect(status().is4xxClientError());
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
                                .header("X-AUTH-TOKEN", jwt))
                .andDo(print());

        //then
        resultActions.andExpect(status().is4xxClientError());
    }


    @DisplayName("사진 없는 게시글 등록 - 인증된 사용자")
    @Test
    void registerPostWithNoPictureWithAuthenticatedUser() throws Exception{

        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail());
        String jwt = getJWT(account.getEmail());

        //when
        ResultActions resultActions = mockMvc.perform(post("/post")
                        .header("X-AUTH-TOKEN", jwt)
                        .param("content", "hello"))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("existFile").value(false));

    }

    @DisplayName("사진 있는 게시글 등록  - 인증된 사용자")
    @Test
    void registerPostWithPictureWithAuthenticatedUser() throws Exception{

        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail());
        String jwt = getJWT(account.getEmail());

        MockMultipartFile image = makeMockImageFile(ORIGINAL_FILE_NAME, PATH);

        //when
        ResultActions resultActions = mockMvc.perform(
                        multipart("/post")
                                .file(image)
                                .param("content", "hello")
                                .header("X-AUTH-TOKEN", jwt))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("existFile").value(true));

    }

    @DisplayName("게시글 가져오기(좋아요 표시) - 로그인한 사용자")
    @Test
    void getPostWithRegisteredUser() throws Exception{

        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail());
        String jwt = getJWT(account.getEmail());
        List<Post> registeredPostList = registerPost(account);
        registerLikeToPost(registeredPostList,account);

        //when
        ResultActions resultActions = mockMvc.perform(get("/post")
                        .header("X-AUTH-TOKEN", jwt))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content",hasSize(5))) // slice 적용되었으므로 5개
                .andExpect(jsonPath("$.content.[0].like").value(true))
                .andExpect(jsonPath("$.content.[2].like").value(false));

    }

    @DisplayName("게시글 가져오기(좋아요 표시) - 등록 안된 사용자")
    @Test
    void getPostWithUnregisteredUser() throws Exception{

        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail());
        List<Post> registeredPostList = registerPost(account);
        registerLikeToPost(registeredPostList,account);

        //when
        ResultActions resultActions = mockMvc.perform(get("/post"))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content",hasSize(5))) //slice 적용되었으므로 5개
                .andExpect(jsonPath("$.content.[0].like").value(false))
                .andExpect(jsonPath("$.content.[2].like").value(false));
    }

    @DisplayName("특정 유저의 게시글 가져오기 - 등록된 사용자")
    @Test
    void getPostFromSpecificUserWithRegisteredUser() throws Exception{

        //given
        Account postingAccount = registerAccount();
        authenticateEmail(postingAccount.getEmail());
        registerPost(postingAccount);

        Account requestingAccount = registerAccount();
        authenticateEmail(requestingAccount.getEmail());
        String requestingAccountJWT = getJWT(requestingAccount.getEmail());
        registerPost(requestingAccount);

        //when
        ResultActions resultActions = mockMvc.perform(get("/post/account/" + postingAccount.getNickname())
                        .header("X-AUTH-TOKEN", requestingAccountJWT))
                .andDo(print());

        //then
        resultActions
                .andExpect(jsonPath("$",hasSize(7)))
                .andExpect(jsonPath("$.[0].authorNickname").value(postingAccount.getNickname()))
                .andExpect(jsonPath("$.[4].authorNickname").value(postingAccount.getNickname()));

    }

    @DisplayName("특정 유저의 게시글 가져오기 - 등록 안된 사용자")
    @Test
    void getPostFromSpecificUserWithUnregisteredUser() throws Exception{

        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail());
        registerPost(account);

        //when
        ResultActions resultActions = mockMvc.perform(get("/post/account/" + account.getNickname()))
                .andDo(print());

        //then
        resultActions.andExpect(status().is4xxClientError());
    }

    @DisplayName("게시글 검색 - 등록 안된 사용자")
    @Test
    void searchPostWithUnregisteredUser() throws Exception{
        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail());
        registerPost(account);

        String keyword = "like";

        //when
        ResultActions resultActions = mockMvc.perform(get("/post/search/" + keyword))
                .andDo(print());

        //then
        resultActions.andExpect(status().isForbidden());
    }

    @DisplayName("게시글 검색 - 등록된 사용자")
    @Test
    void searchPostWithRegisteredUser() throws Exception{
        //given
        Account postingAccount = registerAccount();
        authenticateEmail(postingAccount.getEmail());
        registerPost(postingAccount);

        Account requestingAccount = registerAccount();
        String requestingAccountJWT = getJWT(requestingAccount.getEmail());

        String keyword = "like";

        //when
        ResultActions resultActions = mockMvc.perform(get("/post/search/" + keyword)
                        .header("X-AUTH-TOKEN", requestingAccountJWT))
                .andDo(print());

        //then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(2)));
    }

    @DisplayName("게시글 삭제 - 인증된 사용자, 등록 안된 게시글 요청")
    @Test
    void deleteUnregisteredPostWithAuthenticatedUser() throws Exception{
        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail());
        String jwt = getJWT(account.getEmail());
        registerPost(account);

        //when
        ResultActions resultActions = mockMvc.perform(delete("/post/11111111")
                        .header("X-AUTH-TOKEN", jwt))
                .andDo(print());

        //then
        resultActions.andExpect(status().is4xxClientError());
    }

    @DisplayName("게시글 삭제 - 인증된 사용자, 본인이 작성하지 않은 게시글 요청")
    @Test
    void deleteNotOwnedPostWithAuthenticatedUser() throws Exception{
        //given
        Account postingAccount = registerAccount();
        authenticateEmail(postingAccount.getEmail());
        List<Post> registeredPostList = registerPost(postingAccount);

        Account requestingAccount = registerAccount();
        authenticateEmail(requestingAccount.getEmail());
        String requestingAccountJWT = getJWT(requestingAccount.getEmail());

        //when
        ResultActions resultActions = mockMvc.perform(delete("/post/" + registeredPostList.get(0).getId())
                        .header("X-AUTH-TOKEN", requestingAccountJWT))
                .andDo(print());

        //then
        resultActions.andExpect(status().is4xxClientError());
    }

    @DisplayName("게시글 삭제 - 인증된 사용자, 본인이 작성하고 게시글에 좋아요 존재하는 경우")
    @Test
    void deletePostWithLikeWithAuthenticatedUser() throws Exception{
        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail());
        String jwt = getJWT(account.getEmail());
        List<Post> registeredPostList = registerPost(account);
        registerLikeToPost(registeredPostList,account);

        //when
        ResultActions resultActions = mockMvc.perform(delete("/post/" + registeredPostList.get(registeredPostList.size() - 1).getId()) //like 마킹된 마지막 게시글
                        .header("X-AUTH-TOKEN", jwt))
                .andDo(print());

        //then
        resultActions.andExpect(status().is4xxClientError());
    }

    @DisplayName("게시글 삭제 - 인증된 사용자, 본인이 작성하고 게시글에 댓글 존재하는 경우")
    @Test
    void deletePostWithCommentWithAuthenticatedUser() throws Exception{
        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail());
        String jwt = getJWT(account.getEmail());
        List<Post> registeredPostList = registerPost(account);
        registerCommentToPost(registeredPostList,account);

        //when
        ResultActions resultActions = mockMvc.perform(delete("/post/" + registeredPostList.get(0).getId())
                        .header("X-AUTH-TOKEN", jwt))
                .andDo(print());

        //then
        resultActions.andExpect(status().is4xxClientError());
    }

    @DisplayName("게시글 삭제 - 인증된 사용자, 본인이 작성하고 게시글에 좋아요/댓글 없는 경우")
    @Test
    void deletePostWithAuthenticatedUser() throws Exception{
        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail());
        String jwt = getJWT(account.getEmail());
        List<Post> registeredPostList = registerPost(account);
        Long deletePostId = registeredPostList.get(0).getId();

        //when
        ResultActions resultActions = mockMvc.perform(delete("/post/" + deletePostId)
                        .header("X-AUTH-TOKEN", jwt))
                .andDo(print());

        //then
        resultActions.andExpect(status().isOk());

        assertFalse(postRepository.findById(deletePostId).isPresent());
    }

    @DisplayName("게시글 삭제(이미지 존재) - 인증된 사용자, 본인이 작성하고 게시글에 좋아요/댓글 없는 경우")
    @Test
    void deleteImagePostWithAuthenticatedUser() throws Exception{
        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail());
        String jwt = getJWT(account.getEmail());

        Post deletePost = registerPostWithImage(account);
        Long deletePostId = deletePost.getId();
        String imageName = deletePost.getPostImage().getOriginalFileName();

        //when
        ResultActions resultActions = mockMvc.perform(delete("/post/" + deletePostId)
                        .header("X-AUTH-TOKEN", jwt))
                .andDo(print());

        //then
        resultActions.andExpect(status().isOk());

        assertFalse(postRepository.findById(deletePostId).isPresent());
        File image = new File("./src/main/resources/images/"+imageName);
        assertFalse(image.exists());
    }

    @DisplayName("이미지 다운로드 - 정상적인 요청")
    @Test
    void downloadImage() throws Exception{
        //given
        Account account = registerAccount();
        authenticateEmail(account.getEmail());

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
        resultActions.andExpect(status().is4xxClientError());
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

    private void authenticateEmail(String email) {
        accountService.verifyEmailToken(email);
    }

    private String getJWT(String email) {
        String jwt = accountService.processSignInAccount(email);
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
            likeService.addLike(account.getEmail(),registeredPostList.get(i).getId());
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