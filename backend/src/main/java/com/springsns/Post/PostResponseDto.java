package com.springsns.Post;

import com.springsns.domain.Post;
import lombok.Getter;

@Getter
public class PostResponseDto {

    private Long id;

    private String authorNickname;

    private String content;

    private boolean isLike;

    public PostResponseDto(Post post,boolean isLike){
        this.id=post.getId();
        this.authorNickname=post.getAuthorNickname();
        this.content=post.getContent();
        this.isLike=isLike;
    }

}
