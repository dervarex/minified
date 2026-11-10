package com.dervarex.minified.launch.launch.modding.quilt;

import com.dervarex.minified.launch.exceptions.loader.FailedToLoadLoaderProfileException;
import com.dervarex.minified.launch.exceptions.loader.NoLoadersFoundException;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.internal.CacheManager;
import com.dervarex.minified.utils.json.JsonObject;
import org.apiguardian.api.API;

/**
 * Loads the Quilt profile JSON, from the cache if possible
 */
@API(status = API.Status.STABLE)
public class QuiltProfileJsonLoader {

    private QuiltProfileJsonLoader() {}

    /**
     * {@return Quilt Profile JSON as JsonObject}
     *
     * @param version minecraft version
     * @param launchConfig launch configuration given by the launcher
     * @param online is the user online?
     * @throws NoLoadersFoundException if Quilt doesn't have the loader for the Minecraft version
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
                    } catch (NoLoadersFoundException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new FailedToLoadLoaderProfileException("Failed to load the Quilt profile JSON", "QUILT", e);
                    }
                }
        );
    }
}
