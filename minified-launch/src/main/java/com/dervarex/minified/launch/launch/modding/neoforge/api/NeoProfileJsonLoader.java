package com.dervarex.minified.launch.launch.modding.neoforge.api;

import com.dervarex.minified.launch.launch.internal.CacheManager;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import org.apiguardian.api.API;

import java.nio.file.Path;

public class NeoProfileJsonLoader {
    @API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
    public static JsonObject loadNeoforgeProfileJson(
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
