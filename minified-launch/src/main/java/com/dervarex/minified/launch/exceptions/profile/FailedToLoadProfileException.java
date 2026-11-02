package com.dervarex.minified.launch.exceptions.profile;

import org.apiguardian.api.API;

/**
 * Gets thrown when there was an issue loading a Profile
 */
@API(status = API.Status.STABLE)
public class FailedToLoadProfileException extends RuntimeException {
    public FailedToLoadProfileException(String message, Throwable t) {
        super(message, t);
    }
}
