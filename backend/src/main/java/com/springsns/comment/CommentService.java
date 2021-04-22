package com.springsns.comment;

import com.springsns.Post.PostRepository;
import com.springsns.domain.Comment;
import com.springsns.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public List<CommentResponseDto> findAllComments(Long postId){
        Post post = postRepository.findById(postId).orElseThrow();
        List<Comment> comments = commentRepository.findCommentByPost(post);
        List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();

        for(Comment comment:comments){
            commentResponseDtoList.add(new CommentResponseDto(comment));
        }

        return commentResponseDtoList;
    }

}
