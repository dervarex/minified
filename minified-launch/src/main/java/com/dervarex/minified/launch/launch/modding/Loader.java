package com.dervarex.minified.launch.launch.modding;

import org.apiguardian.api.API;

/**
 * A Minecraft Modloader.
 */
@API(status = API.Status.STABLE)
public interface Loader {
    /**
     * Used internally for the inbuild JSON Profile Factory, but can also be used externally
     *
     * @return the name in uppercase, like FABRIC
     */
    String name();

    /**
     * {@return the Minecraft version}
     */
    String mcVersion();

    /**
     * {@return the loader version}
     */
    String loaderVersion();

    /**
     * {@return a URL to the loader's icon}
     */
    String iconUrl();
}