package com.springsns.comment;

import com.springsns.Post.PostRepository;
import com.springsns.domain.Comment;
import com.springsns.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentService commentService;

    @GetMapping("/comment/post/{postId}")
    public ResponseEntity getPostComment(@PathVariable Long postId){

        System.out.println("here is get com.springsns.comment");

        List<CommentResponseDto> result=commentService.findAllComments(postId);

        return new ResponseEntity(result, HttpStatus.OK);
    }

    @PostMapping("/comment/post/{postId}")
    public ResponseEntity createComment(@PathVariable Long postId, @RequestBody CommentForm commentForm, Principal principal){
        System.out.println("here is post com.springsns.comment");
        Post post = postRepository.findById(postId).orElseThrow();
        String email = principal.getName();

        Comment comment = Comment.builder()
                .authorEmail(email)
                .authorNickname(commentForm.getAuthorNickname())
                .content(commentForm.getContent())
                .post(post)
                .postedAt(LocalDateTime.now())
                .build();

        commentRepository.save(comment);

        CommentResponseDto commentResponseDto = new CommentResponseDto(comment);

        return new ResponseEntity(commentResponseDto,HttpStatus.OK);
    }

}
