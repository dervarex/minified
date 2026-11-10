package com.dervarex.minified.launch.profile;

import com.dervarex.minified.utils.sha.Hasher;
import org.apiguardian.api.API;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A file that belongs into the game directory of a profile, like a mod, resource pack or shader pack
 * Pretty close to a file entry of Modrinth's mrpack format actually :)
 *
 * @param path      where the file goes, relative to the game directory and with slashes, for example {@code mods/sodium.jar}
 * @param name      a display name, can be null
 * @param hashes    the hashes of the file by algorithm ({@code sha1}, {@code sha256}, {@code sha512}), used to check downloads
 * @param downloads URLs the file can be downloaded from, tried in order. emptry if the file has to be copied over by hand
 * @param size      the file size in bytes, -1 if unknown
 * @param client    if the client needs the file, {@link SideRequirement#UNSUPPORTED} files are not installed
 * @param server    if a server needs the file
 * @param enabled   disabled files are installed with a {@code .disabled}, the way most launchers toggle mods off
 * @param source    where the file comes from, can be null
 */
@API(status = API.Status.STABLE)
public record ProfileFile(
        String path,
        @Nullable
        String name,
        Map<String, String> hashes,
        List<String> downloads,
        long size,
        SideRequirement client,
        SideRequirement server,
        boolean enabled,
        @Nullable
        ProfileFileSource source
) {
    /** Added to the file name of disabled files */
    public static final String DISABLED_SUFFIX = ".disabled";

    /**
     * Checks the path, copies the hashes and downloads, null sides become {@link SideRequirement#REQUIRED}
     */
    public ProfileFile {
        validatePath(path);
        hashes = hashes == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(hashes));
        downloads = downloads == null ? List.of() : List.copyOf(downloads);
        client = client == null ? SideRequirement.REQUIRED : client;
        server = server == null ? SideRequirement.REQUIRED : server;
    }

    /**
     * {@return the last part of {@link #path()}, for example {@code sodium.jar}}
     */
    public String fileName() {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    /**
     * Creates an entry for a file that already is in the game directory, with its hashes and size but without download URLs
     *
     * @param gameDirectory the game directory of the profile
     * @param file          the file, has to be inside the game directory
     * @throws IllegalArgumentException if the file isn't inside the game directory
     * @throws UncheckedIOException if the file can't be read
     * @return the entry
     */
    public static ProfileFile fromFile(Path gameDirectory, Path file) {
        Path root = gameDirectory.toAbsolutePath().normalize();
        Path absolute = file.toAbsolutePath().normalize();
        if (!absolute.startsWith(root)) {
            throw new IllegalArgumentException(file + " is not inside " + gameDirectory);
        }

        String relative = root.relativize(absolute).toString().replace('\\', '/');
        boolean enabled = !relative.endsWith(DISABLED_SUFFIX);
        if (!enabled) {
            relative = relative.substring(0, relative.length() - DISABLED_SUFFIX.length());
        }

        try {
            Map<String, String> hashes = new LinkedHashMap<>();
            hashes.put("sha1", Hasher.sha1(absolute));
            hashes.put("sha512", Hasher.sha(absolute, 512));
            return builder(relative)
                    .hashes(hashes)
                    .size(Files.size(absolute))
                    .enabled(enabled)
                    .build();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + file, e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash " + file, e);
        }
    }

    private static void validatePath(String path) {
        Objects.requireNonNull(path, "path");
        if (path.isBlank() || path.startsWith("/") || path.contains("\\") || path.contains(":")) {
            throw new IllegalArgumentException("File path has to be relative with forward slashes: " + path);
        }
        for (String part : path.split("/", -1)) {
            if (part.isEmpty() || part.equals(".") || part.equals("..")) {
                throw new IllegalArgumentException("File path must not contain empty, '.' or '..' parts: " + path);
            }
        }
    }

    /**
     * {@return a builder for a file}
     *
     * @param path where the file goes, relative to the game directory, for example {@code mods/sodium.jar}
     */
    public static Builder builder(String path) {
        return new Builder(path);
    }

    /**
     * {@return a builder with the values of this file}
     */
    public Builder toBuilder() {
        return new Builder(path)
                .name(name)
                .hashes(hashes)
                .downloads(downloads)
                .size(size)
                .client(client)
                .server(server)
                .enabled(enabled)
                .source(source);
    }

    /**
     * Builds a {@link ProfileFile}
     */
    public static class Builder {
        private final String path;
        private String name;
        private final Map<String, String> hashes = new LinkedHashMap<>();
        private final List<String> downloads = new ArrayList<>();
        private long size = -1;
        private SideRequirement client = SideRequirement.REQUIRED;
        private SideRequirement server = SideRequirement.REQUIRED;
        private boolean enabled = true;
        private ProfileFileSource source;

        private Builder(String path) {
            this.path = path;
        }

        /**
         * Sets the display name
         *
         * @param name the name, can be null
         * @return this builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Adds a hash
         *
         * @param algorithm the algorithm, like {@code sha1}
         * @param hash the hash
         * @return this builder
         */
        public Builder hash(String algorithm, String hash) {
            hashes.put(algorithm, hash);
            return this;
        }

        /**
         * Adds hashes, null is ignored
         *
         * @param hashes the hashes by algorithm
         * @return this builder
         */
        public Builder hashes(Map<String, String> hashes) {
            if (hashes != null) {
                this.hashes.putAll(hashes);
            }
            return this;
        }

        /**
         * Adds a download URL
         *
         * @param url the URL
         * @return this builder
         */
        public Builder download(String url) {
            downloads.add(url);
            return this;
        }

        /**
         * Adds download URLs, null is ignored
         *
         * @param urls the URLs, tried in order
         * @return this builder
         */
        public Builder downloads(List<String> urls) {
            if (urls != null) {
                downloads.addAll(urls);
            }
            return this;
        }

        /**
         * Sets the file size, -1 by default
         *
         * @param size the size in bytes
         * @return this builder
         */
        public Builder size(long size) {
            this.size = size;
            return this;
        }

        /**
         * Sets if the client needs the file, required by default
         *
         * @param client the requirement
         * @return this builder
         */
        public Builder client(SideRequirement client) {
            this.client = client;
            return this;
        }

        /**
         * Sets if a server needs the file, required by default
         *
         * @param server the requirement
         * @return this builder
         */
        public Builder server(SideRequirement server) {
            this.server = server;
            return this;
        }

        /**
         * Turns the file on or off, on by default
         *
         * @param enabled whether the file is enabled
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets where the file comes from
         *
         * @param source the source, can be null
         * @return this builder
         */
        public Builder source(ProfileFileSource source) {
            this.source = source;
            return this;
        }

        /**
         * {@return the file}
         */
        public ProfileFile build() {
            return new ProfileFile(path, name, hashes, downloads, size, client, server, enabled, source);
        }
    }
}
