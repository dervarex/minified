package com.dervarex.minified.modrinth;

/**
 * Thrown when dependency resolution cannot be completed.
 */
public final class ModrinthDependencyResolutionException extends ModrinthException {
    public ModrinthDependencyResolutionException(String message) {
        super(message);
    }

    public ModrinthDependencyResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

