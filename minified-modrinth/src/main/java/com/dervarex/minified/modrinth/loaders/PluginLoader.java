package com.dervarex.minified.modrinth.loaders;

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

    public String getApiValue() {
        return apiValue;
    }

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
