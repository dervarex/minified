package com.dervarex.minified.modrinth;

import org.apiguardian.api.API;

/**
 * Thrown when a model operation requires an attached Modrinth client.
 */
@API(status = API.Status.STABLE)
public final class ModrinthStateException extends ModrinthException {
    public ModrinthStateException(String message) {
        super(message);
    }
}

