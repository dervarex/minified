package com.dervarex.minified.launch.launch.modding.forge.api;

import com.dervarex.minified.launch.ApiEndpoints;

public class ForgeInstallerFetcher {
    public static String getInstallerLink(String loaderVersion) {
        return ApiEndpoints.ForgeInstallerBaseUrl + loaderVersion + "/forge-" + loaderVersion + "-installer.jar";
    }
}