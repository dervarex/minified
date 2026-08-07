## Roadmap

* convert classes to record classes
* minified-worlds
* Test and document supported Minecraft versions
* Support older Minecraft versions for all currently supported loaders

**When I have way too much time:**
* MiniLoader - own mod loader focused on directly editing Minecraft's source code. Mods are distributed as `.patch` files, which are merged and applied to the original Minecraft source.

### Abandoned Features
* Own launcher to test the library in a real-world scenario
    * The launcher was supposed to be built using Compose Desktop.
      Unfortunately, Compose Desktop and I had creative differences regarding the definition of a "working build". The feature was therefore sacrificed in favor of preserving my remaining motivation.
* **Server Management**
    * No concrete plan yet for what this should do or how the launcher would handle it, may be reconsidered when requested