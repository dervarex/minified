package com.dervarex.minified.launch.launch.modding.neoforge;

import com.dervarex.minified.launch.launch.modding.Loader;
import org.apiguardian.api.API;

/**
 * The <a href="https://neoforged.net/">Neoforged Loader</a>.
 * @param mcVersion minecraft version
 * @param loaderVersion neoforge loader version
 */
@API(status = API.Status.STABLE)
public record NeoforgeLoader(String mcVersion, String loaderVersion) implements Loader {
    @Override
    public String iconUrl() {
        return "https://raw.githubusercontent.com/dervarex/loader-logos/main/neoforge/neoforge.png";
    }

    @Override
    public String name() { return "NEOFORGE"; }
}