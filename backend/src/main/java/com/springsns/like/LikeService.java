package com.springsns.like;

import com.springsns.exception.PostNotFoundException;
import com.springsns.post.PostRepository;
import com.springsns.account.AccountRepository;
import com.springsns.domain.Account;
import com.springsns.domain.Like;
import com.springsns.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public void processAddAndDeleteLike(String email, Long postId){
        Account account = accountRepository.findActivateAccountByEmail(email).orElseThrow(()->new IllegalArgumentException("존재하지 않는 계정입니다."));

        Post post = postRepository.findById(postId).orElseThrow(()-> new PostNotFoundException("존재하지 않는 게시글 입니다."));


        Like like = likeRepository.findByAccountAndPost(account,post);

        if(like==null){
            //좋아요 하기위해 누른 경우
            likeRepository.save(new Like(account,post));
        }else{
            //좋아요 취소하기 위해 누른 경우
            likeRepository.delete(like);
        }
    }
}
