package com.dervarex.minified.launch.download.assets;

import com.dervarex.minified.launch.exceptions.download.AssetDownloadException;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;
import org.apiguardian.api.API;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Versions before 1.7.3 want the files under their real names
 */
@API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
public final class LegacyAssets {

    private LegacyAssets() {
    }

    /**
     * @return the directory the game expects as {@code ${game_assets}}
     */
    public static Path resolveGameAssetsDirectory(JsonFile versionJson, LaunchConfiguration config) {
        Path assetsDir = config.getAssetsDirectory().toAbsolutePath();
        String indexId = assetIndexId(versionJson);
        if (indexId == null) {
            return assetsDir;
        }

        JsonObject index = readIndex(assetsDir, indexId);
        if (isTrue(index, "map_to_resources")) {
            return resourcesDirectory(config);
        }
        if (isTrue(index, "virtual")) {
            return assetsDir.resolve("virtual").resolve(indexId);
        }
        return assetsDir;
    }

    /**
     * Copies the downloaded asset objects to where legacy versions look for them
     *
     * @throws AssetDownloadException if an asset could not be copied
     */
    public static void reconstruct(JsonFile versionJson, LaunchConfiguration config) {
        Path assetsDir = config.getAssetsDirectory().toAbsolutePath();
        String indexId = assetIndexId(versionJson);
        if (indexId == null) {
            return;
        }

        JsonObject index = readIndex(assetsDir, indexId);
        if (index == null || !(isTrue(index, "map_to_resources") || isTrue(index, "virtual"))) {
            return;
        }

        Path target = resolveGameAssetsDirectory(versionJson, config);
        JsonObject objects = index.get("objects").asObject();

        try {
            for (String name : objects.keys()) {
                JsonObject asset = objects.get(name).asObject();
                String hash = asset.get("hash").asString();
                Path object = assetsDir.resolve("objects").resolve(hash.substring(0, 2)).resolve(hash);
                Path output = target.resolve(name).normalize();

                if (!output.startsWith(target) || !Files.exists(object)) {
                    continue;
                }
                if (Files.exists(output) && Files.size(output) == Files.size(object)) {
                    continue;
                }

                Files.createDirectories(output.getParent());
                Files.copy(object, output, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new AssetDownloadException("Failed to copy legacy assets to " + target, e);
        }
    }

    private static Path resourcesDirectory(LaunchConfiguration config) {
        return config.getJarFile().toAbsolutePath().getParent().resolve("resources");
    }

    private static String assetIndexId(JsonFile versionJson) {
        JsonValue assetIndex = versionJson.get("assetIndex");
        if (assetIndex == null || assetIndex.asObject().get("id") == null) {
            return null;
        }
        return assetIndex.asObject().get("id").asString();
    }

    private static JsonObject readIndex(Path assetsDir, String indexId) {
        Path indexPath = assetsDir.resolve("indexes").resolve(indexId + ".json");
        if (!Files.exists(indexPath)) {
            return null;
        }
        try {
            return new JsonFile(Files.readString(indexPath)).asObject();
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isTrue(JsonObject index, String key) {
        if (index == null) {
            return false;
        }
        Boolean value = index.getBoolean(key);
        return value != null && value;
    }
}
