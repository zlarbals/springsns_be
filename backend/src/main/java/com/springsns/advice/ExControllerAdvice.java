package com.springsns.advice;

import com.springsns.exception.AccountUnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ExControllerAdvice {

    @ExceptionHandler
    public ResponseEntity accountUnauthorizedExHandle(AccountUnauthorizedException e){
        log.error("[ExHandle] ex",e);
        return new ResponseEntity(HttpStatus.UNAUTHORIZED);
    }
}
