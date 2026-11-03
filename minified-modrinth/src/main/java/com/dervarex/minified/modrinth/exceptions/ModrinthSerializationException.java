package com.dervarex.minified.modrinth.exceptions;

import org.apiguardian.api.API;

/**
 * Thrown when a Modrinth JSON payload cannot be parsed into a model. Usually the API changed its response format,
 * or the body wasn't valid JSON
 */
@API(status = API.Status.STABLE)
public final class ModrinthSerializationException extends ModrinthException {
    public ModrinthSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}

