<h1 align="center">
  <br>
  <img width="150" src="minified.svg" alt="Minified Logo"> 
  <br>
  <p align="center">Minified</p>
</h1>

<p align="center">
  <a href="https://github.com/dervarex/minified/releases">
    <img src="https://img.shields.io/github/v/release/dervarex/minified?style=for-the-badge&color=orange" alt="Latest Release">
  </a>
  <a href="LICENSE">
    <img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=for-the-badge" alt="License">
  </a>
  
  <br>

  <a href="https://discord.gg/fhbcfMSvBy">
    <img src="https://img.shields.io/discord/1518629359714570392?style=for-the-badge&logo=discord&label=Discord&color=5865F2" alt="Join the Minified Discord"/>
  </a>
  &nbsp;&nbsp;
  <a href="https://fluxer.gg/iXLSs5Wf">
    <img src="https://img.shields.io/badge/Fluxer-Join_the_Community-5865F2?style=for-the-badge&labelColor=555555&logo=data:image/svg+xml;base64,PHN2ZyByb2xlPSJpbWciIHZpZXdCb3g9IjAgMCAyNCAyNCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48dGl0bGU+Rmx1eGVyPC90aXRsZT48cGF0aCBmaWxsPSIjNTg2NUYyIiBkPSJNMTIgMGM2LjYyNyAwIDEyIDUuMzczIDEyIDEyczUuMzczIDEyLTEyIDEyUzAgMTguNjI3IDAgMTIgNS4zNzMgMCAxMiAwTTguNzkgMTIuNDcxcS0xLjA5MiAwLTIuMDc4LjQ5My0uOTc1LjQ5My0xLjU4NiAxLjU3NS0uMzk1LjcxMi0uNTIgMS43MjZjLS4wNzguNjI2LjQ0OCAxLjEzNSAxLjA3OSAxLjEzNS42NDUgMCAxLjEyOC0uNTQzIDEuMjg0LTEuMTdxLjEzMy0uNTMxLjQyOS0uODQ0LjU2OC0uNiAxLjQzNS0uNi41OCAwIDEuMDYxLjI4OS40ODIuMjc5IDEuMjU0Ljk1NCAxLjE3OCAxLjAzOCAyLjA3OCAxLjUxLjkuNDYgMS45OTMuNDYxIDEuMDkzIDAgMi4wNzktLjQ5My45ODUtLjQ5MiAxLjU5Ni0xLjU3NS40MDQtLjcxNC41MjItMS43MzRjLjA3Mi0uNjIzLS40NTUtMS4xMjctMS4wODMtMS4xMjctLjY1IDAtMS4xMzQuNTQ5LTEuMzA3IDEuMTc2YTIuMSAyLjEgMCAwIDEtLjM4Mi43NzRxLS41MzUuNjY1LTEuNDY4LjY2NS0uNTc5IDAtMS4wNS0uMjc5LS40Ni0uMjktMS4yNjQtLjk2NC0xLjE5LS45OTYtMi4wOS0xLjQ3OWE0IDQgMCAwIDAtMS45ODItLjQ5M004Ljc5IDZxLTEuMDkyIDAtMi4wNzguNDkzLS45NzUuNDkyLTEuNTg2IDEuNTc1LS4zOTUuNzEyLS41MiAxLjcyNmMtLjA3OC42MjUuNDQ4IDEuMTM1IDEuMDc5IDEuMTM1LjY0NSAwIDEuMTI4LS41NDMgMS4yODQtMS4xN3EuMTMzLS41MzMuNDI5LS44NDUuNTY4LS42IDEuNDM1LS42LjU4IDAgMS4wNjEuMjkuNDgyLjI3OCAxLjI1NC45NTMgMS4xNzggMS4wNCAyLjA3OCAxLjUxLjkuNDYyIDEuOTkzLjQ2MnQyLjA3OS0uNDkzcS45ODUtLjQ5MyAxLjU5Ni0xLjU3NS40MDQtLjcxNi41MjItMS43MzRjLjA3Mi0uNjI0LS40NTUtMS4xMjctMS4wODMtMS4xMjctLjY1IDAtMS4xMzQuNTQ5LTEuMzA3IDEuMTc1YTIuMSAyLjEgMCAwIDEtLjM4Mi43NzVxLS41MzUuNjY0LTEuNDY4LjY2NC0uNTc5IDAtMS4wNS0uMjc4LS40Ni0uMjktMS4yNjQtLjk2NS0xLjE5LS45OTYtMi4wOS0xLjQ3OEE0IDQgMCAwIDAgOC43OSA2Ii8+PC9zdmc+&v=2" alt="Join the Fluxer Community"/>
</p>

<p align="center">
  A lightweight Java library for downloading, managing and launching Minecraft.<br>
  Supports Vanilla, Forge, NeoForge, Fabric and Quilt.
</p>

---

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
* Event System
* Java Manager
* Modrinth API Integration
* Object Oriented NBT & MCA Parser
* Full Minecraft World Parser with Terrain Editor

[Roadmap](ROADMAP.md)

## Quick Start

### Maven Central

```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation 'io.github.dervarex.minified:minified-launch:3.3.0'
}
```

Releases up to v3.2.3 are only on JitPack, as `com.github.dervarex.minified:<module>:v<version>`.

### Launch Minecraft

```java
LaunchConfiguration config = new LaunchConfiguration.Builder()
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
* `minified-events`
* `minified-java`
* `minified-launch`
* `minified-modrinth`
* `minified-utils`
* `minified-worlds`

## Documentation

Documentation is available in the Javadoc, and on the [Documentation Website](https://dervarex.github.io/minified/)

## Community

Need help, have questions, or want to share your launcher?

Join the Discord server:

https://discord.gg/fhbcfMSvBy

## Credits

Big thanks to [etkmlm](https://github.com/etkmlm) for explaining important details about the launch process and the Forge installer. Without him, I wouldn't have been able to implement the base of Forge support.

Thanks to MinecraftAuth by RaphiMC for providing an excellent authentication library, which is used by Minified's authentication module.

A significant portion of the Modrinth integration was inspired by my earlier project, PandaClient, a Minecraft launcher I developed in 2023–2024. While almost everything of the implementation has since been rewritten and improved for Minified, the original project laid the foundation for this module. I may share the full story behind this repository in the future.

## License

Licensed under the Apache License 2.0.

See the LICENSE file for details.

# Disclaimer

`minified-modrinth` is an unofficial integration with the Modrinth API. It is not affiliated with, endorsed by, or sponsored by Modrinth or Rinth, Inc.

This project is not an official Minecraft service and is not approved by or associated with Mojang or Microsoft.
