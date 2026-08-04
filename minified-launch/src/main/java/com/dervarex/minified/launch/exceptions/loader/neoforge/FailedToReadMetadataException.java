package com.dervarex.minified.launch.exceptions.loader.neoforge;

public class FailedToReadMetadataException extends RuntimeException {
    public FailedToReadMetadataException(String message) {
        super(message);
    }
    public FailedToReadMetadataException(String message, Throwable cause) {
        super(message, cause);
    }
}
