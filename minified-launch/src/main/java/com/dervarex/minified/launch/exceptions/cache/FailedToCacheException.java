package com.dervarex.minified.launch.exceptions.cache;

import org.apiguardian.api.API;

/**
 * Gets thrown when there was an issue caching something
 */
@API(status = API.Status.STABLE)
public class FailedToCacheException extends RuntimeException {
    public FailedToCacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
