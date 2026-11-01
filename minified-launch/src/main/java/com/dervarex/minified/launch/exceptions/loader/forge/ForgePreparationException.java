package com.dervarex.minified.launch.exceptions.loader.forge;

import org.apiguardian.api.API;

/**
 * Gets thrown when there was an issue preparing the environment for the forge installer
 */
@API(status = API.Status.STABLE)
public class ForgePreparationException extends RuntimeException {
    public ForgePreparationException(Throwable cause) {
        super(cause);
    }
}
