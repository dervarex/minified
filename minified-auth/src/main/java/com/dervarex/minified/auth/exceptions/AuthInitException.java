package com.dervarex.minified.auth.exceptions;

import org.apiguardian.api.API;

/**
 * Gets thrown when the AuthManager couldn't be initialized, for example because
 * the base directory can't be created or the master key can't be loaded or created
 */
@API(status = API.Status.STABLE)
public class AuthInitException extends RuntimeException {
    public AuthInitException(String message, Throwable t) {
        super(message, t);
    }
}
