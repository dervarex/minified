package com.dervarex.minified.launch.exceptions.launch;

/**
 * Gets thrown when the launch failed for a reason that has no more specific exception, check the cause for details
 */
public class LaunchFailedException extends RuntimeException {
    public LaunchFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
