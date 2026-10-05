---
title: Loader
description: Learn how to use the Loaders to launch a modified Minecraft client.
---

## 1. What is a Loader?

A loader (commonly known as a **Modloader**) acts as the bridge between Minecraft and your custom modifications. 

Think of Minecraft as a smartphone and mods as apps: the game itself doesn't know how to run these custom files natively. The loader works like an operating system - it boots up the game, injects the mods into the code at runtime, and provides the necessary hooks (APIs) so that your code can interact with Minecraft's engine.

### Popular Loaders in the Ecosystem:
* **Vanilla:** The unmodified, pure version of Minecraft. Not really a modloader, but we'll include it here for completeness.
* **NeoForge / Forge:** The traditional, powerful heavyweight for large-scale modpacks.
* **Fabric:** A lightweight, ultra-fast alternative favored for modern, highly optimized projects.
* **Quilt:** A modern fork of Fabric with a focus on community and compatibility.

## 2. Using Loaders with Minified Launch
You can simply edit the `loader` property in the `LaunchConfiguration` to switch between different loaders like `VanillaLoader` or `FabricLoader`.

And that's it! Minified abstracts away the complexities of each loader, allowing you to focus on your own stuff. 

## Currently Supported Loaders:

* **VanillaLoader**
  Launches the original, unmodified Minecraft game directly from the Mojang servers. Perfect for standard vanilla setups or testing baseline performance.

* **FabricLoader**
  Powers the modern, lightweight Fabric ecosystem. It boots the game incredibly fast and uses minimal resources compared to traditional loaders. Ideal for highly optimized, modular setups.

* **QuiltLoader**
  A fork of Fabric designed to offer enhanced developer tools while maintaining high backward compatibility meaning most Fabric mods will run here out of the box.

* **NeoforgeLoader**
  The official successor to the original Forge project. This is the new powerhouse for massive modpacks and complex gameplay overhauls on modern Minecraft versions.

* **ForgeLoader**
  The legacy pioneer of Minecraft modding. Primarily used for older Minecraft versions today, as most classic, heavyweight mods from recent years were built on top of it.

* **CustomLoader**
  For loaders Minified doesn't support out of the box. You provide the main class, JVM/game arguments and classpath entries yourself, and Minified won't touch installation - it just launches the game the way you configured it.
