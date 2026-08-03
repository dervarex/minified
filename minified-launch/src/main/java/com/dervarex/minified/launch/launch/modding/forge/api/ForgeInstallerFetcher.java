package com.dervarex.minified.launch.launch.modding.forge.api;

import com.dervarex.minified.utils.ApiEndpoints;

public class ForgeInstallerFetcher {
    public static String getInstallerLink(String loaderVersion) {
        return ApiEndpoints.FORGE_INSTALLER_BASE_URL + loaderVersion + "/forge-" + loaderVersion + "-installer.jar";
    }
}