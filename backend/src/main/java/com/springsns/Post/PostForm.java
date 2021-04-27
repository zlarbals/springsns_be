package com.springsns.Post;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;

@Data
public class PostForm {

    @NotBlank
    private String content;

}
