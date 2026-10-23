package com.dervarex.minified.launch.exceptions.launch;

/**
 * Gets thrown when the Game's exit code isn't zero, which most likely indicates a crash or any other issues
 */
public class NonZeroExitCodeException extends RuntimeException {

    private final int exitCode;

    public NonZeroExitCodeException(String message, int exitCode) {
        super(message);
        this.exitCode = exitCode;
    }
}
