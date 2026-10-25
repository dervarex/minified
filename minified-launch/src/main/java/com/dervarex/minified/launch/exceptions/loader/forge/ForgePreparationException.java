package com.dervarex.minified.launch.exceptions.loader.forge;

/**
 * Gets thrown when there was an issue preparing the environment for the forge installer
 */
public class ForgePreparationException extends RuntimeException {
    public ForgePreparationException(Throwable cause) {
        super(cause);
    }
}
