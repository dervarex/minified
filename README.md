# Minified

A lightweight Java library for downloading, managing and launching Minecraft.

Supports Vanilla, Forge, NeoForge, Fabric and Quilt.

Minified provides a high-level API for authentication, version management, asset downloading, library resolution and game launching without requiring developers to reimplement Mojang's launcher logic.

## Features

* Version management
* Asset downloading
* Library downloading
* Native extraction
* Minecraft launching
* Microsoft account authentication (simplified wrapper around `net.raphimc.MinecraftAuth`)
* Session management
* Forge support
* NeoForge support
* Fabric support
* Quilt support

## Quick Start

### JitPack

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.dervarex.minified:minified-launch:v2.1.0'
}
```

### Launch Minecraft

```java
LaunchConfigurator config = new LaunchConfigurator.Builder()
        .downloadThreads(10)
        .launcherName("MinifiedLauncher")
        .launcherVersion("1.0.0")
        .assetsDirectory(Path.of("<assets-directory>"))
        .librariesDirectory(Path.of("<libraries-directory>"))
        .jarFile(Path.of("<client.jar>"))
        .isDemoUser(false)
        .loader(new VanillaLoader("1.21.11"))
        .build();

Launcher.launchMinecraft(
        user,
        config
);
```

Passing `null` as the user launches Minecraft in offline mode.

## Available Modules

* `minified-auth`
* `minified-java`
* `minified-launch`
* `minified-utils`
* `minified-modrinth`

## Documentation

Documentation is available in the Javadoc, and on the [Documentation Website](https://dervarex.github.io/minified-docs/)

## Roadmap

* Test and document supported Minecraft versions
* Support older Minecraft versions for all currently supported loaders

**When I have way too much time:**

* MiniLoader — a custom mod loader focused on directly editing Minecraft's source code. Mods are distributed as `.patch` files, which are merged and applied to the original Minecraft source.

### Abandoned Features

* Own launcher to test the library in a real-world scenario

    * The launcher was supposed to be built using Compose Desktop.

      Unfortunately, Compose Desktop and I had creative differences regarding the definition of a "working build". The feature was therefore sacrificed in favor of preserving my remaining motivation.

## Community

Need help, have questions, or want to share your launcher?

Join the Discord server:

https://discord.gg/fhbcfMSvBy

## Credits

Big thanks to [etkmlm](https://github.com/etkmlm) for explaining important details about the launch process and the Forge installer. Without him, I wouldn't have been able to implement Forge support.

Thanks to MinecraftAuth by RaphiMC for providing an excellent authentication library, which is used by Minified's authentication module.

A significant portion of the Modrinth integration was inspired by my earlier project, PandaClient, a Minecraft launcher I developed in 2023–2024. While much of the implementation has since been rewritten and improved for Minified, the original project laid the foundation for this module. I may share the full story behind this repository in the future.

## License

Licensed under the Apache License 2.0.

See the LICENSE file for details.

# Disclaimer

`minified-modrinth` is an unofficial integration with the Modrinth API. It is not affiliated with, endorsed by, or sponsored by Modrinth or Rinth, Inc.

This project is not an official Minecraft service and is not approved by or associated with Mojang or Microsoft.
