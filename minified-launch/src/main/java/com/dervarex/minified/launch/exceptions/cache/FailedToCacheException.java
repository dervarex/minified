package com.dervarex.minified.launch.exceptions.cache;

/**
 * Gets thrown when there was an issue caching something
 */
public class FailedToCacheException extends RuntimeException {
    public FailedToCacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
