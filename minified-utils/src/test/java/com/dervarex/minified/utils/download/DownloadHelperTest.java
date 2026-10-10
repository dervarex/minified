package com.dervarex.minified.utils.download;

import com.dervarex.minified.utils.exceptions.HttpException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DownloadHelperTest {

    private static final String CONTENT = "minified";
    private static final String SHA1 = sha1(CONTENT);

    @TempDir
    Path tempDir;

    private HttpServer server;
    private String baseUrl;
    private final AtomicInteger requests = new AtomicInteger();
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/file", exchange -> {
            requests.incrementAndGet();
            respond(exchange, 200, CONTENT);
        });
        // the cdn sends us a 503 sometimes, then works again a second later, the infrastructure must be very well designed
        server.createContext("/flaky", exchange -> respond(exchange, requests.incrementAndGet() == 1 ? 503 : 200, CONTENT));
        server.createContext("/missing", exchange -> {
            requests.incrementAndGet();
            respond(exchange, 404, "nope");
        });
        server.createContext("/manifest", exchange -> respond(exchange, 200,
                "{ \"downloads\": { \"client\": { \"url\": \"https://example.com/client.jar\" } } }"));
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void downloadsAndVerifiesTheChecksum() throws IOException {
        Path target = tempDir.resolve("some/folder/file.txt");
        AtomicLong progress = new AtomicLong();

        assertTrue(DownloadHelper.download(baseUrl + "/file", target, SHA1, client, progress::addAndGet));

        assertEquals(CONTENT, Files.readString(target));
        assertEquals(CONTENT.length(), progress.get());
        assertFalse(Files.exists(tempDir.resolve("some/folder/file.txt.tmp")));
    }

    @Test
    void keepsNothingWhenTheChecksumIsWrong() {
        Path target = tempDir.resolve("file.txt");

        assertFalse(DownloadHelper.download(baseUrl + "/file", target, sha1("something else"), client));

        assertFalse(Files.exists(target));
        assertFalse(Files.exists(tempDir.resolve("file.txt.tmp")));
    }

    @Test
    void skipsFilesThatAreAlreadyThere() throws IOException {
        Path target = Files.writeString(tempDir.resolve("file.txt"), CONTENT);

        assertTrue(DownloadHelper.download(baseUrl + "/file", target, SHA1, client));

        assertEquals(0, requests.get());
    }

    @Test
    void replacesBrokenFiles() throws IOException {
        Path target = Files.writeString(tempDir.resolve("file.txt"), "half a file");

        assertTrue(DownloadHelper.download(baseUrl + "/file", target, SHA1, client));

        assertEquals(CONTENT, Files.readString(target));
    }

    @Test
    void retriesWhenTheServerHasAMoment() throws IOException {
        Path target = tempDir.resolve("file.txt");

        assertTrue(DownloadHelper.download(baseUrl + "/flaky", target, SHA1, client));

        assertEquals(2, requests.get());
        assertEquals(CONTENT, Files.readString(target));
    }

    @Test
    void doesNotRetryMissingFiles() {
        assertThrows(RuntimeException.class,
                () -> DownloadHelper.download(baseUrl + "/missing", tempDir.resolve("file.txt"), SHA1, client));

        assertEquals(1, requests.get());
    }

    @Test
    void downloadsInThePool() throws Exception {
        Path target = tempDir.resolve("file.txt");
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            DownloadHelper.download(baseUrl + "/file", target, SHA1, pool, client).get();
        } finally {
            pool.shutdown();
        }

        assertEquals(CONTENT, Files.readString(target));
    }

    @Test
    void downloadsWithoutAChecksum() throws IOException {
        Path target = tempDir.resolve("some/folder/file.txt");
        AtomicLong progress = new AtomicLong();

        DownloadHelper.downloadWithoutSha1(baseUrl + "/file", target, client, progress::addAndGet);

        assertEquals(CONTENT, Files.readString(target));
        assertEquals(CONTENT.length(), progress.get());
        assertFalse(Files.exists(tempDir.resolve("some/folder/file.txt.tmp")));
    }

    @Test
    void skipsExistingFilesWithoutAChecksum() throws IOException {
        Path target = Files.writeString(tempDir.resolve("file.txt"), "already here");

        DownloadHelper.downloadWithoutSha1(baseUrl + "/file", target, client, bytes -> {});

        assertEquals(0, requests.get());
        assertEquals("already here", Files.readString(target));
    }

    @Test
    void failsOnMissingFilesWithoutAChecksum() {
        Path target = tempDir.resolve("file.txt");

        RuntimeException e = assertThrows(RuntimeException.class,
                () -> DownloadHelper.downloadWithoutSha1(baseUrl + "/missing", target, client, bytes -> {}));

        HttpException http = assertInstanceOf(HttpException.class, e.getCause());
        assertEquals(404, http.getStatusCode());

        assertFalse(Files.exists(target));
        assertFalse(Files.exists(tempDir.resolve("file.txt.tmp")));
    }

    @Test
    void downloadsWithoutAChecksumInThePool() throws Exception {
        Path target = tempDir.resolve("file.txt");
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            DownloadHelper.downloadWithoutSha1(baseUrl + "/file", target, pool, client, bytes -> {}).get();
        } finally {
            pool.shutdown();
        }

        assertEquals(CONTENT, Files.readString(target));
    }

    @Test
    void preparesTheClientRequestFromTheManifest() throws Exception {
        assertEquals(URI.create("https://example.com/client.jar"),
                DownloadHelper.prepareClientRequest(baseUrl + "/manifest", "client").uri());
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static String sha1(String content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
