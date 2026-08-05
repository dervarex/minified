package com.dervarex.minified.launch.launch.modding.neoforge;

import com.dervarex.minified.launch.launch.modding.Loader;
import org.apiguardian.api.API;

public record NeoforgeLoader(String mcVersion, String loaderVersion) implements Loader {
    @Override
    public String iconUrl() {
        return "https://raw.githubusercontent.com/dervarex/loader-logos/main/neoforge/neoforge.png";
    }

    @Override
    public String name() { return "NEOFORGE"; }
}