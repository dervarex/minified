package com.dervarex.minified.launch.launch.modding.forge.api;

import com.dervarex.minified.launch.exceptions.loader.NoLoadersFoundException;
import com.dervarex.minified.launch.exceptions.loader.forge.FailedToFetchPromotionsException;
import com.dervarex.minified.launch.exceptions.version.FailedToFetchVersionsException;
import com.dervarex.minified.utils.ApiEndpoints;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonParser;
import org.apiguardian.api.API;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Utility for Fetching forge Versions
 */
@API(status = API.Status.STABLE)
public class ForgeVersionFetcher {
    private final HttpClient client = HttpClient.newHttpClient();

    private List<String> cachedVersions;
    private JsonObject cachedPromotions;

    /**
     * Creates a fetcher with an empty cache
     */
    public ForgeVersionFetcher() {}

    /**
     * {@return all Forge versions}
     *
     * @throws FailedToFetchVersionsException if the request fails
     */
    @API(status = API.Status.STABLE)
    public List<String> getAvailableVersions() {
        if (cachedVersions != null) {
            return cachedVersions;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiEndpoints.FORGE_MAVEN_METADATA_URL))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            String xml = response.body();

            Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

            cachedVersions = doc.select("version").eachText();
            return cachedVersions;

        } catch (IOException | InterruptedException e) {
            throw new FailedToFetchVersionsException("Failed to fetch Forge versions", "FORGE", e);
        }
    }

    /**
     * {@return the Forge versions for the Minecraft version}
     *
     * @param minecraftVersion minecraft version
     * @throws FailedToFetchVersionsException if the request fails
     */
    @API(status = API.Status.STABLE)
    public List<String> getVersionsForMinecraft(String minecraftVersion) {
        return getAvailableVersions()
                .stream()
                .filter(version -> version.startsWith(minecraftVersion + "-"))
                .toList();
    }

    /**
     * {@return the latest Forge version for the Minecraft version}
     *
     * @param minecraftVersion minecraft version
     * @throws NoLoadersFoundException if Forge has no build for the Minecraft version
     * @throws FailedToFetchPromotionsException if the request fails
     */
    @API(status = API.Status.STABLE)
    public String getLatest(String minecraftVersion) {
        JsonObject promos = getPromotions();
        if (!promos.containsKey(minecraftVersion + "-latest")) {
            throw new NoLoadersFoundException("No Forge version found for Minecraft " + minecraftVersion, "FORGE");
        }

        String forgeVersion = promos
                .get(minecraftVersion + "-latest")
                .asString();

        return toMavenVersion(minecraftVersion, forgeVersion);
    }

    /**
     * {@return the recommended Forge version for the Minecraft version, or null if there's none}
     *
     * @param minecraftVersion minecraft version
     * @throws NoLoadersFoundException if Forge has no build for the Minecraft version
     * @throws FailedToFetchPromotionsException if the request fails
     */
    @API(status = API.Status.STABLE)
    public String getRecommended(String minecraftVersion) {
        JsonObject promos = getPromotions();
        if (!promos.containsKey(minecraftVersion + "-latest") && !promos.containsKey(minecraftVersion + "-recommended")) {
            throw new NoLoadersFoundException("No Forge version found for Minecraft " + minecraftVersion, "FORGE");
        }

        if (!promos.containsKey(minecraftVersion + "-recommended")) {
            return null;
        }

        String forgeVersion = promos
                .get(minecraftVersion + "-recommended")
                .asString();

        return toMavenVersion(minecraftVersion, forgeVersion);
    }

    /**
     * Some old builds have a branch suffix in the maven version that the promotions are missing,
     * e.g. 10.13.4.1614 for 1.7.10 is published as {@code 1.7.10-10.13.4.1614-1.7.10}
     *
     * @return the maven version of the forge build
     */
    private String toMavenVersion(String minecraftVersion, String forgeVersion) {
        String version = minecraftVersion + "-" + forgeVersion;
        List<String> versions;
        try {
            versions = getAvailableVersions();
        } catch (FailedToFetchVersionsException e) {
            return version;
        }

        if (versions.contains(version)) {
            return version;
        }
        for (String mavenVersion : versions) {
            if (mavenVersion.startsWith(version + "-")) {
                return mavenVersion;
            }
        }
        return version;
    }

    /**
     * @return forge promotions as JsonObject
     */
    private JsonObject getPromotions() {
        if (cachedPromotions != null) {
            return cachedPromotions;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiEndpoints.FORGE_PROMOTIONS_URL))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            JsonObject root = JsonParser.parse(response.body()).asObject();

            cachedPromotions = root
                    .get("promos")
                    .asObject();

            return cachedPromotions;

        } catch (IOException | InterruptedException e) {
            throw new FailedToFetchPromotionsException("Failed to fetch Forge promotions", e);
        }
    }
}