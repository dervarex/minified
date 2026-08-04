package com.dervarex.minified.launch.launch.modding.quilt;

import com.dervarex.minified.launch.exceptions.version.FailedToFetchVersionsException;
import com.dervarex.minified.launch.exceptions.loader.NoLoadersFoundException;
import com.dervarex.minified.utils.ApiEndpoints;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonParser;

public class QuiltLoaderFetcher {

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

    public static void main(String[] args) {
        try {
            String minecraftVersion = "1.21.11";
            JsonObject profile = getLatestProfile(minecraftVersion);
            System.out.println("Latest Quilt loader profile for Minecraft " + minecraftVersion + ":");
            System.out.println(profile.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}