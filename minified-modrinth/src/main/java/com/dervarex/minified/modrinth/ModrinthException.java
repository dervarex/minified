package com.dervarex.minified.modrinth;

/**
 * Base runtime exception for Modrinth API failures.
 */
public class ModrinthException extends RuntimeException {
    public ModrinthException(String message) {
        super(message);
    }

    public ModrinthException(String message, Throwable cause) {
        super(message, cause);
    }
}

