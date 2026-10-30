package com.dervarex.minified.worlds.exceptions;

import lombok.Getter;

import java.nio.file.Path;

/**
 * Gets thrown when a folder isn't a Minecraft world, because it doesn't exist or has no level.dat
 */
@Getter
public class InvalidWorldException extends RuntimeException {
    /**
     * The folder that was given as the world
     */
    private final transient Path worldDirectory;

    public InvalidWorldException(String message, Path worldDirectory) {
        super(message);
        this.worldDirectory = worldDirectory;
    }
}
