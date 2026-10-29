package com.dervarex.minified.launch.exceptions.loader.neoforge;

/**
 * Gets thrown when the neoforge metadata is malformed
 */
public class MalformedMetadataException extends IllegalStateException {
    public MalformedMetadataException(String message) {
        super(message);
    }
}
