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

/**
 * @hidden
 */
@ApiStatus.Internal
public class DownloadHelper {
    public static Future<?> download(String url, Path path, String expectedSha1, ExecutorService pool, HttpClient client) {

        return pool.submit(() -> {

            Path tempFile = Path.of(path + ".tmp");
            try {

                if (Files.exists(path)) {

                    String existingSha1 = Hasher.sha1(path);

                    if (existingSha1.equalsIgnoreCase(expectedSha1)) {
                        System.out.println("Already exists: " + path);
                        return;
                    }

                    Files.delete(path);
                }

                Files.createDirectories(path.getParent());

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

                String actualSha1 = Hasher.bytesToHex(digest.digest());

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
                    Files.deleteIfExists(tempFile);
                } catch (Exception ignored) {
                }

                throw new RuntimeException(
                        "Failed to download " + path,
                        e
                );
            }
        });
    }
    /**
     * @hidden
     * @param manifestUrl the manifest url(version url)
     * @param type can be either server or client
     * @return the HttpRequest
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
