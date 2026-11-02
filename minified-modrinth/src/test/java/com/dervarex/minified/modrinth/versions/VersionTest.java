package com.dervarex.minified.modrinth.versions;

import com.dervarex.minified.events.Event;
import com.dervarex.minified.events.EventBus;
import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.events.DependencyConflictEvent;
import com.dervarex.minified.modrinth.events.DependencyDownloadedEvent;
import com.dervarex.minified.modrinth.events.DependencyResolvedEvent;
import com.dervarex.minified.modrinth.events.DependencySkippedEvent;
import com.dervarex.minified.modrinth.events.DependencySkippedEvent.Reason;
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

    @Test
    void resolveDependencies_postsWhatItResolvedAndWhatItSkipped() throws IOException {
        List<Event> events = new ArrayList<>();
        Modrinth modrinth = fakeModrinth(Map.of(
                "root", version("root", "required:a", "optional:c", "incompatible:enemy", "embedded:inside", "required:nothing"),
                "a", version("a", "required:root"),
                "c", version("c"),
                "nothing", "{\"id\":null}"), events);

        modrinth.versions().get("root").resolveDependencies();

        assertEquals(List.of(
                "resolved root -> a",
                "skipped a -> root CIRCULAR",
                "skipped root -> c OPTIONAL",
                "skipped root -> enemy INCOMPATIBLE",
                "skipped root -> inside EMBEDDED",
                "skipped root -> nothing UNRESOLVABLE"
        ), describe(events));
    }

    @Test
    void resolveDependencies_postsUnknownTypes() throws IOException {
        List<Event> events = new ArrayList<>();
        Modrinth modrinth = fakeModrinth(Map.of("root", version("root", "weird:x")), events);

        assertTrue(modrinth.versions().get("root").resolveDependencies().isEmpty());
        assertEquals(List.of("skipped root -> x UNKNOWN_TYPE"), describe(events));
    }

    @Test
    void downloadDependencies_countsUp(@TempDir Path tempDir) throws IOException {
        List<Event> events = new ArrayList<>();
        Modrinth modrinth = fakeModrinth(Map.of(
                "root", version("root", "required:a", "required:b"),
                "a", withFile(version("a")),
                "b", withFile(version("b"))), events);

        List<Path> files = modrinth.versions().get("root").downloadDependencies(tempDir);

        List<DependencyDownloadedEvent> downloaded = events.stream()
                .filter(DependencyDownloadedEvent.class::isInstance).map(DependencyDownloadedEvent.class::cast).toList();
        assertEquals(2, downloaded.size());
        assertEquals("a", downloaded.get(0).dependency().getId());
        assertEquals(1, downloaded.get(0).completed());
        assertEquals(2, downloaded.get(1).completed());
        assertEquals(2, downloaded.get(1).total());
        assertEquals(files.get(1), downloaded.get(1).file());
        assertEquals("root", downloaded.get(1).root().getId());
    }

    @Test
    void resolveDependencies_postsConflictsBetweenWhatGetsInstalled() throws IOException {
        List<Event> events = new ArrayList<>();
        Modrinth modrinth = fakeModrinth(Map.of(
                // root can't run with anything from project p-b, but a pulls in b anyway
                "root", version("root", "required:a", "required:c").replace("]}",
                        ",{\"project_id\":\"p-b\",\"dependency_type\":\"incompatible\"}]}"),
                "a", version("a", "required:b"),
                "b", version("b"),
                // c can't run with version a
                "c", version("c", "incompatible:a")), events);

        assertEquals(List.of("a", "b", "c"), ids(modrinth.versions().get("root").resolveDependencies()));

        List<String> conflicts = events.stream().filter(DependencyConflictEvent.class::isInstance)
                .map(DependencyConflictEvent.class::cast)
                .map(e -> e.declaredBy().getId() + " vs " + e.conflicting().getId()).toList();
        assertEquals(List.of("root vs b", "c vs a"), conflicts);
    }

    @Test
    void resolveDependencies_noConflictWhenTheIncompatibleOneStaysOut() throws IOException {
        List<Event> events = new ArrayList<>();
        Modrinth modrinth = fakeModrinth(Map.of(
                "root", version("root", "required:a", "incompatible:enemy"),
                "a", version("a"),
                "enemy", version("enemy")), events);

        modrinth.versions().get("root").resolveDependencies();

        assertTrue(events.stream().noneMatch(DependencyConflictEvent.class::isInstance));
    }

    @Test
    void resolvedEventsSayWhenAVersionIsNotListed() throws IOException {
        List<Event> events = new ArrayList<>();
        Modrinth modrinth = fakeModrinth(Map.of(
                "root", version("root", "required:old", "required:fine", "required:nostatus"),
                "old", version("old").replace("{", "{\"status\":\"archived\","),
                "fine", version("fine").replace("{", "{\"status\":\"listed\","),
                "nostatus", version("nostatus")), events);

        modrinth.versions().get("root").resolveDependencies();

        List<String> notListed = events.stream().filter(DependencyResolvedEvent.class::isInstance)
                .map(DependencyResolvedEvent.class::cast)
                .filter(DependencyResolvedEvent::notListed)
                .map(e -> e.resolved().getId()).toList();
        assertEquals(List.of("old"), notListed);
    }

    private Modrinth fakeModrinth(Map<String, String> versions, List<Event> events) throws IOException {
        EventBus eventBus = new EventBus();
        eventBus.subscribe(DependencyResolvedEvent.class, events::add);
        eventBus.subscribe(DependencySkippedEvent.class, events::add);
        eventBus.subscribe(DependencyDownloadedEvent.class, events::add);
        eventBus.subscribe(DependencyConflictEvent.class, events::add);
        Modrinth plain = fakeModrinth(versions);
        return new Modrinth(plain.getBaseUrl(), eventBus);
    }

    private static List<String> describe(List<Event> events) {
        return events.stream().map(event -> switch (event) {
            case DependencyResolvedEvent e -> "resolved " + e.parent().getId() + " -> " + e.resolved().getId();
            case DependencySkippedEvent e -> "skipped " + e.parent().getId() + " -> " + e.dependency().getVersionId() + " " + e.reason();
            default -> event.toString();
        }).toList();
    }

    private static String withFile(String versionJson) {
        String id = versionJson.substring(7, versionJson.indexOf('"', 7));
        return versionJson.substring(0, versionJson.length() - 1)
                + ",\"files\":[{\"url\":\"BASE/file/" + id + ".jar\",\"filename\":\"" + id + ".jar\",\"primary\":true,\"hashes\":{}}]}";
    }

    private Modrinth fakeModrinth(Map<String, String> versions) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/version/", exchange -> {
            String json = versions.get(exchange.getRequestURI().getPath().substring("/version/".length()));
            if (json != null) {
                json = json.replace("BASE", "http://localhost:" + exchange.getLocalAddress().getPort());
            }
            byte[] body = (json == null ? "{}" : json).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(json == null ? 404 : 200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/file/", exchange -> {
            byte[] body = exchange.getRequestURI().getPath().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
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
