package com.dervarex.minified.launch.exceptions.loader.forge;

/**
 * Gets thrown when there was an issue fetching the forge promotions.
 */
public class FailedToFetchPromotionsException extends RuntimeException {

    public FailedToFetchPromotionsException(String message) {
        super(message);
    }

    public FailedToFetchPromotionsException(String message, Throwable cause) {
        super(message, cause);
    }
}
