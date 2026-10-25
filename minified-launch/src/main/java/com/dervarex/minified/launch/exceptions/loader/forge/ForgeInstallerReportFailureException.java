package com.dervarex.minified.launch.exceptions.loader.forge;

/**
 * Gets thrown when the forge installer reported a failure
 */
public class ForgeInstallerReportFailureException extends IllegalStateException {
    public ForgeInstallerReportFailureException(String s) {
        super(s);
    }
}
