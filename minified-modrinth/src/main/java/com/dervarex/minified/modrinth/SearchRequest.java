package com.dervarex.minified.modrinth;

import com.dervarex.minified.modrinth.loaders.Loader;
import com.dervarex.minified.modrinth.projects.Environment;
import com.dervarex.minified.modrinth.projects.ProjectType;
import org.apiguardian.api.API;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * A project search, for {@link com.dervarex.minified.modrinth.projects.ProjectsClient#search(SearchRequest)}.
 * Build one using {@link #builder()}:
 * <pre>{@code
 * SearchRequest request = SearchRequest.builder()
 *         .query("optimization")
 *         .loaders(ModLoader.FABRIC)
 *         .gameVersions("1.21.11")
 *         .limit(20)
 *         .build();
 * }</pre>
 * Inside one filter, any of the values is enough, so
 * {@code .loaders(ModLoader.FABRIC, ModLoader.QUILT)} will find projects for Fabric **or** Quilt.
 */
@API(status = API.Status.STABLE)
public final class SearchRequest {
    /** The most results Modrinth returns for one search */
    public static final int MAX_LIMIT = 100;

    private final String query;
    private final SearchIndex index;
    private final int limit;
    private final int offset;
    private final List<List<String>> facets;

    private SearchRequest(Builder builder) {
        this.query = builder.query;
        this.index = builder.index;
        this.limit = builder.limit;
        this.offset = builder.offset;
        this.facets = builder.facets.stream().map(List::copyOf).toList();
    }

    /**
     * Starts a new search
     *
     * @return a builder without filters
     */
    public static Builder builder() {
        return new Builder();
    }

    /** {@return the search text, {@code null} to search without one} */
    public String getQuery() {
        return query;
    }

    /** {@return how the results are sorted} */
    public SearchIndex getIndex() {
        return index;
    }

    /** {@return how many results to return at most} */
    public int getLimit() {
        return limit;
    }

    /** {@return how many results to skip, for paging} */
    public int getOffset() {
        return offset;
    }

    /**
     * {@return the filters as Modrinth "facets", like {@code [["categories:optimization"], ["versions:1.21.11"]]}}
     */
    public List<List<String>> getFacets() {
        return facets;
    }

    /**
     * Builds a {@link SearchRequest}
     */
    public static final class Builder {
        private String query;
        private SearchIndex index = SearchIndex.RELEVANCE;
        private int limit = 10;
        private int offset = 0;
        private final List<List<String>> facets = new ArrayList<>();

        private Builder() {
        }

        /**
         * Sets the text to search for
         *
         * @param query the search text, {@code null} to search without one
         * @return this builder
         */
        public Builder query(String query) {
            this.query = query;
            return this;
        }

        /**
         * Sets how the results are sorted, {@link SearchIndex#RELEVANCE} by default
         *
         * @param index the sort order
         * @return this builder
         */
        public Builder index(SearchIndex index) {
            this.index = Objects.requireNonNull(index, "index");
            return this;
        }

        /**
         * Only finds projects of one of these types.
         *
         * @param projectTypes the project types
         * @return this builder
         */
        public Builder projectTypes(ProjectType... projectTypes) {
            return facet("project_type", values(projectTypes, ProjectType::getApiValue));
        }

        /**
         * Only finds projects that run in one of these environments
         *
         * @param environments the environments
         * @return this builder
         */
        public Builder environments(Environment... environments) {
            return facet("environment", values(environments, Environment::getApiValue));
        }

        /**
         * Only finds projects in one of these categories
         *
         * @param categories the category names, like {@code optimization}
         * @return this builder
         */
        public Builder categories(String... categories) {
            return facet("categories", categories);
        }

        /**
         * Only finds projects for one of these Minecraft versions.
         *
         * @param gameVersions the Minecraft versions, like {@code 1.21.11}
         * @return this builder
         */
        public Builder gameVersions(String... gameVersions) {
            return facet("versions", gameVersions);
        }

        /**
         * Only finds projects for one of these loaders
         *
         * @param loaders the loaders
         * @return this builder
         */
        public Builder loaders(Loader... loaders) {
            return facet("categories", values(loaders, Loader::getApiValue));
        }

        /**
         * Sets how many results to return, default is 10
         *
         * @param limit between 0 and {@link #MAX_LIMIT}
         * @return this builder
         * @throws IllegalArgumentException if the limit is out of range
         */
        public Builder limit(int limit) {
            if (limit < 0 || limit > MAX_LIMIT) {
                throw new IllegalArgumentException("limit must be between 0 and " + MAX_LIMIT + ", was " + limit);
            }
            this.limit = limit;
            return this;
        }

        /**
         * Sets how many results to skip, for paging.
         *
         * @param offset how many results to skip, 0 or more
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
         * Adds any other Modrinth "facet", for filters this builder has no method for, like {@code facet("license",
         * "mit")}. Any of the values has to match
         *
         * @param key    the facet name
         * @param values the values, blank and duplicate ones are left out
         * @return this builder
         */
        public Builder facet(String key, String... values) {
            Objects.requireNonNull(key, "key");
            List<String> group = new ArrayList<>();
            if (values != null) {
                Arrays.stream(values)
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(value -> !value.isEmpty())
                        .distinct()
                        .forEach(value -> group.add(key + ":" + value));
            }
            if (!group.isEmpty()) {
                facets.add(group);
            }
            return this;
        }

        /**
         * Builds the search
         *
         * @return the search request
         */
        public SearchRequest build() {
            return new SearchRequest(this);
        }

        private static <T> String[] values(T[] values, Function<T, String> apiValue) {
            return values == null ? new String[0]
                    : Arrays.stream(values).filter(Objects::nonNull).map(apiValue).toArray(String[]::new);
        }
    }

    /**
     * How search results are sorted
     */
    public enum SearchIndex {
        RELEVANCE("relevance"),
        DOWNLOADS("downloads"),
        FOLLOWS("follows"),
        NEWEST("newest"),
        UPDATED("updated");

        private final String apiValue;

        SearchIndex(String apiValue) {
            this.apiValue = apiValue;
        }

        /** {@return the value the Modrinth API uses} */
        public String getApiValue() {
            return apiValue;
        }
    }
}
