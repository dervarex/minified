package com.dervarex.minified.launch.launch.modding.fabric;

import com.dervarex.minified.launch.ApiEndpoints;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonParser;

public class FabricLoaderFetcher {

    public static String getLatestLoaderVersion() throws Exception {

        JsonArray loaders = JsonParser
                .parse(HttpUtil.get(ApiEndpoints.FabricLoaderMetaUrl))
                .asArray();

        if (loaders.size() == 0) {
            throw new RuntimeException("No Fabric loaders found");
        }

        return loaders
                .get(0)
                .asObject()
                .get("version")
                .asString();
    }

    public static JsonObject getProfileJson(
            String minecraftVersion,
            String loaderVersion
    ) throws Exception {

        String url =
                ApiEndpoints.FabricLoaderMetaUrl
                        + "/"
                        + minecraftVersion
                        + "/"
                        + loaderVersion
                        + "/profile/json";

        return JsonParser
                .parse(HttpUtil.get(url))
                .asObject();
    }

    public static JsonObject getLatestProfile(
            String minecraftVersion
    ) throws Exception {

        String loaderVersion = getLatestLoaderVersion();

        return getProfileJson(
                minecraftVersion,
                loaderVersion
        );
    }
}