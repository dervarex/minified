package com.dervarex.minified.modrinth.exceptions;

import org.apiguardian.api.API;

/**
 * Base runtime exception for Modrinth API failures. Every exception {@code minified-modrinth} throws extends this
 * one, but catch this one as a generic fallback.
 */
@API(status = API.Status.STABLE)
public class ModrinthException extends RuntimeException {
    public ModrinthException(String message) {
        super(message);
    }

    public ModrinthException(String message, Throwable cause) {
        super(message, cause);
    }
}

