package com.dervarex.minified.modrinth.loaders;

import org.apiguardian.api.API;

/**
 * Represents the platform or host plugin a plugin is built for.
 */
public enum PluginPlatform implements Loader {
    /** BungeeCord, a proxy that connects several servers into one network */
    BUNGEECORD("bungeecord"),
    /** Extensions that add functionality directly to Geyser */
    GEYSER_EXTENSION("geyserextension"),
    /** Velocity, a modern, high performance proxy for server networks */
    VELOCITY("velocity"),
    /** Waterfall, a BungeeCord fork focused on performance and stability */
    WATERFALL("waterfall");

    private final String apiValue;

    PluginPlatform(String apiValue) {
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
}
