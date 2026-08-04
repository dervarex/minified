package com.dervarex.minified.launch.launch.modding.neoforge.api;

import com.dervarex.minified.launch.launch.CacheManager;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;

import java.nio.file.Path;

public class NeoLoaderFetcher {
    public static JsonObject loadNeoForgeProfileJson( //todo move
                                                      String version,
                                                      LaunchConfiguration launchConfig,
                                                      boolean online
    ) {
        return CacheManager.loadProfileJson(
                version,
                "neoforge",
                online,
                () -> {
                    try {
                        Path parent = launchConfig.getJarFile().getParent().toAbsolutePath();
                        String latest = new NeoVersionFetcher().getLatest(version);
                        JsonFile neoVersionJson = NeoVersionJson.getVersionJson(parent, latest);
                        return neoVersionJson.asObject();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
