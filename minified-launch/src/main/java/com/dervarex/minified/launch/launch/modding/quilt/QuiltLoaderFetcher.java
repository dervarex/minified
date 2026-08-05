package com.dervarex.minified.launch.launch.modding.quilt;

import com.dervarex.minified.launch.exceptions.loader.NoLoadersFoundException;
import com.dervarex.minified.launch.exceptions.version.FailedToFetchVersionsException;
import com.dervarex.minified.launch.launch.CacheManager;
import com.dervarex.minified.utils.ApiEndpoints;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonParser;
import org.apiguardian.api.API;

public class QuiltLoaderFetcher {

    @API(status = API.Status.STABLE)
    public static String getLatestLoaderVersion(
            String minecraftVersion
    ) throws Exception {

        JsonArray loaders = JsonParser
                .parse(HttpUtil.get(
                        ApiEndpoints.QUILT_LOADER_META_URL + "/" + minecraftVersion
                ))
                .asArray();

        if (loaders.size() == 0) {
            throw new NoLoadersFoundException(
                    "No Quilt loaders found for Minecraft "
                            + minecraftVersion,
                    minecraftVersion
            );
        }

        JsonObject loader = loaders
                .get(0)
                .asObject();

        if (!loader.has("loader")) {
            throw new FailedToFetchVersionsException(
                    "Invalid Quilt loader response",
                    "QUILT"
            );
        }

        return loader
                .get("loader")
                .asObject()
                .get("version")
                .asString();
    }

    @API(status = API.Status.STABLE)
    public static JsonObject getProfileJson(
            String minecraftVersion,
            String loaderVersion
    ) throws Exception {

        String url =
                ApiEndpoints.QUILT_LOADER_META_URL
                        + "/"
                        + minecraftVersion
                        + "/"
                        + loaderVersion
                        + "/profile/json";

        return JsonParser
                .parse(HttpUtil.get(url))
                .asObject();
    }

    @API(status = API.Status.STABLE)
    public static JsonObject getLatestProfile(
            String minecraftVersion
    ) throws Exception {

        String loaderVersion =
                getLatestLoaderVersion(minecraftVersion);

        return getProfileJson(
                minecraftVersion,
                loaderVersion
        );
    }
}