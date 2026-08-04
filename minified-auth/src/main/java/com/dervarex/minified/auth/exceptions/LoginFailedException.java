package com.dervarex.minified.auth.exceptions;

public class LoginFailedException extends RuntimeException {
    public LoginFailedException(String message, Throwable t) {
        super(message, t);
    }
}
