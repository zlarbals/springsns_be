package com.springsns.post;

import com.springsns.domain.Post;
import lombok.Getter;

@Getter
public class PostResponseDto {

    private Long id;

    private String authorNickname;

    private String content;

    private boolean isLike;

    private boolean isExistFile;

    private String fileName;

    public PostResponseDto(Post post) {
        this.id=post.getId();
        this.authorNickname=post.getAccount().getNickname();
        this.content=post.getContent();
        this.isLike=false;
        if(post.getPostImage()==null){
            this.isExistFile=false;
        }else{
            this.isExistFile=true;
            this.fileName=post.getPostImage().getOriginalFileName();
        }
    }

    public PostResponseDto(Post post, boolean isLike)  {
        this.id=post.getId();
        this.authorNickname=post.getAccount().getNickname();
        this.content=post.getContent();
        this.isLike=isLike;
        if(post.getPostImage()==null){
            this.isExistFile=false;
        }else{
            this.isExistFile=true;
            this.fileName=post.getPostImage().getOriginalFileName();
        }
    }

}