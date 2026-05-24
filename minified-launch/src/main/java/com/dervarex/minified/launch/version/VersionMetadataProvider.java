package com.dervarex.minified.launch.version;

import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonValue;

import java.io.IOException;

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
        String url = getVersionJsonUrl(version);
        if (url == null) {
            return -1; // todo replace with latest LTS java version, because Java has downward compatibility that should work
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

