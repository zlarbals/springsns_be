package com.springsns.controller.dto;

import com.springsns.domain.Comment;
import lombok.Getter;

@Getter
public class CommentResponseDto {

    private Long id;

    private String authorNickname;

    private String content;

    public CommentResponseDto(Comment comment){
        this.id=comment.getId();
        this.authorNickname=comment.getAccount().getNickname();
        this.content=comment.getContent();
    }

}
