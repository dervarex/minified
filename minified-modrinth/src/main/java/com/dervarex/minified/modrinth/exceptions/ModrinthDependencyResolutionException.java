package com.dervarex.minified.modrinth.exceptions;

import org.apiguardian.api.API;

/**
 * Thrown when dependency resolution cannot be completed.
 */
@API(status = API.Status.STABLE)
public final class ModrinthDependencyResolutionException extends ModrinthException {
    public ModrinthDependencyResolutionException(String message) {
        super(message);
    }

    public ModrinthDependencyResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

