package com.dervarex.minified.launch.launch.modding.quilt;

import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.internal.CacheManager;
import com.dervarex.minified.utils.json.JsonObject;
import org.apiguardian.api.API;

public class QuiltProfileJsonLoader {

    /**
     * @param version minecraft version
     * @param launchConfig launch configuration given by the launcher
     * @param online is the user online?
     * @return Quilt Profile JSON as JsonObject
     */
    @API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
    public static JsonObject loadQuiltProfileJson(String version, LaunchConfiguration launchConfig, boolean online) {
        String loaderVersion = launchConfig.getLoader().loaderVersion();
        return CacheManager.loadProfileJson(
                version,
                "quilt",
                loaderVersion,
                online,
                () -> {
                    try {
                        return QuiltLoaderFetcher.getProfileJson(version, loaderVersion);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
