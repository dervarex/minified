package com.dervarex.minified.java;

/**
 * Utility class for checking the current Java runtime version
 */
public final class JavaPlatform {
    private JavaPlatform() {
    }

    /**
     * Gets the major version of the current Java runtime
     *
     * @return the major version (e.g., 17, 21)
     */
    public static int majorVersion() {
        return Runtime.version().feature();
    }

    /**
     * Checks if the current Java runtime is at least the specified major version
     *
     * @param version the minimum major version to check against (e.g., 17)
     * @return {@code true} if the current version is greater than or equal to {@code version}, otherwise {@code false}
     */
    public static boolean isAtLeast(int version) {
        return majorVersion() >= version;
    }
}
