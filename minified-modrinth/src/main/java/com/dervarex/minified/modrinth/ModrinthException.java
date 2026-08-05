package com.dervarex.minified.modrinth;

import org.apiguardian.api.API;

/**
 * Base runtime exception for Modrinth API failures.
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

