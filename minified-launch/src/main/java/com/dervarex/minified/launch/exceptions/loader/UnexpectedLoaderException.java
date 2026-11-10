package com.dervarex.minified.launch.exceptions.loader;

import org.apiguardian.api.API;

/**
 * Gets thrown when an unexpected loader has been used
 */
@API(status = API.Status.STABLE)
public class UnexpectedLoaderException extends IllegalStateException {
    /**
     * Creates the exception
     *
     * @param message what went wrong
     */
    public UnexpectedLoaderException(String message) {
        super(message);
    }
    /**
     * Creates the exception without a message
     */
    public UnexpectedLoaderException() {
        super();
    }
    /**
     * Creates the exception
     *
     * @param message what went wrong
     * @param cause   the original error
     */
    public UnexpectedLoaderException(String message, Throwable cause) {
        super(message, cause);
    }
    /**
     * Creates the exception
     *
     * @param cause the original error
     */
    public UnexpectedLoaderException(Throwable cause) {
        super(cause);
    }
}
