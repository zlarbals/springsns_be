package com.springsns.comment;

import com.springsns.post.PostRepository;
import com.springsns.account.AccountRepository;
import com.springsns.domain.Account;
import com.springsns.domain.Comment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final PostRepository postRepository;
    private final CommentService commentService;
    private final AccountRepository accountRepository;

    @GetMapping("/comment/post/{postId}")
    public ResponseEntity getPostComment(@PathVariable Long postId){
        log.info("CommentController.Get./comment/post/{postId}");

        if(!isPostExist(postId)){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        List<Comment> comments=commentService.findAllComments(postId);

        List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();
        for(Comment comment:comments){
            commentResponseDtoList.add(new CommentResponseDto(comment));
        }

        Map<String,Object> resultMap=new HashMap<>();
        resultMap.put("comments",commentResponseDtoList);

        return ResponseEntity.ok().body(resultMap);
    }

    @PostMapping("/comment/post/{postId}")
    public ResponseEntity createComment(@PathVariable Long postId, @Validated @RequestBody CommentForm commentForm, BindingResult bindingResult, Principal principal){
        log.info("CommentController.Post./comment/post/{postId}");

        if(bindingResult.hasErrors()){
            log.info("register comment error : {}", bindingResult);
            return new ResponseEntity(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }

        String email = principal.getName();

        if(!isRegisterCommentConditionValid(postId,email)){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        Comment comment = commentService.registerComment(postId,email,commentForm);

        CommentResponseDto commentResponseDto = new CommentResponseDto(comment);

        return new ResponseEntity(commentResponseDto,HttpStatus.OK);
    }

    private boolean isRegisterCommentConditionValid(Long postId, String email) {

        Account account = accountRepository.findByEmail(email);

        //comment 등록할 post가 존재하는지 확인.
        if(!isPostExist(postId)){
            return false;
        }

        //이메일 인증 했는지 확인.
        if(!account.isEmailVerified()){
            return false;
        }

        return true;
    }

    private boolean isPostExist(Long postId) {
        return postRepository.existsById(postId);
    }

}
