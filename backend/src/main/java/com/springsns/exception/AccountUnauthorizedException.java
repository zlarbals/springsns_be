package com.springsns.exception;

public class AccountUnauthorizedException extends RuntimeException{
    public AccountUnauthorizedException() {
        super();
    }

    public AccountUnauthorizedException(String message) {
        super(message);
    }

    public AccountUnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountUnauthorizedException(Throwable cause) {
        super(cause);
    }
}
