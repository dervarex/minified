package com.dervarex.minified.java;

import com.dervarex.minified.events.EventBus;
import com.dervarex.minified.java.events.download.JavaArchiveDownloadEvent;
import com.dervarex.minified.java.events.extract.ExtractArchiveEvent;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonValue;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JavaManagerTest {

    @TempDir
    Path tempDir;

    private final EventBus eventBus = new EventBus();
    private HttpServer server;
    private final AtomicInteger downloads = new AtomicInteger();

    @BeforeEach
    void setUp() {
        JavaManager.init(tempDir, eventBus);
    }

    @AfterEach
    void stopServer() {
        if (server != null) server.stop(0);
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
    @DisplayName("ensureExactJavaVersion returns the current JVM when the version matches exactly")
    void ensureExactJavaVersion_returnsCurrentJvmWhenMatching() throws Exception {
        int currentMajor = JavaPlatform.majorVersion();

        JavaInstallation result = JavaManager.ensureExactJavaVersion(currentMajor);

        assertEquals(currentMajor, result.majorVersion());
        assertFalse(result.managed());
    }

    @Test
    @DisplayName("ensureExactJavaVersion returns the current JVM for an invalid version")
    void ensureExactJavaVersion_returnsCurrentJvmForInvalidVersion() throws Exception {
        JavaInstallation result = JavaManager.ensureExactJavaVersion(-1);
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

    @Test
    @DisplayName("getRequiredJavaVersion fetches and caches unknown versions")
    void getRequiredJavaVersion_fetchesAndCaches() throws Exception {
        assertEquals(8, JavaManager.getRequiredJavaVersion("1.16.5"));
        assertTrue(Files.exists(tempDir.resolve("cache/versions/1.16.5.json")));
    }

    @Test
    @DisplayName("a managed runtime gets downloaded once, unpacked and reused")
    void managedRuntime_installsFromTarGzAndIsReused() throws Exception {
        byte[] archive = tarGz(Map.of("jdk-99+1-jre/bin/" + javaName(), "#!/bin/sh", "jdk-99+1-jre/release", "JAVA_VERSION=99"));
        offerRuntime(99, "jre.tar.gz", archive, sha256(archive));
        List<JavaArchiveDownloadEvent> downloadEvents = new ArrayList<>();
        List<ExtractArchiveEvent> extractEvents = new ArrayList<>();
        eventBus.subscribe(JavaArchiveDownloadEvent.class, downloadEvents::add);
        eventBus.subscribe(ExtractArchiveEvent.class, extractEvents::add);

        JavaInstallation installed = JavaManager.ensureExactJavaVersion(99);
        JavaInstallation again = JavaManager.ensureExactJavaVersion(99);

        assertTrue(installed.managed());
        assertEquals(99, installed.majorVersion());
        assertEquals("jdk-99+1", installed.releaseName());
        assertEquals("jdk-99+1-jre", installed.home().getFileName().toString());
        assertTrue(Files.isExecutable(installed.executable()));
        assertEquals(installed.executable(), again.executable());
        assertEquals(1, downloads.get());
        assertEquals(1.0, downloadEvents.getLast().progress());
        assertEquals(100, extractEvents.getLast().progress());
    }

    @Test
    @DisplayName("everything executable in the archive stays executable")
    void managedRuntime_keepsExecutableBits() throws Exception {
        assumeTrue(!platformOs().equals("windows"), "no exec bits on windows");
        byte[] archive = tarGz(Map.of("jdk-95+1-jre/bin/java", "#!/bin/sh", "jdk-95+1-jre/lib/jspawnhelper", "#!/bin/sh"));
        offerRuntime(95, "jre.tar.gz", archive, sha256(archive));

        JavaInstallation installed = JavaManager.ensureExactJavaVersion(95);

        assertTrue(Files.isExecutable(installed.home().resolve("lib/jspawnhelper")));
    }

    @Test
    @DisplayName("windows runtimes come as zip")
    void managedRuntime_installsFromZip() throws Exception {
        byte[] archive = zip(Map.of("jdk-98+1-jre/bin/" + javaName(), "#!/bin/sh"));
        offerRuntime(98, "jre.zip", archive, sha256(archive));

        assertTrue(Files.exists(JavaManager.ensureExactJavaVersion(98).executable()));
    }

    @Test
    @DisplayName("a runtime with the wrong checksum is not installed")
    void managedRuntime_rejectsWrongChecksum() throws Exception {
        byte[] archive = tarGz(Map.of("jdk-97+1-jre/bin/" + javaName(), "#!/bin/sh"));
        offerRuntime(97, "jre.tar.gz", archive, sha256("something else".getBytes(StandardCharsets.UTF_8)));

        assertThrows(IOException.class, () -> JavaManager.ensureExactJavaVersion(97));
        try (var files = Files.walk(tempDir.resolve("runtimes"))) {
            assertTrue(files.noneMatch(path -> path.getFileName().toString().equals(javaName())));
        }
    }

    @Test
    @DisplayName("archives can't write outside of the runtime folder")
    void managedRuntime_blocksPathTraversal() throws Exception {
        byte[] archive = tarGz(Map.of("../../../../escaped", "gotcha"));
        offerRuntime(96, "jre.tar.gz", archive, sha256(archive));

        assertThrows(IOException.class, () -> JavaManager.ensureExactJavaVersion(96));
        assertFalse(Files.exists(tempDir.resolve("escaped")));
    }

    @Test
    @DisplayName("Java 8 can be installed for old Minecraft versions")
    void ensureExactJavaVersion_installsARealJava8() throws Exception {
        assumeTrue(JavaPlatform.majorVersion() != 8, "this JVM already is Java 8");

        JavaInstallation java8 = JavaManager.ensureExactJavaVersion(8);

        assertTrue(java8.managed());
        Process process = new ProcessBuilder(java8.executable().toString(), "-version").redirectErrorStream(true).start();
        assertTrue(process.waitFor(30, TimeUnit.SECONDS));
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, process.exitValue(), output);
        assertTrue(output.contains("\"1.8.0"), output);
    }

    private void offerRuntime(int majorVersion, String packageName, byte[] archive, String checksum) throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/" + packageName, exchange -> {
            downloads.incrementAndGet();
            exchange.sendResponseHeaders(200, archive.length);
            exchange.getResponseBody().write(archive);
            exchange.close();
        });
        server.start();

        // pretend adoptium already told us about this runtime
        Path assets = Files.createDirectories(tempDir.resolve("cache/java/adoptium/" + majorVersion)).resolve("jre.json");
        Files.writeString(assets, """
                [{
                  "release_name": "jdk-%d+1",
                  "binaries": [{
                    "os": "%s", "architecture": "%s", "image_type": "jre", "jvm_impl": "hotspot",
                    "package": { "link": "http://localhost:%d/%s", "checksum": "%s", "name": "%s" }
                  }]
                }]
                """.formatted(majorVersion, platformOs(), platformArchitecture(), server.getAddress().getPort(),
                packageName, checksum, packageName));
    }

    private static String platformOs() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        return os.contains("win") ? "windows" : os.contains("mac") ? "mac" : "linux";
    }

    private static String platformArchitecture() {
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
        return arch.contains("aarch64") || arch.contains("arm64") ? "aarch64" : "x64";
    }

    private static String javaName() {
        return platformOs().equals("windows") ? "java.exe" : "java";
    }

    private static byte[] tarGz(Map<String, String> files) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (OutputStream out = new GZIPOutputStream(bytes)) {
            for (Map.Entry<String, String> file : new LinkedHashMap<>(files).entrySet()) {
                byte[] content = file.getValue().getBytes(StandardCharsets.UTF_8);
                byte[] header = new byte[512];
                byte[] name = file.getKey().getBytes(StandardCharsets.UTF_8);
                System.arraycopy(name, 0, header, 0, name.length);
                byte[] mode = "0000755".getBytes(StandardCharsets.US_ASCII);
                System.arraycopy(mode, 0, header, 100, mode.length);
                byte[] size = "%011o".formatted(content.length).getBytes(StandardCharsets.US_ASCII);
                System.arraycopy(size, 0, header, 124, size.length);
                header[156] = '0';
                out.write(header);
                out.write(content);
                out.write(new byte[(512 - content.length % 512) % 512]);
            }
            out.write(new byte[1024]);
        }
        return bytes.toByteArray();
    }

    private static byte[] zip(Map<String, String> files) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream out = new ZipOutputStream(bytes)) {
            for (Map.Entry<String, String> file : files.entrySet()) {
                out.putNextEntry(new ZipEntry(file.getKey()));
                out.write(file.getValue().getBytes(StandardCharsets.UTF_8));
                out.closeEntry();
            }
        }
        return bytes.toByteArray();
    }

    private static String sha256(byte[] content) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
    }
}
