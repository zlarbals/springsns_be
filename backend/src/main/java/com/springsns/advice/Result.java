package com.springsns.advice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public class Result<T> {
    private HttpStatus status;

    private T response;
}
