package com.springsns.like;

import com.springsns.post.PostRepository;
import com.springsns.account.AccountRepository;
import com.springsns.domain.Account;
import com.springsns.domain.Like;
import com.springsns.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;

    public void processAddAndDeleteLike(String email, Long postId){

        Account account = accountRepository.findByEmail(email);

        Post post = postRepository.findById(postId).get();

        Like like = likeRepository.findByAccountAndPost(account,post);

        //좋아요 하기위해 누른 경우
        if(like==null){
            likeRepository.save(new Like(account,post));
        }else{
            //좋아요 취소하기 위해 누른 경우
            likeRepository.delete(like);
        }
    }

    public List<Like> findAllLikes(String email) {
        Account account = accountRepository.findByEmail(email);

        List<Like> likes = likeRepository.findAllByAccount(account);
        return likes;
    }
}
