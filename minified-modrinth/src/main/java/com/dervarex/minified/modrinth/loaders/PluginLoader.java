package com.dervarex.minified.modrinth.loaders;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public enum PluginLoader {
    PAPER("paper"),
    SPIGOT("spigot"),
    BUKKIT("bukkit"),
    FOLIA("folia"),
    PURPUR("purpur"),
    SPONGE("sponge");

    private final String apiValue;

    PluginLoader(String apiValue) {
        this.apiValue = apiValue;
    }

    @API(status = API.Status.INTERNAL)
    public String getApiValue() {
        return apiValue;
    }

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

    @Override
    public String toString() {
        return apiValue;
    }
}
