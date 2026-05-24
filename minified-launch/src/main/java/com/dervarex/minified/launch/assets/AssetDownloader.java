package com.dervarex.minified.launch.assets;

import com.dervarex.minified.launch.ApiEndpoints;
import com.dervarex.minified.launch.version.VersionManifestClient;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
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

public class AssetDownloader {

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private final ExecutorService pool;

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public AssetDownloader(int downloadThreads) {
        this.pool = Executors.newFixedThreadPool(downloadThreads);
    }

    /**
     * Downloads the minecraft assets
     * @param version the game version the assets should be downloaded from
     * @param gameDir the directory the assets will be put in
     */
    public void downloadAssets(String version, Path gameDir) {

        try {

            NetworkUtil.ensureOnline("downloading assets for version " + version);

            JsonObject versionEntry = VersionManifestClient
                    .getVersionEntry(version)
                    .asObject();

            JsonFile versionJson = new JsonFile(
                    HttpUtil.get(versionEntry.get("url").asString())
            );

            JsonObject assetIndex = versionJson
                    .get("assetIndex")
                    .asObject();

            String assetIndexId = assetIndex
                    .get("id")
                    .asString();

            String assetIndexUrl = assetIndex
                    .get("url")
                    .asString();

            Path assetsDir = gameDir.resolve("assets");

            Path indexesDir = assetsDir.resolve("indexes");
            Path objectsDir = assetsDir.resolve("objects");

            Files.createDirectories(indexesDir);
            Files.createDirectories(objectsDir);

            String assetIndexContent = HttpUtil.get(assetIndexUrl);

            Path indexPath = indexesDir.resolve(assetIndexId + ".json");

            Files.writeString(indexPath, assetIndexContent);

            JsonObject objects = new JsonFile(assetIndexContent)
                    .get("objects")
                    .asObject();

            List<Future<?>> futures = new ArrayList<>();

            for (String key : objects.keys()) {

                JsonObject asset = objects
                        .get(key)
                        .asObject();

                String hash = asset
                        .get("hash")
                        .asString();

                String subDir = hash.substring(0, 2);

                String url =
                        ApiEndpoints.ResourcesUrl
                                + subDir
                                + "/"
                                + hash;

                Path output = objectsDir
                        .resolve(subDir)
                        .resolve(hash);

                futures.add(download(url, output, hash));
            }

            for (Future<?> future : futures) {
                future.get();
            }

            System.out.println("All assets downloaded.");

        } catch (Exception e) {
            throw new RuntimeException("Failed to download assets", e);
        } finally {
            pool.shutdown();
        }
    }

    private Future<?> download(String url, Path path, String expectedSha1) {

        return pool.submit(() -> {

            try {

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