package com.dervarex.minified.launch.launch.modding;

import org.apiguardian.api.API;

/**
 * A Minecraft Modloader.
 */
@API(status = API.Status.STABLE)
public interface Loader {
    /**
     * Used internally for the inbuild JSON Profile Factory, but can also be used externally
     */
    String name();
    String mcVersion();
    String loaderVersion();
    String iconUrl();
}