package com.dervarex.minified.launch.version;

import com.dervarex.minified.utils.exceptions.HttpException;
import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings("unused")
@Deprecated(forRemoval = true)
public class VersionProvider {
    public static ArrayList<String> getVersions() {
        try {
            return VersionListProvider.getVersions();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public static ArrayList<String> getReleaseVersions() {
        try {
            return VersionListProvider.getReleaseVersions();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public static String getVersionJson(String version) {
        try {
            return VersionMetadataProvider.getVersionJsonUrl(version);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getLatestReleaseVersion() throws HttpException, IOException {
        return VersionListProvider.getLatestReleaseVersion();
    }
    public static String getLatestSnapshotVersion() throws HttpException, IOException {
        return VersionListProvider.getLatestSnapshotVersion();
    }
    public static int getMinimumJavaVersion(String version)  throws HttpException, IOException {
        return VersionMetadataProvider.getMinimumJavaVersion(version);
    }
    public static String getMainClass(String version)  throws HttpException, IOException {
        return VersionMetadataProvider.getMainClass(version);
    }
}
