package com.dervarex.minified.launch.exceptions.loader.forge;

import org.apiguardian.api.API;

/**
 * Gets thrown when the forge installer reported a failure
 */
@API(status = API.Status.STABLE)
public class ForgeInstallerReportFailureException extends IllegalStateException {
    public ForgeInstallerReportFailureException(String s) {
        super(s);
    }
}
