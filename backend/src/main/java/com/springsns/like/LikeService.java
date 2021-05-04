package com.springsns.like;

import com.springsns.Post.PostRepository;
import com.springsns.account.AccountRepository;
import com.springsns.domain.Account;
import com.springsns.domain.Like;
import com.springsns.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;

    public boolean addLike(String email, Long postId){

        Account account = accountRepository.findByEmail(email);

        if(!account.isEmailVerified()){
            return false;
        }

        Post post = postRepository.findById(postId).orElseThrow();

        Like like = likeRepository.findByAccountAndPost(account,post);

        //좋아요 하기위해 누른 경우
        if(like==null){
            likeRepository.save(new Like(account,post));
        }else{
            //좋아요 취소하기 위해 누른 경우
            likeRepository.delete(like);
        }

        return true;
    }
}
