package com.springsns.like;

import com.springsns.post.PostRepository;
import com.springsns.post.PostResponseDto;
import com.springsns.domain.Like;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;
    private final PostRepository postRepository;

    @PostMapping("/like/{postId}")
    public ResponseEntity addAndDeleteLike(@PathVariable Long postId, Principal principal){
        log.info("LikeController.Post./like/{postId}");

        if(!isPostExist(postId)){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        String email = principal.getName();
        likeService.processAddAndDeleteLike(email,postId);

        return new ResponseEntity(HttpStatus.OK);
    }

    //좋아요 한 게시글 가져오기.
    @GetMapping("/like")
    public ResponseEntity getMyLikePosts(Principal principal) {
        log.info("LikeController.Get./like");

        String email = principal.getName();

        List<Like> likes = likeService.findAllLikes(email);
        List<PostResponseDto> likedPostsResponseDto = changeLikedPostToPostResponseDto(likes);

        return new ResponseEntity(likedPostsResponseDto,HttpStatus.OK);
    }

    private boolean isPostExist(Long postId) {
        return postRepository.existsById(postId);
    }

    private List<PostResponseDto> changeLikedPostToPostResponseDto(List<Like> likes){
        List<PostResponseDto> postResponseDtoList = new ArrayList<>();
        for(Like like:likes){
            postResponseDtoList.add(new PostResponseDto(like.getPost(),true));
        }

        return postResponseDtoList;
    }

}
