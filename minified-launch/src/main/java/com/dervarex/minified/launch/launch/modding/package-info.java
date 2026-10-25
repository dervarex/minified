/**
 * Modding related code for the launcher.
 * <p>Currently supported ModLoaders:</p>
 * {@link com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader}
 * {@link com.dervarex.minified.launch.launch.modding.fabric.FabricLoader}
 * {@link com.dervarex.minified.launch.launch.modding.quilt.QuiltLoader}
 * {@link com.dervarex.minified.launch.launch.modding.forge.ForgeLoader}
 * {@link com.dervarex.minified.launch.launch.modding.neoforge.NeoforgeLoader}
 * {@link com.dervarex.minified.launch.launch.modding.custom.CustomLoader}
 * <br><br><br><br><br>
 * <h3>What is a Loader?</h3>
 * <p>A loader (commonly known as a <b>Modloader</b>) acts as the bridge between Minecraft and your custom modifications</p>
 * <br>
 * <p>Think of Minecraft as a smartphone and mods as apps:
 * the game itself doesn't know how to run these custom files natively.
 * The loader works like an operating system, it boots up the game, injects the mods into the code at runtime,
 * and provides the necessary hooks (APIs) so that your code can interact with Minecraft's engine</p>
 * <br>
 * <h4>Using Loaders with minified-launch</h4>
 * <p>You can simply edit the loader property in the {@link LaunchConfiguration}
 * to switch between different loaders like {@link VanillaLoader} or {@link FabricLoader}</p>.
 */
package com.dervarex.minified.launch.launch.modding;

import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoader;