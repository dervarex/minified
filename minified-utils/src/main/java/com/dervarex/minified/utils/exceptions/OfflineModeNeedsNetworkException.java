package com.dervarex.minified.utils.exceptions;

import org.apiguardian.api.API;

import java.util.Collections;
import java.util.List;

/**
 * This exception will get thrown when the user tries to launch in offline mode, but the launcher needs network to launch for a specified {@link Reason}
 */
@API(status = API.Status.STABLE)
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

    /**
     * @param message the message
     */
    public OfflineModeNeedsNetworkException(String message) {
        this(message, Reason.UNKNOWN, Collections.emptyList(), null);
    }

    /**
     * @param message the message
     * @param cause the cause
     */
    public OfflineModeNeedsNetworkException(String message, Throwable cause) {
        this(message, Reason.UNKNOWN, Collections.emptyList(), cause);
    }
    /**
     * @param message the message
     * @param reason why network is needed
     * @param cause the cause
     */
    public OfflineModeNeedsNetworkException(String message, Reason reason, Throwable cause) {
        this(message, reason, Collections.emptyList(), cause);
    }

    /**
     * @param message the message
     * @param reason why network is needed
     */
    public OfflineModeNeedsNetworkException(String message, Reason reason) {
        this(message, reason, Collections.emptyList(), null);
    }

    /**
     * @param message the message
     * @param reason why network is needed
     * @param missingResources what's missing
     */
    public OfflineModeNeedsNetworkException(String message, Reason reason, List<String> missingResources) {
        this(message, reason, missingResources, null);
    }

    /**
     * @param message the message
     * @param reason why network is needed, null means {@link Reason#UNKNOWN}
     * @param missingResources what's missing
     * @param cause the cause
     */
    public OfflineModeNeedsNetworkException(String message, Reason reason, List<String> missingResources, Throwable cause) {
        super(message, cause);
        this.reason = reason != null ? reason : Reason.UNKNOWN;
        this.missingResources = missingResources != null
                ? List.copyOf(missingResources)
                : Collections.emptyList();
    }

    /**
     * {@return the reason}
     */
    public Reason getReason() {
        return reason;
    }

    /**
     * {@return the missing resources}
     */
    public List<String> getMissingResources() {
        return missingResources;
    }

    /**
     * {@return true if any missing resources are listed}
     */
    public boolean hasMissingResources() {
        return !missingResources.isEmpty();
    }
}
