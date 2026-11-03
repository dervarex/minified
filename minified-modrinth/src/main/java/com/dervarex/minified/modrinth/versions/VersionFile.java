package com.dervarex.minified.modrinth.versions;

import com.dervarex.minified.events.Event;
import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.events.DownloadFinishedEvent;
import com.dervarex.minified.modrinth.events.DownloadProgressEvent;
import com.dervarex.minified.modrinth.events.DownloadStartedEvent;
import com.dervarex.minified.modrinth.events.OutdatedFileReplacedEvent;
import com.dervarex.minified.modrinth.events.UnverifiedDownloadEvent;
import com.dervarex.minified.modrinth.exceptions.ModrinthDownloadException;
import com.dervarex.minified.modrinth.exceptions.ModrinthHashMismatchException;
import com.dervarex.minified.modrinth.internal.ModrinthJson;
import com.dervarex.minified.utils.json.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;

/**
 * A file of a version, with its hashes. Get it from {@link Version#getFiles()}.
 */
public final class VersionFile {
    private final Modrinth modrinth;
    private final String url;
    private final String filename;
    private final boolean primary;
    private final long size;
    private final String fileType;
    private final Map<String, String> hashes;

    VersionFile(Modrinth modrinth, JsonObject json) {
        this.modrinth = modrinth;
        url = ModrinthJson.string(json, "url");
        filename = ModrinthJson.string(json, "filename");
        primary = ModrinthJson.bool(json, "primary");
        size = ModrinthJson.number(json, "size");
        fileType = ModrinthJson.string(json, "file_type");
        hashes = ModrinthJson.stringMap(json, "hashes");
    }

    /** {@return the download URL} */
    public String getUrl() {
        return url;
    }

    /** {@return the file name, like {@code sodium-fabric-0.8.0.jar}} */
    public String getFilename() {
        return filename;
    }

    /** {@return whether this is the main file of the version} */
    public boolean isPrimary() {
        return primary;
    }

    /** {@return the size in bytes} */
    public long getSize() {
        return size;
    }

    /** {@return the kind of extra file, like {@code required-resource-pack}, {@code null} for normal files} */
    public String getFileType() {
        return fileType;
    }

    /** {@return the hashes of the file by algorithm, like {@code sha1} and {@code sha512}} */
    public Map<String, String> getHashes() {
        return hashes;
    }

    /**
     * Downloads the file into a directory and checks it against its hash.
     *
     * @param directory where to put the file, created if it doesn't exist
     * @return the downloaded file
     * @throws ModrinthDownloadException if the download fails
     * @throws ModrinthHashMismatchException if the downloaded file doesn't match its hash
     */
    public Path download(Path directory) {
        Objects.requireNonNull(directory, "directory");
        if (url == null || url.isBlank()) {
            throw new ModrinthDownloadException("File URL is missing for " + filename);
        }
        try {
            Files.createDirectories(directory);
            Path destination = directory.resolve(resolveFilename());
            String hashName = selectSupportedHash();
            if (Files.exists(destination) && hashName != null && hashMatches(destination, hashName, hashes.get(hashName))) {
                notifyIfUnverified(hashName, destination);
                post(new DownloadFinishedEvent(resolveFilename(), url, destination, true));
                return destination;
            }
            if (Files.exists(destination) && hashName != null) {
                post(new OutdatedFileReplacedEvent(resolveFilename(), destination, hashName,
                        hashes.get(hashName), computeHash(destination, hashName)));
            }

            Path temp = Files.createTempFile(directory, "modrinth-", ".download");
            try {
                post(new DownloadStartedEvent(resolveFilename(), url, destination, size > 0 ? size : -1));
                Path downloaded = downloadTo(temp, destination, hashName);
                notifyIfUnverified(hashName, downloaded);
                post(new DownloadFinishedEvent(resolveFilename(), url, downloaded, false));
                return downloaded;
            } finally {
                Files.deleteIfExists(temp);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ModrinthDownloadException("Failed to download " + resolveFilename(), ex);
        } catch (IOException ex) {
            throw new ModrinthDownloadException("Failed to download " + resolveFilename(), ex);
        }
    }

    // the temp file gets cleaned up by the caller, whatever happens in here
    private Path downloadTo(Path temp, Path destination, String hashName) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        java.net.http.HttpResponse<InputStream> response = client.send(request, BodyHandlers.ofInputStream());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            response.body().close();
            throw new ModrinthDownloadException("Failed to download " + resolveFilename() + " (HTTP " + response.statusCode() + ")");
        }
        long totalBytes = response.headers().firstValueAsLong("Content-Length").orElse(size > 0 ? size : -1);
        try (InputStream in = response.body(); OutputStream out = Files.newOutputStream(temp)) {
            copyWithProgress(in, out, totalBytes);
        }
        if (hashName != null && !hashMatches(temp, hashName, hashes.get(hashName))) {
            throw new ModrinthHashMismatchException(resolveFilename(), hashName, hashes.get(hashName), computeHash(temp, hashName));
        }
        Files.move(temp, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return destination;
    }

    // progress events are throttled to 100ms
    private void copyWithProgress(InputStream in, OutputStream out, long totalBytes) throws IOException {
        byte[] buffer = new byte[8192];
        long downloadedBytes = 0;
        long lastEventTime = 0;
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
            downloadedBytes += read;
            long now = System.currentTimeMillis();
            if (now - lastEventTime >= 100) {
                postProgress(downloadedBytes, totalBytes);
                lastEventTime = now;
            }
        }
        postProgress(downloadedBytes, totalBytes);
    }

    private void postProgress(long downloadedBytes, long totalBytes) {
        double progress = totalBytes > 0 ? Math.min(1.0, (double) downloadedBytes / totalBytes) : -1.0;
        post(new DownloadProgressEvent(resolveFilename(), url, progress, downloadedBytes, totalBytes));
    }

    private void post(Event event) {
        modrinth.getEventBus().post(event);
    }

    private String resolveFilename() {
        if (filename == null || filename.isBlank()) {
            return "modrinth-file";
        }
        return filename;
    }

    private String selectSupportedHash() {
        if (hashes.isEmpty()) {
            return null;
        }
        for (String candidate : new String[]{"sha512", "sha256", "sha1", "sha384", "sha224", "md5"}) {
            if (hashes.containsKey(candidate)) {
                return candidate;
            }
        }
        return hashes.keySet().iterator().next();
    }

    private boolean hashMatches(Path file, String algorithm, String expected) throws IOException {
        if (expected == null || expected.isBlank()) {
            return true;
        }
        String actual = computeHash(file, algorithm);
        return actual == null || expected.equalsIgnoreCase(actual);
    }

    // file will still be downloaded, we're just informing the launcher that we didn't check it
    private void notifyIfUnverified(String hashName, Path destination) {
        String expected = hashes.get(hashName);
        if (expected == null || expected.isBlank() || isKnownAlgorithm(hashName)) {
            return;
        }
        post(new UnverifiedDownloadEvent(resolveFilename(), url, hashName, destination));
    }

    private static boolean isKnownAlgorithm(String algorithm) {
        try {
            MessageDigest.getInstance(normalizeAlgorithm(algorithm));
            return true;
        } catch (NoSuchAlgorithmException ex) {
            return false;
        }
    }

    // null if the JVM doesn't know the algorithm
    private static String computeHash(Path file, String algorithm) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance(normalizeAlgorithm(algorithm));
            return toHex(digest.digest(Files.readAllBytes(file)));
        } catch (NoSuchAlgorithmException ex) {
            return null;
        }
    }

    private static String normalizeAlgorithm(String algorithm) {
        return switch (algorithm.toLowerCase()) {
            case "sha1" -> "SHA-1";
            case "sha224" -> "SHA-224";
            case "sha256" -> "SHA-256";
            case "sha384" -> "SHA-384";
            case "sha512" -> "SHA-512";
            case "md5" -> "MD5";
            default -> algorithm;
        };
    }

    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(Character.forDigit((value >>> 4) & 0xF, 16));
            builder.append(Character.forDigit(value & 0xF, 16));
        }
        return builder.toString();
    }
}

