package com.dervarex.minified.launch.exceptions.launch;

import org.apiguardian.api.API;

/**
 * Gets thrown when the Game's exit code isn't zero, which most likely indicates a crash or any other issues
 */
@API(status = API.Status.STABLE)
public class NonZeroExitCodeException extends RuntimeException {

    private final int exitCode;

    public NonZeroExitCodeException(String message, int exitCode) {
        super(message);
        this.exitCode = exitCode;
    }
}
