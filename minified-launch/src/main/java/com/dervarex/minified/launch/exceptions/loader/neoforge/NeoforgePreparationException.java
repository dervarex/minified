package com.dervarex.minified.launch.exceptions.loader.neoforge;

import org.apiguardian.api.API;

/**
 * Gets thrown when there was an issue preparing the environment for the neoforge installer
 */
@API(status = API.Status.STABLE)
public class NeoforgePreparationException extends RuntimeException {
    public NeoforgePreparationException(Throwable cause) {
        super(cause);
    }
}
