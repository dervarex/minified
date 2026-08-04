package com.dervarex.minified.launch.exceptions.profile;

public class FailedToLoadProfileException extends RuntimeException {
    public FailedToLoadProfileException(String message, Throwable t) {
        super(message, t);
    }
}
