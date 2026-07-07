package com.dervarex.minified.modrinth.loaders;

public enum ShaderLoader {
    IRIS("iris"),
    OPTIFINE("optifine"),
    VANILLA_SHADER("vanilla"),
    CANVAS("canvas");

    private final String apiValue;

    ShaderLoader(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

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

    @Override
    public String toString() {
        return apiValue;
    }
}
