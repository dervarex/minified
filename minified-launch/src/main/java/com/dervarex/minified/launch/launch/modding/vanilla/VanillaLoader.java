package com.dervarex.minified.launch.launch.modding.vanilla;

import com.dervarex.minified.launch.launch.modding.Loader;
import org.apiguardian.api.API;

/**
 * Vanilla Minecraft, without a loader
 * @param mcVersion minecraft version
 */
@API(status = API.Status.STABLE)
public record VanillaLoader(String mcVersion) implements Loader {
    /**
     * {@return VANILLA}
     */
    @Override
    public String name() { return "VANILLA"; }
    /**
     * {@return the Minecraft version}
     */
    @Override
    public String loaderVersion() {
        return mcVersion;
    }
    /**
     * {@return a URL to the vanilla icon}
     */
    @Override
    public String iconUrl() {
        return "https://raw.githubusercontent.com/dervarex/loader-logos/main/vanilla/vanilla.png";
    }
}