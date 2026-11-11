package com.dervarex.minified.launch.exceptions.launch;

import lombok.Getter;
import org.apiguardian.api.API;

/**
 * Gets thrown when the Game's exit code isn't zero, which most likely indicates a crash or any other issues
 */
@Getter
@API(status = API.Status.STABLE)
public class NonZeroExitCodeException extends RuntimeException {

    /** The exit code the game stopped with */
    private final int exitCode;

    /**
     * Creates the exception
     *
     * @param message  what went wrong
     * @param exitCode the game's exit code
     */
    public NonZeroExitCodeException(String message, int exitCode) {
        super(message);
        this.exitCode = exitCode;
    }
}
