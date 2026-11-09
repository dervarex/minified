package com.dervarex.minified.launch.exceptions.loader.neoforge;

import org.apiguardian.api.API;

/**
 * Gets thrown when there was an issue reading the neoforge metadata
 */
@API(status = API.Status.STABLE)
public class FailedToReadMetadataException extends RuntimeException {
    /**
     * Creates the exception
     *
     * @param message what went wrong
     */
    public FailedToReadMetadataException(String message) {
        super(message);
    }
    /**
     * Creates the exception
     *
     * @param message what went wrong
     * @param cause   the original error
     */
    public FailedToReadMetadataException(String message, Throwable cause) {
        super(message, cause);
    }
}
