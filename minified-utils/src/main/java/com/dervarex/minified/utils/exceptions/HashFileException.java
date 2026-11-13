package com.dervarex.minified.utils.exceptions;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Gets thrown when an IO error occurs while hashing a file (or at least attempting to)
 */
public class HashFileException extends IOException {
    public enum Phase { OPEN, READ }
    private final Path path;
    private final Phase phase;
    public HashFileException(Path path, Phase phase, Throwable cause) {
        super("Failed to " + phase.name().toLowerCase() + " file for hashing: " + path, cause);
        this.path = path;
        this.phase = phase;
    }
    public Path getPath() { return path; }
    public Phase getPhase() { return phase; }
}