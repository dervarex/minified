package com.dervarex.minified.modrinth.loaders;

import org.apiguardian.api.API;

/**
 * Mod loaders that Modrinth knows,and the value the API uses
 */
@API(status = API.Status.STABLE)
public enum ModLoader implements Loader {
    /** Fabric, a lightweight and widely used modding toolchain. */
    FABRIC("fabric"),
    /** Forge, one of the oldest and largest Minecraft modding platforms out there */
    FORGE("forge"),
    /** NeoForge, a modern modding platform originally forked from Forge */
    NEOFORGE("neoforge"),
    /** Babric, a Fabric based modding toolchain for beta versions */
    BABRIC("babric"),
    /** Mods for "Better Than Adventure!" */
    BTA("bta"),
    /** Mods loaded through a Java agent */
    JAVA_AGENT("javaagent"),
    /** Legacy Fabric */
    LEGACY_FABRIC("legacyfabric"),
    /** LiteLoader, a lightweight loader that was used mostly client side */
    LITE_LOADER("liteloader"),
    /** Risugami's ModLoader, one of the first loaders */
    RISUGAMIS_MODLOADER("risugamis-modloader"),
    /** NilLoader, a lightweight loader for modifying older Minecraft versions */
    NIL_LOADER("nilloader"),
    /** Ornithe, a modding ecosystem for older Minecraft versions */
    ORNITHE("ornithe"),
    /** Quilt, a Fabric fork with its own ecosystem. */
    QUILT("quilt"),
    /** Rift, a lightweight mod loader built for Minecraft 1.13 */
    RIFT("rift");

    private final String apiValue;

    ModLoader(String apiValue) {
        this.apiValue = apiValue;
    }

    @Override
    public String getApiValue() {
        return apiValue;
    }

    @API(status = API.Status.INTERNAL)
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
}
