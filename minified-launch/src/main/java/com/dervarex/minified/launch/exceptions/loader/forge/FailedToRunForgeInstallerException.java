package com.dervarex.minified.launch.exceptions.loader.forge;

/**
 * Gets thrown when the forge installer couldn't be loaded or started
 */
public class FailedToRunForgeInstallerException extends RuntimeException {
    public FailedToRunForgeInstallerException(Throwable cause) {
        super(cause);
    }
}
