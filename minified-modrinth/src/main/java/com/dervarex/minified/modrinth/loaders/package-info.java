/**
 * Loaders that modrinth currently supports, with the value the API uses for each:
 * <ul>
 *     <li>{@link com.dervarex.minified.modrinth.loaders.ModLoader}: mod loaders</li>
 *     <li>{@link com.dervarex.minified.modrinth.loaders.PluginLoader}: server plugin loaders</li>
 *     <li>{@link com.dervarex.minified.modrinth.loaders.PluginPlatform}: proxies and other platforms plugins are built for</li>
 *     <li>{@link com.dervarex.minified.modrinth.loaders.ShaderLoader}: shader loaders</li>
 * </ul>
 */
@API(status = API.Status.STABLE)
package com.dervarex.minified.modrinth.loaders;

import org.apiguardian.api.API;