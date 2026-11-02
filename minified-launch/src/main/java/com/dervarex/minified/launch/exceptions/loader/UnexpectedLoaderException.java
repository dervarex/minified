package com.dervarex.minified.launch.exceptions.loader;

import org.apiguardian.api.API;

/**
 * Gets thrown when an unexpected loader has been used
 */
@API(status = API.Status.STABLE)
public class UnexpectedLoaderException extends IllegalStateException {
    public UnexpectedLoaderException(String message) {
        super(message);
    }
    public UnexpectedLoaderException() {
        super();
    }
    public UnexpectedLoaderException(String message, Throwable cause) {
        super(message, cause);
    }
    public UnexpectedLoaderException(Throwable cause) {
        super(cause);
    }
}
