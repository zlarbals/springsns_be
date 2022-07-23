package com.springsns.advice;

import com.springsns.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ExControllerAdvice {

    @ExceptionHandler({IllegalArgumentException.class,IllegalStateException.class})
    public ResponseEntity<ErrorResult> illegalArgumentExHandle(Exception e){
        log.error("[ExHandle] ex",e);
        ErrorResult errorResult = new ErrorResult(HttpStatus.BAD_REQUEST,e.getMessage());
        return new ResponseEntity(errorResult,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResult> accountUnauthorizedExHandle(AccountUnauthorizedException e){
        log.error("[ExHandle] ex",e);
        ErrorResult errorResult = new ErrorResult(HttpStatus.UNAUTHORIZED,e.getMessage());
        return new ResponseEntity(errorResult,HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({PostNotFoundException.class,ImageNotFoundException.class})
    public ResponseEntity<ErrorResult> notFoundExHandle(Exception e){
        log.error("[ExHandle] ex",e);
        ErrorResult errorResult = new ErrorResult(HttpStatus.NOT_FOUND,e.getMessage());
        return new ResponseEntity(errorResult,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResult> emailNotVerifiedExHandle(EmailNotVerifiedException e){
        log.error("[ExHandle] ex",e);
        ErrorResult errorResult = new ErrorResult(HttpStatus.FORBIDDEN,e.getMessage());
        return new ResponseEntity(errorResult,HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResult> accountDuplicatedException(AccountDuplicatedException e){
        log.error("[ExHandle] ex",e);
        ErrorResult errorResult = new ErrorResult(HttpStatus.CONFLICT,e.getMessage());
        return new ResponseEntity(errorResult,HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResult> ExHandle(Exception e){
        log.error("[ExHandle] ex",e);
        ErrorResult errorResult = new ErrorResult(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());
        return new ResponseEntity(errorResult,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
