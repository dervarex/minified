package com.dervarex.minified.utils.download;

import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonValue;
import com.dervarex.minified.utils.sha.Hasher;
import org.apiguardian.api.API;

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
 * Download helper for downloading files and verifying SHA-1 checksums.
 * Recently moved over from the Launch module, be careful.
 */
@API(status = API.Status.STABLE)
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

    /**
     * Downloads a file async without checksum verification, for files that come without a SHA-1
     * Existing not empty files are skipped
     *
     * @param url the download URL
     * @param path target file path
     * @param pool executor service used for the download task
     * @param client HTTP client used for the request
     * @param progressConsumer receives the number of bytes read for each chunk
     * @return a Future representing the download task
     */
    public static Future<?> downloadWithoutSha1(
            String url,
            Path path,
            ExecutorService pool,
            HttpClient client,
            LongConsumer progressConsumer
    ) {
        return pool.submit(() -> downloadWithoutSha1(url, path, client, progressConsumer));
    }

    /**
     * Downloads a single file without checksum verification, for files that come without a SHA-1
     * Existing not empty files are skipped
     *
     * @param url the url to download from
     * @param path the path to save the file to
     * @param client HTTP client used for the request
     * @param progressConsumer receives the number of bytes read for each chunk
     * @throws RuntimeException if the download fails; when the server does not answer with HTTP 200 the cause is an {@link HttpException}
     */
    @API(status = API.Status.STABLE, since = "v3.2.1")
    public static void downloadWithoutSha1(String url, Path path, HttpClient client, LongConsumer progressConsumer) {
        Path tempFile = Path.of(path + ".tmp");

        try {
            if (Files.exists(path) && Files.size(path) > 0) {
                return;
            }

            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<InputStream> response =
                    client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                response.body().close();
                throw new HttpException.Builder()
                        .status(response.statusCode())
                        .method(HttpException.Method.GET)
                        .url(url)
                        .transientFailure(isTransient(response.statusCode()))
                        .build();
            }

            try (
                    InputStream in = response.body();
                    var out = Files.newOutputStream(tempFile)
            ) {
                byte[] buffer = new byte[8192];
                int read;

                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                    progressConsumer.accept(read);
                }
            }

            Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (Exception ignored) {
            }
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Failed to download " + url, e);
        }
    }

    private static final int MAX_ATTEMPTS = 3;

    private static boolean downloadInternal(
            String url,
            Path path,
            String expectedSha1,
            HttpClient client,
            LongConsumer progressConsumer
    ) {
        // network errors like "Connection reset" are retried, we don't want one to cancel the launch just because some network reset happened
        for (int attempt = 1; ; attempt++) {
            try {
                return downloadOnce(url, path, expectedSha1, client, progressConsumer);
            } catch (IOException e) {
                if (attempt >= MAX_ATTEMPTS) {
                    throw new RuntimeException("Failed to download " + path, e);
                }
                try {
                    Thread.sleep(1000L * attempt);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while downloading " + path, interrupted);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to download " + path, e);
            }
        }
    }

    private static boolean isTransient(int statusCode) {
        return statusCode == 304 || statusCode == 408 || statusCode == 429 || statusCode >= 500;
    }

    private static boolean downloadOnce(
            String url,
            Path path,
            String expectedSha1,
            HttpClient client,
            LongConsumer progressConsumer
    ) throws Exception {
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
                response.body().close();
                String message = "HTTP " + response.statusCode() + " for " + url;
                // CDN hiccups, the minecraft resources CDN sometimes answers with a 304
                if (isTransient(response.statusCode())) {
                    throw new IOException(message);
                }
                throw new RuntimeException(message);
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

            throw e;
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