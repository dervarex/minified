package com.dervarex.minified.modrinth.loaders;

import org.apiguardian.api.API;

/**
 * Plugin platforms Modrinth knows, with the value the API uses for them
 */
@API(status = API.Status.STABLE)
public enum PluginLoader implements Loader {
    /** Paper, a high performance server software based on Spigot */
    PAPER("paper"),
    /** Spigot, a widely used server platform with a large plugin ecosystem */
    SPIGOT("spigot"),
    /** Bukkit, the original server plugin API that Spigot and Paper are based on */
    BUKKIT("bukkit"),
    /** Folia, a Paper fork built around multithreading */
    FOLIA("folia"),
    /** Purpur, a Paper fork focused on customization and extra configuration options */
    PURPUR("purpur"),
    /** Sponge, a plugin platform with its own API and ecosystem */
    SPONGE("sponge");

    private final String apiValue;

    PluginLoader(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String getApiValue() {
        return apiValue;
    }

    /**
     * Finds the loader for a value of the Modrinth API, ignores casing
     *
     * @param value the API value, like {@code fabric}
     * @return the loader, {@code null} for {@code null}
     * @throws IllegalArgumentException if the value is no known loader
     */
    @API(status = API.Status.INTERNAL)
    public static PluginLoader fromApiValue(String value) {
        if (value == null) {
            return null;
        }
        for (PluginLoader loader : values()) {
            if (loader.apiValue.equalsIgnoreCase(value)) {
                return loader;
            }
        }
        throw new IllegalArgumentException("Unknown plugin loader: " + value);
    }
}
