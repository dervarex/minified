package com.dervarex.minified.launch.launch.modding.vanilla;

import com.dervarex.minified.launch.launch.modding.Loader;

public record VanillaLoader(String mcVersion) implements Loader {
    @Override
    public String loaderVersion() {
        return mcVersion;
    }
    @Override
    public String iconUrl() {
        return "https://raw.githubusercontent.com/dervarex/loader-logos/main/vanilla/vanilla.png";
    }
}