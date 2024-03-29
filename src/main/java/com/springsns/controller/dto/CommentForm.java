package com.springsns.controller.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class CommentForm {

    @NotBlank
    String content;

}
