package com.dervarex.minified.launch.launch.modding.neoforge.api;

import com.dervarex.minified.launch.exceptions.loader.FailedToLoadLoaderProfileException;
import com.dervarex.minified.launch.launch.internal.CacheManager;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import org.apiguardian.api.API;

import java.nio.file.Path;

@API(status = API.Status.STABLE)
public class NeoProfileJsonLoader {
    /**
     * Loads the Neoforge Profile Json
     * @param version minecraft version
     * @param launchConfig launch configuration given by the launcher
     * @param online is the user online?
     * @return Neoforge Profile JSON as an JSON Object
     */
    @API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
    public static JsonObject loadNeoforgeProfileJson(
            String version,
            LaunchConfiguration launchConfig,
            boolean online
    ) {
        return CacheManager.loadProfileJson(
                version,
                "neoforge",
                launchConfig.getLoader().loaderVersion(),
                online,
                () -> {
                    try {
                        Path parent = launchConfig.getJarFile().getParent().toAbsolutePath();
                        // the version the installer installed
                        JsonFile neoVersionJson = NeoVersionJson.getVersionJson(parent, launchConfig.getLoader().loaderVersion());
                        return neoVersionJson.asObject();
                    } catch (Exception e) {
                        throw new FailedToLoadLoaderProfileException("Failed to load the NeoForge profile JSON", "NEOFORGE", e);
                    }
                }
        );
    }
}
