package com.dervarex.minified.launch.download.libraries;

import com.dervarex.minified.launch.launch.modding.Loader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoaderFetcher;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoaderFetcher;
import com.dervarex.minified.launch.utils.DownloadHelper;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
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
     * Downloads the libraries.
     *
     * @param version the game version
     * @param loader the loader to use
     * @param librariesDir the directory the libraries should be downloaded to
     */
    public void downloadLibraries(String version, Loader loader, Path librariesDir) {
        try {
            NetworkUtil.ensureOnline("downloading libraries for version " + version);
            librariesDir.toFile().mkdirs();

            JsonObject versionEntry = Objects.requireNonNull(
                    VersionManifestClient.getVersionEntry(version)
            ).asObject();

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

                JsonObject artifact = downloads.get("artifact").asObject();

                String url = artifact.get("url").asString();
                String sha1 = artifact.get("sha1").asString();

                Path path = Path.of(
                        librariesDir.toAbsolutePath().toString(),
                        artifact.get("path").asString()
                );

                futures.add(DownloadHelper.download(url, path, sha1, pool, client));
            }

            switch (loader) {
                case Vanilla:
                    // Nothing additional to download for vanilla
                    break;

                case Fabric:
                    downloadModLoaderLibraries(
                            FabricLoaderFetcher.getLatestProfile(version).get("libraries").asArray(),
                            librariesDir,
                            futures
                    );
                    break;

                case Quilt:
                    downloadModLoaderLibraries(
                            QuiltLoaderFetcher.getLatestProfile(version).get("libraries").asArray(),
                            librariesDir,
                            futures
                    );
                    break;
                case Forge:
                    // Nothing additional to download for forge, as the installer will handle it for us :)
                    break;
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

    /**
     * Download the Libraries for the selected modloader
     * @param libraries the libraries array, which can be obtained from the modloader's version json under the "libraries" key
     * @param librariesDir the directory the libraries should be downloaded to
     * @param futures The futures list to add the download tasks to, so they can be waited on later. This is used to run the modloader library downloads in parallel with the normal library downloads.
     */
    private void downloadModLoaderLibraries(
            JsonArray libraries,
            Path librariesDir,
            List<Future<?>> futures
    ) {
        for (JsonValue libraryValue : libraries) {
            JsonObject library = libraryValue.asObject();

            // Skip unsupported OS rules
            if (!isAllowed(library)) {
                continue;
            }

            JsonValue nameValue = library.get("name");
            JsonValue urlValue = library.get("url");

            if (nameValue == null || urlValue == null) {
                continue;
            }

            String[] parts = nameValue.asString().split(":");

            if (parts.length != 3) {
                continue;
            }

            String artifactPath = getArtifactPath(parts);

            String baseUrl = urlValue.asString();
            if (!baseUrl.endsWith("/")) {
                baseUrl += "/";
            }

            String url = baseUrl + artifactPath;
            Path path = librariesDir.resolve(artifactPath);

            futures.add(
                    pool.submit(() -> {
                        downloadWithoutSha1(url, path);
                        return null;
                    })
            );
        }
    }

    /**
     * @param parts the parts of the library name, split by ":", e.g. ["net.fabricmc", "fabric-loader", "0.14.19"]
     * @return the path to the specified artifact, e.g. "net/fabricmc/fabric-loader/0.14.19/fabric-loader-0.14.19.jar"
     */
    private static String getArtifactPath(String[] parts) {
        String groupId = parts[0];
        String artifactId = parts[1];
        String version = parts[2];

        return groupId.replace('.', '/')
                + "/"
                + artifactId
                + "/"
                + version
                + "/"
                + artifactId
                + "-"
                + version
                + ".jar";
    }

    private void downloadWithoutSha1(String url, Path path) {
        try {
            if (Files.exists(path) && Files.size(path) > 0) {
                return;
            }

            Files.createDirectories(path.getParent());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<InputStream> response =
                    client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Failed to download " + url + " (HTTP " + response.statusCode() + ")"
                );
            }

            try (InputStream in = response.body()) {
                Files.copy(
                        in,
                        path,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to download Fabric library from " + url, e);
        }
    }

    /**
     * Determines if the library is allowed on the users operating system
     * @param library the JsonObject of the library
     * @return true if the library is allowed to be downloaded on the users operating system, false otherwise
     */
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

    /**
     * @return the operating system of the user, in the format that Minecraft uses for library rules
     */
    private String getMinecraftOs() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return "windows";
        }

        if (os.contains("mac") || os.contains("darwin")) {
            return "osx";
        }

        if (os.contains("linux")
                // || os.contains("bsd")
                || os.contains("unix")) {
            return "linux";
        }

        return "unknown";
    }
}