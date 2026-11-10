package com.dervarex.minified.launch.exceptions.loader.forge;

import org.apiguardian.api.API;

/**
 * Gets thrown when there was an issue fetching the forge promotions.
 */
@API(status = API.Status.STABLE)
public class FailedToFetchPromotionsException extends RuntimeException {

    /**
     * Creates the exception
     *
     * @param message what went wrong
     */
    public FailedToFetchPromotionsException(String message) {
        super(message);
    }

    /**
     * Creates the exception
     *
     * @param message what went wrong
     * @param cause   the original error
     */
    public FailedToFetchPromotionsException(String message, Throwable cause) {
        super(message, cause);
    }
}
