package com.dervarex.minified.launch.exceptions.version;

public class FailedToFetchVersionsException extends RuntimeException {
    /**
     * Loader as String in UPPERCASE, for example FABRIC or NEOFORGE
     */
    private final String loader;

    public FailedToFetchVersionsException(String message, String loader) {
        super(message);
        this.loader = loader;
    }

    public FailedToFetchVersionsException(String message, String loader, Throwable cause) {
        super(message, cause);
        this.loader = loader;
    }

}
