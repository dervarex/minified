package com.dervarex.minified.launch.exceptions.version;

/**
 * Gets thrown when the version JSON is malformed
 */
public class MalformedVersionJsonException extends IllegalStateException {
    public MalformedVersionJsonException(String message) {
        super(message);
    }
}
