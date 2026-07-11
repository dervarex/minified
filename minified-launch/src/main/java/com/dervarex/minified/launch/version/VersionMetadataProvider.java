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
    public static String getClientSha1(String version) throws HttpException, IOException {
        String url = getVersionJsonUrl(version);
        if (url == null) {
            return null;
        }

        JsonFile json = new JsonFile(HttpUtil.get(url));

        return json.get("downloads")
                .asObject()
                .get("client")
                .asObject()
                .get("sha1")
                .asString();
    }
    public static String getClientUrl(String version)
            throws HttpException, IOException {

        String url = getVersionJsonUrl(version);
        if (url == null) {
            return null;
        }

        JsonFile json = new JsonFile(HttpUtil.get(url));

        return json.get("downloads")
                .asObject()
                .get("client")
                .asObject()
                .get("url")
                .asString();
    }

}

