package com.springsns.comment;

import com.springsns.advice.Result;
import com.springsns.domain.Comment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/comment/post/{postId}")
    public ResponseEntity<Result> getPostComment(@PathVariable Long postId,HttpServletRequest request){
        log.info("CommentController.Get./comment/post/{postId}");

        String email = (String) request.getAttribute("SignInAccountEmail");

        //TODO 이메일 요청에서 받고 계정 존재하는지 확인해야함.
        List<Comment> comments=commentService.findAllComments(postId,email);

        List<CommentResponseDto> commentResponseDtoList = comments.stream()
                .map(comment -> new CommentResponseDto(comment))
                .collect(Collectors.toList());

        return new ResponseEntity(new Result(HttpStatus.OK,commentResponseDtoList),HttpStatus.OK);
    }

    @PostMapping("/comment/post/{postId}")
    public ResponseEntity<Result> createComment(@PathVariable Long postId, @Validated @RequestBody CommentForm commentForm, BindingResult bindingResult, HttpServletRequest request){
        log.info("CommentController.Post./comment/post/{postId}");

        if(bindingResult.hasErrors()){
            log.info("register comment error : {}", bindingResult);
            throw new IllegalArgumentException("잘못된 형식입니다.");
        }

        String email = (String) request.getAttribute("SignInAccountEmail");
        Comment comment = commentService.registerComment(postId,email,commentForm.getContent());

        CommentResponseDto commentResponseDto = new CommentResponseDto(comment);

        return new ResponseEntity(new Result(HttpStatus.CREATED,commentResponseDto),HttpStatus.CREATED);
    }

}
