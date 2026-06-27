package com.dervarex.minified.launch.launch.modding.neoforge.api;

import com.dervarex.minified.launch.ApiEndpoints;

public final class NeoInstallerFetcher {
    private NeoInstallerFetcher() {
    }

    public static String getInstallerLink(String neoForgeVersion) {
        return ApiEndpoints.NeoForgeInstallerBaseUrl + neoForgeVersion + "/neoforge-" + neoForgeVersion + "-installer.jar";
    }
}
