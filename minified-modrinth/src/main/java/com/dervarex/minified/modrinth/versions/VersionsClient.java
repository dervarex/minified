package com.dervarex.minified.modrinth.versions;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.VersionSearchOptions;
import com.dervarex.minified.modrinth.internal.AbstractModrinthClient;
import com.dervarex.minified.modrinth.internal.ModrinthJson;
import com.dervarex.minified.modrinth.loaders.Loader;
import com.dervarex.minified.utils.json.JsonObject;
import org.apiguardian.api.API;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Gets versions by ID, project or file hash. Get it from {@link Modrinth#versions()}
 */
public final class VersionsClient extends AbstractModrinthClient {
    @API(status = API.Status.INTERNAL)
    public VersionsClient(Modrinth modrinth) {
        super(modrinth);
    }

    /**
     * Gets a version by its ID
     *
     * @throws com.dervarex.minified.modrinth.exceptions.ModrinthNotFoundException if there is no such version
     */
    public Version get(String id) {
        return get("/version/" + encode(id), this::parse);
    }

    /**
     * Gets several versions by their IDs, in the same order.
     */
    public List<Version> getMany(String... ids) {
        List<String> wanted = ids == null ? List.of() : List.of(ids);
        if (wanted.isEmpty()) {
            return List.of();
        }
        Map<String, Version> byId = new LinkedHashMap<>();
        for (Version version : getList("/versions", query("ids", toJsonArray(wanted)), this::parse)) {
            byId.put(version.getId(), version);
        }
        return wanted.stream().distinct().map(byId::get).filter(Objects::nonNull).toList();
    }

    /**
     * Gets a version of a project by its version number, like {@code mc1.21.11-0.8.0}
     *
     * @throws com.dervarex.minified.modrinth.exceptions.ModrinthNotFoundException if the project has no such version
     */
    public Version getByNumber(String projectIdOrSlug, String versionNumber) {
        return get("/project/" + encode(projectIdOrSlug) + "/version/" + encode(versionNumber), this::parse);
    }

    /**
     * Gets all versions of a project, newest first
     */
    public List<Version> getByProject(String projectIdOrSlug) {
        return getByProject(projectIdOrSlug, VersionSearchOptions.builder().build());
    }

    /**
     * Gets the versions of a project that match the options, newest first
     */
    public List<Version> getByProject(String projectIdOrSlug, VersionSearchOptions options) {
        Objects.requireNonNull(options, "options");
        return getByProject(projectIdOrSlug, options.getGameVersions(),
                options.getLoaders().stream().map(Loader::getApiValue).toList(),
                options.getFeatured(), options.getLimit(), options.getOffset());
    }

    /**
     * Gets the newest version of a project that matches the options
     *
     * @return the version, {@code null} if none matches
     */
    public Version getLatest(String projectIdOrSlug, VersionSearchOptions options) {
        Objects.requireNonNull(options, "options");
        return getLatest(projectIdOrSlug, options.getGameVersions(),
                options.getLoaders().stream().map(Loader::getApiValue).toList());
    }

    /**
     * Gets the versions that files belong to, by the files' sha1 or sha512 hashes
     *
     * @return the versions in the order of the hashes, hashes of unknown files are left out
     */
    public List<Version> fromHashes(String... hashes) {
        return inHashOrder(postObject("/version_files", hashBody(hashes, null)), hashes);
    }

    /**
     * Gets the version a file belongs to, by the file's sha1 or sha512 hash
     *
     * @throws com.dervarex.minified.modrinth.exceptions.ModrinthNotFoundException if Modrinth doesn't know the file
     */
    public Version fromHash(String hash) {
        Objects.requireNonNull(hash, "hash");
        return get("/version_file/" + encode(hash), query("algorithm", algorithmOf(hash)), this::parse);
    }

    /**
     * Gets the newest version of each file's project, by the files sha1 or sha512 hashes
     *
     * @param options only the game versions and loaders are used, empty options for any
     * @return the versions in the order of the hashes, hashes of unknown files are left out
     */
    public List<Version> latestFromHashes(VersionSearchOptions options, String... hashes) {
        Objects.requireNonNull(options, "options");
        return inHashOrder(postObject("/version_files/update", hashBody(hashes, options)), hashes);
    }

    Version getLatest(String projectIdOrSlug, List<String> gameVersions, List<String> loaders) {
        List<Version> versions = getByProject(projectIdOrSlug, gameVersions, loaders, null, 1, 0);
        return versions.isEmpty() ? null : versions.getFirst();
    }

    private List<Version> getByProject(String projectIdOrSlug, List<String> gameVersions, List<String> loaders,
                                       Boolean featured, int limit, int offset) {
        Map<String, String> query = new LinkedHashMap<>();
        if (!gameVersions.isEmpty()) {
            query.put("game_versions", toJsonArray(gameVersions));
        }
        if (!loaders.isEmpty()) {
            query.put("loaders", toJsonArray(loaders));
        }
        if (featured != null) {
            query.put("featured", String.valueOf(featured));
        }
        if (limit > 0) {
            query.put("limit", String.valueOf(limit));
        }
        if (offset > 0) {
            query.put("offset", String.valueOf(offset));
        }
        return getList("/project/" + encode(projectIdOrSlug) + "/version", query, this::parse);
    }

    private Version parse(JsonObject json) {
        return new Version(modrinth, json);
    }

    private static String hashBody(String[] hashes, VersionSearchOptions options) {
        StringBuilder body = new StringBuilder("{\"hashes\":").append(toJsonArray(hashes))
                .append(",\"algorithm\":\"").append(hashes.length == 0 ? "sha1" : algorithmOf(hashes[0])).append('"');
        if (options != null && !options.getLoaders().isEmpty()) {
            body.append(",\"loaders\":").append(toJsonArray(options.getLoaders().stream().map(Loader::getApiValue).toList()));
        }
        if (options != null && !options.getGameVersions().isEmpty()) {
            body.append(",\"game_versions\":").append(toJsonArray(options.getGameVersions()));
        }
        return body.append('}').toString();
    }

    // sha512 is 128 hex characters, sha1 40
    private static String algorithmOf(String hash) {
        return hash.length() > 40 ? "sha512" : "sha1";
    }

    private List<Version> inHashOrder(JsonObject byHash, String... hashes) {
        List<Version> result = new ArrayList<>();
        for (String hash : hashes) {
            JsonObject version = ModrinthJson.object(byHash, hash);
            if (version != null) {
                result.add(ModrinthJson.parse(version, this::parse));
            }
        }
        return result;
    }
}
