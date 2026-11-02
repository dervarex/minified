package com.dervarex.minified.modrinth.versions;

import com.dervarex.minified.events.EventBus;
import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.events.Event;
import com.dervarex.minified.modrinth.events.DownloadFinishedEvent;
import com.dervarex.minified.modrinth.events.DownloadProgressEvent;
import com.dervarex.minified.modrinth.events.DownloadStartedEvent;
import com.dervarex.minified.modrinth.events.OutdatedFileReplacedEvent;
import com.dervarex.minified.modrinth.events.UnverifiedDownloadEvent;
import com.dervarex.minified.modrinth.exceptions.ModrinthDownloadException;
import com.dervarex.minified.modrinth.exceptions.ModrinthHashMismatchException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionFileTest {

    private static final byte[] JAR = "pretend this is a mod".getBytes(StandardCharsets.UTF_8);

    @TempDir
    Path tempDir;

    private HttpServer server;
    private String baseUrl;
    private final AtomicInteger requests = new AtomicInteger();

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/mod.jar", exchange -> {
            requests.incrementAndGet();
            exchange.sendResponseHeaders(200, JAR.length);
            exchange.getResponseBody().write(JAR);
            exchange.close();
        });
        server.createContext("/cut.jar", exchange -> {
            // promises 1000 bytes, delivers 10, then the connection is gone
            exchange.sendResponseHeaders(200, 1000);
            exchange.getResponseBody().write(JAR, 0, 10);
            exchange.close();
        });
        server.createContext("/gone.jar", exchange -> {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void downloadsAndChecksTheStrongestHash() throws Exception {
        // sha1 is wrong on purpose, sha512 wins
        Path jar = file("/mod.jar", Map.of("sha1", "0".repeat(40), "sha512", hash("SHA-512"))).download(tempDir);

        assertEquals(tempDir.resolve("mod.jar"), jar);
        assertEquals("pretend this is a mod", Files.readString(jar));
    }

    @Test
    void skipsFilesThatAreAlreadyThere() throws Exception {
        Files.write(tempDir.resolve("mod.jar"), JAR);

        file("/mod.jar", Map.of("sha1", hash("SHA-1"))).download(tempDir);

        assertEquals(0, requests.get());
    }

    @Test
    void leavesNothingBehindWhenTheHashIsWrong() throws Exception {
        ModrinthHashMismatchException e = assertThrows(ModrinthHashMismatchException.class,
                () -> file("/mod.jar", Map.of("sha1", "0".repeat(40))).download(tempDir));
        assertEquals("sha1", e.getAlgorithm());
        assertEquals("0".repeat(40), e.getExpectedHash());
        assertEquals(hash("SHA-1"), e.getActualHash());

        try (var files = Files.list(tempDir)) {
            assertEquals(0, files.count());
        }
    }

    @Test
    void brokenTransfersLeaveNothingBehindEither() throws IOException {
        assertThrows(ModrinthDownloadException.class, () -> file("/cut.jar", Map.of()).download(tempDir));

        try (var files = Files.list(tempDir)) {
            assertEquals(0, files.count());
        }
    }

    @Test
    void httpErrorsBecomeDownloadExceptions() {
        assertThrows(ModrinthDownloadException.class, () -> file("/gone.jar", Map.of()).download(tempDir));
    }

    @Test
    void downloadsAnywayButTellsTheLauncherWhenTheHashCannotBeChecked() {
        List<UnverifiedDownloadEvent> events = new ArrayList<>();
        VersionFile file = attached(file("/mod.jar", Map.of("blake3", "0".repeat(64))), events);

        Path jar = file.download(tempDir);

        assertTrue(Files.exists(jar));
        assertEquals(1, events.size());
        assertEquals("blake3", events.get(0).algorithm());
        assertEquals(jar, events.get(0).destination());
    }

    @Test
    void checkedDownloadsPostNoEvent() throws Exception {
        List<UnverifiedDownloadEvent> events = new ArrayList<>();
        attached(file("/mod.jar", Map.of("sha1", hash("SHA-1"))), events).download(tempDir);

        assertTrue(events.isEmpty());
    }

    @Test
    void postsStartProgressAndFinish() throws Exception {
        List<Event> events = new ArrayList<>();
        Path jar = withDownloadEvents(file("/mod.jar", Map.of("sha1", hash("SHA-1"))), events).download(tempDir);

        DownloadStartedEvent started = assertInstanceOf(DownloadStartedEvent.class, events.get(0));
        assertEquals("mod.jar", started.filename());
        assertEquals(jar, started.destination());

        DownloadProgressEvent last = assertInstanceOf(DownloadProgressEvent.class, events.get(events.size() - 2));
        assertEquals(1.0, last.progress());
        assertEquals(JAR.length, last.downloadedBytes());
        assertEquals(JAR.length, last.totalBytes());

        DownloadFinishedEvent finished = assertInstanceOf(DownloadFinishedEvent.class, events.get(events.size() - 1));
        assertEquals(jar, finished.destination());
        assertFalse(finished.alreadyPresent());
    }

    @Test
    void filesThatAreAlreadyThereOnlyPostFinished() throws Exception {
        Files.write(tempDir.resolve("mod.jar"), JAR);
        List<Event> events = new ArrayList<>();
        withDownloadEvents(file("/mod.jar", Map.of("sha1", hash("SHA-1"))), events).download(tempDir);

        assertEquals(1, events.size());
        assertTrue(assertInstanceOf(DownloadFinishedEvent.class, events.get(0)).alreadyPresent());
    }

    @Test
    void failedDownloadsDontPostFinished() {
        List<Event> events = new ArrayList<>();
        VersionFile file = withDownloadEvents(file("/mod.jar", Map.of("sha1", "0".repeat(40))), events);

        assertThrows(ModrinthHashMismatchException.class, () -> file.download(tempDir));
        assertTrue(events.stream().noneMatch(DownloadFinishedEvent.class::isInstance));
    }

    @Test
    void replacingABrokenFilePostsWhatWasWrong() throws Exception {
        Files.writeString(tempDir.resolve("mod.jar"), "half a mod");
        List<OutdatedFileReplacedEvent> events = new ArrayList<>();
        EventBus eventBus = new EventBus();
        eventBus.subscribe(OutdatedFileReplacedEvent.class, events::add);
        VersionFile file = file("/mod.jar", Map.of("sha1", hash("SHA-1")));
        Version version = new Version();
        version.files = new VersionFile[]{file};
        version.attach(new Modrinth(Modrinth.DEFAULT_BASE_URL, eventBus));

        Path jar = file.download(tempDir);

        assertEquals("pretend this is a mod", Files.readString(jar));
        assertEquals(1, events.size());
        assertEquals(hash("SHA-1"), events.get(0).expectedHash());
        assertEquals(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest("half a mod".getBytes(StandardCharsets.UTF_8))),
                events.get(0).actualHash());
    }

    private static VersionFile withDownloadEvents(VersionFile file, List<Event> events) {
        EventBus eventBus = new EventBus();
        eventBus.subscribe(DownloadStartedEvent.class, events::add);
        eventBus.subscribe(DownloadProgressEvent.class, events::add);
        eventBus.subscribe(DownloadFinishedEvent.class, events::add);
        Version version = new Version();
        version.files = new VersionFile[]{file};
        version.attach(new Modrinth(Modrinth.DEFAULT_BASE_URL, eventBus));
        return file;
    }

    private static VersionFile attached(VersionFile file, List<UnverifiedDownloadEvent> events) {
        EventBus eventBus = new EventBus();
        eventBus.subscribe(UnverifiedDownloadEvent.class, events::add);
        Version version = new Version();
        version.files = new VersionFile[]{file};
        version.attach(new Modrinth(Modrinth.DEFAULT_BASE_URL, eventBus));
        return file;
    }

    private VersionFile file(String path, Map<String, String> hashes) {
        VersionFile file = new VersionFile();
        file.url = baseUrl + path;
        file.filename = "mod.jar";
        file.hashes.putAll(hashes);
        return file;
    }

    private static String hash(String algorithm) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance(algorithm).digest(JAR));
    }
}
