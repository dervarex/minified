package com.dervarex.minified.java;

import com.dervarex.minified.events.EventBus;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JavaManagerTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        JavaManager.init(tempDir, new EventBus());
    }

    @Test
    @DisplayName("init stores the base directory as an absolute path")
    void init_storesBaseDirectoryAsAbsolutePath() {
        Path relative = Path.of("some", "relative", "dir");
        JavaManager.init(relative);

        assertTrue(JavaManager.getBaseDir().isAbsolute());
        assertTrue(JavaManager.getBaseDir().endsWith(relative));
    }

    @Test
    @DisplayName("init rejects a null base directory")
    void init_rejectsNullBaseDirectory() {
        assertThrows(NullPointerException.class, () -> JavaManager.init(null));
    }

    @Test
    @DisplayName("getRequiredJavaVersion reads the major version from valid JSON")
    void getRequiredJavaVersion_readsMajorVersionFromValidJson() {
        String json = """
                {
                  "id": "1.21.4",
                  "javaVersion": {
                    "component": "java-runtime-delta",
                    "majorVersion": 21
                  }
                }
                """;

        assertEquals(21, JavaManager.getRequiredJavaVersion(new JsonFile(json)));
    }

    @Test
    @DisplayName("getRequiredJavaVersion returns -1 when javaVersion is missing")
    void getRequiredJavaVersion_returnsMinusOneWhenJavaVersionMissing() {
        String json = """
                {
                  "id": "1.12.2"
                }
                """;

        assertEquals(-1, JavaManager.getRequiredJavaVersion(new JsonFile(json)));
    }

    @Test
    @DisplayName("getRequiredJavaVersion returns -1 for null input")
    void getRequiredJavaVersion_returnsMinusOneForNullInput() {
        assertEquals(-1, JavaManager.getRequiredJavaVersion((JsonFile) null));
        assertEquals(-1, JavaManager.getRequiredJavaVersion((JsonValue) null));
    }

    @Test
    @DisplayName("getRequiredJavaVersion returns -1 for non-object JSON")
    void getRequiredJavaVersion_returnsMinusOneForNonObjectJson() {
        JsonValue array = new JsonFile("[1, 2, 3]").getRoot();
        assertEquals(-1, JavaManager.getRequiredJavaVersion(array));
    }

    @Test
    @DisplayName("getRequiredJavaVersion reads the version from a file path")
    void getRequiredJavaVersion_readsVersionFromFilePath() throws IOException {
        Path jsonPath = tempDir.resolve("1.21.4.json");
        Files.writeString(jsonPath, """
                {
                  "javaVersion": { "majorVersion": 17 }
                }
                """);

        assertEquals(17, JavaManager.getRequiredJavaVersion(jsonPath));
    }

    @Test
    @DisplayName("getRequiredJavaVersion returns -1 for a missing file path")
    void getRequiredJavaVersion_returnsMinusOneForMissingFilePath() throws IOException {
        Path missing = tempDir.resolve("does-not-exist.json");
        assertEquals(-1, JavaManager.getRequiredJavaVersion(missing));
    }

    @Test
    @DisplayName("currentRuntime describes the running JVM")
    void currentRuntime_describesRunningJvm() {
        JavaInstallation current = JavaManager.currentRuntime();

        assertNotNull(current);
        assertTrue(current.majorVersion() > 0);
        assertNotNull(current.home());
        assertNotNull(current.executable());
        assertTrue(Files.exists(current.executable()));
        assertFalse(current.managed());
    }

    @Test
    @DisplayName("ensureJavaVersion returns the current JVM when it is sufficient")
    void ensureJavaVersion_returnsCurrentJvmWhenSufficient() throws Exception {
        int currentMajor = JavaPlatform.majorVersion();

        JavaInstallation result = JavaManager.ensureJavaVersion(currentMajor);

        assertEquals(currentMajor, result.majorVersion());
        assertFalse(result.managed());
    }

    @Test
    @DisplayName("ensureJavaVersion returns the current JVM for an invalid version")
    void ensureJavaVersion_returnsCurrentJvmForInvalidVersion() throws Exception {
        JavaInstallation result = JavaManager.ensureJavaVersion(-1);
        assertEquals(JavaPlatform.majorVersion(), result.majorVersion());
    }

    @Test
    @DisplayName("ensureJavaExecutable returns the current JVM executable when sufficient")
    void ensureJavaExecutable_returnsCurrentJvmExecutableWhenSufficient() throws Exception {
        int currentMajor = JavaPlatform.majorVersion();
        Path executable = JavaManager.ensureJavaExecutable(currentMajor);

        assertNotNull(executable);
        assertTrue(Files.exists(executable));
        assertEquals(JavaManager.currentRuntime().executable(), executable);
    }

    @Test
    @DisplayName("getVersionJsonUrl returns null for an unknown Minecraft version")
    void getVersionJsonUrl_returnsNullForUnknownMinecraftVersion() throws Exception {
        String url = JavaManager.getVersionJsonUrl("definitely-not-a-real-mc-version-xyz-123");
        assertNull(url);
    }

    @Test
    @DisplayName("getRequiredJavaVersion uses the cached version JSON when present")
    void getRequiredJavaVersion_usesCachedVersionJsonWhenPresent() throws Exception {
        Path cacheDir = JavaManager.getBaseDir().resolve("cache").resolve("versions");
        Files.createDirectories(cacheDir);

        String version = "1.21.4";
        Path cached = cacheDir.resolve(version + ".json");
        Files.writeString(cached, """
                {
                  "id": "1.21.4",
                  "javaVersion": { "majorVersion": 21 }
                }
                """);

        assertEquals(21, JavaManager.getRequiredJavaVersion(version));
    }
}