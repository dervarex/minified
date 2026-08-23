package com.dervarex.minified.launch.launch.internal;

import com.dervarex.minified.java.JavaManager;
import com.dervarex.minified.launch.exceptions.cache.FailedToCacheException;
import com.dervarex.minified.launch.launch.Launcher;
import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.exceptions.OfflineModeNeedsNetworkException;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.version.VersionMetadataProvider;
import org.apiguardian.api.API;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
public class CacheManager {
    private static Path cacheRoot() {
        return JavaManager.getBaseDir().resolve("cache");
    }
    private static void writeCache(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }
    private static Path cachedProfileJsonPath(String loaderName, String version) {
        return CacheManager.cacheRoot()
                .resolve("profiles")
                .resolve(loaderName)
                .resolve(version + ".json");
    }

    private static Path cachedVersionJsonPath(String version) {
        return CacheManager.cacheRoot()
                .resolve("versions")
                .resolve(version + ".json");
    }
    public static JsonObject loadProfileJson(
            String version,
            String loaderName,
            boolean online,
            Launcher.ProfileSupplier supplier
    ) {
        Path cachePath = cachedProfileJsonPath(loaderName, version);

        if (Files.exists(cachePath)) {
            try {
                return new JsonFile(Files.readString(cachePath)).asObject();
            } catch (Exception ignored) {
                // broken cache, fall through
            }
        }

        if (!online) {
            throw new OfflineModeNeedsNetworkException(
                    "Missing cached " + loaderName + " profile: " + cachePath
            );
        }

        JsonObject profile = supplier.get();
        try {
            writeCache(cachePath, profile.toString());
        } catch (IOException e) {
            throw new FailedToCacheException("Failed to cache " + loaderName + " profile", e);
        }
        return profile;
    }
    public static JsonFile loadVersionJson(String version, boolean online) throws IOException {
        Path cachePath = CacheManager.cachedVersionJsonPath(version);

        if (Files.exists(cachePath)) {
            try {
                return new JsonFile(Files.readString(cachePath));
            } catch (Exception ignored) {
                // broken cache, fall through to network if possible
            }
        }

        if (!online) {
            throw new OfflineModeNeedsNetworkException(
                    "Missing cached version JSON: " + cachePath
            );
        }

        String versionJsonUrl;
        String raw;
        try {
            versionJsonUrl = VersionMetadataProvider.getVersionJsonUrl(version);

            raw = HttpUtil.get(versionJsonUrl);
        } catch (HttpException e) {
            throw new RuntimeException(e); // no custom exception
        }
        writeCache(cachePath, raw);
        return new JsonFile(raw);
    }
}
