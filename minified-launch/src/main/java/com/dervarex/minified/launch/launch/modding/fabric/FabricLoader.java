package com.dervarex.minified.launch.launch.modding.fabric;

import com.dervarex.minified.launch.launch.modding.Loader;
import org.apiguardian.api.API;

public record FabricLoader(String mcVersion, String loaderVersion) implements Loader {
    @Override
    public String iconUrl() {
        return "https://raw.githubusercontent.com/dervarex/loader-logos/main/fabric/fabric.png";
    }

    @Override
    public String name() { return "FABRIC"; }
}