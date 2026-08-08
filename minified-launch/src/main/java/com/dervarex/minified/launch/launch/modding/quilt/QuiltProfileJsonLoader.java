package com.dervarex.minified.launch.launch.modding.quilt;

import com.dervarex.minified.launch.launch.internal.CacheManager;
import com.dervarex.minified.utils.json.JsonObject;
import org.apiguardian.api.API;

public class QuiltProfileJsonLoader {
    @API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
    public static JsonObject loadQuiltProfileJson(String version, boolean online) {
        return CacheManager.loadProfileJson(
                version,
                "quilt",
                online,
                () -> {
                    try {
                        return QuiltLoaderFetcher.getLatestProfile(version);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
