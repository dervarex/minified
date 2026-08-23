package com.dervarex.minified.utils.version;

import com.dervarex.minified.utils.ApiEndpoints;
import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonValue;
import org.apiguardian.api.API;

import java.io.IOException;

@API(status = API.Status.INTERNAL)
public final class VersionManifestClient {
    private VersionManifestClient() {
    }

    /**
     * @return the Minecraft version manifest
     * @throws HttpException when no connection could be established or any other Http Error occurs
     * @throws IOException if there is an error reading the response
     */
    public static JsonFile getManifest() throws HttpException, IOException {
        return new JsonFile(HttpUtil.get(ApiEndpoints.VERSION_MANIFEST_URL));
    }

    /**
     * @return all Minecraft versions as an iterable of JSON values,
     * each containing the version's metadata (id, type, url, time, releaseTime)
     * @throws HttpException when no connection could be established or any other Http Error occurs
     * @throws IOException if there is an error reading the response
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

