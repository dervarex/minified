package com.dervarex.minified.utils.exceptions;

/**
 * This exception will get thrown when the user tries to launch in offline mode, but the launcher needs network to launch (e.g. to download missing assets or libraries)
 */
public class OfflineModeNeedsNetworkException extends RuntimeException {
    public OfflineModeNeedsNetworkException(String message) {
        super(message);
    }
}
