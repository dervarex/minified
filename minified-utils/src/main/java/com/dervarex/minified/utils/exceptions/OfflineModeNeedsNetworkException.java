package com.dervarex.minified.utils.exceptions;

import org.apiguardian.api.API;

import java.util.Collections;
import java.util.List;

/**
 * This exception will get thrown when the user tries to launch in offline mode, but the launcher needs network to launch for a specified {@link Reason}
 */
@API(status = API.Status.EXPERIMENTAL)
public class OfflineModeNeedsNetworkException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Reasons why network access is required
     */
    public enum Reason {
        MISSING_ASSETS,
        MISSING_LIBRARIES,
        MISSING_CLIENT_JAR,
        MISSING_VERSION_MANIFEST, // missing or corrupt
        MISSING_JAVA_RUNTIME,
        MISSING_LOADER_PROFILE,
        AUTH_REQUIRED,
        UNKNOWN
    }

    private final Reason reason;
    private final List<String> missingResources;

    public OfflineModeNeedsNetworkException(String message) {
        this(message, Reason.UNKNOWN, Collections.emptyList(), null);
    }

    public OfflineModeNeedsNetworkException(String message, Throwable cause) {
        this(message, Reason.UNKNOWN, Collections.emptyList(), cause);
    }
    public OfflineModeNeedsNetworkException(String message, Reason reason, Throwable cause) {
        this(message, reason, Collections.emptyList(), cause);
    }

    public OfflineModeNeedsNetworkException(String message, Reason reason) {
        this(message, reason, Collections.emptyList(), null);
    }

    public OfflineModeNeedsNetworkException(String message, Reason reason, List<String> missingResources) {
        this(message, reason, missingResources, null);
    }

    public OfflineModeNeedsNetworkException(String message, Reason reason, List<String> missingResources, Throwable cause) {
        super(message, cause);
        this.reason = reason != null ? reason : Reason.UNKNOWN;
        this.missingResources = missingResources != null
                ? List.copyOf(missingResources)
                : Collections.emptyList();
    }

    public Reason getReason() {
        return reason;
    }

    public List<String> getMissingResources() {
        return missingResources;
    }

    public boolean hasMissingResources() {
        return !missingResources.isEmpty();
    }
}