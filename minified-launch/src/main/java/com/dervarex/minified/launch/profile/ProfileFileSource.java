package com.dervarex.minified.launch.profile;

import org.apiguardian.api.API;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Where a {@link ProfileFile} comes from, so a launcher can look for updates or show the project page
 *
 * @param platform  the platform the file was taken from, for example {@code modrinth}
 * @param projectId the project id on that platform, can be null
 * @param versionId the version id on that platform, can be null
 */
@API(status = API.Status.STABLE)
public record ProfileFileSource(String platform, @Nullable String projectId, @Nullable String versionId) {
    /** The platform name for Modrinth */
    public static final String MODRINTH = "modrinth";

    /**
     * Checks that the platform is set
     */
    public ProfileFileSource {
        Objects.requireNonNull(platform, "platform");
    }

    /**
     * {@return a Modrinth source}
     *
     * @param projectId the project id, can be null
     * @param versionId the version id, can be null
     */
    public static ProfileFileSource modrinth(String projectId, String versionId) {
        return new ProfileFileSource(MODRINTH, projectId, versionId);
    }
}
