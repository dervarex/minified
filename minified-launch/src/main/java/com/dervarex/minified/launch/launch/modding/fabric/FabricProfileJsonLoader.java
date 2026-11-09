package com.dervarex.minified.launch.launch.modding.fabric;

import com.dervarex.minified.launch.exceptions.loader.FailedToLoadLoaderProfileException;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.internal.CacheManager;
import com.dervarex.minified.utils.json.JsonObject;
import org.apiguardian.api.API;

/**
 * Loads the Fabric profile JSON, from the cache if possible
 */
@API(status = API.Status.STABLE)
public class FabricProfileJsonLoader {
    /**
     * {@return the Fabric profile JSON}
     *
     * @param version minecraft version
     * @param launchConfig launch configuration given by the launcher
     * @param online is the user online?
     * @throws FailedToLoadLoaderProfileException if it's neither cached nor fetchable
     */
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
