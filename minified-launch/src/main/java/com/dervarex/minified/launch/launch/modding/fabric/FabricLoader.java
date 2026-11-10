package com.dervarex.minified.launch.launch.modding.fabric;

import com.dervarex.minified.launch.launch.modding.Loader;
import org.apiguardian.api.API;

/**
 * The <a href="https://fabricmc.net/">Fabric Loader</a>.
 * @param mcVersion minecraft version
 * @param loaderVersion fabric loader version
 */
@API(status = API.Status.STABLE)
public record FabricLoader(String mcVersion, String loaderVersion) implements Loader {
    /**
     * {@return a URL to the Fabric icon}
     */
    @Override
    public String iconUrl() {
        return "https://raw.githubusercontent.com/dervarex/loader-logos/main/fabric/fabric.png";
    }

    /**
     * {@return FABRIC}
     */
    @Override
    public String name() { return "FABRIC"; }
}