package com.dervarex.minified.java;

import com.dervarex.minified.events.EventBus;
import com.dervarex.minified.java.events.EnsureJavaVersionEvent;
import com.dervarex.minified.java.events.download.JavaArchiveDownloadEvent;
import com.dervarex.minified.java.events.extract.ArchiveType;
import com.dervarex.minified.java.events.extract.ExtractArchiveEvent;
import com.dervarex.minified.utils.ApiEndpoints;
import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Resolves, downloads, and caches Java runtimes for Minecraft launches.
 *
 * <p>This manager can inspect Minecraft version metadata, determine the required Java feature
 * version, and download a matching runtime when the current JVM is too old.</p>
 *
 * <p>The default runtime cache is placed under a user-specific application directory, but callers
 * may override it with {@link #init(Path)}.</p>
 */
@SuppressWarnings("unused")
public final class JavaManager {
    private static final String VERSION_MANIFEST_URL = ApiEndpoints.VERSION_MANIFEST_URL;

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static Path baseDir = defaultBaseDir();

    private static EventBus localEventBus;

    private JavaManager() {
    }

    /**
     * Configures the root directory used for managed Java runtimes.
     *
     * @param baseDir the directory where managed runtimes will be stored
     * @param eventBus the EventBus Events will be pushed to
     */
    public static synchronized void init(Path baseDir, EventBus eventBus) {
        Objects.requireNonNull(baseDir, "baseDir");
        JavaManager.baseDir = baseDir.toAbsolutePath();
        localEventBus = eventBus;
    }
    public static synchronized void init(Path baseDir) {
        Objects.requireNonNull(baseDir, "baseDir");
        JavaManager.baseDir = baseDir.toAbsolutePath();
        localEventBus = new EventBus();
    }

    /**
     * @return the configured root directory used for managed Java runtimes
     */
    public static synchronized Path getBaseDir() {
        return baseDir;
    }

    /**
     * Resolves the current JVM as a Java installation.
     *
     * @return the current runtime information
     */
    public static JavaInstallation currentRuntime() {
        Path home = Path.of(System.getProperty("java.home")).toAbsolutePath();
        Path executable = resolveExecutable(home);
        return new JavaInstallation(JavaPlatform.majorVersion(), home, executable, false, null);
    }

    /**
     * Reads the required Java major version from a Minecraft version JSON document.
     *
     * @param versionJson a parsed Minecraft version JSON
     * @return the required Java major version, or {@code -1} if the document does not contain one
     */
    public static int getRequiredJavaVersion(JsonValue versionJson) {
        if (versionJson == null || !versionJson.isObject()) {
            return -1;
        }
        JsonObject javaVersion = versionJson.asObject().getObject("javaVersion");
        if (javaVersion == null) {
            return -1;
        }
        Integer majorVersion = javaVersion.getInt("majorVersion");
        return majorVersion == null ? -1 : majorVersion;
    }

    /**
     * Reads the required Java major version from a Minecraft version JSON document.
     *
     * @param versionJson a parsed Minecraft version JSON file
     * @return the required Java major version, or {@code -1} if the document does not contain one
     */
    public static int getRequiredJavaVersion(JsonFile versionJson) {
        return versionJson == null ? -1 : getRequiredJavaVersion(versionJson.getRoot());
    }

    /**
     * Resolves the required Java version for the given Minecraft version id.
     *
     * @param minecraftVersion the Minecraft version id, such as {@code 1.21.4}
     * @return the required Java major version, or {@code -1} if it could not be resolved
     * @throws HttpException if a manifest request fails
     * @throws IOException if the version JSON cannot be read
     */
    public static int getRequiredJavaVersion(String minecraftVersion) throws HttpException, IOException {
        Path cachedPath = cachedVersionJsonPath(minecraftVersion);

        if (Files.exists(cachedPath)) {
            try {
                return getRequiredJavaVersion(new JsonFile(Files.readString(cachedPath)));
            } catch (Exception ignored) {
                // broken cache, fall through to network
            }
        }

        String versionJsonUrl = getVersionJsonUrl(minecraftVersion);
        if (versionJsonUrl == null) {
            return -1;
        }

        try {
            String raw = HttpUtil.get(versionJsonUrl);
            writeCache(cachedPath, raw);
            return getRequiredJavaVersion(new JsonFile(raw));
        } catch (Exception e) {
            if (Files.exists(cachedPath)) {
                try {
                    return getRequiredJavaVersion(new JsonFile(Files.readString(cachedPath)));
                } catch (Exception ignored) {
                    // continue to error below
                }
            }
            throw new com.dervarex.minified.utils.exceptions.OfflineModeNeedsNetworkException(
                    "Version metadata missing for Minecraft " + minecraftVersion
            );
        }
    }

    /**
     * Resolves the required Java version for the given Minecraft version id from a local cache file.
     *
     * @param versionJsonPath the cached Minecraft version JSON path
     * @return the required Java major version, or {@code -1} if it could not be resolved
     * @throws IOException if the file cannot be read
     */
    public static int getRequiredJavaVersion(Path versionJsonPath) throws IOException {
        if (versionJsonPath == null || !Files.exists(versionJsonPath)) {
            return -1;
        }
        try {
            return getRequiredJavaVersion(new JsonFile(Files.readString(versionJsonPath)));
        } catch (Exception e) {
            throw new IOException("Failed to read cached version JSON: " + versionJsonPath, e);
        }
    }

    /**
     * Ensures that a suitable Java runtime is available for a Minecraft version.
     *
     * @param minecraftVersion the Minecraft version id, such as {@code 1.21.4}
     * @return the current JVM if it matches the required feature version, otherwise a managed Java installation
     * @throws HttpException if a manifest request fails
     * @throws IOException if a runtime download or extraction fails
     */
    public static JavaInstallation ensureJavaForMinecraftVersion(String minecraftVersion) throws HttpException, IOException {
        int requiredJavaVersion = getRequiredJavaVersion(minecraftVersion);
        return ensureJavaVersion(requiredJavaVersion);
    }

    /**
     * Ensures that a suitable Java runtime is available for the given feature version.
     *
     * @param requiredMajorVersion the required Java feature version
     * @return the current JVM if it matches the required feature version, otherwise a managed Java installation
     * @throws HttpException if the runtime manifest request fails
     * @throws IOException if a runtime download or extraction fails
     */
    public static JavaInstallation ensureJavaVersion(int requiredMajorVersion) throws HttpException, IOException {
        JavaInstallation current = currentRuntime();
        if (requiredMajorVersion <= 0 || current.majorVersion() >= requiredMajorVersion) {
            return current;
        }
        localEventBus.post(new EnsureJavaVersionEvent(requiredMajorVersion));

        Path runtimeRoot = runtimeInstallRoot()
                .resolve(platformId())
                .resolve(String.valueOf(requiredMajorVersion));

        Path existingExecutable = locateExecutable(runtimeRoot);
        if (Files.exists(existingExecutable)) {
            ensureExecutableBit(existingExecutable);
            return new JavaInstallation(
                    requiredMajorVersion,
                    inferHome(existingExecutable),
                    existingExecutable,
                    true,
                    inferReleaseName(existingExecutable)
            );
        }

        RuntimeAsset asset = resolveRuntimeAsset(requiredMajorVersion);

        Path installRoot = runtimeRoot
                .resolve(sanitizeSegment(asset.releaseName()));

        Path executable = locateExecutable(installRoot);
        if (Files.exists(executable)) {
            ensureExecutableBit(executable);
            return new JavaInstallation(
                    requiredMajorVersion,
                    inferHome(executable),
                    executable,
                    true,
                    asset.releaseName()
            );
        }

        Files.createDirectories(installRoot);
        Path archive = Files.createTempFile(
                installRoot.getParent(),
                "java-runtime-",
                archiveSuffix(asset.packageName())
        );

        try {
            downloadArchive(asset.downloadUrl(), asset.checksum(), archive);
            extractArchive(archive, installRoot, asset.packageName());

            Path installedExecutable = locateExecutable(installRoot);
            if (!Files.exists(installedExecutable)) {
                throw new IOException(
                        "Downloaded Java runtime did not contain a java executable: " + installRoot
                );
            }

            ensureExecutableBit(installedExecutable);

            return new JavaInstallation(
                    requiredMajorVersion,
                    inferHome(installedExecutable),
                    installedExecutable,
                    true,
                    asset.releaseName()
            );
        } finally {
            try {
                Files.deleteIfExists(archive);
            } catch (IOException ignored) {
            }
        }
    }

    private static void ensureExecutableBit(Path executable) {
        if (isWindows()) {
            return;
        }

        try {
            Files.setPosixFilePermissions(
                    executable,
                    PosixFilePermissions.fromString("rwxr-xr-x")
            );
        } catch (Exception ignored) {
            executable.toFile().setExecutable(true, false);
        }
    }

    /**
     * Resolves the Java executable for a Minecraft version, downloading a compatible runtime if
     * the current JVM is too old.
     *
     * @param minecraftVersion the Minecraft version id
     * @return the Java executable to use for launching Minecraft
     * @throws HttpException if a manifest request fails
     * @throws IOException if a runtime download or extraction fails
     */
    public static Path ensureJavaExecutable(String minecraftVersion) throws HttpException, IOException {
        return ensureJavaForMinecraftVersion(minecraftVersion).executable();
    }

    /**
     * Resolves the Java executable for a feature version.
     *
     * @param requiredMajorVersion the required Java feature version
     * @return the Java executable to use for launching Minecraft
     * @throws HttpException if a runtime manifest request fails
     * @throws IOException if a runtime download or extraction fails
     */
    public static Path ensureJavaExecutable(int requiredMajorVersion) throws HttpException, IOException {
        return ensureJavaVersion(requiredMajorVersion).executable();
    }

    /**
     * Resolves the version JSON URL for a Minecraft version id.
     *
     * @param minecraftVersion the Minecraft version id
     * @return the Mojang metadata URL, or {@code null} if the version was not found
     * @throws HttpException if the version manifest request fails
     * @throws IOException if the manifest cannot be read
     */
    public static String getVersionJsonUrl(String minecraftVersion) throws HttpException, IOException {
        JsonFile manifest = new JsonFile(HttpUtil.get(VERSION_MANIFEST_URL));
        JsonArray versions = manifest.getArray("versions");
        if (versions == null) {
            return null;
        }

        for (JsonValue value : versions) {
            JsonObject version = value.asObject();
            String id = version.getString("id");
            if (minecraftVersion.equals(id)) {
                return version.getString("url");
            }
        }
        return null;
    }

    private static RuntimeAsset resolveRuntimeAsset(int requiredMajorVersion)
            throws IOException {

        for (String imageType : new String[]{"jre", "jdk"}) {
            JsonArray assets = loadAdoptiumAssets(requiredMajorVersion, imageType);

            if (assets == null || assets.size() == 0) {
                continue;
            }

            for (JsonValue assetValue : assets) {
                RuntimeAsset asset = parseRuntimeAsset(
                        assetValue,
                        requiredMajorVersion,
                        imageType
                );

                if (asset != null) {
                    return asset;
                }
            }
        }

        throw new IOException(
                "No supported Java runtime asset found for version "
                        + requiredMajorVersion
                        + " on "
                        + platformId()
        );
    }

    private static JsonArray loadAdoptiumAssets(int majorVersion, String imageType) throws IOException {
        Path cachePath = cachedAdoptiumAssetsPath(majorVersion, imageType);

        if (Files.exists(cachePath)) {
            try {
                JsonArray cached = new JsonFile(Files.readString(cachePath)).asArray();
                if (cached != null && cached.size() > 0) {
                    return cached;
                }
            } catch (Exception ignored) {
                // broken cache, fall through to network
            }
        }

        String url = adoptiumAssetUrl(majorVersion, imageType);

        try {
            String raw = HttpUtil.get(url);
            writeCache(cachePath, raw);

            JsonArray assets = new JsonFile(raw).asArray();
            if (assets != null && assets.size() > 0) {
                return assets;
            }

            return assets;
        } catch (HttpException e) {
            if (Files.exists(cachePath)) {
                try {
                    return new JsonFile(Files.readString(cachePath)).asArray();
                } catch (Exception ignored) {
                    // continue below
                }
            }

            if (e.getStatusCode() == 404) {
                return null;
            }

            throw new com.dervarex.minified.utils.exceptions.OfflineModeNeedsNetworkException(
                    "Java runtime metadata missing for Java "
                            + majorVersion
                            + " ("
                            + imageType
                            + ")"
            );
        } catch (IOException e) {
            if (Files.exists(cachePath)) {
                try {
                    return new JsonFile(Files.readString(cachePath)).asArray();
                } catch (Exception ignored) {
                    // continue below
                }
            }

            throw new com.dervarex.minified.utils.exceptions.OfflineModeNeedsNetworkException(
                    "Java runtime metadata missing for Java "
                            + majorVersion
                            + " ("
                            + imageType
                            + ")"
            );
        }
    }

    private static RuntimeAsset parseRuntimeAsset(
            JsonValue assetValue,
            int requiredMajorVersion,
            String expectedImageType
    ) {
        if (assetValue == null || !assetValue.isObject()) {
            return null;
        }

        JsonObject assetObject = assetValue.asObject();

        JsonArray binaries = assetObject.getArray("binaries");
        if (binaries == null || binaries.size() == 0) {
            return null;
        }

        for (JsonValue binaryValue : binaries) {
            if (!binaryValue.isObject()) {
                continue;
            }

            JsonObject binary = binaryValue.asObject();
            JsonObject packageObject = binary.getObject("package");

            if (packageObject == null) {
                continue;
            }

            String os = binary.getString("os");
            String architecture = binary.getString("architecture");
            String imageType = binary.getString("image_type");
            String jvmImpl = binary.getString("jvm_impl");

            String downloadUrl = packageObject.getString("link");
            String checksum = packageObject.getString("checksum");
            String packageName = packageObject.getString("name");

            String releaseName = assetObject.getString("release_name");

            if (!platformOs().equals(os)) {
                continue;
            }

            if (!platformArchitecture().equals(architecture)) {
                continue;
            }

            if (imageType == null
                    || !expectedImageType.equalsIgnoreCase(imageType)) {
                continue;
            }

            if (jvmImpl != null
                    && !"hotspot".equalsIgnoreCase(jvmImpl)) {
                continue;
            }

            if (downloadUrl == null
                    || checksum == null
                    || packageName == null) {
                continue;
            }

            if (releaseName == null || releaseName.isBlank()) {
                releaseName = "java-" + requiredMajorVersion;
            }

            return new RuntimeAsset(
                    requiredMajorVersion,
                    releaseName,
                    downloadUrl,
                    checksum,
                    packageName,
                    platformId()
            );
        }

        return null;
    }

    private static void downloadArchive(String url, String expectedChecksum, Path archive) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<InputStream> response;
        try {
            response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while downloading Java runtime", e);
        }
        if (response.statusCode() != 200) {
            throw new IOException("Failed to download Java runtime: HTTP " + response.statusCode() + " from " + url);
        }

        long totalBytes = response.headers().firstValueAsLong("Content-Length").orElse(-1L);

        Path tempFile = Files.createTempFile(archive.getParent(), "java-runtime-download-", ".tmp");
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            long downloadedBytes = 0;
            long lastEventTime = System.currentTimeMillis();

            try (InputStream in = response.body(); OutputStream out = Files.newOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                    digest.update(buffer, 0, read);
                    downloadedBytes += read;

                    long now = System.currentTimeMillis();
                    if (now - lastEventTime >= 100) {
                        double progress = totalBytes > 0 ? (double) downloadedBytes / totalBytes : -1.0;
                        localEventBus.post(new JavaArchiveDownloadEvent(progress, downloadedBytes, totalBytes, url));
                        lastEventTime = now;
                    }
                }
            }

            // final 100% event to ensure the listener notices the finish
            double finalProgress = totalBytes > 0 ? 1.0 : -1.0;
            localEventBus.post(new JavaArchiveDownloadEvent(finalProgress, downloadedBytes, totalBytes, url));

            String actualChecksum = bytesToHex(digest.digest());
            if (!actualChecksum.equalsIgnoreCase(expectedChecksum)) {
                throw new IOException("Checksum mismatch for Java runtime download. Expected "
                        + expectedChecksum + " but got " + actualChecksum);
            }
            Files.move(tempFile, archive, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
            }
            if (e instanceof IOException ioException) {
                throw ioException;
            }
            throw new IOException("Failed to download Java runtime", e);
        }
    }

    private static void extractArchive(Path archive, Path destination, String packageName) throws IOException {
        if (packageName.endsWith(".zip")) {
            extractZip(archive, destination);
            return;
        }
        if (packageName.endsWith(".tar.gz") || packageName.endsWith(".tgz")) {
            extractTarGz(archive, destination);
            return;
        }
        throw new IOException("Unsupported Java runtime archive format: " + packageName);
    }

    private static void extractZip(Path archive, Path destination) throws IOException {
        try (ZipFile zipFile = new ZipFile(archive.toFile())) {
            int totalEntries = zipFile.size();
            int processedEntries = 0;
            long lastEventTime = System.currentTimeMillis();

            localEventBus.post(new ExtractArchiveEvent(ArchiveType.zip, 0));

            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path target = resolveExtractionTarget(destination, entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    try (InputStream in = zipFile.getInputStream(entry)) {
                        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                processedEntries++;

                long now = System.currentTimeMillis();
                if (now - lastEventTime >= 100 || processedEntries == totalEntries) {
                    int progress = totalEntries > 0
                            ? (int) ((processedEntries * 100L) / totalEntries)
                            : 100;
                    localEventBus.post(new ExtractArchiveEvent(ArchiveType.zip, progress));
                    lastEventTime = now;
                }
            }
        }
    }

    private static void extractTarGz(Path archive, Path destination) throws IOException {
        long totalCompressedBytes = Files.size(archive);
        long lastEventTime = System.currentTimeMillis();

        try (CountingInputStream countingIn = new CountingInputStream(Files.newInputStream(archive));
             GZIPInputStream gzipInputStream = new GZIPInputStream(countingIn)) {

            localEventBus.post(new ExtractArchiveEvent(ArchiveType.tarGz, 0));

            byte[] header = new byte[512];
            while (true) {
                int read = readFully(gzipInputStream, header);
                if (read == -1 || isEmptyBlock(header)) {
                    break;
                }
                String name = readTarString(header, 0, 100);
                String prefix = readTarString(header, 345, 155);
                if (!prefix.isBlank()) {
                    name = prefix + "/" + name;
                }
                long size = readTarSize(header, 124, 12);
                char typeFlag = (char) header[156];
                Path target = resolveExtractionTarget(destination, name);
                if (typeFlag == '5') {
                    Files.createDirectories(target);
                } else if (typeFlag == '0' || typeFlag == '\0' || typeFlag == 0) {
                    Files.createDirectories(target.getParent());
                    try (OutputStream out = Files.newOutputStream(target)) {
                        copyFixedSize(gzipInputStream, out, size);
                    }
                } else {
                    skipFully(gzipInputStream, size);
                }
                skipFully(gzipInputStream, padding(size));

                long now = System.currentTimeMillis();
                if (now - lastEventTime >= 100) {
                    int progress = totalCompressedBytes > 0
                            ? (int) Math.min(99, (countingIn.getCount() * 100L) / totalCompressedBytes)
                            : -1;
                    localEventBus.post(new ExtractArchiveEvent(ArchiveType.tarGz, progress));
                    lastEventTime = now;
                }
            }

            localEventBus.post(new ExtractArchiveEvent(ArchiveType.tarGz, 100));
        }
    }

    /** Counts read bytes of the Stream (for progress calculation) */
    private static final class CountingInputStream extends FilterInputStream {
        private long count = 0;

        CountingInputStream(InputStream in) {
            super(in);
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            if (b != -1) count++;
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int n = super.read(b, off, len);
            if (n != -1) count += n;
            return n;
        }

        long getCount() {
            return count;
        }
    }

    private static JavaInstallation currentOrManaged(Path executableRoot, int majorVersion, boolean managed, String releaseName) {
        Path executable = locateExecutable(executableRoot);
        return new JavaInstallation(majorVersion, inferHome(executable), executable, managed, releaseName);
    }

    private static Path locateExecutable(Path root) {
        Path direct = resolveExecutable(root);
        if (Files.exists(direct)) {
            return direct;
        }

        if (Files.exists(root)) {
            try (var stream = Files.walk(root)) {
                return stream
                        .filter(path -> Files.isRegularFile(path) && isJavaExecutable(path))
                        .findFirst()
                        .orElse(direct);
            } catch (IOException ignored) {
            }
        }

        return direct;
    }

    private static Path inferHome(Path executable) {
        Path parent = executable.getParent();
        if (parent != null && parent.getFileName() != null && "bin".equalsIgnoreCase(parent.getFileName().toString())) {
            Path home = parent.getParent();
            if (home != null) {
                return home.toAbsolutePath();
            }
        }
        return executable.getParent() == null ? executable.toAbsolutePath() : executable.getParent().toAbsolutePath();
    }

    private static String inferReleaseName(Path executable) {
        Path parent = executable.getParent();
        if (parent != null) {
            Path home = parent.getParent();
            if (home != null && home.getFileName() != null) {
                return home.getFileName().toString();
            }
        }
        return "cached";
    }

    private static Path resolveExecutable(Path home) {
        String executableName = isWindows() ? "java.exe" : "java";
        return home.resolve("bin").resolve(executableName).toAbsolutePath();
    }

    private static Path runtimeInstallRoot() {
        return getBaseDir().resolve("runtimes");
    }

    private static Path cacheRoot() {
        return getBaseDir().resolve("cache");
    }

    private static Path cachedVersionJsonPath(String minecraftVersion) {
        return cacheRoot()
                .resolve("versions")
                .resolve(minecraftVersion + ".json");
    }

    private static Path cachedAdoptiumAssetsPath(int majorVersion, String imageType) {
        return cacheRoot()
                .resolve("java")
                .resolve("adoptium")
                .resolve(String.valueOf(majorVersion))
                .resolve(imageType + ".json");
    }

    private static void writeCache(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }

    private static String platformId() {
        return platformOs() + "-" + platformArchitecture();
    }

    private static String platformOs() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return "windows";
        }
        if (os.contains("mac") || os.contains("darwin")) {
            return "mac";
        }
        return "linux";
    }

    private static String platformArchitecture() {
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        if (arch.contains("aarch64") || arch.contains("arm64")) {
            return "aarch64";
        }
        if (arch.contains("86") || arch.contains("amd64") || arch.contains("x64")) {
            return "x64";
        }
        return arch.isBlank() ? "x64" : arch;
    }

//    private static String adoptiumAssetUrl(int majorVersion, String imageType) {
//        return "https://api.adoptium.net/v3/assets/latest/%d/ga?architecture=%s&heap_size=normal&image_type=%s&jvm_impl=hotspot&os=%s&vendor=eclipse"
//                .formatted(majorVersion, platformArchitecture(), imageType, platformOs());
//    }

    private static String adoptiumAssetUrl(int majorVersion, String imageType) {
        return String.format(Locale.ROOT, ApiEndpoints.ADOPTIUM_ASSET_URL_TEMPLATE,
                majorVersion, platformArchitecture(), imageType, platformOs());
    }

    private static Path defaultBaseDir() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.isBlank()) {
                return Path.of(appData, "Minified", "java").toAbsolutePath();
            }
            return Path.of(System.getProperty("user.home"), "AppData", "Roaming", "Minified", "java").toAbsolutePath();
        }
        if (os.contains("mac") || os.contains("darwin")) {
            return Path.of(System.getProperty("user.home"), "Library", "Application Support", "Minified", "java").toAbsolutePath();
        }
        return Path.of(System.getProperty("user.home"), ".local", "share", "Minified", "java").toAbsolutePath();
    }

    private static String sanitizeSegment(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        StringBuilder builder = new StringBuilder();
        for (char c : value.toCharArray()) {
            if (Character.isLetterOrDigit(c) || c == '.' || c == '-' || c == '_') {
                builder.append(c);
            } else {
                builder.append('_');
            }
        }
        return builder.toString();
    }

    private static String archiveSuffix(String packageName) {
        if (packageName.endsWith(".tar.gz") || packageName.endsWith(".tgz")) {
            return ".tar.gz";
        }
        int dot = packageName.lastIndexOf('.');
        return dot >= 0 ? packageName.substring(dot) : ".bin";
    }

    private static Path resolveExtractionTarget(Path destination, String entryName) throws IOException {
        Path target = destination.resolve(entryName).normalize();
        if (!target.startsWith(destination.normalize())) {
            throw new IOException("Blocked path traversal entry in archive: " + entryName);
        }
        return target;
    }

    private static boolean isJavaExecutable(Path path) {
        String fileName = path.getFileName() == null ? "" : path.getFileName().toString().toLowerCase(Locale.ROOT);
        return fileName.equals("java") || fileName.equals("java.exe");
    }

    private static boolean isWindows() {
        return platformOs().equals("windows");
    }

    private static int readFully(InputStream inputStream, byte[] buffer) throws IOException {
        int offset = 0;
        while (offset < buffer.length) {
            int read = inputStream.read(buffer, offset, buffer.length - offset);
            if (read == -1) {
                return offset == 0 ? -1 : offset;
            }
            offset += read;
        }
        return offset;
    }

    private static boolean isEmptyBlock(byte[] block) {
        for (byte b : block) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    private static String readTarString(byte[] header, int offset, int length) {
        int end = offset;
        int limit = Math.min(header.length, offset + length);
        while (end < limit && header[end] != 0) {
            end++;
        }
        return new String(header, offset, end - offset).trim();
    }

    private static long readTarSize(byte[] header, int offset, int length) {
        String value = readTarString(header, offset, length).trim();
        if (value.isEmpty()) {
            return 0L;
        }
        return Long.parseLong(value, 8);
    }

    private static long padding(long size) {
        long remainder = size % 512L;
        return remainder == 0L ? 0L : 512L - remainder;
    }

    private static void copyFixedSize(InputStream inputStream, OutputStream outputStream, long size) throws IOException {
        byte[] buffer = new byte[8192];
        long remaining = size;
        while (remaining > 0) {
            int read = inputStream.read(buffer, 0, (int) Math.min(buffer.length, remaining));
            if (read == -1) {
                throw new IOException("Unexpected end of tar archive");
            }
            outputStream.write(buffer, 0, read);
            remaining -= read;
        }
    }

    private static void skipFully(InputStream inputStream, long size) throws IOException {
        long remaining = size;
        while (remaining > 0) {
            long skipped = inputStream.skip(remaining);
            if (skipped <= 0) {
                if (inputStream.read() == -1) {
                    break;
                }
                skipped = 1;
            }
            remaining -= skipped;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        final char[] hex = "0123456789abcdef".toCharArray();
        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xFF;
            chars[i * 2] = hex[value >>> 4];
            chars[i * 2 + 1] = hex[value & 0x0F];
        }
        return new String(chars);
    }

    private record RuntimeAsset(int majorVersion, String releaseName, String downloadUrl, String checksum, String packageName, String platformId) {
    }
}