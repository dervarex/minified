package com.dervarex.minified.modrinth;

import com.dervarex.minified.modrinth.loaders.ModLoader;
import com.dervarex.minified.modrinth.projects.Environment;
import com.dervarex.minified.modrinth.projects.ProjectType;
import com.dervarex.minified.modrinth.projects.SideSupport;
import org.apiguardian.api.API;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Builder-based search request for the public project search endpoint.
 */
@API(status = API.Status.STABLE)
public final class SearchRequest extends SearchOptions {
    public String query;
    public String index = "relevance";
    public ProjectType projectType;
    public SideSupport clientSide;
    public SideSupport serverSide;
    public String[] environment;
    public String[] categories;
    public String[] gameVersions;
    public String[] loaders;
    public List<List<String>> facets = new ArrayList<>();

    private SearchRequest() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getQuery() {
        return query;
    }

    public String getIndex() {
        return index;
    }

    public static final class Builder {
        private final SearchRequest request = new SearchRequest();

        public Builder query(String query) {
            request.query = query;
            return this;
        }

        public Builder index(String index) {
            request.index = Objects.requireNonNull(index, "index");
            return this;
        }

        public Builder index(SearchIndex index) {
            request.index = index == null ? null : index.apiValue;
            return this;
        }

        public Builder projectType(ProjectType projectType) {
            request.projectType = projectType;
            if (projectType != null) {
                facet("project_type", projectType.getApiValue());
            }
            return this;
        }

        public Builder clientSide(SideSupport support) {
            request.clientSide = support;
            if (support != null) {
                facet("client_side", support.getApiValue());
            }
            return this;
        }

        public Builder serverSide(SideSupport support) {
            request.serverSide = support;
            if (support != null) {
                facet("server_side", support.getApiValue());
            }
            return this;
        }

        public Builder environment(Environment... values) {
            Environment[] input = values == null ? new Environment[0] : values;
            String[] apiValues = new String[input.length];
            int count = 0;
            for (Environment value : input) {
                if (value != null) {
                    apiValues[count++] = value.toString();
                }
            }
            request.environment = Arrays.copyOf(apiValues, count);
            facet("environment", request.environment);
            return this;
        }

        public Builder categories(String... categories) {
            request.categories = normalize(categories);
            facet("categories", request.categories);
            return this;
        }

        public Builder gameVersions(String... versions) {
            request.gameVersions = normalize(versions);
            facet("versions", request.gameVersions);
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
            request.loaders = Arrays.copyOf(values, count);
            facet("loaders", request.loaders);
            return this;
        }

        public Builder loaders(String... loaders) {
            request.loaders = normalize(loaders);
            facet("loaders", request.loaders);
            return this;
        }

        public Builder limit(int limit) {
            request.limit = Math.max(0, limit);
            return this;
        }

        public Builder offset(int offset) {
            request.offset = Math.max(0, offset);
            return this;
        }

        public Builder facet(String key, String... values) {
            String[] normalized = normalize(values);
            if (normalized.length == 0) {
                return this;
            }
            List<String> group = new ArrayList<>(normalized.length);
            for (String value : normalized) {
                group.add(key + ":" + value);
            }
            request.facets.add(group);
            return this;
        }

        public Builder facets(List<List<String>> facets) {
            request.facets = new ArrayList<>();
            if (facets != null) {
                for (List<String> facet : facets) {
                    if (facet != null && !facet.isEmpty()) {
                        request.facets.add(new ArrayList<>(facet));
                    }
                }
            }
            return this;
        }

        public SearchRequest build() {
            return request;
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
    }
}

