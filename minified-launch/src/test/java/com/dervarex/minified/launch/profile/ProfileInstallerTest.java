package com.dervarex.minified.launch.profile;

import com.dervarex.minified.launch.exceptions.download.HttpDownloadException;
import com.dervarex.minified.launch.exceptions.profile.FailedToInstallProfileFileException;
import com.dervarex.minified.utils.sha.Hasher;
import com.sun.net.httpserver.HttpExchange;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ProfileInstallerTest {
    private static final String CONTENT = "minified";
    private static final String SHA1 = sha("SHA-1", CONTENT);
    private static final String SHA512 = sha("SHA-512", CONTENT);

    @TempDir
    Path gameDir;

    private HttpServer server;
    private String baseUrl;
    private final AtomicInteger requests = new AtomicInteger();

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/mod.jar", exchange -> respond(exchange, 200, CONTENT));
        server.createContext("/tampered.jar", exchange -> respond(exchange, 200, "not minified"));
        server.createContext("/missing.jar", exchange -> respond(exchange, 404, "nope"));
        server.createContext("/redirect", exchange -> {
            requests.incrementAndGet();
            exchange.getResponseHeaders().add("Location", baseUrl + "/mod.jar");
            exchange.sendResponseHeaders(302, -1);
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
    void downloadsAndChecksFiles() throws IOException {
        ProfileFile mod = mod("mods/mod.jar", "/mod.jar").build();

        assertEquals(List.of(), ProfileInstaller.install(List.of(mod), gameDir));
        assertEquals(CONTENT, Files.readString(gameDir.resolve("mods/mod.jar")));

        int before = requests.get();
        ProfileInstaller.install(List.of(mod), gameDir);
        assertEquals(before, requests.get(), "files with the right hash are not downloaded again");
    }

    @Test
    void triesTheNextUrlAndFollowsRedirects() throws IOException {
        ProfileFile mod = ProfileFile.builder("mods/mod.jar")
                .hash("sha512", SHA512)
                .download(baseUrl + "/missing.jar")
                .download(baseUrl + "/redirect")
                .build();

        ProfileInstaller.install(List.of(mod), gameDir);

        assertEquals(CONTENT, Files.readString(gameDir.resolve("mods/mod.jar")));
        assertEquals(3, requests.get(), "the 404, the redirect and the file");
    }

    @Test
    void rejectsFilesWithTheWrongHash() {
        ProfileFile mod = mod("mods/mod.jar", "/tampered.jar").build();

        FailedToInstallProfileFileException e = assertThrows(FailedToInstallProfileFileException.class,
                () -> ProfileInstaller.install(List.of(mod), gameDir));

        assertEquals(mod, e.getFile());
        assertFalse(Files.exists(gameDir.resolve("mods/mod.jar")));
        assertEquals(List.of(), list(gameDir.resolve("mods")), "no temp files are left behind");
    }

    @Test
    void keepsTheStatusCodeOfFailedDownloads() {
        ProfileFile mod = mod("mods/mod.jar", "/missing.jar").build();

        FailedToInstallProfileFileException e = assertThrows(FailedToInstallProfileFileException.class,
                () -> ProfileInstaller.install(List.of(mod), gameDir));

        HttpDownloadException http = assertInstanceOf(HttpDownloadException.class, e.getCause());
        assertEquals(404, http.getStatusCode());
    }

    @Test
    void reportsFilesWithoutUrls() {
        ProfileFile handmade = ProfileFile.builder("resourcepacks/our-pack.zip").hash("sha1", SHA1).build();

        assertEquals(List.of(handmade), ProfileInstaller.install(List.of(handmade), gameDir));
    }

    @Test
    void filesAlreadyThereNeedNoUrl() throws IOException {
        Files.createDirectories(gameDir.resolve("resourcepacks"));
        Files.writeString(gameDir.resolve("resourcepacks/our-pack.zip"), CONTENT);

        ProfileFile pack = ProfileFile.builder("resourcepacks/our-pack.zip").hash("sha1", SHA1).build();

        assertEquals(List.of(), ProfileInstaller.install(List.of(pack), gameDir));
    }

    @Test
    void disabledFilesGetTheDisabledSuffix() throws IOException {
        ProfileFile mod = mod("mods/mod.jar", "/mod.jar").enabled(false).build();

        ProfileInstaller.install(List.of(mod), gameDir);
        assertTrue(Files.exists(gameDir.resolve("mods/mod.jar.disabled")));

        // turning it back on renames the file instead of downloading it again
        server.removeContext("/mod.jar");
        ProfileInstaller.install(List.of(mod.toBuilder().enabled(true).build()), gameDir);
        assertTrue(Files.exists(gameDir.resolve("mods/mod.jar")));
        assertFalse(Files.exists(gameDir.resolve("mods/mod.jar.disabled")));
    }

    @Test
    void skipsServerOnlyFiles() {
        ProfileFile serverOnly = mod("mods/server.jar", "/mod.jar").client(SideRequirement.UNSUPPORTED).build();

        ProfileInstaller.install(List.of(serverOnly), gameDir);

        assertFalse(Files.exists(gameDir.resolve("mods/server.jar")));
    }

    @Test
    void installsIntoTheGameDirectoryOfTheProfile() throws IOException {
        Path instance = gameDir.resolve("instance");
        Profile profile = new Profile(
                ProfileMetadata.builder().build(),
                new com.dervarex.minified.launch.launch.LaunchConfiguration.Builder()
                        .jarFile(instance.resolve("client.jar"))
                        .assetsDirectory(gameDir.resolve("assets"))
                        .librariesDirectory(gameDir.resolve("libraries"))
                        .loader(new com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader("1.21.11"))
                        .build(),
                List.of(mod("mods/mod.jar", "/mod.jar").build())
        );

        ProfileInstaller.install(profile);

        assertEquals(CONTENT, Files.readString(instance.resolve("mods/mod.jar")));
    }

    private ProfileFile.Builder mod(String path, String url) {
        return ProfileFile.builder(path)
                .hash("sha1", SHA1)
                .hash("sha512", SHA512)
                .size(CONTENT.length())
                .download(baseUrl + url);
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        requests.incrementAndGet();
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static List<Path> list(Path directory) {
        try (var files = Files.list(directory)) {
            return files.toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String sha(String algorithm, String content) {
        try {
            return Hasher.bytesToHex(MessageDigest.getInstance(algorithm).digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
