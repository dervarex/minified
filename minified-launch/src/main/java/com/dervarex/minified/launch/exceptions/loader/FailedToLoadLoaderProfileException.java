package com.dervarex.minified.launch.exceptions.loader;

import lombok.Getter;

/**
 * Gets thrown when the profile JSON of a mod loader couldn't be fetched or read
 */
@Getter
public class FailedToLoadLoaderProfileException extends RuntimeException {
    /**
     * Loader as String in UPPERCASE, for example FABRIC or NEOFORGE
     */
    private final String loader;

    public FailedToLoadLoaderProfileException(String message, String loader, Throwable cause) {
        super(message, cause);
        this.loader = loader;
    }
}
