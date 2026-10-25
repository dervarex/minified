package com.dervarex.minified.launch.launch.modding.forge.api;

import com.dervarex.minified.utils.ApiEndpoints;
import org.apiguardian.api.API;

/**
 * Fetches the Forge Installer
 */
public class ForgeInstallerFetcher {
    /**
     * @param loaderVersion loader version to fetch the loader of
     * @return the forge installer download url for the given loader version
     */
    @API(status = API.Status.STABLE)
    public static String getInstallerLink(String loaderVersion) {
        return ApiEndpoints.FORGE_INSTALLER_BASE_URL + loaderVersion + "/forge-" + loaderVersion + "-installer.jar";
    }
}