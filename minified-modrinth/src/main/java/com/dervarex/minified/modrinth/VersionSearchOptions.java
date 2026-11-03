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

        /** Only versions for one of these Minecraft versions, like {@code 1.21.11} */
        public Builder gameVersions(String... gameVersions) {
            this.gameVersions = gameVersions == null ? List.of() : Arrays.stream(gameVersions)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .distinct()
                    .toList();
            return this;
        }

        /** Only versions for one of these loaders */
        public Builder loaders(Loader... loaders) {
            this.loaders = loaders == null ? List.of() : Arrays.stream(loaders).filter(Objects::nonNull).distinct().toList();
            return this;
        }

        /** Only featured versions for {@code true}, only not featured ones for {@code false}, both for {@code null} */
        public Builder featured(Boolean featured) {
            this.featured = featured;
            return this;
        }

        /** How many versions to return at most, {@code 0} (the default) for all of them */
        public Builder limit(int limit) {
            if (limit < 0) {
                throw new IllegalArgumentException("limit must not be negative, was " + limit);
            }
            this.limit = limit;
            return this;
        }

        /** How many versions to skip, for paging */
        public Builder offset(int offset) {
            if (offset < 0) {
                throw new IllegalArgumentException("offset must not be negative, was " + offset);
            }
            this.offset = offset;
            return this;
        }

        public VersionSearchOptions build() {
            return new VersionSearchOptions(this);
        }
    }
}
