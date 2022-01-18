package com.springsns.comment;

import com.springsns.post.PostRepository;
import com.springsns.account.AccountRepository;
import com.springsns.domain.Account;
import com.springsns.domain.Comment;
import com.springsns.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentService commentService;
    private final AccountRepository accountRepository;

    @GetMapping("/comment/post/{postId}")
    public ResponseEntity getPostComment(@PathVariable Long postId){

        System.out.println("get /comment/post/{postId}");

        List<CommentResponseDto> result=commentService.findAllComments(postId);
        Map<String,Object> resultMap=new HashMap<>();
        resultMap.put("comments",result);

        return ResponseEntity.ok().body(resultMap);
    }

    @PostMapping("/comment/post/{postId}")
    public ResponseEntity createComment(@PathVariable Long postId, @RequestBody CommentForm commentForm, Principal principal){
        System.out.println("post /comment/post/{postId}");
        String email = principal.getName();
        Account account = accountRepository.findByEmail(email);

        if(account==null || !account.isEmailVerified()){
            return ResponseEntity.badRequest().build();
        }

        Post post = postRepository.findById(postId).orElseThrow();

        Comment comment = Comment.builder()
                .account(account)
                .post(post)
                .content(commentForm.getContent())
                .build();

        commentRepository.save(comment);

        CommentResponseDto commentResponseDto = new CommentResponseDto(comment);

        return new ResponseEntity(commentResponseDto,HttpStatus.OK);
    }

}
