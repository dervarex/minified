package com.dervarex.minified.launch.launch.modding.fabric;

import com.dervarex.minified.launch.exceptions.loader.NoLoadersFoundException;
import com.dervarex.minified.launch.launch.modding.Loader;
import com.dervarex.minified.utils.ApiEndpoints;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonParser;

public class FabricLoaderFetcher {

    public static String getLatestLoaderVersion() throws Exception {

        JsonArray loaders = JsonParser
                .parse(HttpUtil.get(ApiEndpoints.FABRIC_LOADER_META_URL))
                .asArray();

        if (loaders.size() == 0) {
            throw new NoLoadersFoundException("No Fabric loaders found", "FABRIC");
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
                ApiEndpoints.FABRIC_LOADER_META_URL
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