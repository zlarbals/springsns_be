package com.springsns.post;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class PostForm {

    private MultipartFile image;

    @NotBlank
    private String content;

}
