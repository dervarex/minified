package com.dervarex.minified.modrinth.versions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VersionDependencyTest {

    @Test
    void getters_returnSetValues() {
        VersionDependency dependency = new VersionDependency();
        dependency.versionId = "v1";
        dependency.projectId = "p1";
        dependency.fileName = "file.jar";
        dependency.dependencyType = DependencyType.REQUIRED;

        assertEquals("v1", dependency.getVersionId());
        assertEquals("p1", dependency.getProjectId());
        assertEquals("file.jar", dependency.getFileName());
        assertEquals(DependencyType.REQUIRED, dependency.getDependencyType());
    }

    @Test
    void isOptional_returnsTrue_whenTypeOptional() {
        VersionDependency dependency = new VersionDependency();
        dependency.dependencyType = DependencyType.OPTIONAL;
        assertTrue(dependency.isOptional());
    }

    @Test
    void isOptional_returnsFalse_whenTypeRequired() {
        VersionDependency dependency = new VersionDependency();
        dependency.dependencyType = DependencyType.REQUIRED;
        assertFalse(dependency.isOptional());
    }

    @Test
    void isOptional_returnsFalse_whenTypeNull() {
        VersionDependency dependency = new VersionDependency();
        assertFalse(dependency.isOptional());
    }
}