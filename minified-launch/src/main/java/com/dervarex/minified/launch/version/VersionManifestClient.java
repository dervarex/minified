package com.dervarex.minified.launch.version;

import com.dervarex.minified.launch.ApiEndpoints;
import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonValue;

import java.io.IOException;

@SuppressWarnings("unused")
public final class VersionManifestClient {
    private VersionManifestClient() {
    }

    /**
     * @return the minecraft version manifest
     * @throws HttpException if there is no working connection
     * @throws IOException if there is an error reading the response
     */
    public static JsonFile getManifest() throws HttpException, IOException {
        return new JsonFile(HttpUtil.get(ApiEndpoints.VersionManifestUrl));
    }

    /**
     * @return all minecraft versions as an iterable of JSON values,
     * each containing the version's metadata (id, type, url, time, releaseTime)
     * @throws HttpException
     * @throws IOException
     */
    public static Iterable<JsonValue> getVersions() throws HttpException, IOException {
        return getManifest().get("versions").asArray();
    }

    public static JsonValue getVersionEntry(String version) throws HttpException, IOException {
        for (JsonValue v : getVersions()) {
            if (v.asObject().get("id").asString().equals(version)) {
                return v;
            }
        }
        return null;
    }
}

