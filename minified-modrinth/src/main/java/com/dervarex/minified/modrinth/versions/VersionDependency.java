package com.dervarex.minified.modrinth.versions;

import com.dervarex.minified.modrinth.internal.ModrinthJson;
import com.dervarex.minified.utils.json.JsonObject;

/**
 * A dependency of a version. It points to an exact version, or to a project when any fitting version of it works.
 */
public final class VersionDependency {
    private final String versionId;
    private final String projectId;
    private final String fileName;
    private final DependencyType dependencyType;

    VersionDependency(JsonObject json) {
        versionId = ModrinthJson.string(json, "version_id");
        projectId = ModrinthJson.string(json, "project_id");
        fileName = ModrinthJson.string(json, "file_name");
        dependencyType = DependencyType.fromApiValue(ModrinthJson.string(json, "dependency_type"));
    }

    /** {@return the ID of the exact version this depends on, {@code null} when any version of the project works} */
    public String getVersionId() {
        return versionId;
    }

    /** {@return the ID of the project this depends on, can be {@code null} when {@link #getVersionId()} is set} */
    public String getProjectId() {
        return projectId;
    }

    /** {@return the file name of a dependency that isn't on Modrinth, {@code null} for ones that are} */
    public String getFileName() {
        return fileName;
    }

    /** {@return how the version depends on it} */
    public DependencyType getDependencyType() {
        return dependencyType;
    }
}
