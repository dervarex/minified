package com.dervarex.minified.worlds.exceptions;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Gets thrown when a world's session.lock is already taken
 */
@Getter
public class WorldLockedException extends IOException {
    /**
     * The session.lock file
     */
    private final transient Path lockFile;

    /**
     * @param message the message
     * @param lockFile the session.lock file
     * @param cause why it couldn't be locked
     */
    public WorldLockedException(String message, Path lockFile, Throwable cause) {
        super(message, cause);
        this.lockFile = lockFile;
    }
}
