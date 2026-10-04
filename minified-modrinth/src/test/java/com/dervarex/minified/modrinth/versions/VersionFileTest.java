package com.dervarex.minified.modrinth.versions;

import com.dervarex.minified.modrinth.exceptions.ModrinthDownloadException;
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
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void leavesNothingBehindWhenTheHashIsWrong() throws IOException {
        assertThrows(ModrinthDownloadException.class, () -> file("/mod.jar", Map.of("sha1", "0".repeat(40))).download(tempDir));

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
