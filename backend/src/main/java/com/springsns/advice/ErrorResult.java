package com.springsns.advice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public class ErrorResult {

    private HttpStatus status;

    private String errorMessage;

}
