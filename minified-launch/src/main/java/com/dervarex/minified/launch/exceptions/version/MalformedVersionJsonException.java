package com.dervarex.minified.launch.exceptions.version;

import org.apiguardian.api.API;

/**
 * Gets thrown when the version JSON is malformed
 */
@API(status = API.Status.STABLE)
public class MalformedVersionJsonException extends IllegalStateException {
    /**
     * Creates the exception
     *
     * @param message what went wrong
     */
    public MalformedVersionJsonException(String message) {
        super(message);
    }
}
