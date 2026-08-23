package com.dervarex.minified.utils.exceptions;

import org.apiguardian.api.API;

/**
 * This exception will get thrown when the user tries to launch in offline mode, but the launcher needs network to launch (e.g. to download missing assets or libraries)
 * Will get expanded with Minified v3.0. #todo
 */
@API(status = API.Status.EXPERIMENTAL)
public class OfflineModeNeedsNetworkException extends RuntimeException {
    public OfflineModeNeedsNetworkException(String message) {
        super(message);
    }
}
