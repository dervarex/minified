package com.dervarex.minified.launch.exceptions.launch;

import org.apiguardian.api.API;

/**
 * Gets thrown when the launch failed for a reason that has no more specific exception, check the cause for details
 */
@API(status = API.Status.STABLE)
public class LaunchFailedException extends RuntimeException {
    /**
     * Creates the exception
     *
     * @param message what went wrong
     * @param cause   the original error
     */
    public LaunchFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
