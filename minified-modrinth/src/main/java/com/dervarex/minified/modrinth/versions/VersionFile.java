package com.dervarex.minified.modrinth.versions;

import com.dervarex.minified.modrinth.ModrinthDownloadException;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class VersionFile {
    public Map<String, String> hashes = new LinkedHashMap<>();
    public String url;
    public String filename;
    public boolean primary;
    public long size;
    public String fileType;

    transient Version owner;

    public VersionFile() {
    }

    public String getUrl() {
        return url;
    }

    public String getFilename() {
        return filename;
    }

    public boolean isPrimary() {
        return primary;
    }

    public long getSize() {
        return size;
    }

    public String getFileType() {
        return fileType;
    }

    public Map<String, String> getHashes() {
        return hashes;
    }

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
                return destination;
            }

            Path temp = Files.createTempFile(directory, "modrinth-", ".download");
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
            java.net.http.HttpResponse<InputStream> response = client.send(request, BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                Files.deleteIfExists(temp);
                throw new ModrinthDownloadException("Failed to download " + resolveFilename() + " (HTTP " + response.statusCode() + ")");
            }
            try (InputStream in = response.body(); OutputStream out = Files.newOutputStream(temp)) {
                in.transferTo(out);
            }
            if (hashName != null && !hashMatches(temp, hashName, hashes.get(hashName))) {
                Files.deleteIfExists(temp);
                throw new ModrinthDownloadException("Hash verification failed for " + resolveFilename() + " using " + hashName);
            }
            Files.move(temp, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return destination;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ModrinthDownloadException("Failed to download " + resolveFilename(), ex);
        } catch (IOException ex) {
            throw new ModrinthDownloadException("Failed to download " + resolveFilename(), ex);
        }
    }

    public boolean hasHash(String name) {
        return hashes != null && name != null && hashes.containsKey(name);
    }

    public String getHash(String name) {
        return hashes == null || name == null ? null : hashes.get(name);
    }

    private String resolveFilename() {
        if (filename == null || filename.isBlank()) {
            return "modrinth-file";
        }
        return filename;
    }

    private String selectSupportedHash() {
        if (hashes == null || hashes.isEmpty()) {
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
        try {
            MessageDigest digest = MessageDigest.getInstance(normalizeAlgorithm(algorithm));
            byte[] actual = digest.digest(Files.readAllBytes(file));
            return expected.equalsIgnoreCase(toHex(actual));
        } catch (NoSuchAlgorithmException ex) {
            return true;
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

    void attach(Version version) {
        this.owner = version;
    }
}

