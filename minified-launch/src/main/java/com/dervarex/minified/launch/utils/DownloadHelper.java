package com.dervarex.minified.launch.utils;

import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonValue;
import com.dervarex.minified.utils.sha.Hasher;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.LongConsumer;

/**
 * Internal helper for downloading files and verifying SHA-1 checksums.
 * <p>Not intended to be used by launcher developers.</p>
 * @hidden
 */
@ApiStatus.Internal
public class DownloadHelper {
    /**
     * Downloads a file asynchronously and verifies its SHA-1 checksum.
     * Existing files with a matching checksum are skipped.
     *
     * @param url the download URL
     * @param path target file path
     * @param expectedSha1 expected SHA-1 checksum
     * @param pool executor service used for the download task
     * @param client HTTP client used for the request
     * @return a Future representing the download task
     */
    public static Future<?> download(String url, Path path, String expectedSha1, ExecutorService pool, HttpClient client) {
        return download(url, path, expectedSha1, pool, client, bytes -> {});
    }

    public static Future<?> download(
            String url,
            Path path,
            String expectedSha1,
            ExecutorService pool,
            HttpClient client,
            LongConsumer progressConsumer
    ) {
        return pool.submit(() -> downloadInternal(url, path, expectedSha1, client, progressConsumer));
    }

    /**
     * Downloads a single file and checks if the sha1 values match
     *
     * @param url the url to download from
     * @param path the path to save the file to
     * @param expectedSha1 the expected sha1 value of the file
     * @return true if the file was downloaded or already exists with the correct sha1, false if the file has a wrong sha1 value after downloading
     */
    public static boolean download(String url, Path path, String expectedSha1) {
        return download(url, path, expectedSha1, HttpClient.newHttpClient());
    }

    public static boolean download(String url, Path path, String expectedSha1, HttpClient client) {
        return downloadInternal(url, path, expectedSha1, client, bytes -> {});
    }

    public static boolean download(
            String url,
            Path path,
            String expectedSha1,
            HttpClient client,
            LongConsumer progressConsumer
    ) {
        return downloadInternal(url, path, expectedSha1, client, progressConsumer);
    }

    private static boolean downloadInternal(
            String url,
            Path path,
            String expectedSha1,
            HttpClient client,
            LongConsumer progressConsumer
    ) {
        Path tempFile = Path.of(path + ".tmp");

        try {
            if (Files.exists(path)) {
                String existingSha1 = Hasher.sha1(path);

                if (existingSha1.equalsIgnoreCase(expectedSha1)) {
                    return true;
                }

                Files.delete(path);
            }

            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

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
                    progressConsumer.accept(read);
                }
            }

            String actualSha1 = Hasher.bytesToHex(digest.digest());

            if (!actualSha1.equalsIgnoreCase(expectedSha1)) {
                Files.deleteIfExists(tempFile);
                return false;
            }

            Files.move(
                    tempFile,
                    path,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return true;

        } catch (Exception e) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (Exception ignored) {
            }

            throw new RuntimeException(
                    "Failed to download " + path,
                    e
            );
        }
    }

    /**
     * Creates a download request from a version manifest.
     *
     * @param manifestUrl URL of the version manifest
     * @param type download type, usually {@code client} or {@code server}
     * @return the prepared download request
     * @throws HttpException if the manifest request fails
     * @throws IOException if the manifest cannot be read
     */
    public static HttpRequest prepareClientRequest(String manifestUrl, String type) throws HttpException, IOException {
        JsonFile json = new JsonFile(HttpUtil.get(manifestUrl));
        JsonValue downloads = json.get("downloads");
        JsonValue clientValue = downloads.asObject().get(type);
        String downloadUrl = clientValue.asObject().get("url").asString();

        return HttpRequest.newBuilder()
                .uri(URI.create(downloadUrl))
                .GET()
                .build();
    }
}