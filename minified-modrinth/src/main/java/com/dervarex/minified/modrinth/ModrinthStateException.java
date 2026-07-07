package com.dervarex.minified.modrinth;

/**
 * Thrown when a model operation requires an attached Modrinth client.
 */
public final class ModrinthStateException extends ModrinthException {
    public ModrinthStateException(String message) {
        super(message);
    }
}

