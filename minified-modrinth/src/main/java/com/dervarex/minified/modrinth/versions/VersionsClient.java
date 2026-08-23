package com.dervarex.minified.modrinth.versions;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.VersionSearchOptions;
import com.dervarex.minified.modrinth.internal.AbstractModrinthClient;
import com.dervarex.minified.modrinth.internal.ModrinthJson;
import com.dervarex.minified.modrinth.projects.Environment;

import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class VersionsClient extends AbstractModrinthClient {
    public VersionsClient(Modrinth modrinth) {
        super(modrinth);
    }

    public Version get(String id) {
        Version version = parseVersion(getObject("/version/" + encode(id)));
        attach(version);
        return version;
    }

    public List<Version> getMany(String... ids) {
        Map<String, String> query = query("ids", toJsonArray(ids));
        return getList("/versions", query, this::parseAndAttach);
    }

    public List<Version> getByProject(String projectIdOrSlug) {
        return getByProject(projectIdOrSlug, new VersionSearchOptions());
    }

    public List<Version> getByProject(String projectIdOrSlug, VersionSearchOptions options) {
        Map<String, String> query = new LinkedHashMap<>();
        if (options != null) {
            if (options.gameVersions != null && options.gameVersions.length > 0) {
                query.put("game_versions", toJsonArray(options.gameVersions));
            }
            if (options.loaders != null && options.loaders.length > 0) {
                query.put("loaders", toJsonArray(options.loaders));
            }
            if (options.featured != null) {
                query.put("featured", String.valueOf(options.featured));
            }
            query.put("limit", String.valueOf(Math.max(0, options.limit)));
            query.put("offset", String.valueOf(Math.max(0, options.offset)));
        }
        return getList("/project/" + encode(projectIdOrSlug) + "/version", query, this::parseAndAttach);
    }

    public Version getVersionByNumber(String projectIdOrSlug, String versionNumber) {
        List<Version> versions = getByProject(projectIdOrSlug);
        for (Version version : versions) {
            if (versionNumber != null && versionNumber.equalsIgnoreCase(version.versionNumber)) {
                return version;
            }
        }
        return null;
    }

    public Version resolveLatest(String projectIdOrSlug, VersionSearchOptions options) {
        List<Version> versions = getByProject(projectIdOrSlug, options);
        return versions.isEmpty() ? null : versions.getFirst();
    }

    public List<Version> fromHashes(String... hashes) {
        Map<String, String> query = query("hashes", toJsonArray(hashes));
        return getList("/versions", query, this::parseAndAttach);
    }

    public Version fromHash(String hash) {
        if (hash == null || hash.isBlank()) {
            return null;
        }
        return parseAndAttach(getObject("/version/" + encode(hash)));
    }

    public List<Version> latestFromHashes(String... hashes) {
        Map<String, String> query = query("hashes", toJsonArray(hashes));
        JsonArray array = getArray("/version_files/" + encode(String.join(",", hashes)), query);
        return array.values().stream()
                .map(JsonValue::asObject)
                .map(this::parseAndAttach)
                .toList();
    }

    public List<VersionDependency> dependencies(String versionId) {
        JsonArray array = getArray("/version/" + encode(versionId) + "/dependencies");
        return array.values().stream()
                .map(JsonValue::asObject)
                .map(this::parseDependency)
                .toList();
    }

    private Version parseAndAttach(JsonObject object) {
        Version version = parseVersion(object);
        attach(version);
        return version;
    }

    private Version parseVersion(JsonObject object) {
        if (object == null) {
            return null;
        }
        Version version = new Version();
        version.id = ModrinthJson.string(object, "id");
        version.projectId = ModrinthJson.string(object, "project_id");
        version.authorId = ModrinthJson.string(object, "author_id");
        version.name = ModrinthJson.string(object, "name");
        version.versionNumber = ModrinthJson.string(object, "version_number");
        version.changelog = ModrinthJson.string(object, "changelog");
        version.changelogUrl = ModrinthJson.string(object, "changelog_url");
        version.versionType = VersionType.fromApiValue(ModrinthJson.string(object, "version_type"));
        version.status = VersionStatus.fromApiValue(ModrinthJson.string(object, "status"));
        version.requestedStatus = ModrinthJson.string(object, "requested_status");
        Boolean featured = ModrinthJson.bool(object, "featured");
        version.featured = featured != null && featured;
        Long downloads = ModrinthJson.longValue(object, "downloads");
        version.downloads = downloads == null ? 0L : downloads;
        version.published = ModrinthJson.instant(object, "date_published");
        version.gameVersions = ModrinthJson.strings(object, "game_versions");
        version.loaders = ModrinthJson.strings(object, "loaders");
        version.environment = Environment.fromApiValue(ModrinthJson.string(object, "environment"));
        JsonArray dependencies = ModrinthJson.array(object, "dependencies");
        if (dependencies != null) {
            version.dependencies = dependencies.values().stream()
                    .filter(value -> value != null && !value.isNull())
                    .map(JsonValue::asObject)
                    .map(this::parseDependency)
                    .toArray(VersionDependency[]::new);
        }
        JsonArray files = ModrinthJson.array(object, "files");
        if (files != null) {
            version.files = files.values().stream()
                    .filter(value -> value != null && !value.isNull())
                    .map(JsonValue::asObject)
                    .map(this::parseFile)
                    .toArray(VersionFile[]::new);
        }
        return version;
    }

    private VersionDependency parseDependency(JsonObject object) {
        if (object == null) {
            return null;
        }
        VersionDependency dependency = new VersionDependency();
        dependency.versionId = ModrinthJson.string(object, "version_id");
        dependency.projectId = ModrinthJson.string(object, "project_id");
        dependency.fileName = ModrinthJson.string(object, "file_name");
        dependency.dependencyType = DependencyType.fromApiValue(ModrinthJson.string(object, "dependency_type"));
        return dependency;
    }

    private VersionFile parseFile(JsonObject object) {
        if (object == null) {
            return null;
        }
        VersionFile file = new VersionFile();
        file.url = ModrinthJson.string(object, "url");
        file.filename = ModrinthJson.string(object, "filename");
        Boolean primary = ModrinthJson.bool(object, "primary");
        file.primary = primary != null && primary;
        Long size = ModrinthJson.longValue(object, "size");
        file.size = size == null ? 0L : size;
        file.fileType = ModrinthJson.string(object, "file_type");
        JsonObject hashes = ModrinthJson.object(object, "hashes");
        if (hashes != null) {
            Map<String, String> map = new LinkedHashMap<>();
            for (String key : hashes.keys()) {
                map.put(key, ModrinthJson.string(hashes, key));
            }
            file.hashes = map;
        }
        return file;
    }

    private void attach(Version version) {
        if (version != null) {
            version.attach(modrinth);
        }
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

