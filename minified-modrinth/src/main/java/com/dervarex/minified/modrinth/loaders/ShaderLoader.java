package com.dervarex.minified.modrinth.loaders;

import org.apiguardian.api.API;

/**
 * Shader loaders that Modrinth knows and how the api calls them
 */
public enum ShaderLoader implements Loader {
    /** Iris, the modern shader loader */
    IRIS("iris"),
    /** OptiFine, an optimization mod with built-in shader support, replaced by modern alternatives */
    OPTIFINE("optifine"),
    /** Minecraft's built-in shader system. */
    VANILLA_SHADER("vanilla"),
    /** Canvas, an alternative renderer with support for advanced visual effects */
    CANVAS("canvas");

    private final String apiValue;

    ShaderLoader(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String getApiValue() {
        return apiValue;
    }

    @API(status = API.Status.INTERNAL)
    public static ShaderLoader fromApiValue(String value) {
        if (value == null) {
            return null;
        }
        for (ShaderLoader loader : values()) {
            if (loader.apiValue.equalsIgnoreCase(value)) {
                return loader;
            }
        }
        throw new IllegalArgumentException("Unknown shader loader: " + value);
    }
}
