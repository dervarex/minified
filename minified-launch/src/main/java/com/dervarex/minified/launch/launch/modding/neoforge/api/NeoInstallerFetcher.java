package com.dervarex.minified.launch.launch.modding.neoforge.api;

import com.dervarex.minified.utils.ApiEndpoints;

public final class NeoInstallerFetcher {
    private NeoInstallerFetcher() {
    }

    public static String getInstallerLink(String neoForgeVersion) {
        return ApiEndpoints.NEOFORGE_INSTALLER_BASE_URL + neoForgeVersion + "/neoforge-" + neoForgeVersion + "-installer.jar";
    }
}
