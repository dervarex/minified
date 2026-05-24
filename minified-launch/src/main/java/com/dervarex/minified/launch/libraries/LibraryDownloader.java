package com.dervarex.minified.launch.libraries;

import com.dervarex.minified.launch.version.VersionManifestClient;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;
import com.dervarex.minified.utils.network.NetworkUtil;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class LibraryDownloader {

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private final ExecutorService pool;

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public LibraryDownloader(int downloadThreads) {
        this.pool = Executors.newFixedThreadPool(downloadThreads);
    }

    /**
     * Downloads the Libraries
     * @param version the game version the assets should be downloaded from
     * @param librariesPath the directory the assets should be downloaded to
     */
    public void downloadLibraries(String version, String librariesPath) {
        try {
            NetworkUtil.ensureOnline("downloading libraries for version " + version);

            JsonObject versionEntry = VersionManifestClient
                    .getVersionEntry(version)
                    .asObject();

            JsonFile versionJson = new JsonFile(
                    HttpUtil.get(versionEntry.get("url").asString())
            );

            List<Future<?>> futures = new ArrayList<>();

            JsonArray libraries = versionJson.get("libraries").asArray();

            for (JsonValue libraryValue : libraries) {

                JsonObject library = libraryValue.asObject();


                 // Skip unsupported OS rules
                if (!isAllowed(library)) {
                    continue;
                }

                JsonObject downloads = library.get("downloads").asObject();


                 // Some libraries only contain classifiers
                if (!downloads.has("artifact")) {
                    continue;
                }

                JsonObject artifact = downloads
                        .get("artifact")
                        .asObject();

                String url = artifact.get("url").asString();

                String sha1 = artifact.get("sha1").asString();

                Path path = Path.of(
                        librariesPath,
                        artifact.get("path").asString()
                );

                futures.add(download(url, path, sha1));
            }

            // Wait for all downloads
            for (Future<?> future : futures) {
                future.get();
            }

            pool.shutdown();

            if (!pool.awaitTermination(1, TimeUnit.HOURS)) {
                throw new RuntimeException("Download pool timeout");
            }

            System.out.println("All libraries downloaded.");

        } catch (Exception e) {
            throw new RuntimeException("Failed to download libraries", e);
        }
    }

    public Future<?> download(String url, Path path, String expectedSha1) {

        return pool.submit(() -> {

            try {
                 // Skip existing valid file
                if (Files.exists(path)) {

                    String existingSha1 = sha1(path);

                    if (existingSha1.equalsIgnoreCase(expectedSha1)) {
                        System.out.println("Already exists: " + path);
                        return;
                    }

                    Files.delete(path);
                }

                Files.createDirectories(path.getParent());

                Path tempFile = Path.of(path + ".tmp");

                MessageDigest digest = MessageDigest.getInstance("SHA-1");

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(30))
                        .GET()
                        .build();

                HttpResponse<InputStream> response = client.send(
                        request,
                        HttpResponse.BodyHandlers.ofInputStream()
                );

                if (response.statusCode() != 200) {
                    throw new RuntimeException(
                            "HTTP " + response.statusCode() + " for " + url
                    );
                }

                try (
                        InputStream in = response.body();
                        var out = Files.newOutputStream(tempFile)
                ) {

                    byte[] buffer = new byte[8192];

                    int read;

                    while ((read = in.read(buffer)) != -1) {

                        out.write(buffer, 0, read);

                        digest.update(buffer, 0, read);
                    }
                }

                String actualSha1 = bytesToHex(digest.digest());

                if (!actualSha1.equalsIgnoreCase(expectedSha1)) {

                    Files.deleteIfExists(tempFile);

                    throw new RuntimeException(
                            "\nSHA1 mismatch for " + path +
                                    "\nExpected: " + expectedSha1 +
                                    "\nActual:   " + actualSha1
                    );
                }

                Files.move(
                        tempFile,
                        path,
                        StandardCopyOption.REPLACE_EXISTING
                );

                System.out.println("Downloaded: " + path);

            } catch (Exception e) {

                try {
                    Files.deleteIfExists(Path.of(path + ".tmp"));
                } catch (Exception ignored) {
                }

                throw new RuntimeException(
                        "Failed to download " + path,
                        e
                );
            }
        });
    }

    private boolean isAllowed(JsonObject library) {
         // No rules = allowed

        if (!library.has("rules")) {
            return true;
        }

        JsonArray rules = library.get("rules").asArray();

        String os = getMinecraftOs();

        boolean allowed = false;

        for (JsonValue ruleValue : rules) {

            JsonObject rule = ruleValue.asObject();

            String action = rule.get("action").asString();


            // Rule without OS
            if (!rule.has("os")) {

                allowed = action.equals("allow");

                continue;
            }

            JsonObject osObject = rule.get("os").asObject();

            String ruleOs = osObject.get("name").asString();

            if (ruleOs.equals(os)) {
                allowed = action.equals("allow");
            }
        }

        return allowed;
    }

    private String getMinecraftOs() {

        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return "windows";
        }

        if (os.contains("mac")) {
            return "osx";
        }

        if (os.contains("linux")) {
            return "linux";
        }

        return "unknown";
    }

    private String sha1(Path path) throws Exception {

        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        try (InputStream in = Files.newInputStream(path)) {

            byte[] buffer = new byte[8192];

            int read;

            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }

        return bytesToHex(digest.digest());
    }

    private static String bytesToHex(byte[] bytes) {

        char[] chars = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {

            int v = bytes[i] & 0xFF;

            chars[i * 2] = HEX[v >>> 4];
            chars[i * 2 + 1] = HEX[v & 0x0F];
        }

        return new String(chars);
    }
}