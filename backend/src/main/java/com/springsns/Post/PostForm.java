package com.springsns.Post;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PostForm {

    @NotBlank
    private String content;

}
