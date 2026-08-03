package com.dervarex.minified.utils.version;

import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.json.JsonValue;

import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings("unused")
public final class VersionListProvider {
    private VersionListProvider() {
    }

    public static ArrayList<String> getVersions() throws HttpException, IOException {
        ArrayList<String> versions = new ArrayList<>();
        for (JsonValue version : VersionManifestClient.getVersions()) {
            versions.add(version.asObject().get("id").asString());
        }
        return versions;
    }

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

    public static String getLatestReleaseVersion() throws HttpException, IOException {
        return VersionManifestClient.getManifest()
                .get("latest").asObject().get("release").asString();
    }

    public static String getLatestSnapshotVersion() throws HttpException, IOException {
        return VersionManifestClient.getManifest()
                .get("latest").asObject().get("snapshot").asString();
    }
}

