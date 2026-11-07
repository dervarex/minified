package com.dervarex.minified.utils.version;

import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.json.JsonValue;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Minecraft version lists from the version manifest
 */
public final class VersionListProvider {
    private VersionListProvider() {
    }

    /**
     * {@return every version id, newest first}
     *
     * @throws HttpException if the request fails
     * @throws IOException if the response can't be read
     */
    public static ArrayList<String> getVersions() throws HttpException, IOException {
        ArrayList<String> versions = new ArrayList<>();
        for (JsonValue version : VersionManifestClient.getVersions()) {
            versions.add(version.asObject().get("id").asString());
        }
        return versions;
    }

    /**
     * {@return every release version id, newest first}
     *
     * @throws HttpException if the request fails
     * @throws IOException if the response can't be read
     */
    public static ArrayList<String> getReleaseVersions() throws HttpException, IOException {
        ArrayList<String> versions = new ArrayList<>();
        for (JsonValue version : VersionManifestClient.getVersions()) {
            String type = version.asObject().get("type").asString();
            if (
                    type.equals("snapshot") ||
                    type.equals("old_beta") ||
                    type.equals("old_alpha")
            ) {
                continue;
            }
            versions.add(version.asObject().get("id").asString());
        }
        return versions;
    }

    /**
     * {@return the latest release}
     *
     * @throws HttpException if the request fails
     * @throws IOException if the response can't be read
     */
    public static String getLatestReleaseVersion() throws HttpException, IOException {
        return VersionManifestClient.getManifest()
                .get("latest").asObject().get("release").asString();
    }

    /**
     * {@return the latest snapshot}
     *
     * @throws HttpException if the request fails
     * @throws IOException if the response can't be read
     */
    public static String getLatestSnapshotVersion() throws HttpException, IOException {
        return VersionManifestClient.getManifest()
                .get("latest").asObject().get("snapshot").asString();
    }
}

