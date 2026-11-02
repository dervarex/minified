package com.dervarex.minified.launch.launch.modding.forge;

import com.dervarex.minified.launch.launch.modding.Loader;
import org.apiguardian.api.API;

/**
 * The <a href="files.minecraftforge.net">Forge Loader</a>.
 * @param mcVersion minecraft version
 * @param loaderVersion forge loader version
 */
@API(status = API.Status.STABLE)
public record ForgeLoader(String mcVersion, String loaderVersion) implements Loader {
    @Override
    public String iconUrl() {
        return "https://raw.githubusercontent.com/dervarex/loader-logos/main/forge/forge-square.png";
    }

    @Override
    public String name() { return "FORGE"; }
}