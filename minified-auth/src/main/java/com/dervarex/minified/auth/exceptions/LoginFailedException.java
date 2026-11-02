package com.dervarex.minified.auth.exceptions;

import org.apiguardian.api.API;

/**
 * Gets thrown when the Login has failed for some Reason.
 */
@API(status = API.Status.STABLE)
public class LoginFailedException extends RuntimeException {
    public LoginFailedException(String message, Throwable t) {
        super(message, t);
    }
}
