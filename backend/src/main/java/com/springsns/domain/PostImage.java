package com.springsns.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PostImage {

    private String originalFileName;

    private String fileName;

    private String extension;

}
