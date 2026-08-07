package com.dervarex.minified.modrinth.versions;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.exceptions.ModrinthDependencyResolutionException;
import com.dervarex.minified.modrinth.exceptions.ModrinthStateException;
import com.dervarex.minified.modrinth.loaders.ModLoader;
import com.dervarex.minified.modrinth.projects.Project;

import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

public class Version {
    public String id;
    public String projectId;
    public String authorId;
    public String name;
    public String versionNumber;
    public String changelog;
    public String changelogUrl;
    public VersionType versionType;
    public VersionStatus status;
    public String requestedStatus;
    public boolean featured;
    public long downloads;
    public Instant published;
    public String[] gameVersions;
    public String[] loaders;
    public VersionDependency[] dependencies;
    public VersionFile[] files;

    transient Modrinth client;

    public Version() {
    }

    public String getId() {
        return id;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getName() {
        return name;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public VersionType getVersionType() {
        return versionType;
    }

    public VersionStatus getStatus() {
        return status;
    }

    public boolean isFeatured() {
        return featured;
    }

    public long getDownloads() {
        return downloads;
    }

    public Instant getPublished() {
        return published;
    }

    public String[] getGameVersions() {
        return gameVersions;
    }

    public String[] getLoaders() {
        return loaders;
    }

    public VersionFile[] getFiles() {
        return files;
    }

    public VersionFile getPrimaryFile() {
        if (files == null || files.length == 0) {
            return null;
        }
        for (VersionFile file : files) {
            if (file != null && file.primary) {
                return file;
            }
        }
        return files[0];
    }

    public boolean hasLoader(ModLoader loader) {
        return loader != null && hasLoader(loader.getApiValue());
    }

    public boolean hasLoader(String loader) {
        if (loaders == null || loader == null) {
            return false;
        }
        for (String entry : loaders) {
            if (loader.equalsIgnoreCase(entry)) {
                return true;
            }
        }
        return false;
    }

    public boolean supportsVersion(String version) {
        if (gameVersions == null || version == null) {
            return false;
        }
        for (String entry : gameVersions) {
            if (version.equalsIgnoreCase(entry)) {
                return true;
            }
        }
        return false;
    }

    public Path download(Path directory) {
        VersionFile file = getPrimaryFile();
        if (file == null) {
            throw new ModrinthDependencyResolutionException("Version " + id + " has no downloadable files");
        }
        return file.download(directory);
    }

    public List<Version> resolveDependencies() {
        return resolveDependencies(true, false);
    }

    public List<Version> resolveDependencies(boolean recursive, boolean includeOptional) {
        ensureClient();
        LinkedHashMap<String, Version> resolved = new LinkedHashMap<>();
        LinkedHashSet<String> visiting = new LinkedHashSet<>();
        resolveDependenciesInternal(this, resolved, visiting, recursive, includeOptional);
        return new ArrayList<>(resolved.values());
    }

    public List<Path> downloadDependencies(Path directory) {
        List<Path> paths = new ArrayList<>();
        for (Version dependency : resolveDependencies(true, false)) {
            paths.add(dependency.download(directory));
        }
        return paths;
    }

    public Project getProject() {
        ensureClient();
        return client.projects().get(projectId);
    }

    private void resolveDependenciesInternal(Version current,
                                             Map<String, Version> resolved,
                                             Set<String> visiting,
                                             boolean recursive,
                                             boolean includeOptional) {
        if (current.dependencies == null) {
            return;
        }
        visiting.add(current.id);
        for (VersionDependency dependency : current.dependencies) {
            if (dependency == null) {
                continue;
            }
            if (dependency.dependencyType == DependencyType.OPTIONAL && !includeOptional) {
                continue;
            }
            Version resolvedDependency = resolveDependency(dependency);
            if (resolvedDependency == null || resolvedDependency.id == null) {
                continue;
            }
            if (resolved.putIfAbsent(resolvedDependency.id, resolvedDependency) == null && recursive && visiting.add(resolvedDependency.id)) {
                resolveDependenciesInternal(resolvedDependency, resolved, visiting, true, includeOptional);
            }
        }
        visiting.remove(current.id);
    }

    private Version resolveDependency(VersionDependency dependency) {
        if (dependency.versionId != null && !dependency.versionId.isBlank()) {
            return client.versions().get(dependency.versionId);
        }
        if (dependency.projectId != null && !dependency.projectId.isBlank()) {
            Project project = client.projects().get(dependency.projectId);
            return project.getLatestVersion(gameVersions, loaders);
        }
        return null;
    }

    private void ensureClient() {
        if (client == null) {
            throw new ModrinthStateException("This version is not attached to a Modrinth client");
        }
    }

    void attach(Modrinth client) {
        this.client = client;
        if (files != null) {
            for (VersionFile file : files) {
                if (file != null) {
                    file.attach(this);
                }
            }
        }
    }
}

