package com.dervarex.minified.launch.launch.modding.fabric;

import com.dervarex.minified.launch.launch.modding.Loader;

public record FabricLoader(String mcVersion, String loaderVersion) implements Loader {
    @Override
    public String iconUrl() {
        return "https://raw.githubusercontent.com/dervarex/loader-logos/main/fabric/fabric.png";
    }
}