package com.springsns.comment;

import com.springsns.account.AccountRepository;
import com.springsns.domain.Account;
import com.springsns.exception.EmailNotVerifiedException;
import com.springsns.exception.PostNotFoundException;
import com.springsns.post.PostRepository;
import com.springsns.domain.Comment;
import com.springsns.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;

    public List<Comment> findAllComments(Long postId,String email){
        accountRepository.findActivateAccountByEmail(email).orElseThrow(()->new IllegalArgumentException("존재하지 않는 계정입니다."));

        Post post = postRepository.findById(postId).orElseThrow(()-> new PostNotFoundException("존재하지 않는 게시글 입니다."));

        return commentRepository.findCommentByPost(post);
    }

    @Transactional
    public Comment registerComment(Long postId, String email, String commentContent) {
        Account account = accountRepository.findActivateAccountByEmail(email).orElseThrow(()->new IllegalArgumentException("존재하지 않는 계정입니다."));

        //이메일인증했는지 확인.
        validateEmailVerified(account);

        //게시글이 존재하는지 확인
        Post post = postRepository.findById(postId).orElseThrow(()-> new PostNotFoundException("존재하지 않는 게시글 입니다."));

        return saveNewComment(account,post,commentContent);
    }

    private void validateEmailVerified(Account account){
        if(!account.isEmailVerified()){
            throw new EmailNotVerifiedException("이메일 인증이 완료되지 않았습니다.");
        }
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
