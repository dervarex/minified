package com.dervarex.minified.modrinth.exceptions;

import org.apiguardian.api.API;

/**
 * Thrown when a model operation requires an attached Modrinth client. Models you get from a client are attached to
 * the client, so this usually happens when you call something like
 * {@link com.dervarex.minified.modrinth.projects.Project#getLatestVersion(com.dervarex.minified.modrinth.VersionSearchOptions)}
 * on a {@code Project} you created yourself
 */
@API(status = API.Status.STABLE)
public final class ModrinthStateException extends ModrinthException {
    public ModrinthStateException(String message) {
        super(message);
    }
}
