package com.dervarex.minified.java;

import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;

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
import java.util.Locale;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
    private static final String VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static Path baseDir = defaultBaseDir();

    private JavaManager() {
    }

    /**
     * Configures the root directory used for managed Java runtimes.
     *
     * @param baseDir the directory where managed runtimes will be stored
     */
    public static synchronized void init(Path baseDir) {
        Objects.requireNonNull(baseDir, "baseDir");
        JavaManager.baseDir = baseDir.toAbsolutePath();
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
        String versionJsonUrl = getVersionJsonUrl(minecraftVersion);
        if (versionJsonUrl == null) {
            return -1;
        }
        return getRequiredJavaVersion(new JsonFile(HttpUtil.get(versionJsonUrl)));
    }

    /**
     * Ensures that a suitable Java runtime is available for a Minecraft version.
     *
     * @param minecraftVersion the Minecraft version id, such as {@code 1.21.4}
     * @return the current JVM if it is new enough, otherwise a managed Java installation
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
     * @return the current JVM if it is new enough, otherwise a managed Java installation
     * @throws HttpException if the runtime manifest request fails
     * @throws IOException if a runtime download or extraction fails
     */
    public static JavaInstallation ensureJavaVersion(int requiredMajorVersion) throws HttpException, IOException {
        JavaInstallation current = currentRuntime();
        if (requiredMajorVersion <= 0 || current.majorVersion() >= requiredMajorVersion) {
            return current;
        }

        RuntimeAsset asset = resolveRuntimeAsset(requiredMajorVersion);
        Path installRoot = runtimeInstallRoot()
                .resolve(asset.platformId())
                .resolve(String.valueOf(requiredMajorVersion))
                .resolve(sanitizeSegment(asset.releaseName()));

        Path executable = locateExecutable(installRoot);
        if (Files.exists(executable)) {
            return new JavaInstallation(requiredMajorVersion, inferHome(executable), executable, true, asset.releaseName());
        }

        Files.createDirectories(installRoot);
        Path archive = Files.createTempFile(installRoot.getParent(), "java-runtime-", archiveSuffix(asset.packageName()));
        try {
            downloadArchive(asset.downloadUrl(), asset.checksum(), archive);
            extractArchive(archive, installRoot, asset.packageName());
            Path installedExecutable = locateExecutable(installRoot);
            if (!Files.exists(installedExecutable)) {
                throw new IOException("Downloaded Java runtime did not contain a java executable: " + installRoot);
            }
            return new JavaInstallation(requiredMajorVersion, inferHome(installedExecutable), installedExecutable, true, asset.releaseName());
        } finally {
            try {
                Files.deleteIfExists(archive);
            } catch (IOException ignored) {
            }
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

    private static RuntimeAsset resolveRuntimeAsset(int requiredMajorVersion) throws HttpException, IOException {
        for (String imageType : new String[]{"jre", "jdk"}) {
            String url = adoptiumAssetUrl(requiredMajorVersion, imageType);
            JsonArray assets = new JsonFile(HttpUtil.get(url)).asArray();
            if (assets == null || assets.size() == 0) {
                continue;
            }

            for (JsonValue assetValue : assets) {
                RuntimeAsset asset = parseRuntimeAsset(assetValue, requiredMajorVersion, imageType);
                if (asset != null) {
                    return asset;
                }
            }
        }

        throw new IOException("No supported Java runtime asset found for version " + requiredMajorVersion + " on " + platformId());
    }

    private static RuntimeAsset parseRuntimeAsset(JsonValue assetValue, int requiredMajorVersion, String expectedImageType) {
        if (assetValue == null || !assetValue.isObject()) {
            return null;
        }

        JsonObject assetObject = assetValue.asObject();
        JsonObject binary = assetObject.getObject("binary");
        JsonObject packageObject = binary == null ? null : binary.getObject("package");
        if (binary == null || packageObject == null) {
            return null;
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
            return null;
        }
        if (!platformArchitecture().equals(architecture)) {
            return null;
        }
        if (imageType == null || !expectedImageType.equalsIgnoreCase(imageType)) {
            return null;
        }
        if (jvmImpl != null && !"hotspot".equalsIgnoreCase(jvmImpl)) {
            return null;
        }
        if (downloadUrl == null || checksum == null || packageName == null) {
            return null;
        }
        if (releaseName == null || releaseName.isBlank()) {
            releaseName = "java-" + requiredMajorVersion;
        }

        return new RuntimeAsset(requiredMajorVersion, releaseName, downloadUrl, checksum, packageName, platformId());
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

        Path tempFile = Files.createTempFile(archive.getParent(), "java-runtime-download-", ".tmp");
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            try (InputStream in = response.body(); OutputStream out = Files.newOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                    digest.update(buffer, 0, read);
                }
            }

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
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(archive))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path target = resolveExtractionTarget(destination, entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(zipInputStream, target, StandardCopyOption.REPLACE_EXISTING);
                }
                zipInputStream.closeEntry();
            }
        }
    }

    private static void extractTarGz(Path archive, Path destination) throws IOException {
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(Files.newInputStream(archive))) {
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
            }
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

    private static Path resolveExecutable(Path home) {
        String executableName = isWindows() ? "java.exe" : "java";
        return home.resolve("bin").resolve(executableName).toAbsolutePath();
    }

    private static Path runtimeInstallRoot() {
        return getBaseDir().resolve("runtimes");
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

    private static String adoptiumAssetUrl(int majorVersion, String imageType) {
        return "https://api.adoptium.net/v3/assets/latest/%d/ga?architecture=%s&heap_size=normal&image_type=%s&jvm_impl=hotspot&os=%s&vendor=eclipse"
                .formatted(majorVersion, platformArchitecture(), imageType, platformOs());
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

