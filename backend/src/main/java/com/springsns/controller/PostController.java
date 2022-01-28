package com.springsns.controller;

import com.springsns.controller.dto.Result;
import com.springsns.controller.dto.PostForm;
import com.springsns.controller.dto.PostResponseDto;
import com.springsns.domain.Post;
import com.springsns.service.PostService;
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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/post")
    public ResponseEntity<Result> registerPost(@Validated @ModelAttribute PostForm postForm, BindingResult bindingResult, HttpServletRequest request) throws IOException {
        log.info("PostController.Post./post");

        if (bindingResult.hasErrors()) {
            log.info("register post error : {}", bindingResult);
            throw new IllegalArgumentException("잘못된 형식입니다.");
        }

        String email = (String) request.getAttribute("SignInAccountEmail");
        Post post = postService.processRegisterPost(postForm, email);

        PostResponseDto postResponseDto = new PostResponseDto(post,false);



        return new ResponseEntity(new Result(HttpStatus.CREATED,postResponseDto), HttpStatus.CREATED);
    }

    @GetMapping("/post")
    public ResponseEntity<Result> getPostsByPageSlice(@PageableDefault(size = 5, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable, HttpServletRequest request) {
        log.info("PostController.Get./post");

        //jwt가 없어도 접근할 수 있기 때문에 email이 null일 수 있다.
        String email = (String) request.getAttribute("SignInAccountEmail");

        Slice<PostResponseDto> postResponseDtoSlice = postService.findPostsByPagingAsDto(pageable,email);

        return new ResponseEntity(new Result(HttpStatus.OK,postResponseDtoSlice),HttpStatus.OK);
    }

    @GetMapping("/post/account/{nickname}")
    public ResponseEntity<Result> getPostsByNickname(@PathVariable String nickname,HttpServletRequest request) {
        log.info("PostController.Get./post/account/{nickname}");

        String email = (String) request.getAttribute("SignInAccountEmail");

        List<PostResponseDto> postResponseDtoList = postService.findPostsByNicknameAsDto(nickname,email);

        return new ResponseEntity(new Result(HttpStatus.OK,postResponseDtoList),HttpStatus.OK);
    }

    @GetMapping("/post/image/{imageName:.+}")
    public ResponseEntity downloadImage(@PathVariable String imageName,HttpServletRequest request) throws IOException {
        log.info("PostController.Get./post/image/{imageName:.+}");

        Resource resource = postService.getImageAsResource(imageName);

        String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/post/search/{keyword}")
    public ResponseEntity searchPostsByKeyword(@PathVariable String keyword, HttpServletRequest request) {
        log.info("PostController.Get./post/search/{keyword}");

        String email = (String) request.getAttribute("SignInAccountEmail");

        List<PostResponseDto> postResponseDtoList = postService.findPostsByKeywordSearchAsDto(keyword,email);

        return new ResponseEntity(new Result(HttpStatus.OK,postResponseDtoList), HttpStatus.OK);
    }

    @DeleteMapping("/post/{postId}")
    public ResponseEntity deletePost(@PathVariable long postId, HttpServletRequest request) {
        log.info("PostController.Delete./post/{postId}");

        String email = (String) request.getAttribute("SignInAccountEmail");

        postService.deletePost(postId,email);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/post/like")
    public ResponseEntity getMyLikedPosts(HttpServletRequest request) {
        log.info("PostController.Get./post/like");

        String email = (String) request.getAttribute("SignInAccountEmail");

        List<Post> likedPosts = postService.findLikedPosts(email);

        List<PostResponseDto> postResponseDtoList = likedPosts.stream()
                .map(post -> new PostResponseDto(post,true))
                .collect(Collectors.toList());

        return new ResponseEntity(new Result(HttpStatus.OK,postResponseDtoList),HttpStatus.OK);
    }
}
