package com.springsns.controller.dto.format;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PagingResult extends Result {
    private int pageSize;
    private boolean isFinal;

    public PagingResult(HttpStatus status, Object response,int pageSize,boolean isFinal) {
        super(status, response);
        this.pageSize=pageSize;
        this.isFinal=isFinal;
    }
}
