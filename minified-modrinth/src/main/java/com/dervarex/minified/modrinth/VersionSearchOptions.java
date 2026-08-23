package com.dervarex.minified.modrinth;

import com.dervarex.minified.modrinth.loaders.ModLoader;
import org.apiguardian.api.API;

import java.util.Arrays;
import java.util.Objects;

/**
 * Filters used when listing versions for a project.
 */
@API(status = API.Status.STABLE)
public final class VersionSearchOptions extends SearchOptions {
    public String[] gameVersions;
    public String[] loaders;
    public Boolean featured;

    public VersionSearchOptions() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final VersionSearchOptions options = new VersionSearchOptions();

        public Builder gameVersions(String... gameVersions) {
            options.gameVersions = normalize(gameVersions);
            return this;
        }

        public Builder loaders(String... loaders) {
            options.loaders = normalize(loaders);
            return this;
        }

        public Builder loaders(ModLoader... loaders) {
            ModLoader[] input = loaders == null ? new ModLoader[0] : loaders;
            String[] values = new String[input.length];
            int count = 0;
            for (ModLoader loader : input) {
                if (loader != null) {
                    values[count++] = loader.toString();
                }
            }
            options.loaders = Arrays.copyOf(values, count);
            return this;
        }

        public Builder featured(Boolean featured) {
            options.featured = featured;
            return this;
        }

        public Builder limit(int limit) {
            options.limit = Math.max(0, limit);
            return this;
        }

        public Builder offset(int offset) {
            options.offset = Math.max(0, offset);
            return this;
        }

        public VersionSearchOptions build() {
            return options;
        }

        private static String[] normalize(String... values) {
            if (values == null || values.length == 0) {
                return new String[0];
            }
            return Arrays.stream(values)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .distinct()
                    .toArray(String[]::new);
        }
    }
}

