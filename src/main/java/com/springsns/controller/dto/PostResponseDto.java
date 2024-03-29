package com.springsns.controller.dto;

import com.springsns.domain.Post;
import com.springsns.domain.PostImage;
import lombok.Getter;

@Getter
public class PostResponseDto {

    private Long id;

    private String authorNickname;

    private String content;

    private boolean isLike;

    private PostImage image;

    public PostResponseDto(Post post, boolean isLike)  {
        this.id=post.getId();
        this.authorNickname=post.getAccount().getNickname();
        this.content=post.getContent();
        this.isLike=isLike;
        this.image = post.getPostImage();
    }

}
