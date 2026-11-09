package com.dervarex.minified.launch.profile;

import com.dervarex.minified.launch.launch.LaunchConfiguration;
import org.apiguardian.api.API;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * A Profile saved by {@link ProfileFactory}
 *
 * @param metadata      name, author and so on
 * @param configuration how the game is launched
 * @param files         files that {@link ProfileInstaller} puts into the game directory
 */
@API(status = API.Status.EXPERIMENTAL)
public record Profile(ProfileMetadata metadata, LaunchConfiguration configuration, List<ProfileFile> files) {
    /**
     * Copies the file list, null becomes an empty list
     */
    public Profile {
        Objects.requireNonNull(metadata, "metadata");
        Objects.requireNonNull(configuration, "configuration");
        files = files == null ? List.of() : List.copyOf(files);
    }

    /**
     * Creates a profile without files
     *
     * @param metadata      name, author and so on
     * @param configuration how the game is launched
     */
    public Profile(ProfileMetadata metadata, LaunchConfiguration configuration) {
        this(metadata, configuration, List.of());
    }

    /**
     * {@return the directory the game runs in, see {@link LaunchConfiguration#resolveGameDirectory()}}
     */
    public Path gameDirectory() {
        return configuration.resolveGameDirectory();
    }

    /**
     * {@return a copy with other metadata}
     *
     * @param metadata the new metadata
     */
    public Profile withMetadata(ProfileMetadata metadata) {
        return new Profile(metadata, configuration, files);
    }

    /**
     * {@return a copy with another launch configuration}
     *
     * @param configuration the new launch configuration
     */
    public Profile withConfiguration(LaunchConfiguration configuration) {
        return new Profile(metadata, configuration, files);
    }

    /**
     * {@return a copy with other files}
     *
     * @param files the new files
     */
    public Profile withFiles(List<ProfileFile> files) {
        return new Profile(metadata, configuration, files);
    }
}
