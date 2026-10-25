/**
 * Modding related code for the launcher.
 * <p>Currently supported ModLoaders:</p>
 * <pre>
 * - {@link VanillaLoader}
 * - {@link FabricLoader}
 * - {@link QuiltLoader}
 * - {@link ForgeLoader}
 * - {@link NeoforgeLoader}
 * - {@link CustomLoader}
 * </pre>
 * <h2>What is a Loader?</h2>
 * <p>A loader (commonly known as a <b>Modloader</b>) acts as the bridge between Minecraft and your custom modifications</p>
 * <br>
 * <p>Think of Minecraft as a smartphone and mods as apps:
 * the game itself doesn't know how to run these custom files natively.
 * The loader works like an operating system, it boots up the game, injects the mods into the code at runtime,
 * and provides the necessary hooks (APIs) so that your code can interact with Minecraft's engine</p>
 * <br>
 * <h3>Using Loaders with minified-launch</h3>
 * <p>You can simply edit the loader property in the {@link LaunchConfiguration}
 * to switch between different loaders like {@link VanillaLoader} or {@link FabricLoader}</p>.
 */
package com.dervarex.minified.launch.launch.modding;

import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoader;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoader;
import com.dervarex.minified.launch.launch.modding.forge.ForgeLoader;
import com.dervarex.minified.launch.launch.modding.neoforge.NeoforgeLoader;
import com.dervarex.minified.launch.launch.modding.custom.CustomLoader;