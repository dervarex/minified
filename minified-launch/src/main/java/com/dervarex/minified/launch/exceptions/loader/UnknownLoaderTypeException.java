package com.dervarex.minified.launch.exceptions.loader;

/**
 * Gets thrown when an unknown loader type has been used
 */
public class UnknownLoaderTypeException extends RuntimeException {
    public UnknownLoaderTypeException(String message) {
        super(message);
    }
}
