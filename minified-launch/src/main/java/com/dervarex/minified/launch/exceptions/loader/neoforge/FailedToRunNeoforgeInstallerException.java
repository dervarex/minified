package com.dervarex.minified.launch.exceptions.loader.neoforge;

/**
 * Gets thrown when the neoforge installer couldn't be loaded or started
 */
public class FailedToRunNeoforgeInstallerException extends RuntimeException {
    public FailedToRunNeoforgeInstallerException(Throwable cause) {
        super(cause);
    }
}
