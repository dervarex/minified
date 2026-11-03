package com.dervarex.minified.modrinth.versions;

import com.dervarex.minified.utils.json.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VersionDependencyTest {

    private static VersionDependency dependency(String json) {
        return new VersionDependency(JsonParser.parse(json).asObject());
    }

    @Test
    void readsEveryField() {
        VersionDependency dependency = dependency(
                "{\"version_id\":\"v1\",\"project_id\":\"p1\",\"file_name\":\"file.jar\",\"dependency_type\":\"required\"}");

        assertEquals("v1", dependency.getVersionId());
        assertEquals("p1", dependency.getProjectId());
        assertEquals("file.jar", dependency.getFileName());
        assertEquals(DependencyType.REQUIRED, dependency.getDependencyType());
    }

    @Test
    void unknownTypes_becomeUnknown() {
        assertEquals(DependencyType.UNKNOWN, dependency("{\"dependency_type\":\"something_new\"}").getDependencyType());
    }

    @Test
    void missingFields_areNull() {
        VersionDependency dependency = dependency("{}");
        assertNull(dependency.getVersionId());
        assertNull(dependency.getDependencyType());
    }
}
