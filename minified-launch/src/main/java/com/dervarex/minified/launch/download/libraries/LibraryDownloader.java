package com.dervarex.minified.launch.download.libraries;

import com.dervarex.minified.launch.utils.DownloadHelper;
import com.dervarex.minified.launch.version.VersionManifestClient;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;
import com.dervarex.minified.utils.network.NetworkUtil;

import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class LibraryDownloader {

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
     * @param version the game version
     * @param librariesPath the directory the libraries should be downloaded to
     */
    public void downloadLibraries(String version, String librariesPath) {
        try {
            NetworkUtil.ensureOnline("downloading libraries for version " + version);

            JsonObject versionEntry = Objects.requireNonNull(VersionManifestClient
                            .getVersionEntry(version))
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

                futures.add(DownloadHelper.download(url, path, sha1, pool, client));
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
}