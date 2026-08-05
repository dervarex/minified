package com.dervarex.minified.launch.download.assets;

import com.dervarex.minified.launch.events.download.assets.DownloadAssetsEvent;
import com.dervarex.minified.launch.exceptions.download.AssetDownloadException;
import com.dervarex.minified.launch.launch.LaunchContext;
import com.dervarex.minified.utils.ApiEndpoints;
import com.dervarex.minified.utils.download.DownloadHelper;
import com.dervarex.minified.utils.exceptions.NoConnectionException;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;
import com.dervarex.minified.utils.network.NetworkUtil;
import com.dervarex.minified.utils.sha.Hasher;
import com.dervarex.minified.utils.version.VersionManifestClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class AssetDownloader {

    private final ExecutorService pool;

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public AssetDownloader(int downloadThreads) {
        this.pool = Executors.newFixedThreadPool(downloadThreads);
    }

    public void downloadAssets(String version, Path assetsDir, LaunchContext context) {
        downloadAssets(version, assetsDir, progress -> {}, context);
    }

    /**
     * Downloads the assets
     * @param version the game version
     * @param assetsDir the directory the assets should be downloaded to
     */
    public void downloadAssets(String version, Path assetsDir, Consumer<Double> progressConsumer, LaunchContext context) {
        try {
            boolean online = true;
            try {
                NetworkUtil.ensureOnline("downloading assets for version " + version);
            } catch (NoConnectionException e) {
                online = false;
            }

            if (!online) {
                AssetOfflineValidator.validate(version, assetsDir);
                return;
            }

            assetsDir.toFile().mkdirs();

            JsonObject versionEntry = Objects.requireNonNull(
                    VersionManifestClient.getVersionEntry(version)
            ).asObject();

            Path cacheRoot = resolveCacheRoot(assetsDir);
            Files.createDirectories(cacheRoot);

            String versionJsonRaw = HttpUtil.get(versionEntry.get("url").asString());
            Path versionCachePath = cacheRoot.resolve("versions").resolve(version + ".json");
            Files.createDirectories(versionCachePath.getParent());
            Files.writeString(versionCachePath, versionJsonRaw);

            JsonFile versionJson = new JsonFile(versionJsonRaw);

            JsonObject assetIndex = versionJson
                    .get("assetIndex")
                    .asObject();

            String assetIndexId = assetIndex
                    .get("id")
                    .asString();

            String assetIndexUrl = assetIndex
                    .get("url")
                    .asString();

            Path indexesDir = assetsDir.resolve("indexes");
            Path objectsDir = assetsDir.resolve("objects");

            Files.createDirectories(indexesDir);
            Files.createDirectories(objectsDir);

            String assetIndexContent = HttpUtil.get(assetIndexUrl);

            Path indexPath = indexesDir.resolve(assetIndexId + ".json");
            Files.writeString(indexPath, assetIndexContent);

            Path cachedIndexPath = cacheRoot
                    .resolve("assets")
                    .resolve("indexes")
                    .resolve(assetIndexId + ".json");

            Files.createDirectories(cachedIndexPath.getParent());
            Files.writeString(cachedIndexPath, assetIndexContent);

            JsonObject objects = new JsonFile(assetIndexContent)
                    .get("objects")
                    .asObject();

            List<Future<?>> futures = new ArrayList<>();
            Set<String> seenHashes = ConcurrentHashMap.newKeySet();
            List<AssetTarget> targets = new ArrayList<>();

            for (String key : objects.keys()) {
                JsonObject asset = objects
                        .get(key)
                        .asObject();

                String hash = asset
                        .get("hash")
                        .asString();

                if (!seenHashes.add(hash)) {
                    continue;
                }

                String subDir = hash.substring(0, 2);

                String url = ApiEndpoints.RESOURCES_URL + subDir + "/" + hash;

                Path output = objectsDir
                        .resolve(subDir)
                        .resolve(hash);

                long size = resolveAssetSize(asset, url);
                targets.add(new AssetTarget(url, output, hash, size));
            }

            long totalBytes = 0L;
            for (AssetTarget target : targets) {
                totalBytes += Math.max(target.size(), 0L);
            }
            final long totalBytesFinal = Math.max(totalBytes, 1L);

            AtomicLong downloadedBytes = new AtomicLong();

            for (AssetTarget target : targets) {
                String currentFile = target.path().getFileName().toString();

                if (isAlreadyDownloaded(target.path(), target.sha1())) {
                    long current = downloadedBytes.addAndGet(target.size());
                    updateProgress(
                            current,
                            totalBytesFinal,
                            currentFile,
                            target.size(),
                            target.size(),
                            progressConsumer,
                            context
                    );
                    continue;
                }

                AtomicLong currentFileBytes = new AtomicLong();

                futures.add(
                        DownloadHelper.download(
                                target.url(),
                                target.path(),
                                target.sha1(),
                                pool,
                                client,
                                bytes -> {
                                    long current = downloadedBytes.addAndGet(bytes);
                                    long fileCurrent = currentFileBytes.addAndGet(bytes);
                                    updateProgress(
                                            current,
                                            totalBytesFinal,
                                            currentFile,
                                            fileCurrent,
                                            target.size(),
                                            progressConsumer,
                                            context
                                    );
                                }
                        )
                );
            }

            for (Future<?> future : futures) {
                future.get();
            }

            String finalFile = targets.isEmpty() ? "" : targets.get(targets.size() - 1).path().getFileName().toString();
            long finalFileSize = targets.isEmpty() ? 0L : targets.get(targets.size() - 1).size();

            updateProgress(
                    totalBytesFinal,
                    totalBytesFinal,
                    finalFile,
                    finalFileSize,
                    finalFileSize,
                    progressConsumer,
                    context
            );

        } catch (Exception e) {
            throw new AssetDownloadException("Failed to download assets", e);
        } finally {
            pool.shutdown();
        }
    }

    private void updateProgress(
            long downloadedBytes,
            long totalBytes,
            String currentFile,
            long currentFileBytes,
            long currentFileSize,
            Consumer<Double> progressConsumer,
            LaunchContext context
    ) {
        double progress = Math.min(1.0, downloadedBytes / (double) Math.max(totalBytes, 1L));
        progressConsumer.accept(progress);

        if (context != null) {
            context.getEventBus().post(new DownloadAssetsEvent(
                    progress,
                    downloadedBytes,
                    totalBytes,
                    currentFile,
                    currentFileBytes,
                    currentFileSize
            ));
        }
    }

    private long resolveAssetSize(JsonObject asset, String url) {
        JsonValue sizeValue = asset.get("size");

        if (sizeValue != null) {
            if (sizeValue.isNumber()) {
                return sizeValue.asNumber().longValue();
            }

            if (sizeValue.isString()) {
                return Long.parseLong(sizeValue.asString());
            }
        }

        return resolveContentLength(url);
    }

    private long resolveContentLength(String url) {
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

    private boolean isAlreadyDownloaded(Path path, String sha1) {
        try {
            if (!Files.exists(path) || Files.size(path) <= 0) {
                return false;
            }
            return Hasher.sha1(path).equalsIgnoreCase(sha1);
        } catch (Exception e) {
            return false;
        }
    }

    private Path resolveCacheRoot(Path assetsDir) {
        Path parent = assetsDir.toAbsolutePath().getParent();
        return Objects.requireNonNullElseGet(parent, assetsDir::toAbsolutePath).resolve("cache");
    }

    private record AssetTarget(String url, Path path, String sha1, long size) {
    }
}