package com.springsns.Post;

import com.springsns.domain.Post;
import lombok.Getter;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
        if(post.getPostFile()==null){
            this.isExistFile=false;
        }else{
            this.isExistFile=true;
            this.fileName=post.getPostFile().getFileName();
        }
    }

    public PostResponseDto(Post post, boolean isLike)  {
        this.id=post.getId();
        this.authorNickname=post.getAccount().getNickname();
        this.content=post.getContent();
        this.isLike=isLike;
        if(post.getPostFile()==null){
            this.isExistFile=false;
        }else{
            this.isExistFile=true;
            this.fileName=post.getPostFile().getFileName();
        }
    }

}
