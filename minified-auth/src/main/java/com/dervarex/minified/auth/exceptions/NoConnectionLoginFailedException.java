package com.dervarex.minified.auth.exceptions;

import org.apiguardian.api.API;

/**
 * Gets thrown when the Login has failed because there's no network connection
 * The message is meant to be shown to the user, the cause is the original {@code NoConnectionException}
 */
@API(status = API.Status.STABLE)
public class NoConnectionLoginFailedException extends LoginFailedException {
    /**
     * Creates the exception for a login without connection
     *
     * @param message the message for the user
     * @param t       the original {@code NoConnectionException}
     */
    public NoConnectionLoginFailedException(String message, Throwable t) {
        super(message, t);
    }
}
