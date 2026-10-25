package com.dervarex.minified.auth.exceptions;

/**
 * Gets thrown when the Login has failed for some Reason.
 */
public class LoginFailedException extends RuntimeException {
    public LoginFailedException(String message, Throwable t) {
        super(message, t);
    }
}
