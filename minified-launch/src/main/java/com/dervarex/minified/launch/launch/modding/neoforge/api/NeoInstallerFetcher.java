package com.dervarex.minified.launch.launch.modding.neoforge.api;

import com.dervarex.minified.utils.ApiEndpoints;
import org.apiguardian.api.API;

public final class NeoInstallerFetcher {
    private NeoInstallerFetcher() {
    }

    @API(status = API.Status.STABLE)
    public static String getInstallerLink(String neoForgeVersion) {
        return ApiEndpoints.NEOFORGE_INSTALLER_BASE_URL + neoForgeVersion + "/neoforge-" + neoForgeVersion + "-installer.jar";
    }
}
