package com.dervarex.minified.launch.exceptions.profile;

public class FailedToSaveProfileException extends RuntimeException {
    public FailedToSaveProfileException(String message, Throwable t) {
        super(message, t);
    }
}
