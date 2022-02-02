package com.springsns.service;

import com.springsns.exception.PostNotFoundException;
import com.springsns.repository.LikeRepository;
import com.springsns.repository.PostRepository;
import com.springsns.repository.AccountRepository;
import com.springsns.domain.Account;
import com.springsns.domain.Like;
import com.springsns.domain.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
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
            log.info("add like : {}",like);
        }else{
            //좋아요 취소하기 위해 누른 경우
            likeRepository.delete(like);
            log.info("delete like : {}",like);
        }
    }
}
