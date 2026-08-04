package com.dervarex.minified.utils.version;

import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.exceptions.OfflineModeNeedsNetworkException;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
    public static String getServerSha1(String version)
            throws HttpException, IOException {
        String url = getVersionJsonUrl(version);
        if (url == null) {
            return null;
        }
        JsonFile json = new JsonFile(HttpUtil.get(url));
        return json.get("downloads")
                .asObject()
                .get("server")
                .asObject()
                .get("sha1")
                .asString();
    }

    public static String getServerUrl(String version)
            throws HttpException, IOException {
        String url = getVersionJsonUrl(version);
        if (url == null) {
            return null;
        }
        JsonFile json = new JsonFile(HttpUtil.get(url));
        return json.get("downloads")
                .asObject()
                .get("server")
                .asObject()
                .get("url")
                .asString();
    }
}