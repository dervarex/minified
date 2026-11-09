package com.dervarex.minified.launch.launch.modding.fabric;

import com.dervarex.minified.launch.exceptions.loader.NoLoadersFoundException;
import com.dervarex.minified.utils.ApiEndpoints;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonParser;
import org.apiguardian.api.API;

/**
 * Fetches Fabric loader versions and profile JSONs
 */
@API(status = API.Status.STABLE)
public class FabricLoaderFetcher {

    /**
     * {@return the latest Fabric loader version}
     *
     * @throws NoLoadersFoundException if Fabric lists no loaders
     * @throws Exception if the request fails
     */
    @API(status = API.Status.STABLE)
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

    /**
     * {@return the profile JSON of the Fabric loader for the Minecraft version}
     *
     * @param minecraftVersion minecraft version
     * @param loaderVersion FabricMC loader version
     * @throws Exception if the request fails
     */
    @API(status = API.Status.STABLE)
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

    /**
     * {@return the profile JSON of the latest Fabric loader for the Minecraft version}
     *
     * @param minecraftVersion minecraft version
     * @throws NoLoadersFoundException if Fabric lists no loaders
     * @throws Exception if a request fails
     */
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
