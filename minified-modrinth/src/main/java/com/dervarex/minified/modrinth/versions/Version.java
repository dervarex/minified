package com.dervarex.minified.modrinth.versions;

import com.dervarex.minified.events.Event;
import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.events.DependencyConflictEvent;
import com.dervarex.minified.modrinth.events.DependencyDownloadedEvent;
import com.dervarex.minified.modrinth.events.DependencyResolvedEvent;
import com.dervarex.minified.modrinth.events.DependencySkippedEvent;
import com.dervarex.minified.modrinth.exceptions.ModrinthDependencyResolutionException;
import com.dervarex.minified.modrinth.internal.ModrinthJson;
import com.dervarex.minified.modrinth.loaders.Loader;
import com.dervarex.minified.modrinth.projects.Environment;
import com.dervarex.minified.utils.json.JsonObject;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A version of a project, with its files and dependencies. Get one from {@link VersionsClient}, it can download
 * itself and resolve its dependencies
 * <p>
 * Lists are empty when Modrinth has nothing for them, not (how you might expect it) null :)
 */
public final class Version {
    private final Modrinth modrinth;
    private final String id;
    private final String projectId;
    private final String authorId;
    private final String name;
    private final String versionNumber;
    private final String changelog;
    private final VersionType versionType;
    private final VersionStatus status;
    private final boolean featured;
    private final long downloads;
    private final Instant published;
    private final List<String> gameVersions;
    private final List<String> loaders;
    private final Environment environment;
    private final List<VersionDependency> dependencies;
    private final List<VersionFile> files;

    Version(Modrinth modrinth, JsonObject json) {
        this.modrinth = modrinth;
        id = ModrinthJson.string(json, "id");
        projectId = ModrinthJson.string(json, "project_id");
        authorId = ModrinthJson.string(json, "author_id");
        name = ModrinthJson.string(json, "name");
        versionNumber = ModrinthJson.string(json, "version_number");
        changelog = ModrinthJson.string(json, "changelog");
        versionType = VersionType.fromApiValue(ModrinthJson.string(json, "version_type"));
        status = VersionStatus.fromApiValue(ModrinthJson.string(json, "status"));
        featured = ModrinthJson.bool(json, "featured");
        downloads = ModrinthJson.number(json, "downloads");
        published = ModrinthJson.instant(json, "date_published");
        gameVersions = ModrinthJson.strings(json, "game_versions");
        loaders = ModrinthJson.strings(json, "loaders");
        environment = Environment.fromApiValue(ModrinthJson.string(json, "environment"));
        dependencies = ModrinthJson.objects(json, "dependencies", VersionDependency::new);
        files = ModrinthJson.objects(json, "files", file -> new VersionFile(modrinth, file));
    }

    /** {@return the ID of the version} */
    public String getId() {
        return id;
    }

    /** {@return the ID of the project this is a version of} */
    public String getProjectId() {
        return projectId;
    }

    /** {@return the ID of the user who published the version} */
    public String getAuthorId() {
        return authorId;
    }

    /** {@return the display name of the version} */
    public String getName() {
        return name;
    }

    /** {@return the version number, like {@code mc1.21.11-0.8.0}} */
    public String getVersionNumber() {
        return versionNumber;
    }

    /** {@return the changelog, in Markdown, {@code null} if there is none} */
    public String getChangelog() {
        return changelog;
    }

    /** {@return the release channel of the version} */
    public VersionType getVersionType() {
        return versionType;
    }

    /** {@return whether the version is listed, unlisted, archived and so on} */
    public VersionStatus getStatus() {
        return status;
    }

    /** {@return whether the version is featured on the project page} */
    public boolean isFeatured() {
        return featured;
    }

    /** {@return the number of downloads} */
    public long getDownloads() {
        return downloads;
    }

    /** {@return when the version was published} */
    public Instant getPublished() {
        return published;
    }

    /** {@return the Minecraft versions the version supports} */
    public List<String> getGameVersions() {
        return gameVersions;
    }

    /** {@return the API values of the loaders the version supports, like {@code fabric}} */
    public List<String> getLoaders() {
        return loaders;
    }

    /** {@return where the version runs} */
    public Environment getEnvironment() {
        return environment;
    }

    /** {@return the dependencies as Modrinth lists them, see {@link #resolveDependencies()} to get the actual versions} */
    public List<VersionDependency> getDependencies() {
        return dependencies;
    }

    /** {@return the files of the version} */
    public List<VersionFile> getFiles() {
        return files;
    }

    /** {@return the primary file, or the first one if none is marked primary, {@code null} if there are no files} */
    public VersionFile getPrimaryFile() {
        return files.stream().filter(VersionFile::isPrimary).findFirst()
                .orElse(files.isEmpty() ? null : files.getFirst());
    }

    /** {@return whether the version supports a loader} */
    public boolean hasLoader(Loader loader) {
        return loader != null && loaders.contains(loader.getApiValue());
    }

    /** {@return whether the version supports a Minecraft version, like {@code 1.21.11}} */
    public boolean supportsGameVersion(String gameVersion) {
        return gameVersions.contains(gameVersion);
    }

    /**
     * Downloads the primary file into a directory, see {@link VersionFile#download(Path)}
     *
     * @return the downloaded file
     * @throws ModrinthDependencyResolutionException if the version has no files
     */
    public Path download(Path directory) {
        VersionFile file = getPrimaryFile();
        if (file == null) {
            throw new ModrinthDependencyResolutionException("Version " + id + " has no downloadable files");
        }
        return file.download(directory);
    }

    /**
     * Resolves the required dependencies, and theirs, to the versions that should be installed with this one
     */
    public List<Version> resolveDependencies() {
        return resolveDependencies(true, false);
    }

    /**
     * Resolves the dependencies to the versions that should be installed with this one. A dependency on a project
     * resolves to its newest version for the game versions and loaders of this version.
     * <br>
     * @param recursive       also resolve the dependencies of the dependencies
     * @param includeOptional also resolve optional dependencies
     * @return every version to install, each once, without this one
     */
    public List<Version> resolveDependencies(boolean recursive, boolean includeOptional) {
        LinkedHashMap<String, Version> resolved = new LinkedHashMap<>();
        LinkedHashSet<String> visiting = new LinkedHashSet<>();
        resolveDependenciesInternal(this, resolved, visiting, recursive, includeOptional);
        postConflicts(resolved.values());
        return new ArrayList<>(resolved.values());
    }

    /**
     * Resolves the required dependencies like {@link #resolveDependencies()} and downloads each of them into a
     * directory, posting a {@link DependencyDownloadedEvent} after each
     *
     * @return the downloaded files
     */
    public List<Path> downloadDependencies(Path directory) {
        List<Version> dependencies = resolveDependencies(true, false);
        List<Path> paths = new ArrayList<>();
        for (Version dependency : dependencies) {
            Path file = dependency.download(directory);
            paths.add(file);
            post(new DependencyDownloadedEvent(this, dependency, file, paths.size(), dependencies.size()));
        }
        return paths;
    }

    @Override
    public String toString() {
        return "Version{" + versionNumber + ", " + id + "}";
    }

    private void postConflicts(Collection<Version> resolved) {
        List<Version> installed = new ArrayList<>();
        installed.add(this);
        installed.addAll(resolved);
        for (Version declaredBy : installed) {
            for (VersionDependency dependency : declaredBy.dependencies) {
                if (dependency.getDependencyType() != DependencyType.INCOMPATIBLE) {
                    continue;
                }
                for (Version candidate : installed) {
                    if (candidate != declaredBy && matches(dependency, candidate)) {
                        post(new DependencyConflictEvent(declaredBy, dependency, candidate));
                    }
                }
            }
        }
    }

    private static boolean matches(VersionDependency dependency, Version candidate) {
        if (dependency.getVersionId() != null && !dependency.getVersionId().isBlank()) {
            return dependency.getVersionId().equals(candidate.id);
        }
        return dependency.getProjectId() != null && !dependency.getProjectId().isBlank()
                && dependency.getProjectId().equals(candidate.projectId);
    }

    private void resolveDependenciesInternal(Version current,
                                             Map<String, Version> resolved,
                                             Set<String> visiting,
                                             boolean recursive,
                                             boolean includeOptional) {
        visiting.add(current.id);
        for (VersionDependency dependency : current.dependencies) {
            // incompatible ones are mods that must NOT be there, embedded ones already are inside the jar
            DependencySkippedEvent.Reason skipReason = skipReason(dependency.getDependencyType(), includeOptional);
            if (skipReason != null) {
                post(new DependencySkippedEvent(current, dependency, skipReason));
                continue;
            }
            Version resolvedDependency = resolveDependency(dependency);
            if (resolvedDependency == null || resolvedDependency.id == null) {
                post(new DependencySkippedEvent(current, dependency, DependencySkippedEvent.Reason.UNRESOLVABLE));
                continue;
            }
            // already on the way down here (a -> b -> a), that includes the version we started with
            if (visiting.contains(resolvedDependency.id)) {
                post(new DependencySkippedEvent(current, dependency, DependencySkippedEvent.Reason.CIRCULAR));
                continue;
            }
            if (resolved.putIfAbsent(resolvedDependency.id, resolvedDependency) == null) {
                post(new DependencyResolvedEvent(current, dependency, resolvedDependency));
                if (recursive && visiting.add(resolvedDependency.id)) {
                    resolveDependenciesInternal(resolvedDependency, resolved, visiting, true, includeOptional);
                }
            }
        }
        visiting.remove(current.id);
    }

    // null means the dependency is wanted
    private static DependencySkippedEvent.Reason skipReason(DependencyType type, boolean includeOptional) {
        if (type == null) {
            return DependencySkippedEvent.Reason.UNKNOWN_TYPE;
        }
        return switch (type) {
            case REQUIRED -> null;
            case OPTIONAL -> includeOptional ? null : DependencySkippedEvent.Reason.OPTIONAL;
            case INCOMPATIBLE -> DependencySkippedEvent.Reason.INCOMPATIBLE;
            case EMBEDDED -> DependencySkippedEvent.Reason.EMBEDDED;
            case UNKNOWN -> DependencySkippedEvent.Reason.UNKNOWN_TYPE;
        };
    }

    private Version resolveDependency(VersionDependency dependency) {
        if (dependency.getVersionId() != null && !dependency.getVersionId().isBlank()) {
            return modrinth.versions().get(dependency.getVersionId());
        }
        if (dependency.getProjectId() != null && !dependency.getProjectId().isBlank()) {
            return modrinth.versions().getLatest(dependency.getProjectId(), gameVersions, loaders);
        }
        return null;
    }

    private void post(Event event) {
        modrinth.getEventBus().post(event);
    }
}
