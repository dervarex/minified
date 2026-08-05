package com.dervarex.minified.modrinth.loaders;

import org.apiguardian.api.API;

/**
 * Represents the platform or host plugin a plugin is built for.
 */
public enum PluginPlatform {
    BUNGEECORD("bungeecord"),
    GEYSER_EXTENSION("geyserextension"),
    VELOCITY("velocity"),
    WATERFALL("waterfall");

    private final String apiValue;

    PluginPlatform(String apiValue) {
        this.apiValue = apiValue;
    }

    @API(status = API.Status.INTERNAL)
    public String getApiValue() {
        return apiValue;
    }

    @API(status = API.Status.INTERNAL)
    public static PluginPlatform fromApiValue(String value) {
        if (value == null) {
            return null;
        }
        for (PluginPlatform platform : values()) {
            if (platform.apiValue.equalsIgnoreCase(value)) {
                return platform;
            }
        }
        throw new IllegalArgumentException("Unknown plugin platform: " + value);
    }

    @Override
    public String toString() {
        return apiValue;
    }
}
