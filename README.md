# Minified

A lightweight Java library for downloading, managing and launching Minecraft.

Supports Vanilla, Forge, Fabric and Quilt.

Minified provides a high-level API for authentication, version management, asset downloading, library resolution and game launching without requiring developers to reimplement Mojang's launcher logic.

## Features

* Version management
* Asset downloading
* Library downloading
* Native extraction
* Minecraft launching
* Microsoft account authentication
* Session management
* Forge support
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
        .launcherName("MyLauncher")
        .launcherVersion("1.0.0")
        .assetsDirectory(Path.of("assets"))
        .librariesDirectory(Path.of("libraries"))
        .jarFile(Path.of("client.jar"))
        .loader(Loader.Vanilla)
        .build();

Launcher.launchMinecraft(
        "1.21.11",
        user,
        config
);
```

Passing `null` as the user launches Minecraft in offline mode.

## Available Modules

- `minified-auth`
- `minified-java`
- `minified-launch`
- `minified-utils`

## Documentation

Documentation is available in the Javadoc.

## Testing

The project currently contains mostly functional and integration tests.

These tests verify real-world workflows such as authentication, asset downloading, library resolution, native extraction and Minecraft launching.

One of the integration tests performs a complete launch workflow, including:

* Microsoft device code authentication
* Session restoration
* User authentication
* Asset downloading
* Library downloading
* Native extraction
* Minecraft launching

The existing tests are intended to validate functionality and are not necessarily examples of recommended API usage.

More unit tests and dedicated usage examples are planned in the future.

## Roadmap

* NeoForged support
* Only use os-specific natives instead of downloading and extracting all natives
* Fix Java Manager
* Custom Modloader versions
* Modrinth API Integration
* Custom offline username support
* Clean up tests
* More unit tests
* More integration tests
* More documentation(mostly cleanup of existing documentation)
* More usage examples


### Abandoned Features

* Own launcher to test the library in a real-world scenario

    * The launcher was supposed to be built using Compose Desktop. 
      
      Unfortunately, Compose Desktop and I had creative differences regarding the definition of a "working build". The feature was therefore sacrificed in favor of preserving my remaining motivation.

## License

Licensed under the Apache License 2.0.

See the LICENSE file for details.

## Credits

Big thanks to [etkmlm](https://github.com/etkmlm) for explaining me important details about the Launch Process and the Forge installer, without him, I wouldn't have been able to implement Forge support.
