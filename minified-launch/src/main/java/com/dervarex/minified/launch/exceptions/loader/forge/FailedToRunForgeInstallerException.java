package com.dervarex.minified.launch.exceptions.loader.forge;

import org.apiguardian.api.API;

/**
 * Gets thrown when the forge installer couldn't be loaded or started
 */
@API(status = API.Status.STABLE)
public class FailedToRunForgeInstallerException extends RuntimeException {
    public FailedToRunForgeInstallerException(Throwable cause) {
        super(cause);
    }
}
