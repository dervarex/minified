package com.dervarex.minified.launch.launch.modding.quilt;

import com.dervarex.minified.launch.launch.modding.Loader;
import org.apiguardian.api.API;

public record QuiltLoader(String mcVersion, String loaderVersion) implements Loader {
    @Override
    public String name() { return "QUILT"; }

    @Override
    public String iconUrl() {
        return "https://raw.githubusercontent.com/dervarex/loader-logos/main/quilt/quilt.png";
    }
}