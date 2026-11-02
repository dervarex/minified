package com.dervarex.minified.launch.launch.modding.fabric;

import com.dervarex.minified.launch.exceptions.loader.FailedToLoadLoaderProfileException;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.internal.CacheManager;
import com.dervarex.minified.utils.json.JsonObject;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public class FabricProfileJsonLoader {
    @API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
    public static JsonObject loadFabricProfileJson(String version, LaunchConfiguration launchConfig, boolean online) {
        String loaderVersion = launchConfig.getLoader().loaderVersion();
        return CacheManager.loadProfileJson(
                version,
                "fabric",
                loaderVersion,
                online,
                () -> {
                    try {
                        return FabricLoaderFetcher.getProfileJson(version, loaderVersion);
                    } catch (Exception e) {
                        throw new FailedToLoadLoaderProfileException("Failed to load the Fabric profile JSON", "FABRIC", e);
                    }
                }
        );
    }
}
