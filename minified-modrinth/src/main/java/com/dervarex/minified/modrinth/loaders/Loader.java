package com.dervarex.minified.modrinth.loaders;

import org.apiguardian.api.API;

/**
 * Stuff a project or version can run on, {@link ModLoader}, {@link PluginLoader}, {@link PluginPlatform} or
 * {@link ShaderLoader}.
 */
@API(status = API.Status.STABLE)
public sealed interface Loader permits ModLoader, PluginLoader, PluginPlatform, ShaderLoader {

    /**
     * {@return the value the Modrinth API uses for this loader or platform, like {@code fabric}}
     */
    String getApiValue();
}
