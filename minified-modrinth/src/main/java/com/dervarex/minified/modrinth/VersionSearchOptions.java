package com.dervarex.minified.modrinth;

import com.dervarex.minified.modrinth.loaders.Loader;
import org.apiguardian.api.API;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Filters for the versions of a project, for
 * {@link com.dervarex.minified.modrinth.versions.VersionsClient#getByProject(String, VersionSearchOptions)}.
 * Build them with {@link #builder()}:
 * <pre>{@code
 * VersionSearchOptions options = VersionSearchOptions.builder()
 *         .gameVersions("1.21.11")
 *         .loaders(ModLoader.FABRIC)
 *         .build();
 * }</pre>
 * Every filter you set has to match, inside one filter any of the values is enough.
 */
@API(status = API.Status.STABLE)
public final class VersionSearchOptions {
    private final List<String> gameVersions;
    private final List<Loader> loaders;
    private final Boolean featured;
    private final int limit;
    private final int offset;

    private VersionSearchOptions(Builder builder) {
        this.gameVersions = builder.gameVersions;
        this.loaders = builder.loaders;
        this.featured = builder.featured;
        this.limit = builder.limit;
        this.offset = builder.offset;
    }

    /**
     * Starts new version filters
     *
     * @return a builder without filters
     */
    public static Builder builder() {
        return new Builder();
    }

    /** {@return the Minecraft versions, empty for any} */
    public List<String> getGameVersions() {
        return gameVersions;
    }

    /** {@return the loaders, empty for any} */
    public List<Loader> getLoaders() {
        return loaders;
    }

    /** {@return only featured versions for {@code true}, only not featured ones for {@code false}, {@code null} for both} */
    public Boolean getFeatured() {
        return featured;
    }

    /** {@return how many versions to return at most, {@code 0} for all of them} */
    public int getLimit() {
        return limit;
    }

    /** {@return how many versions to skip, for paging} */
    public int getOffset() {
        return offset;
    }

    /**
     * Builds {@link VersionSearchOptions}. Without filters they match every version.
     */
    public static final class Builder {
        private List<String> gameVersions = List.of();
        private List<Loader> loaders = List.of();
        private Boolean featured;
        private int limit = 0;
        private int offset = 0;

        private Builder() {
        }

        /**
         * Only matches versions for one of these Minecraft versions
         *
         * @param gameVersions the Minecraft versions, like {@code 1.21.11}
         * @return this builder
         */
        public Builder gameVersions(String... gameVersions) {
            this.gameVersions = gameVersions == null ? List.of() : Arrays.stream(gameVersions)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .distinct()
                    .toList();
            return this;
        }

        /**
         * Only matches versions for one of these loader
         *
         * @param loaders the loaders
         * @return this builder
         */
        public Builder loaders(Loader... loaders) {
            this.loaders = loaders == null ? List.of() : Arrays.stream(loaders).filter(Objects::nonNull).distinct().toList();
            return this;
        }

        /**
         * Only matches featured or not featured versions
         *
         * @param featured {@code true} for featured ones, {@code false} for not featured ones, {@code null} for both
         * @return this builder
         */
        public Builder featured(Boolean featured) {
            this.featured = featured;
            return this;
        }

        /**
         * Sets how many versions to return at most
         *
         * @param limit the most versions to return, {@code 0} (the default) for all of them
         * @return this builder
         * @throws IllegalArgumentException if the limit is negative
         */
        public Builder limit(int limit) {
            if (limit < 0) {
                throw new IllegalArgumentException("limit must not be negative, was " + limit);
            }
            this.limit = limit;
            return this;
        }

        /**
         * Sets how many versions to skip, for paging
         *
         * @param offset how many versions to skip, 0 or more
         * @return this builder
         * @throws IllegalArgumentException if the offset is negative
         */
        public Builder offset(int offset) {
            if (offset < 0) {
                throw new IllegalArgumentException("offset must not be negative, was " + offset);
            }
            this.offset = offset;
            return this;
        }

        /**
         * Builds the filters
         *
         * @return the version filters
         */
        public VersionSearchOptions build() {
            return new VersionSearchOptions(this);
        }
    }
}
