package com.dervarex.minified.launch.launch.modding.forge.api;

public class ForgeInstallerFetcher {
    public static String getInstallerLink(String loaderVersion) {
        return "https://maven.minecraftforge.net/net/minecraftforge/forge/" + loaderVersion + "/forge-" + loaderVersion + "-installer.jar";
    }
}