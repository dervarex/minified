package com.dervarex.minified.launch.exceptions.loader.neoforge;

import org.apiguardian.api.API;

/**
 * Gets thrown when the neoforge installer reported failure
 */
@API(status = API.Status.STABLE)
public class NeoforgeInstallerReportFailureException extends IllegalStateException {
    public NeoforgeInstallerReportFailureException(String s) {
        super(s);
    }
}
