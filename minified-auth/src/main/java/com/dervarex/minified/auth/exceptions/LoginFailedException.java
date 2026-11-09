package com.dervarex.minified.auth.exceptions;

import org.apiguardian.api.API;

/**
 * Gets thrown when the Login has failed for some Reason.
 */
@API(status = API.Status.STABLE)
public class LoginFailedException extends RuntimeException {
    /**
     * Creates the exception for a failed login
     *
     * @param message what went wrong
     * @param t       the original error
     */
    public LoginFailedException(String message, Throwable t) {
        super(message, t);
    }
}
