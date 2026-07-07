package com.dervarex.minified.modrinth.projects;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.SearchRequest;
import com.dervarex.minified.modrinth.SearchResult;
import com.dervarex.minified.modrinth.VersionSearchOptions;
import com.dervarex.minified.modrinth.internal.AbstractModrinthClient;
import com.dervarex.minified.modrinth.internal.ModrinthJson;
import com.dervarex.minified.modrinth.versions.Version;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ProjectsClient extends AbstractModrinthClient {
    public ProjectsClient(Modrinth modrinth) {
        super(modrinth);
    }

    public Project get(String idOrSlug) {
        Project project = parseProject(getObject("/project/" + encode(idOrSlug)));
        attach(project);
        return project;
    }

    public List<Project> getMany(String... ids) {
        Map<String, String> query = query("ids", toJsonArray(ids));
        return getList("/projects", query, this::parseAndAttach);
    }

    public SearchResult<Project> search(SearchRequest request) {
        SearchRequest value = request == null ? SearchRequest.builder().build() : request;
        Map<String, String> query = new LinkedHashMap<>();
        if (value.query != null && !value.query.isBlank()) {
            query.put("query", value.query);
        }
        if (value.index != null && !value.index.isBlank()) {
            query.put("index", value.index);
        }
        query.put("limit", String.valueOf(Math.max(0, value.limit)));
        query.put("offset", String.valueOf(Math.max(0, value.offset)));
        if (value.facets != null && !value.facets.isEmpty()) {
            query.put("facets", facetsToJson(value.facets));
        }

        JsonObject response = getObject("/search", query);
        JsonArray hits = ModrinthJson.array(response, "hits");
        List<Project> projects = new ArrayList<>();
        if (hits != null) {
            for (JsonValue valueHit : hits) {
                if (valueHit == null || valueHit.isNull()) {
                    continue;
                }
                Project project = parseProject(valueHit.asObject());
                attach(project);
                projects.add(project);
            }
        }
        Project[] array = projects.toArray(Project[]::new);
        Long totalHits = ModrinthJson.longValue(response, "total_hits");
        Integer offset = ModrinthJson.integer(response, "offset");
        Integer limit = ModrinthJson.integer(response, "limit");
        return new SearchResult<>(array, offset == null ? 0 : offset, limit == null ? 0 : limit, totalHits == null ? 0 : totalHits.intValue());
    }

    public List<Version> getVersions(String idOrSlug, VersionSearchOptions options) {
        return modrinth.versions().getByProject(idOrSlug, options);
    }

    public Version getLatestVersion(String idOrSlug, String gameVersion, com.dervarex.minified.modrinth.loaders.ModLoader... loaders) {
        VersionSearchOptions.Builder builder = VersionSearchOptions.builder();
        if (gameVersion != null) {
            builder.gameVersions(gameVersion);
        }
        if (loaders != null && loaders.length > 0) {
            builder.loaders(loaders);
        }
        return modrinth.versions().resolveLatest(idOrSlug, builder.build());
    }

    public Version getLatestVersion(String idOrSlug, String[] gameVersions, String... loaders) {
        VersionSearchOptions.Builder builder = VersionSearchOptions.builder();
        builder.gameVersions(gameVersions);
        builder.loaders(loaders);
        return modrinth.versions().resolveLatest(idOrSlug, builder.build());
    }

    private Project parseAndAttach(JsonObject object) {
        Project project = parseProject(object);
        attach(project);
        return project;
    }

    private Project parseProject(JsonObject object) {
        if (object == null) {
            return null;
        }
        Project project = new Project();
        project.id = ModrinthJson.string(object, "id");
        project.projectId = ModrinthJson.string(object, "project_id");
        project.slug = ModrinthJson.string(object, "slug");
        project.title = ModrinthJson.string(object, "title");
        project.description = ModrinthJson.string(object, "description");
        project.body = ModrinthJson.string(object, "body");
        project.bodyUrl = ModrinthJson.string(object, "body_url");
        project.projectType = ProjectType.fromApiValue(ModrinthJson.string(object, "project_type"));
        project.author = ModrinthJson.string(object, "author");
        project.authorId = ModrinthJson.string(object, "author_id");
        project.organization = ModrinthJson.string(object, "organization");
        project.organizationId = ModrinthJson.string(object, "organization_id");
        project.team = ModrinthJson.string(object, "team");
        project.clientSide = SideSupport.fromApiValue(ModrinthJson.string(object, "client_side"));
        project.serverSide = SideSupport.fromApiValue(ModrinthJson.string(object, "server_side"));
        project.categories = ModrinthJson.strings(object, "categories");
        project.displayCategories = ModrinthJson.strings(object, "display_categories");
        project.additionalCategories = ModrinthJson.strings(object, "additional_categories");
        project.versions = ModrinthJson.strings(object, "versions");
        project.gameVersions = ModrinthJson.strings(object, "game_versions");
        project.loaders = ModrinthJson.strings(object, "loaders");
        Long follows = ModrinthJson.longValue(object, "follows");
        project.follows = follows == null ? 0L : follows;
        Long followers = ModrinthJson.longValue(object, "followers");
        project.followers = followers == null ? 0L : followers;
        Long downloads = ModrinthJson.longValue(object, "downloads");
        project.downloads = downloads == null ? 0L : downloads;
        project.iconUrl = ModrinthJson.string(object, "icon_url");
        project.color = ModrinthJson.integer(object, "color");
        project.threadId = ModrinthJson.string(object, "thread_id");
        project.issuesUrl = ModrinthJson.string(object, "issues_url");
        project.sourceUrl = ModrinthJson.string(object, "source_url");
        project.wikiUrl = ModrinthJson.string(object, "wiki_url");
        project.discordUrl = ModrinthJson.string(object, "discord_url");
        JsonArray donationUrls = ModrinthJson.array(object, "donation_urls");
        if (donationUrls != null) {
            project.donationUrls = donationUrls.values().stream()
                    .filter(value -> value != null && !value.isNull())
                    .map(JsonValue::asObject)
                    .map(this::parseDonationUrl)
                    .toArray(DonationUrl[]::new);
        }
        JsonObject license = ModrinthJson.object(object, "license");
        if (license != null) {
            project.setLicense(parseLicense(license));
        }
        JsonArray gallery = ModrinthJson.array(object, "gallery");
        if (gallery != null) {
            project.gallery = gallery.values().stream()
                    .filter(value -> value != null && !value.isNull())
                    .map(JsonValue::asObject)
                    .map(this::parseGalleryImage)
                    .toArray(GalleryImage[]::new);
        }
        project.published = ModrinthJson.instant(object, "published");
        project.updated = ModrinthJson.instant(object, "updated");
        project.monetizationStatus = ModrinthJson.string(object, "monetization_status");
        return project;
    }

    private DonationUrl parseDonationUrl(JsonObject object) {
        if (object == null) {
            return null;
        }
        DonationUrl donationUrl = new DonationUrl();
        donationUrl.id = ModrinthJson.string(object, "id");
        donationUrl.platform = ModrinthJson.string(object, "platform");
        donationUrl.url = ModrinthJson.string(object, "url");
        return donationUrl;
    }

    private GalleryImage parseGalleryImage(JsonObject object) {
        if (object == null) {
            return null;
        }
        GalleryImage image = new GalleryImage();
        image.url = ModrinthJson.string(object, "url");
        Boolean featured = ModrinthJson.bool(object, "featured");
        image.featured = featured != null && featured;
        image.title = ModrinthJson.string(object, "title");
        image.description = ModrinthJson.string(object, "description");
        image.created = ModrinthJson.instant(object, "created");
        Integer ordering = ModrinthJson.integer(object, "ordering");
        image.ordering = ordering == null ? 0 : ordering;
        return image;
    }

    private com.dervarex.minified.modrinth.projects.ProjectLicense parseLicense(JsonObject object) {
        if (object == null) {
            return null;
        }
        return new com.dervarex.minified.modrinth.projects.ProjectLicense(
                ModrinthJson.string(object, "id"),
                ModrinthJson.string(object, "name"),
                ModrinthJson.string(object, "url")
        );
    }

    private void attach(Project project) {
        if (project != null) {
            project.attach(modrinth);
        }
    }

    private static String facetsToJson(List<List<String>> facets) {
        StringBuilder builder = new StringBuilder("[");
        boolean firstGroup = true;
        for (List<String> group : facets) {
            if (group == null || group.isEmpty()) {
                continue;
            }
            if (!firstGroup) {
                builder.append(',');
            }
            firstGroup = false;
            builder.append('[');
            boolean firstValue = true;
            for (String value : group) {
                if (value == null || value.isBlank()) {
                    continue;
                }
                if (!firstValue) {
                    builder.append(',');
                }
                firstValue = false;
                builder.append('"').append(value.replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
            }
            builder.append(']');
        }
        builder.append(']');
        return builder.toString();
    }

    private static String toJsonArray(String... values) {
        if (values == null || values.length == 0) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('"').append(value.replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
        }
        builder.append(']');
        return builder.toString();
    }
}

