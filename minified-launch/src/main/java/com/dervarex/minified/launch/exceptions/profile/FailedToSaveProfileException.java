package com.dervarex.minified.launch.exceptions.profile;

import org.apiguardian.api.API;

/**
 * Gets thrown when there was an issue saving a Profile
 */
@API(status = API.Status.STABLE)
public class FailedToSaveProfileException extends RuntimeException {
    public FailedToSaveProfileException(String message, Throwable t) {
        super(message, t);
    }
}
