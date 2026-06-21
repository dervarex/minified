package com.dervarex.minified.launch.download.libraries;

import com.dervarex.minified.launch.launch.modding.Loader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoaderFetcher;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoaderFetcher;
import com.dervarex.minified.launch.utils.DownloadHelper;
import com.dervarex.minified.launch.version.VersionManifestClient;
import com.dervarex.minified.utils.exceptions.NoConnectionException;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;
import com.dervarex.minified.utils.network.NetworkUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
        boolean online = true;
        try {
            NetworkUtil.ensureOnline("downloading libraries for version " + version);
        } catch (RuntimeException | NoConnectionException e) {
            online = false;
        }

        if (!online) {
            OfflineLibraryValidator.validate(version, loader, librariesDir);
            return;
        }
        try {
            librariesDir.toFile().mkdirs();

            Path nativesDir = resolveNativesDirectory(librariesDir);
            Files.createDirectories(nativesDir);

            Path nativeDownloadDir = nativesDir.resolve(".downloads");
            Files.createDirectories(nativeDownloadDir);

            JsonObject versionEntry = Objects.requireNonNull(
                    VersionManifestClient.getVersionEntry(version)
            ).asObject();

            String versionJsonRaw = HttpUtil.get(versionEntry.get("url").asString());

            Path cacheRoot = resolveCacheRoot(librariesDir);
            Path versionCachePath = cacheRoot.resolve("versions").resolve(version + ".json");
            Files.createDirectories(versionCachePath.getParent());
            Files.writeString(versionCachePath, versionJsonRaw);

            JsonFile versionJson = new JsonFile(versionJsonRaw);

            List<Future<?>> futures = new ArrayList<>();
            List<NativeArchive> nativeArchives = new ArrayList<>();

            JsonArray libraries = versionJson.get("libraries").asArray();

            for (JsonValue libraryValue : libraries) {
                JsonObject library = libraryValue.asObject();

                // Skip unsupported OS rules
                if (!isAllowed(library)) {
                    continue;
                }

                JsonValue downloadsValue = library.get("downloads");
                if (downloadsValue == null) {
                    continue;
                }

                JsonObject downloads = downloadsValue.asObject();

                JsonValue artifactValue = downloads.get("artifact");
                if (artifactValue != null) {
                    JsonObject artifact = artifactValue.asObject();

                    String url = artifact.get("url").asString();
                    String sha1 = artifact.get("sha1").asString();

                    Path path = Path.of(
                            librariesDir.toAbsolutePath().toString(),
                            artifact.get("path").asString()
                    );

                    futures.add(DownloadHelper.download(url, path, sha1, pool, client));
                }

                NativeDownload nativeDownload = resolveNativeDownload(library, downloads);
                if (nativeDownload != null) {
                    Path archivePath = nativeDownloadDir.resolve(nativeDownload.relativePath());
                    futures.add(DownloadHelper.download(
                            nativeDownload.url(),
                            archivePath,
                            nativeDownload.sha1(),
                            pool,
                            client
                    ));
                    nativeArchives.add(new NativeArchive(archivePath, library));
                }
            }

            switch (loader) {
                case Vanilla:
                    // Nothing additional to download for vanilla
                    break;

                case Fabric:
                    JsonObject fabricProfile = FabricLoaderFetcher.getLatestProfile(version);

                    Path fabricCachePath = resolveCacheRoot(librariesDir)
                            .resolve("profiles")
                            .resolve("fabric")
                            .resolve(version + ".json");

                    Files.createDirectories(fabricCachePath.getParent());
                    Files.writeString(fabricCachePath, fabricProfile.toString());

                    downloadModLoaderLibraries(
                            fabricProfile.get("libraries").asArray(),
                            librariesDir,
                            futures
                    );
                    break;

                case Quilt:
                    JsonObject quiltProfile = QuiltLoaderFetcher.getLatestProfile(version);

                    Path quiltCachePath = resolveCacheRoot(librariesDir)
                            .resolve("profiles")
                            .resolve("quilt")
                            .resolve(version + ".json");

                    Files.createDirectories(quiltCachePath.getParent());
                    Files.writeString(quiltCachePath, quiltProfile.toString());

                    downloadModLoaderLibraries(
                            quiltProfile.get("libraries").asArray(),
                            librariesDir,
                            futures
                    );
                    break;
                case Forge:
                case NeoForge:
                    // Nothing additional to download for forge and neoforge, as the installer will handle it for us :)
                    break;
            }

            // Wait for all downloads
            for (Future<?> future : futures) {
                future.get();
            }

            extractNativeArchives(nativeArchives, nativesDir);

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
     * @param libraries the libraries array, which can be obtained from the modloader's version Json under the "libraries" key
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
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to download Fabric library from " + url, e);
        }
    }

    private void extractNativeArchives(List<NativeArchive> nativeArchives, Path nativesDir) {
        for (NativeArchive nativeArchive : nativeArchives) {
            extractNativeArchive(nativeArchive.archive(), nativesDir, nativeArchive.library());
        }
    }

    private void extractNativeArchive(Path archive, Path nativesDir, JsonObject library) {
        try {
            JsonArray excludes = null;
            JsonValue extractValue = library.get("extract");
            if (extractValue != null) {
                JsonObject extractObject = extractValue.asObject();
                JsonValue excludeValue = extractObject.get("exclude");
                if (excludeValue != null) {
                    excludes = excludeValue.asArray();
                }
            }

            try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(archive))) {
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    if (entry.isDirectory()) {
                        zipInputStream.closeEntry();
                        continue;
                    }

                    String entryName = entry.getName();
                    if (isExcluded(entryName, excludes)) {
                        zipInputStream.closeEntry();
                        continue;
                    }

                    Path target = resolveExtractionTarget(nativesDir, entryName);
                    Files.createDirectories(target.getParent());
                    Files.copy(zipInputStream, target, StandardCopyOption.REPLACE_EXISTING);
                    zipInputStream.closeEntry();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract native archive " + archive, e);
        }
    }

    private boolean isExcluded(String entryName, JsonArray excludes) {
        if (excludes == null) {
            return false;
        }

        for (JsonValue excludeValue : excludes) {
            String exclude = excludeValue.asString();
            if (entryName.startsWith(exclude)) {
                return true;
            }
        }

        return false;
    }

    private Path resolveExtractionTarget(Path destination, String entryName) throws IOException {
        Path target = destination.resolve(entryName).normalize();
        if (!target.startsWith(destination.normalize())) {
            throw new IOException("Blocked path traversal entry in archive: " + entryName);
        }
        return target;
    }

    private NativeDownload resolveNativeDownload(JsonObject library, JsonObject downloads) {
        JsonValue nativesValue = library.get("natives");
        JsonValue classifiersValue = downloads.get("classifiers");

        if (nativesValue == null || classifiersValue == null) {
            return null;
        }

        JsonObject natives = nativesValue.asObject();
        String os = getMinecraftOs();
        JsonValue classifierNameValue = natives.get(os);

        if (classifierNameValue == null) {
            return null;
        }

        String classifierName = classifierNameValue.asString();
        JsonObject classifiers = classifiersValue.asObject();
        JsonValue classifierValue = classifiers.get(classifierName);

        if (classifierValue == null) {
            return null;
        }

        JsonObject classifier = classifierValue.asObject();
        JsonValue pathValue = classifier.get("path");
        JsonValue urlValue = classifier.get("url");
        JsonValue sha1Value = classifier.get("sha1");

        if (pathValue == null || urlValue == null || sha1Value == null) {
            return null;
        }

        return new NativeDownload(
                pathValue.asString(),
                urlValue.asString(),
                sha1Value.asString()
        );
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

    private Path resolveNativesDirectory(Path librariesDir) {
        Path parent = librariesDir.toAbsolutePath().getParent();
        if (parent == null) {
            return librariesDir.toAbsolutePath().resolve("natives");
        }
        return parent.resolve("jar").resolve("natives").toAbsolutePath();
    }

    private record NativeDownload(String relativePath, String url, String sha1) {
    }

    private record NativeArchive(Path archive, JsonObject library) {
    }
    private Path resolveCacheRoot(Path librariesDir) {
        Path parent = librariesDir.toAbsolutePath().getParent();
        if (parent == null) {
            return librariesDir.toAbsolutePath().resolve("cache");
        }
        return parent.resolve("cache");
    }
}