package com.dervarex.minified.launch.exceptions.loader.neoforge;

/**
 * Gets thrown when there was an issue preparing the environment for the neoforge installer
 */
public class NeoforgePreparationException extends RuntimeException {
    public NeoforgePreparationException(Throwable cause) {
        super(cause);
    }
}
