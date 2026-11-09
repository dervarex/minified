package com.dervarex.minified.launch.exceptions.profile;

import org.apiguardian.api.API;

/**
 * Gets thrown when there was an issue loading a Profile
 */
@API(status = API.Status.STABLE)
public class FailedToLoadProfileException extends RuntimeException {
    /**
     * Creates the exception
     *
     * @param message what went wrong
     * @param t       the original error
     */
    public FailedToLoadProfileException(String message, Throwable t) {
        super(message, t);
    }

    /**
     * Creates the exception
     *
     * @param message what went wrong
     */
    public FailedToLoadProfileException(String message) {
        super(message);
    }
}
