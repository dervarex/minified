package com.dervarex.minified.modrinth.versions;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.exceptions.ModrinthStateException;
import com.dervarex.minified.modrinth.loaders.ModLoader;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VersionTest {

    private final List<HttpServer> servers = new ArrayList<>();

    @AfterEach
    void stopServers() {
        servers.forEach(server -> server.stop(0));
    }

    @Test
    void getters_returnSetValues() {
        Version version = new Version();
        version.id = "v1";
        version.projectId = "p1";
        version.authorId = "a1";
        version.name = "Version 1";
        version.versionNumber = "1.0.0";
        version.featured = true;
        version.downloads = 42L;
        version.published = Instant.parse("2024-01-01T00:00:00Z");

        assertEquals("v1", version.getId());
        assertEquals("p1", version.getProjectId());
        assertEquals("a1", version.getAuthorId());
        assertEquals("Version 1", version.getName());
        assertEquals("1.0.0", version.getVersionNumber());
        assertTrue(version.isFeatured());
        assertEquals(42L, version.getDownloads());
        assertEquals(Instant.parse("2024-01-01T00:00:00Z"), version.getPublished());
    }

    @Test
    void getPrimaryFile_returnsPrimaryFile() {
        Version version = new Version();
        VersionFile first = new VersionFile();
        first.primary = false;
        VersionFile second = new VersionFile();
        second.primary = true;
        version.files = new VersionFile[]{first, second};
        assertEquals(second, version.getPrimaryFile());
    }

    @Test
    void getPrimaryFile_returnsFirst_whenNoPrimary() {
        Version version = new Version();
        VersionFile first = new VersionFile();
        first.primary = false;
        VersionFile second = new VersionFile();
        second.primary = false;
        version.files = new VersionFile[]{first, second};
        assertEquals(first, version.getPrimaryFile());
    }

    @Test
    void getPrimaryFile_returnsNull_whenNoFiles() {
        Version version = new Version();
        assertNull(version.getPrimaryFile());
    }

    @Test
    void getPrimaryFile_returnsNull_whenFilesEmpty() {
        Version version = new Version();
        version.files = new VersionFile[0];
        assertNull(version.getPrimaryFile());
    }

    @Test
    void hasLoaderString_returnsTrue_whenMatch() {
        Version version = new Version();
        version.loaders = new String[]{"fabric"};
        assertTrue(version.hasLoader("fabric"));
    }

    @Test
    void hasLoaderString_ignoresCase() {
        Version version = new Version();
        version.loaders = new String[]{"fabric"};
        assertTrue(version.hasLoader("FABRIC"));
    }

    @Test
    void hasLoaderString_returnsFalse_whenNoMatch() {
        Version version = new Version();
        version.loaders = new String[]{"fabric"};
        assertFalse(version.hasLoader("forge"));
    }

    @Test
    void hasLoaderString_returnsFalse_whenNull() {
        Version version = new Version();
        assertFalse(version.hasLoader((String) null));
    }

    @Test
    void hasLoader_ModLoader_returnsTrue() {
        Version version = new Version();
        version.loaders = new String[]{"fabric"};
        assertTrue(version.hasLoader(ModLoader.FABRIC));
    }

    @Test
    void hasLoader_ModLoader_returnsFalse_whenLoaderNull() {
        Version version = new Version();
        version.loaders = new String[]{"fabric"};
        assertFalse(version.hasLoader((ModLoader) null));
    }

    @Test
    void supportsVersion_returnsTrue_whenMatch() {
        Version version = new Version();
        version.gameVersions = new String[]{"1.20"};
        assertTrue(version.supportsVersion("1.20"));
    }

    @Test
    void supportsVersion_ignoresCase() {
        Version version = new Version();
        version.gameVersions = new String[]{"1.20"};
        assertTrue(version.supportsVersion("1.20"));
    }

    @Test
    void supportsVersion_returnsFalse_whenNoMatch() {
        Version version = new Version();
        version.gameVersions = new String[]{"1.20"};
        assertFalse(version.supportsVersion("1.19"));
    }

    @Test
    void supportsVersion_returnsFalse_whenNull() {
        Version version = new Version();
        assertFalse(version.supportsVersion(null));
    }

    @Test
    void resolveDependencies_followsRequiredOnesAndSkipsOptionalOnes() throws IOException {
        Modrinth modrinth = fakeModrinth(Map.of(
                "root", version("root", "required:a", "optional:c"),
                "a", version("a", "required:b"),
                "b", version("b"),
                "c", version("c")));
        Version root = modrinth.versions().get("root");

        assertEquals(List.of("a", "b"), ids(root.resolveDependencies()));
        assertEquals(List.of("a", "b", "c"), ids(root.resolveDependencies(true, true)));
        assertEquals(List.of("a"), ids(root.resolveDependencies(false, false)));
    }

    @Test
    void resolveDependencies_ignoresIncompatibleAndEmbeddedOnes() throws IOException {
        Modrinth modrinth = fakeModrinth(Map.of(
                "root", version("root", "required:a", "incompatible:enemy", "embedded:inside"),
                "a", version("a"),
                "enemy", version("enemy"),
                "inside", version("inside")));

        assertEquals(List.of("a"), ids(modrinth.versions().get("root").resolveDependencies()));
    }

    @Test
    void resolveDependencies_survivesCyclesWithoutListingItself() throws IOException {
        Modrinth modrinth = fakeModrinth(Map.of(
                "root", version("root", "required:a"),
                "a", version("a", "required:root")));

        assertEquals(List.of("a"), ids(modrinth.versions().get("root").resolveDependencies()));
    }

    @Test
    void resolveDependencies_needsAClient() {
        assertThrows(ModrinthStateException.class, () -> new Version().resolveDependencies());
    }

    @Test
    void realIrisNeedsSodiumAndCanBeDownloaded(@TempDir Path tempDir) {
        Version iris = Modrinth.connect().versions().get("ZnhzDm36");

        assertTrue(ids(iris.resolveDependencies()).contains("joBzVWtR"));
        Path jar = iris.download(tempDir);
        assertEquals(2665879L, jar.toFile().length());
    }

    private Modrinth fakeModrinth(Map<String, String> versions) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/version/", exchange -> {
            String json = versions.get(exchange.getRequestURI().getPath().substring("/version/".length()));
            byte[] body = (json == null ? "{}" : json).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(json == null ? 404 : 200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        servers.add(server);
        return new Modrinth("http://localhost:" + server.getAddress().getPort());
    }

    private static String version(String id, String... dependencies) {
        StringBuilder deps = new StringBuilder();
        for (String dependency : dependencies) {
            String[] parts = dependency.split(":");
            if (!deps.isEmpty()) deps.append(',');
            deps.append("{\"version_id\":\"").append(parts[1]).append("\",\"dependency_type\":\"").append(parts[0]).append("\"}");
        }
        return "{\"id\":\"" + id + "\",\"project_id\":\"p-" + id + "\",\"dependencies\":[" + deps + "]}";
    }

    private static List<String> ids(List<Version> versions) {
        return versions.stream().map(Version::getId).toList();
    }
}
