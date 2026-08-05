package com.dervarex.minified.launch.launch.modding.fabric;

import com.dervarex.minified.launch.launch.CacheManager;
import com.dervarex.minified.utils.json.JsonObject;
import org.apiguardian.api.API;

public class FabricProfileJsonLoader {
    @API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
    public static JsonObject loadFabricProfileJson(String version, boolean online) {
        return CacheManager.loadProfileJson(
                version,
                "fabric",
                online,
                () -> {
                    try {
                        return FabricLoaderFetcher.getLatestProfile(version);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
