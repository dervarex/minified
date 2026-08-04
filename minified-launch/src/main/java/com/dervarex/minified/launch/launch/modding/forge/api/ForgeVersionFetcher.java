package com.dervarex.minified.launch.launch.modding.forge.api;

import com.dervarex.minified.launch.exceptions.loader.forge.FailedToFetchPromotionsException;
import com.dervarex.minified.launch.exceptions.version.FailedToFetchVersionsException;
import com.dervarex.minified.utils.ApiEndpoints;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ForgeVersionFetcher {
    private final HttpClient client = HttpClient.newHttpClient();

    private List<String> cachedVersions;
    private JsonObject cachedPromotions;

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

    public List<String> getVersionsForMinecraft(String minecraftVersion) {
        return getAvailableVersions()
                .stream()
                .filter(version -> version.startsWith(minecraftVersion + "-"))
                .toList();
    }

    public String getLatest(String minecraftVersion) {
        JsonObject promos = getPromotions();

        String forgeVersion = promos
                .get(minecraftVersion + "-latest")
                .asString();

        return minecraftVersion + "-" + forgeVersion;
    }

    public String getRecommended(String minecraftVersion) {
        JsonObject promos = getPromotions();

        if (!promos.containsKey(minecraftVersion + "-recommended")) {
            return null;
        }

        String forgeVersion = promos
                .get(minecraftVersion + "-recommended")
                .asString();

        return minecraftVersion + "-" + forgeVersion;
    }

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