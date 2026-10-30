package com.dervarex.minified.java.exceptions;

import java.io.IOException;

/**
 * Gets thrown when a downloaded Java runtime can't be installed, for example
 * because the archive format is unknown or there is no java executable in it
 */
public class JavaRuntimeInstallException extends IOException {

    private static final long serialVersionUID = 1L;

    /**
     * @param message what went wrong
     */
    public JavaRuntimeInstallException(String message) {
        super(message);
    }
}
