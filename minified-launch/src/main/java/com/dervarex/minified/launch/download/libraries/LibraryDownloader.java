package com.dervarex.minified.launch.download.libraries;

import com.dervarex.minified.launch.events.download.libraries.DownloadLibrariesEvent;
import com.dervarex.minified.launch.launch.LaunchContext;
import com.dervarex.minified.launch.launch.modding.Loader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoaderFetcher;
import com.dervarex.minified.launch.launch.modding.forge.ForgeLoader;
import com.dervarex.minified.launch.launch.modding.neoforge.NeoforgeLoader;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoader;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoaderFetcher;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;
import com.dervarex.minified.utils.download.DownloadHelper;
import com.dervarex.minified.launch.utils.OSUtil;
import com.dervarex.minified.utils.version.VersionManifestClient;
import com.dervarex.minified.utils.exceptions.NoConnectionException;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;
import com.dervarex.minified.utils.network.NetworkUtil;
import com.dervarex.minified.utils.sha.Hasher;

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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
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

    public void downloadLibraries(Loader loader, Path librariesDir) {
        downloadLibraries(loader, librariesDir, progress -> {}, null);
    }

    public void downloadLibraries(Loader loader, Path librariesDir, LaunchContext context) {
        downloadLibraries(loader, librariesDir, progress -> {}, context);
    }

    /**
     * Downloads the libraries.
     *
     * @param loader the loader to use
     * @param librariesDir the directory the libraries should be downloaded to
     */
    public void downloadLibraries(Loader loader, Path librariesDir, Consumer<Double> progressConsumer, LaunchContext context) {
        try {
            boolean online = true;
            try {
                NetworkUtil.ensureOnline("downloading libraries for version " + loader.mcVersion());
            } catch (RuntimeException | NoConnectionException e) {
                online = false;
            }

            if (!online) {
                OfflineLibraryValidator.validate(loader.mcVersion(), loader, librariesDir);
                return;
            }

            librariesDir.toFile().mkdirs();

            Path nativesDir = resolveNativesDirectory(librariesDir);
            Files.createDirectories(nativesDir);

            Path nativeDownloadDir = nativesDir.resolve(".downloads");
            Files.createDirectories(nativeDownloadDir);

            JsonObject versionEntry = Objects.requireNonNull(
                    VersionManifestClient.getVersionEntry(loader.mcVersion())
            ).asObject();

            String versionJsonRaw = HttpUtil.get(versionEntry.get("url").asString());

            Path cacheRoot = resolveCacheRoot(librariesDir);
            Path versionCachePath = cacheRoot.resolve("versions").resolve(loader.mcVersion() + ".json");
            Files.createDirectories(versionCachePath.getParent());
            Files.writeString(versionCachePath, versionJsonRaw);

            JsonFile versionJson = new JsonFile(versionJsonRaw);

            List<Future<?>> futures = new ArrayList<>();
            List<NativeArchive> nativeArchives = new ArrayList<>();
            List<DownloadTarget> targets = new ArrayList<>();

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

                    long size = resolveSize(artifact, url);
                    targets.add(new DownloadTarget(url, path, sha1, size, true));
                }

                NativeDownload nativeDownload = resolveNativeDownload(library, downloads);
                if (nativeDownload != null) {
                    Path archivePath = nativeDownloadDownloadPath(nativeDownload, nativeDownloadDir);
                    targets.add(new DownloadTarget(
                            nativeDownload.url(),
                            archivePath,
                            nativeDownload.sha1(),
                            nativeDownload.size() > 0 ? nativeDownload.size() : resolveContentLength(nativeDownload.url()),
                            true
                    ));
                    nativeArchives.add(new NativeArchive(archivePath, library));
                }
            }

            switch (loader) {
                case VanillaLoader vanillaLoader:
                    // Nothing additional to download for vanilla
                    break;

                case FabricLoader fabricLoader:
                    JsonObject fabricProfile = FabricLoaderFetcher.getLatestProfile(loader.mcVersion());

                    Path fabricCachePath = resolveCacheRoot(librariesDir)
                            .resolve("profiles")
                            .resolve("fabric")
                            .resolve(loader.mcVersion() + ".json");

                    Files.createDirectories(fabricCachePath.getParent());
                    Files.writeString(fabricCachePath, fabricProfile.toString());

                    downloadModLoaderLibraries(
                            fabricProfile.get("libraries").asArray(),
                            librariesDir,
                            targets
                    );
                    break;

                case QuiltLoader quiltLoader:
                    JsonObject quiltProfile = QuiltLoaderFetcher.getLatestProfile(loader.mcVersion());

                    Path quiltCachePath = resolveCacheRoot(librariesDir)
                            .resolve("profiles")
                            .resolve("quilt")
                            .resolve(loader.mcVersion() + ".json");

                    Files.createDirectories(quiltCachePath.getParent());
                    Files.writeString(quiltCachePath, quiltProfile.toString());

                    downloadModLoaderLibraries(
                            quiltProfile.get("libraries").asArray(),
                            librariesDir,
                            targets
                    );
                    break;
                case NeoforgeLoader neoforgeLoader:
                    // Nothing additional to download for forge and neoforge, as the installer will handle it for us :)
                    break;
                case ForgeLoader forgeLoader:
                    // Nothing additional to download for forge and neoforge, as the installer will handle it for us :)
                    break;
                default:
                    throw new IllegalStateException("Unexpected loader: " + loader);
            }

            AtomicLong totalBytes = new AtomicLong();
            for (DownloadTarget target : targets) {
                totalBytes.addAndGet(Math.max(target.size(), 1L));
            }
            final long totalBytesFinal = Math.max(totalBytes.get(), 1L);

            AtomicLong downloadedBytes = new AtomicLong();

            for (DownloadTarget target : targets) {
                long targetSize = Math.max(target.size(), 1L);
                String currentFile = target.path().getFileName().toString();

                if (target.useSha1() && target.sha1() != null && isAlreadyDownloaded(target.path(), target.sha1())) {
                    long current = downloadedBytes.addAndGet(targetSize);
                    updateProgress(current, totalBytesFinal, currentFile, targetSize, targetSize, progressConsumer, context);
                    continue;
                }

                AtomicLong currentFileBytes = new AtomicLong();

                LongConsumer progressBytes = bytes -> {
                    long current = downloadedBytes.addAndGet(bytes);
                    long currentFileCurrent = currentFileBytes.addAndGet(bytes);

                    updateProgress(
                            current,
                            totalBytesFinal,
                            currentFile,
                            currentFileCurrent,
                            target.size(),
                            progressConsumer,
                            context
                    );
                };

                if (target.useSha1()) {
                    futures.add(DownloadHelper.download(
                            target.url(),
                            target.path(),
                            target.sha1(),
                            pool,
                            client,
                            progressBytes
                    ));
                } else {
                    futures.add(pool.submit(() -> {
                        downloadWithoutSha1(target.url(), target.path(), progressBytes);
                        return null;
                    }));
                }
            }

            // Wait for all downloads
            for (Future<?> future : futures) {
                future.get();
            }

            extractNativeArchives(nativeArchives, nativesDir);

            updateProgress(
                    totalBytesFinal,
                    totalBytesFinal,
                    targets.isEmpty() ? "done" : targets.get(targets.size() - 1).path().getFileName().toString(),
                    targets.isEmpty() ? 0L : Math.max(targets.get(targets.size() - 1).size(), 1L),
                    targets.isEmpty() ? 0L : targets.get(targets.size() - 1).size(),
                    progressConsumer,
                    context
            );
            //System.out.println("All libraries downloaded.");

        } catch (Exception e) {
            throw new RuntimeException("Failed to download libraries", e);
        } finally {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(1, TimeUnit.HOURS)) {
                    throw new RuntimeException("Download pool timeout");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Failed to shut down download pool", e);
            }
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
            context.getEventBus().post(new DownloadLibrariesEvent(
                    progress,
                    downloadedBytes,
                    totalBytes,
                    currentFile,
                    currentFileBytes,
                    currentFileSize
            ));
        }
    }

    /**
     * Download the Libraries for the selected modloader
     * @param libraries the libraries array, which can be obtained from the modloader's version JSON under the "libraries" key
     * @param librariesDir the directory the libraries should be downloaded to
     * @param targets The targets list to add the download tasks to, so they can be waited on later. This is used to run the modloader library downloads in parallel with the normal library downloads.
     */
    private void downloadModLoaderLibraries(
            JsonArray libraries,
            Path librariesDir,
            List<DownloadTarget> targets
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

            long size = resolveContentLength(url);
            targets.add(new DownloadTarget(url, path, null, size, false));
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

    private void downloadWithoutSha1(String url, Path path, LongConsumer progressConsumer) {
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
                throw new RuntimeException(
                        "Failed to download " + url + " (HTTP " + response.statusCode() + ")"
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
                    progressConsumer.accept(read);
                }
            }

            Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (Exception ignored) {
            }
            throw new RuntimeException("Failed to download Fabric library from " + url, e);
        }
    }

    private long resolveSize(JsonObject artifact, String url) {
        JsonValue sizeValue = artifact.get("size");

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
        JsonValue sizeValue = classifier.get("size");

        if (pathValue == null || urlValue == null || sha1Value == null) {
            return null;
        }

        long size = 0L;
        if (sizeValue != null) {
            try {
                size = Long.parseLong(sizeValue.asString());
            } catch (Exception ignored) {
            }
        }

        return new NativeDownload(
                pathValue.asString(),
                urlValue.asString(),
                sha1Value.asString(),
                size
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
        return OSUtil.getMinecraftOs();
    }

    private Path nativeDownloadDownloadPath(NativeDownload nativeDownload, Path nativeDownloadDir) {
        return nativeDownloadDir.resolve(nativeDownload.relativePath());
    }

    private Path resolveNativesDirectory(Path librariesDir) {
        Path parent = librariesDir.toAbsolutePath().getParent();
        if (parent == null) {
            return librariesDir.toAbsolutePath().resolve("natives");
        }
        return parent.resolve("jar").resolve("natives").toAbsolutePath();
    }

    private record DownloadTarget(String url, Path path, String sha1, long size, boolean useSha1) {
    }

    private record NativeDownload(String relativePath, String url, String sha1, long size) {
    }

    private record NativeArchive(Path archive, JsonObject library) {
    }

    private Path resolveCacheRoot(Path librariesDir) {
        Path parent = librariesDir.toAbsolutePath().getParent();
        return Objects.requireNonNullElseGet(parent, librariesDir::toAbsolutePath).resolve("cache");
    }
}