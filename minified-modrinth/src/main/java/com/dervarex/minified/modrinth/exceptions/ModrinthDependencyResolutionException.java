package com.dervarex.minified.modrinth.exceptions;

import org.apiguardian.api.API;

/**
 * Thrown when dependency resolution cannot be completed, for example by
 * {@link com.dervarex.minified.modrinth.versions.Version#download(java.nio.file.Path)} when the version has no files.
 */
@API(status = API.Status.STABLE)
public final class ModrinthDependencyResolutionException extends ModrinthException {
    /**
     * Creates the exception
     *
     * @param message what went wrong
     */
    public ModrinthDependencyResolutionException(String message) {
        super(message);
    }

    /**
     * Creates the exception
     *
     * @param message what went wrong
     * @param cause   the exception that caused it
     */
    public ModrinthDependencyResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

