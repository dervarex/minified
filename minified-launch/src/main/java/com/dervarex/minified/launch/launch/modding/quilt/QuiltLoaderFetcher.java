package com.dervarex.minified.launch.launch.modding.quilt;

import com.dervarex.minified.launch.exceptions.loader.NoLoadersFoundException;
import com.dervarex.minified.launch.exceptions.version.FailedToFetchVersionsException;
import com.dervarex.minified.utils.ApiEndpoints;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonParser;
import com.dervarex.minified.utils.json.JsonValue;
import org.apiguardian.api.API;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

        List<String> versions = new ArrayList<>();
        for (JsonValue loaderValue : loaders) {
            JsonObject loader = loaderValue.asObject();

            if (!loader.has("loader")) {
                throw new FailedToFetchVersionsException(
                        "Invalid Quilt loader response",
                        "QUILT"
                );
            }

            versions.add(loader
                    .get("loader")
                    .asObject()
                    .get("version")
                    .asString());
        }

        return selectLatestLoaderVersion(versions);
    }

    /**
     * The Quilt meta API does not return the loader versions sorted, so we need to do that
     *
     * @param versions loader versions, for example {@code 0.30.1} or {@code 0.31.0-beta.4}
     * @return the newest stable version, or the newest pre-release if there is no stable version
     */
    @API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
    public static String selectLatestLoaderVersion(List<String> versions) {
        Comparator<String> byVersion = QuiltLoaderFetcher::compareLoaderVersions;

        return versions.stream()
                .filter(version -> !version.contains("-"))
                .max(byVersion)
                .or(() -> versions.stream().max(byVersion))
                .orElseThrow(() -> new IllegalArgumentException("No loader versions given"));
    }

    private static int compareLoaderVersions(String a, String b) {
        String[] aParts = a.split("-", 2);
        String[] bParts = b.split("-", 2);

        int result = compareDotted(aParts[0], bParts[0]);
        if (result != 0) {
            return result;
        }

        // a release is newer than its pre-releases (0.30.1 > 0.30.1-beta.4)
        if (aParts.length != bParts.length) {
            return aParts.length < bParts.length ? 1 : -1;
        }

        return aParts.length == 1 ? 0 : compareDotted(aParts[1], bParts[1]);
    }

    private static int compareDotted(String a, String b) {
        String[] aParts = a.split("\\.");
        String[] bParts = b.split("\\.");

        for (int i = 0; i < Math.max(aParts.length, bParts.length); i++) {
            String aPart = i < aParts.length ? aParts[i] : "0";
            String bPart = i < bParts.length ? bParts[i] : "0";

            int result = aPart.matches("\\d+") && bPart.matches("\\d+")
                    ? Long.compare(Long.parseLong(aPart), Long.parseLong(bPart))
                    : aPart.compareTo(bPart);

            if (result != 0) {
                return result;
            }
        }

        return 0;
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