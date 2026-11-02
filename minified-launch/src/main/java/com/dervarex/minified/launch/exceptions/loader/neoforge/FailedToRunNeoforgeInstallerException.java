package com.dervarex.minified.launch.exceptions.loader.neoforge;

import org.apiguardian.api.API;

/**
 * Gets thrown when the neoforge installer couldn't be loaded or started
 */
@API(status = API.Status.STABLE)
public class FailedToRunNeoforgeInstallerException extends RuntimeException {
    public FailedToRunNeoforgeInstallerException(Throwable cause) {
        super(cause);
    }
}
