package com.dervarex.minified.launch.download;

import com.dervarex.minified.launch.events.type.download.client.DownloadClientJarEvent;
import com.dervarex.minified.launch.launch.LaunchContext;
import com.dervarex.minified.launch.utils.DownloadHelper;
import com.dervarex.minified.launch.version.VersionMetadataProvider;
import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.exceptions.NoConnectionException;
import com.dervarex.minified.utils.network.NetworkUtil;
import com.dervarex.minified.utils.sha.Hasher;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class ClientDownloader {

    /**
     * @param version the game version
     * @param path    the full path to the jar including the file name, for example /home/dervarex/client.jar
     */
    public void downloadClient(String version, Path path)
            throws HttpException, IOException {
        downloadClient(version, path, progress -> {}, null);
    }

    public void downloadClient(String version, Path path, LaunchContext context)
            throws HttpException, IOException {
        downloadClient(version, path, progress -> {}, context);
    }

    public void downloadClient(String version, Path path, Consumer<Double> progressConsumer, LaunchContext context)
            throws HttpException, IOException {

        try {
            NetworkUtil.ensureOnline("download client jar");
        } catch (NoConnectionException nce) {
            System.err.println("You do not have a working internet connection!");
            return;
        }

        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        String versionJsonUrl = VersionMetadataProvider.getVersionJsonUrl(version);
        if (versionJsonUrl == null) {
            System.out.println("ClientDownloader: Cannot find version!");
            return;
        }

        String clientUrl = VersionMetadataProvider.getClientUrl(version);
        String sha1 = VersionMetadataProvider.getClientSha1(version);

        if (clientUrl == null || sha1 == null) {
            System.out.println("ClientDownloader: Cannot find client jar!");
            return;
        }

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        long totalBytes = resolveContentLength(clientUrl, client);
        if (totalBytes <= 0) {
            try {
                if (Files.exists(path)) {
                    totalBytes = Files.size(path);
                }
            } catch (Exception ignored) {
            }
        }
        if (totalBytes <= 0) {
            totalBytes = 1L;
        }
        final long totalBytesFinal = totalBytes;

        if (Files.exists(path)
                && Files.size(path) > 0
                && Hasher.sha1(path).equalsIgnoreCase(sha1)) {
            updateProgress(totalBytesFinal, totalBytesFinal, progressConsumer, context);
            return;
        }

        AtomicLong downloadedBytes = new AtomicLong();

        DownloadHelper.download(
                clientUrl,
                path,
                sha1,
                client,
                bytes -> {
                    long current = downloadedBytes.addAndGet(bytes);
                    updateProgress(current, totalBytesFinal, progressConsumer, context);
                }
        );

        updateProgress(totalBytesFinal, totalBytesFinal, progressConsumer, context);
    }

    private void updateProgress(long downloadedBytes, long totalBytes, Consumer<Double> progressConsumer, LaunchContext context) {
        double progress = Math.min(1.0, downloadedBytes / (double) Math.max(totalBytes, 1L));
        progressConsumer.accept(progress);

        if (context != null) {
            context.getEventBus().post(new DownloadClientJarEvent(progress, downloadedBytes, totalBytes));
        }
    }

    private long resolveContentLength(String url, HttpClient client) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.headers()
                        .firstValueAsLong("Content-Length")
                        .orElse(0L);
            }
        } catch (Exception ignored) {
        }
        return 0L;
    }
}