package com.dervarex.minified.java;

import java.nio.file.Path;

/**
 * Describes a resolved Java runtime.
 *
 * <p>The runtime may be the current JVM that is already running the launcher, or a managed
 * installation downloaded on demand for a Minecraft version.</p>
 *
 * @param majorVersion the Java feature version, such as {@code 17}, {@code 21}, or {@code 25}
 * @param home the runtime home directory
 * @param executable the Java executable that should be launched
 * @param managed whether this runtime was downloaded and managed by {@link JavaManager}
 * @param releaseName the vendor release name for managed runtimes, or {@code null} for the current JVM
 */
public record JavaInstallation(
        int majorVersion,
        Path home,
        Path executable,
        boolean managed,
        String releaseName
) {
}

