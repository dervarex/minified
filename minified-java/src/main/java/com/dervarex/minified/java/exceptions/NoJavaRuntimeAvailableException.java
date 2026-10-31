package com.dervarex.minified.java.exceptions;

import java.io.IOException;

/**
 * Gets thrown when there is no Java runtime to download for the required version on this platform
 */
public class NoJavaRuntimeAvailableException extends IOException {

    private static final long serialVersionUID = 1L;

    /** The Java version that was asked for */
    private final int requiredMajorVersion;
    /** The platform, for example linux-x64 */
    private final String platform;

    /**
     * Creates the exception for a Java version that has no runtime on this platform
     *
     * @param requiredMajorVersion the Java version that was asked for
     * @param platform             the platform, for example linux-x64
     */
    public NoJavaRuntimeAvailableException(int requiredMajorVersion, String platform) {
        super("No supported Java runtime asset found for version " + requiredMajorVersion + " on " + platform);
        this.requiredMajorVersion = requiredMajorVersion;
        this.platform = platform;
    }

    /**
     * {@return the Java version that was asked for}
     */
    public int getRequiredMajorVersion() {
        return requiredMajorVersion;
    }

    /**
     * {@return the platform, for example linux-x64}
     */
    public String getPlatform() {
        return platform;
    }
}
