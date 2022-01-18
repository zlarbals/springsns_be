package com.springsns.like;

import com.springsns.post.PostResponseDto;
import com.springsns.account.AccountRepository;
import com.springsns.domain.Account;
import com.springsns.domain.Like;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;
    private final AccountRepository accountRepository;

    @PostMapping("/like/{postId}")
    public ResponseEntity addLike(Principal principal, @PathVariable Long postId){
        System.out.println("here is post like");

        String email = principal.getName();

        boolean result = likeService.addLike(email,postId);

        if(!result){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(HttpStatus.OK);
    }

    //좋아요 한 게시글 가져오기.
    @GetMapping("/like")
    public ResponseEntity getMyLikePosts(Principal principal) {
        System.out.println("get /like");

        String email = principal.getName();

        Account account = accountRepository.findByEmail(email);

        List<Like> likes = account.getLikes();

        List<PostResponseDto> postList = new ArrayList<>();

        for (Like like : likes) {

            postList.add(new PostResponseDto(like.getPost(), true));
        }

        return ResponseEntity.ok(postList);
    }

}
