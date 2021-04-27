package com.springsns.Post;

import com.springsns.account.AccountRepository;
import com.springsns.domain.Account;
import com.springsns.domain.Post;
import com.springsns.like.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final AccountRepository accountRepository;
    private final LikeRepository likeRepository;

    @Transactional(readOnly = true)
    public List<PostResponseDto> getAllPosts(String email) {

        List<PostResponseDto> result = new ArrayList<>();
        //List<Post> postList = postRepository.findAllPosts();
        List<Post> postList=postRepository.findAll();

        Account account=null;
        if(email!=null)
            account = accountRepository.findByEmail(email);

        if(account==null){
            // 그냥 PostResponseDto에 담아서 넘겨줌
            for(Post post:postList){
                result.add(new PostResponseDto(post,false));
            }
        }else{
            // PostResponseDto에 isLike 부분 true로 처리
            for(Post post:postList){
                if(likeRepository.existsByAccountAndPost(account,post)){
                    result.add(new PostResponseDto(post,true));
                }else{
                    result.add(new PostResponseDto(post,false));
                }
            }
        }

        return result;
    }

}
