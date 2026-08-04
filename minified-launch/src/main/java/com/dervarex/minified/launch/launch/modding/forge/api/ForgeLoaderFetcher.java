package com.dervarex.minified.launch.launch.modding.forge.api;

import com.dervarex.minified.launch.launch.CacheManager;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;

import java.nio.file.Path;

public class ForgeLoaderFetcher {
    public static JsonObject loadForgeProfileJson(//todo move
                                                  String version,
                                                  LaunchConfiguration launchConfig,
                                                  boolean online
    ) {
        return CacheManager.loadProfileJson(
                version,
                "forge",
                online,
                () -> {
                    try {
                        Path parent = launchConfig.getJarFile().getParent().toAbsolutePath();
                        String latest = new ForgeVersionFetcher().getLatest(version);
                        JsonFile forgeVersionJson = ForgeVersionJson.getVersionJson(parent, latest);
                        return forgeVersionJson.asObject();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
