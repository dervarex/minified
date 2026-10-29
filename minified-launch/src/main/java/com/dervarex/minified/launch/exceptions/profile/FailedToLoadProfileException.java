package com.dervarex.minified.launch.exceptions.profile;

/**
 * Gets thrown when there was an issue loading a Profile
 */
public class FailedToLoadProfileException extends RuntimeException {
    public FailedToLoadProfileException(String message, Throwable t) {
        super(message, t);
    }
}
