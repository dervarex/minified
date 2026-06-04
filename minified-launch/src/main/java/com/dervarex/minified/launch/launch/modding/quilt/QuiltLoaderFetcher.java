package com.dervarex.minified.launch.launch.modding.quilt;

import com.dervarex.minified.launch.ApiEndpoints;
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
                        ApiEndpoints.QuiltLoaderMetaUrl + "/" + minecraftVersion
                ))
                .asArray();

        if (loaders.size() == 0) {
            throw new RuntimeException(
                    "No Quilt loaders found for Minecraft "
                            + minecraftVersion
            );
        }

        JsonObject loader = loaders
                .get(0)
                .asObject();

        if (!loader.has("loader")) {
            throw new RuntimeException(
                    "Invalid Quilt loader response"
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
                ApiEndpoints.QuiltLoaderMetaUrl
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