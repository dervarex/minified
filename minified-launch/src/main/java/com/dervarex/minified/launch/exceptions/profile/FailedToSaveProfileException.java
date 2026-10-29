package com.dervarex.minified.launch.exceptions.profile;

/**
 * Gets thrown when there was an issue saving a Profile
 */
public class FailedToSaveProfileException extends RuntimeException {
    public FailedToSaveProfileException(String message, Throwable t) {
        super(message, t);
    }
}
