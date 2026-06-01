package com.dervarex.minified.launch.download.assets;

import com.dervarex.minified.launch.ApiEndpoints;
import com.dervarex.minified.launch.utils.DownloadHelper;
import com.dervarex.minified.launch.version.VersionManifestClient;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.network.NetworkUtil;

import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SuppressWarnings("unused")
public class AssetDownloader {

    private final ExecutorService pool;

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public AssetDownloader(int downloadThreads) {
        this.pool = Executors.newFixedThreadPool(downloadThreads);
    }

    /**
     * Downloads the assets
     * @param version the game version
     * @param assetsDir the directory the assets should be downloaded to
     */
    public void downloadAssets(String version, Path assetsDir) {

        try {

            NetworkUtil.ensureOnline("downloading assets for version " + version);
            assetsDir.toFile().mkdirs();

            JsonObject versionEntry = Objects.requireNonNull(VersionManifestClient
                            .getVersionEntry(version))
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

                futures.add(DownloadHelper.download(url, output, hash, pool, client));
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


}