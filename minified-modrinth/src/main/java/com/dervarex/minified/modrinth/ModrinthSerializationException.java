package com.dervarex.minified.modrinth;

import org.apiguardian.api.API;

/**
 * Thrown when a Modrinth JSON payload cannot be parsed into a model.
 */
@API(status = API.Status.STABLE)
public final class ModrinthSerializationException extends ModrinthException {
    public ModrinthSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}

