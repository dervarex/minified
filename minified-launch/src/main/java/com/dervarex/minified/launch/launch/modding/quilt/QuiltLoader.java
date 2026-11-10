package com.dervarex.minified.launch.launch.modding.quilt;

import com.dervarex.minified.launch.launch.modding.Loader;
import org.apiguardian.api.API;

/**
 * The <a href="https://quiltmc.org/">Quilt Loader</a>.
 * @param mcVersion minecraft version
 * @param loaderVersion quilt loader version
 */
@API(status = API.Status.STABLE)
public record QuiltLoader(String mcVersion, String loaderVersion) implements Loader {
    /**
     * {@return QUILT}
     */
    @Override
    public String name() { return "QUILT"; }

    /**
     * {@return a URL to the Quilt icon}
     */
    @Override
    public String iconUrl() {
        return "https://raw.githubusercontent.com/dervarex/loader-logos/main/quilt/quilt.png";
    }
}