package com.dervarex.minified.worlds.exceptions;

import lombok.Getter;

import java.nio.file.Path;

/**
 * Gets thrown when a world or a part of a World couldn't be read from disk
 */
@Getter
public class FailedToLoadWorldException extends RuntimeException {
    /**
     * The world folder or file that couldn't be read
     */
    private final transient Path path;

    /**
     * @param message the message
     * @param path the world folder or file that couldn't be read
     * @param cause why it couldn't be read
     */
    public FailedToLoadWorldException(String message, Path path, Throwable cause) {
        super(message, cause);
        this.path = path;
    }
}
