package com.dervarex.minified.modrinth.projects;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.SearchRequest;
import com.dervarex.minified.modrinth.SearchResult;
import com.dervarex.minified.modrinth.internal.AbstractModrinthClient;
import com.dervarex.minified.modrinth.internal.ModrinthJson;
import com.dervarex.minified.utils.json.JsonObject;
import org.apiguardian.api.API;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Gets and searches projects, get it from {@link Modrinth#projects()}
 */
public final class ProjectsClient extends AbstractModrinthClient {
    /**
     * Creates the client, use the one from {@link Modrinth}
     *
     * @param modrinth the Modrinth it belongs to
     */
    @API(status = API.Status.INTERNAL)
    public ProjectsClient(Modrinth modrinth) {
        super(modrinth);
    }

    /**
     * Gets a project by its ID or slug
     *
     * @param idOrSlug the ID or slug, like {@code sodium}
     * @return the project
     * @throws com.dervarex.minified.modrinth.exceptions.ModrinthNotFoundException if there is no such project
     */
    public Project get(String idOrSlug) {
        return get("/project/" + encode(idOrSlug), Project::new);
    }

    /**
     * Gets several projects by their IDs or slugs
     *
     * @param idsOrSlugs the IDs or slugs
     * @return the projects, if you fetch one that doesn't exist we'll leave it out
     */
    public List<Project> getMany(String... idsOrSlugs) {
        List<String> wanted = idsOrSlugs == null ? List.of() : List.of(idsOrSlugs);
        if (wanted.isEmpty()) {
            return List.of();
        }
        List<Project> found = getList("/projects", query("ids", toJsonArray(wanted)), Project::new);
        Map<String, Project> byKey = new LinkedHashMap<>();
        for (Project project : found) {
            byKey.put(project.getId(), project);
            byKey.put(project.getSlug(), project);
        }
        List<Project> result = new ArrayList<>();
        for (String key : wanted) {
            Project project = byKey.get(key);
            if (project != null && !result.contains(project)) {
                result.add(project);
            }
        }
        return result;
    }

    /**
     * Searches projects
     *
     * @param request what to search for
     * @return one page of results
     */
    public SearchResult<Project> search(SearchRequest request) {
        Objects.requireNonNull(request, "request");
        Map<String, String> query = new LinkedHashMap<>();
        if (request.getQuery() != null && !request.getQuery().isBlank()) {
            query.put("query", request.getQuery());
        }
        query.put("index", request.getIndex().getApiValue());
        query.put("limit", String.valueOf(request.getLimit()));
        query.put("offset", String.valueOf(request.getOffset()));
        if (!request.getFacets().isEmpty()) {
            query.put("facets", request.getFacets().stream()
                    .map(AbstractModrinthClient::toJsonArray)
                    .collect(Collectors.joining(",", "[", "]")));
        }

        JsonObject response = getObject("/search", query);
        List<String> ids = ModrinthJson.parse(response, json ->
                ModrinthJson.list(json, "hits", hit -> ModrinthJson.string(hit.asObject(), "project_id")));
        List<Project> hits = getMany(ids.toArray(String[]::new));
        return ModrinthJson.parse(response, json -> new SearchResult<>(hits,
                (int) ModrinthJson.number(json, "offset"),
                (int) ModrinthJson.number(json, "limit"),
                (int) ModrinthJson.number(json, "total_hits")));
    }
}
