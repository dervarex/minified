package com.dervarex.minified.launch.exceptions.loader.forge;

public class FailedToFetchPromotionsException extends RuntimeException {

    public FailedToFetchPromotionsException(String message) {
        super(message);
    }

    public FailedToFetchPromotionsException(String message, Throwable cause) {
        super(message, cause);
    }
}
