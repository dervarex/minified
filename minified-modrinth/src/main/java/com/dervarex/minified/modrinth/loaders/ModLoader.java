package com.dervarex.minified.modrinth.loaders;

public enum ModLoader {
    FABRIC("fabric"),
    FORGE("forge"),
    NEOFORGE("neoforge"),
    BABRIC("babric"),
    BTA("bta"),
    JAVA_AGENT("javaagent"),
    LEGACY_FABRIC("legacyfabric"),
    LITE_LOADER("liteloader"),
    RISUGAMIS_MODLOADER("risugamis-modloader"),
    NIL_LOADER("nilloader"),
    ORNITHE("ornithe"), // Still convinced this is a typo; whoever named this had one chance...
    QUILT("quilt"),
    RIFT("rift");

    private final String apiValue;

    ModLoader(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    public static ModLoader fromApiValue(String value) {
        if (value == null) {
            return null;
        }
        for (ModLoader loader : values()) {
            if (loader.apiValue.equalsIgnoreCase(value)) {
                return loader;
            }
        }
        throw new IllegalArgumentException("Unknown mod loader: " + value);
    }

    @Override
    public String toString() {
        return apiValue;
    }
}
