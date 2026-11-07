package com.dervarex.minified.utils.version;

import com.dervarex.minified.utils.ApiEndpoints;
import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonValue;
import org.apiguardian.api.API;

import java.io.IOException;

/**
 * Reads Mojang's version manifest, cached for a few minutes
 */
@API(status = API.Status.INTERNAL)
public final class VersionManifestClient {
    // everything in here asks for the manifest, once every few minutes is plenty
    static final RecentResponses CACHE = new RecentResponses();

    private VersionManifestClient() {
    }

    /**
     * {@return the Minecraft version manifest}
     *
     * @throws HttpException when no connection could be established or any other Http Error occurs
     * @throws IOException if there is an error reading the response
     */
    public static JsonFile getManifest() throws HttpException, IOException {
        return new JsonFile(CACHE.get(ApiEndpoints.VERSION_MANIFEST_URL));
    }

    /**
     * {@return the version entries of the manifest}
     *
     * @throws HttpException when no connection could be established or any other Http Error occurs
     * @throws IOException if there is an error reading the response
     */
    public static Iterable<JsonValue> getVersions() throws HttpException, IOException {
        return getManifest().get("versions").asArray();
    }

    /**
     * {@return the manifest entry of a version, null if it doesn't exist}
     *
     * @param version the version id
     * @throws HttpException if the request fails
     * @throws IOException if the response can't be read
     */
    public static JsonValue getVersionEntry(String version) throws HttpException, IOException {
        for (JsonValue v : getVersions()) {
            if (v.asObject().get("id").asString().equals(version)) {
                return v;
            }
        }
        return null;
    }
}

