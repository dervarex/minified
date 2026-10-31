package com.dervarex.minified.auth.exceptions;

/**
 * Gets thrown when the Login has failed because there's no network connection
 * The message is meant to be shown to the user, the cause is the original {@code NoConnectionException}
 */
public class NoConnectionLoginFailedException extends LoginFailedException {
    public NoConnectionLoginFailedException(String message, Throwable t) {
        super(message, t);
    }
}
