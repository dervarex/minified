package com.dervarex.minified.launch.version;

import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.exceptions.NoConnectionException;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonValue;
import com.dervarex.minified.utils.network.NetworkUtil;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SuppressWarnings("unused")
public final class VersionMetadataProvider {
    private VersionMetadataProvider() {
    }

    public static String getVersionJsonUrl(String version) throws HttpException, IOException {
        JsonValue entry = VersionManifestClient.getVersionEntry(version);
        if (entry == null) {
            return null;
        }
        return entry.asObject().get("url").asString();
    }

    public static int getMinimumJavaVersion(String version) throws HttpException, IOException {
        try {
            NetworkUtil.ensureOnline("getMinimumJavaVersion");
        } catch (NoConnectionException nce) {
            return -1;
        }
                String url = getVersionJsonUrl(version);
        if (url == null) {
            try {
                int latestLTS = Integer.parseInt(
                        HttpClient.newHttpClient()
                                .send(
                                        HttpRequest.newBuilder()
                                                .uri(URI.create("https://api.adoptium.net/v3/info/available_releases"))
                                                .build(),
                                        HttpResponse.BodyHandlers.ofString()
                                )
                                .body()
                                .split("\"most_recent_lts\":")[1]
                                .split(",")[0]
                                .trim()
                );
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        JsonFile json = new JsonFile(HttpUtil.get(url));
        Integer majorVersion = json.get("javaVersion").asObject().getInt("majorVersion");
        return majorVersion == null ? -1 : majorVersion;
    }

    public static String getMainClass(String version) throws HttpException, IOException {
        String url = getVersionJsonUrl(version);
        if (url == null) {
            return null;
        }
        JsonFile json = new JsonFile(HttpUtil.get(url));
        return json.get("mainClass").asString();
    }
    public static String getVersionType(String version) throws HttpException, IOException {
        String url = getVersionJsonUrl(version);
        if (url == null) {
            return null;
        }
        JsonFile json = new JsonFile(HttpUtil.get(url));
        return json.get("type").asString();
    }
    public static String getReleaseTime(String version) throws HttpException, IOException {
        String url = getVersionJsonUrl(version);
        if (url == null) {
            return null;
        }
        JsonFile json = new JsonFile(HttpUtil.get(url));
        return json.get("releaseTime").asString();
    }

}

