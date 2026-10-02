package com.dervarex.minified.launch.launch.modding.forge.api;

import com.dervarex.minified.launch.launch.internal.CacheManager;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import org.apiguardian.api.API;

import java.nio.file.Path;

public class ForgeProfileJsonLoader {
    @API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
    public static JsonObject loadForgeProfileJson(
                                                  String version,
                                                  LaunchConfiguration launchConfig,
                                                  boolean online
    ) {
        return CacheManager.loadProfileJson(
                version,
                "forge",
                launchConfig.getLoader().loaderVersion(),
                online,
                () -> {
                    try {
                        Path parent = launchConfig.getJarFile().getParent().toAbsolutePath();
                        // the version the installer installed
                        JsonFile forgeVersionJson = ForgeVersionJson.getVersionJson(parent, launchConfig.getLoader().loaderVersion());
                        return forgeVersionJson.asObject();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
