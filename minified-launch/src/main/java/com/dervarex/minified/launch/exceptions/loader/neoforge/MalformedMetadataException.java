package com.dervarex.minified.launch.exceptions.loader.neoforge;

import org.apiguardian.api.API;

/**
 * Gets thrown when the neoforge metadata is malformed
 */
@API(status = API.Status.STABLE)
public class MalformedMetadataException extends IllegalStateException {
    public MalformedMetadataException(String message) {
        super(message);
    }
}
