package com.dervarex.minified.servers;

import com.dervarex.minified.events.EventBus;
import com.dervarex.minified.events.type.connection.CheckConnectionEvent;
import com.dervarex.minified.events.type.connection.OfflineEvent;
import com.dervarex.minified.servers.events.download.server.DownloadServerJarEvent;
import com.dervarex.minified.utils.download.DownloadHelper;
import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.exceptions.NoConnectionException;
import com.dervarex.minified.utils.network.NetworkUtil;
import com.dervarex.minified.utils.sha.Hasher;
import com.dervarex.minified.utils.version.VersionMetadataProvider;
import org.jetbrains.annotations.Nullable;

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
public class ServerDownloader {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * @param version  the game version
     * @param path     the full path to the jar including the file name, for example /home/dervarex/server.jar
     * @param eventBus the eventBus to push events to, may be null
     */
    public void downloadServer(String version, Path path, @Nullable EventBus eventBus)
            throws HttpException, IOException {
        downloadServer(version, path, progress -> {}, eventBus);
    }

    public void downloadServer(String version, Path path, Consumer<Double> progressConsumer, @Nullable EventBus eventBus)
            throws HttpException, IOException {

        try {
            if (eventBus != null) {
                eventBus.post(new CheckConnectionEvent());
            }
            NetworkUtil.ensureOnline("download server jar");
        } catch (NoConnectionException nce) {
            System.err.println("You do not have a working internet connection!");
            if (eventBus != null) {
                eventBus.post(new OfflineEvent());
            }
            return;
        }

        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        String versionJsonUrl = VersionMetadataProvider.getVersionJsonUrl(version);
        if (versionJsonUrl == null) {
            System.out.println("ServerDownloader: Cannot find version!");
            return;
        }

        String serverUrl = VersionMetadataProvider.getServerUrl(version);
        String sha1 = VersionMetadataProvider.getServerSha1(version);

        if (serverUrl == null || sha1 == null) {
            System.out.println("ServerDownloader: Cannot find server jar!");
            return;
        }

        long totalBytes = resolveContentLength(serverUrl);
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
            updateProgress(totalBytesFinal, totalBytesFinal, progressConsumer, eventBus);
            return;
        }

        AtomicLong downloadedBytes = new AtomicLong();

        DownloadHelper.download(
                serverUrl,
                path,
                sha1,
                httpClient,
                bytes -> {
                    long current = downloadedBytes.addAndGet(bytes);
                    updateProgress(current, totalBytesFinal, progressConsumer, eventBus);
                }
        );

        updateProgress(totalBytesFinal, totalBytesFinal, progressConsumer, eventBus);
    }

    private void updateProgress(long downloadedBytes, long totalBytes, Consumer<Double> progressConsumer, @Nullable EventBus eventBus) {
        double progress = Math.min(1.0, downloadedBytes / (double) Math.max(totalBytes, 1L));
        progressConsumer.accept(progress);

        if (eventBus != null) {
            eventBus.post(new DownloadServerJarEvent(progress, downloadedBytes, totalBytes));
        }
    }

    private long resolveContentLength(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
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