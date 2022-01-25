package com.springsns.post;

import com.springsns.account.AccountRepository;
import com.springsns.comment.CommentRepository;
import com.springsns.domain.Account;
import com.springsns.domain.Post;
import com.springsns.like.LikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PostController {

    private final AccountRepository accountRepository;
    private final PostRepository postRepository;
    private final PostService postService;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    @PostMapping("/post")
    public ResponseEntity registerPost(@Validated @ModelAttribute PostForm postForm, BindingResult bindingResult, HttpServletRequest request) throws IOException {
        log.info("PostController.Post./post");

        String email = (String) request.getAttribute("SignInAccountEmail");

        //이메일 인증했는지 체크
        accountEmailVerifiedCheck(bindingResult, email);

        if (bindingResult.hasErrors()) {
            log.info("register post error : {}", bindingResult);
            return new ResponseEntity(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }

        Post post = postService.processRegisterPost(postForm, email);

        return new ResponseEntity(new PostResponseDto(post), HttpStatus.OK);
    }

    @GetMapping("/post")
    public ResponseEntity getPostsByPageSlice(@PageableDefault(size = 5, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable, HttpServletRequest request) {
        log.info("PostController.Get./post");

        Slice<Post> slicePost = postRepository.findPostByPaging(pageable);
        //jwt가 없어도 접근할 수 있기 때문에 email이 null일 수 있다.
        String email = (String) request.getAttribute("SignInAccountEmail");
        Slice<PostResponseDto> slicePostsDto = changePostToPostResponseDtoByLike(slicePost, email);

        return new ResponseEntity(slicePostsDto,HttpStatus.OK);
    }

    @GetMapping("/post/account/{nickname}")
    public ResponseEntity getPostsByNickname(@PathVariable String nickname,HttpServletRequest request) {
        log.info("PostController.Get./post/account/{nickname}");

        String email = (String) request.getAttribute("SignInAccountEmail");

        List<Post> postsByNickname = postRepository.findPostsByNickname(nickname);

        List<PostResponseDto> postsDto = changePostToPostResponseDtoByLike(postsByNickname, email);

        return new ResponseEntity(postsDto,HttpStatus.OK);
    }

    @GetMapping("/post/image/{imageName:.+}")
    public ResponseEntity downloadImage(@PathVariable String imageName,HttpServletRequest request) throws IOException {
        log.info("PostController.Get./post/image/{imageName:.+}");

        Resource resource = postService.getImageAsResource(imageName);

        String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());

        if(contentType==null){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/post/search/{keyword}")
    public ResponseEntity searchPostsByKeyword(@PathVariable String keyword, HttpServletRequest request) {
        log.info("PostController.Get./post/search/{keyword}");

        String email = (String) request.getAttribute("SignInAccountEmail");

        List<Post> postsByContentContaining = postRepository.findPostsByContentContaining(keyword);

        List<PostResponseDto> postsDto = changePostToPostResponseDtoByLike(postsByContentContaining,email);

        return new ResponseEntity(postsDto, HttpStatus.OK);
    }

    @DeleteMapping("/post/{postId}")
    public ResponseEntity deletePost(@PathVariable long postId, HttpServletRequest request) {
        log.info("PostController.Delete./post/{postId}");

        String email = (String) request.getAttribute("SignInAccountEmail");

        if(!isPostDeleteConditionValid(postId,email)){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        //삭제
        postService.deletePost(postId);

        return new ResponseEntity(HttpStatus.OK);
    }

    private void accountEmailVerifiedCheck(BindingResult bindingResult, String email) {
        Account account = accountRepository.findByEmail(email);

        if (account == null || !account.isEmailVerified()) {
            bindingResult.reject("authenticate");
        }
    }


    private Slice<PostResponseDto> changePostToPostResponseDtoByLike(Slice<Post> slicePost, String email) {
        if (email == null) {
            return slicePost.map(post -> new PostResponseDto(post, false));
        }

        Account account = accountRepository.findByEmail(email);

        Slice<PostResponseDto> result = slicePost.map(post -> {
            if (likeRepository.existsByAccountAndPost(account, post)) {
                return new PostResponseDto(post, true);
            } else {
                return new PostResponseDto(post, false);
            }
        });

        return result;
    }

    private List<PostResponseDto> changePostToPostResponseDtoByLike(List<Post> postsByNickname,String email){
        Account account = accountRepository.findByEmail(email);

        List<PostResponseDto> result = new ArrayList<>();
        for (Post post : postsByNickname) {
            if(likeRepository.existsByAccountAndPost(account,post)){
                result.add(new PostResponseDto(post,true));
            }else{
                result.add(new PostResponseDto(post,false));
            }
        }

        return result;
    }

    private boolean isPostDeleteConditionValid(long postId,String email){
        Account account = accountRepository.findByEmail(email);

        //게시글이 있는지 확인
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isEmpty()) {
            return false;
        }

        //본인이 작성한 게시글인지 확인
        Post post = optionalPost.get();
        if (!post.getAccount().equals(account)) {
            return false;
        }

        //좋아요나 댓글 존재하는지 확인
        if (isPostHaveLikeOrComment(post)) {
            return false;
        }

        return true;
    }

    private boolean isPostHaveLikeOrComment(Post post) {
        if(likeRepository.existsByPost(post) || commentRepository.existsByPost(post)){
            return true;
        }

        return false;
    }

}
