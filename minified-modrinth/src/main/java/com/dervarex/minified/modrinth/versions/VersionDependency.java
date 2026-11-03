package com.dervarex.minified.modrinth.versions;

/**
 * A dependency of a version, pointing to another version or project
 */
public class VersionDependency {
    public String versionId;
    public String projectId;
    public String fileName;
    public DependencyType dependencyType;

    public VersionDependency() {
    }

    public String getVersionId() {
        return versionId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getFileName() {
        return fileName;
    }

    public DependencyType getDependencyType() {
        return dependencyType;
    }

    public boolean isOptional() {
        return dependencyType == DependencyType.OPTIONAL;
    }
}

