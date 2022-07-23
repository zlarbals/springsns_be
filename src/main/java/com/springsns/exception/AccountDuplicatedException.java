package com.springsns.exception;

public class AccountDuplicatedException extends RuntimeException{

    public AccountDuplicatedException() {
        super();
    }

    public AccountDuplicatedException(String message) {
        super(message);
    }

    public AccountDuplicatedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountDuplicatedException(Throwable cause) {
        super(cause);
    }
}
