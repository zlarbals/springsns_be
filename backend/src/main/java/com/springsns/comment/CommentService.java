package com.springsns.comment;

import com.springsns.account.AccountRepository;
import com.springsns.domain.Account;
import com.springsns.post.PostRepository;
import com.springsns.domain.Comment;
import com.springsns.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;

    public List<Comment> findAllComments(Long postId){
        //controller에서 사전에 존재 확인 완료.
        Post post = postRepository.findById(postId).get();
        List<Comment> comments = commentRepository.findCommentByPost(post);

        return comments;
    }

    public Comment registerComment(Long postId, String email, CommentForm commentForm) {
        Account account = accountRepository.findByEmail(email);

        //controller에서 사전에 존재 확인 완료.
        Post post = postRepository.findById(postId).get();

        return saveNewComment(account,post,commentForm.getContent());
    }

    private Comment saveNewComment(Account account, Post post, String content) {
        Comment comment = Comment.builder()
                .account(account)
                .post(post)
                .content(content)
                .build();

        return commentRepository.save(comment);
    }


}
