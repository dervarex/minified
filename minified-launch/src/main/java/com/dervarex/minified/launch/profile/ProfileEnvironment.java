package com.dervarex.minified.launch.profile;

import org.apiguardian.api.API;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Objects;

/**
 * The pc specific stuff of a profile. {@link ProfileFactory#export(Profile, Path)} leaves it out,
 * whoever loads the profile adds their own with {@link ProfileFactory#loadProfile(Path, ProfileEnvironment)}.
 * Values that are set here overwrite the ones in the profile file
 *
 * @param jarFile              the client jar
 * @param librariesDirectory   the shared libraries directory
 * @param assetsDirectory      the shared assets directory
 * @param nativesDirectory     can be null, see {@link com.dervarex.minified.launch.launch.LaunchConfiguration#resolveNativesDirectory()}
 * @param customJavaExecutable can be null to let the launcher pick a Java version
 * @param offlineUsername      can be null to keep the one from the profile, or the default
 */
@API(status = API.Status.STABLE)
public record ProfileEnvironment(
        Path jarFile,
        Path librariesDirectory,
        Path assetsDirectory,
        @Nullable
        Path nativesDirectory,
        @Nullable
        Path customJavaExecutable,
        @Nullable
        String offlineUsername
) {
    /**
     * Checks that the required paths are set
     */
    public ProfileEnvironment {
        Objects.requireNonNull(jarFile, "jarFile");
        Objects.requireNonNull(librariesDirectory, "librariesDirectory");
        Objects.requireNonNull(assetsDirectory, "assetsDirectory");
    }

    /**
     * {@return an environment with only the required paths}
     *
     * @param jarFile            the client jar
     * @param librariesDirectory the shared libraries directory
     * @param assetsDirectory    the shared assets directory
     */
    public static ProfileEnvironment of(Path jarFile, Path librariesDirectory, Path assetsDirectory) {
        return new ProfileEnvironment(jarFile, librariesDirectory, assetsDirectory, null, null, null);
    }

    /**
     * {@return a copy with another natives directory}
     *
     * @param nativesDirectory the natives directory, can be null
     */
    public ProfileEnvironment withNativesDirectory(Path nativesDirectory) {
        return new ProfileEnvironment(jarFile, librariesDirectory, assetsDirectory, nativesDirectory, customJavaExecutable, offlineUsername);
    }

    /**
     * {@return a copy with another java executable}
     *
     * @param customJavaExecutable the java executable, can be null
     */
    public ProfileEnvironment withCustomJavaExecutable(Path customJavaExecutable) {
        return new ProfileEnvironment(jarFile, librariesDirectory, assetsDirectory, nativesDirectory, customJavaExecutable, offlineUsername);
    }

    /**
     * {@return a copy with another offline username}
     *
     * @param offlineUsername the username, can be null
     */
    public ProfileEnvironment withOfflineUsername(String offlineUsername) {
        return new ProfileEnvironment(jarFile, librariesDirectory, assetsDirectory, nativesDirectory, customJavaExecutable, offlineUsername);
    }
}
