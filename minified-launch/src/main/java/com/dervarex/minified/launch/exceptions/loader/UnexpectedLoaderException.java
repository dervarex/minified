package com.dervarex.minified.launch.exceptions.loader;

/**
 * Gets thrown when an unexpected loader has been used
 */
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
