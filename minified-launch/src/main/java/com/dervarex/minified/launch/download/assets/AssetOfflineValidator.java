package com.dervarex.minified.launch.download.assets;


import com.dervarex.minified.utils.exceptions.OfflineModeNeedsNetworkException;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AssetOfflineValidator {

    private AssetOfflineValidator() {
    }

    public static void validate(String version, Path assetsDir) {
        List<String> problems = new ArrayList<>();
        Path cacheRoot = resolveCacheRoot(assetsDir);

        Path versionJsonPath = cacheRoot.resolve("versions").resolve(version + ".json");
        if (!Files.exists(versionJsonPath)) {
            throw new OfflineModeNeedsNetworkException(
                    "Missing cached version JSON: " + versionJsonPath
            );
        }

        JsonFile versionJson;
        try {
            versionJson = new JsonFile(Files.readString(versionJsonPath));
        } catch (Exception e) {
            throw new OfflineModeNeedsNetworkException(
                    "Failed to read cached version JSON: " + versionJsonPath
            );
        }

        JsonObject assetIndex = versionJson.get("assetIndex").asObject();
        String assetIndexId = assetIndex.get("id").asString();

        Path indexPath = assetsDir.resolve("indexes").resolve(assetIndexId + ".json");
        if (!Files.exists(indexPath)) {
            problems.add("Missing cached asset index: " + indexPath);
        }

        JsonFile indexFile;
        try {
            indexFile = new JsonFile(Files.readString(indexPath));
        } catch (Exception e) {
            throw new OfflineModeNeedsNetworkException(
                    "Failed to read cached asset index: " + indexPath
            );
        }

        JsonObject objects = indexFile.get("objects").asObject();

        for (String key : objects.keys()) {
            JsonObject asset = objects.get(key).asObject();

            String hash = asset.get("hash").asString();
            String subDir = hash.substring(0, 2);

            Path objectPath = assetsDir
                    .resolve("objects")
                    .resolve(subDir)
                    .resolve(hash);

            if (!Files.exists(objectPath) || !Files.isRegularFile(objectPath)) {
                problems.add("Missing asset: " + objectPath);
                continue;
            }

            try {
                String actualSha1 = sha1(objectPath);
                if (!actualSha1.equalsIgnoreCase(hash)) {
                    problems.add("Corrupted asset: " + objectPath);
                }
            } catch (Exception e) {
                problems.add("Failed to hash asset: " + objectPath);
            }
        }

        if (!problems.isEmpty()) {
            throw new OfflineModeNeedsNetworkException(
                    "Offline launch blocked:\n- " + String.join("\n- ", problems)
            );
        }
    }

    private static String sha1(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] bytes = Files.readAllBytes(file);
        byte[] hash = digest.digest(bytes);

        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            sb.append(Character.forDigit((b >>> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    private static Path resolveCacheRoot(Path assetsDir) {
        Path parent = assetsDir.toAbsolutePath().getParent();
        return Objects.requireNonNullElseGet(parent, assetsDir::toAbsolutePath).resolve("cache");
    }
}