package com.dervarex.minified.modrinth;

/**
 * Thrown when a Modrinth JSON payload cannot be parsed into a model.
 */
public final class ModrinthSerializationException extends ModrinthException {
    public ModrinthSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}

