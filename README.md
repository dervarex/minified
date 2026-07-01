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
    implementation 'com.github.dervarex.minified:minified-launch:v1.2.2'
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

## Documentation

Documentation is available in the Javadoc.

## Roadmap

* Modrinth API integration
* More documentation (mostly cleanup of existing documentation)
* Usage examples

### Abandoned Features

* Own launcher to test the library in a real-world scenario

    * The launcher was supposed to be built using Compose Desktop.

      Unfortunately, Compose Desktop and I had creative differences regarding the definition of a "working build". The feature was therefore sacrificed in favor of preserving my remaining motivation.

## License

Licensed under the Apache License 2.0.

See the LICENSE file for details.

## Community

Need help, have questions, or want to share your launcher?

Join the Discord server:

https://discord.gg/fhbcfMSvBy

## Credits

Big thanks to [etkmlm](https://github.com/etkmlm) for explaining important details about the launch process and the Forge installer. Without him, I wouldn't have been able to implement Forge support.

Thanks to `net.raphimc.MinecraftAuth` for providing a great library for Minecraft authentication, which is used by Minified's authentication module.
