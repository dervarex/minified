package com.dervarex.minified.modrinth.exceptions;

import org.apiguardian.api.API;

/**
 * Thrown when dependency resolution cannot be completed, for example by
 * {@link com.dervarex.minified.modrinth.versions.Version#download(java.nio.file.Path)} when the version has no files.
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

