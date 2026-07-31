## Roadmap

* Test and document supported Minecraft versions
* Support older Minecraft versions for all currently supported loaders

**When I have way too much time:**
* [Server Information Implementation](#server-information-implementation)
* MiniLoader - own mod loader focused on directly editing Minecraft's source code. Mods are distributed as `.patch` files, which are merged and applied to the original Minecraft source.

### Abandoned Features
* Own launcher to test the library in a real-world scenario
    * The launcher was supposed to be built using Compose Desktop.
      Unfortunately, Compose Desktop and I had creative differences regarding the definition of a "working build". The feature was therefore sacrificed in favor of preserving my remaining motivation.


---

### Server Information Implementation
Get information from a specific Minecraft server.

**Step 1: Anonymous**

Available info:
* MOTD
* Version name
* Player count (online/max)
* Player sample (maybe)
* Server icon
* Ping

**Step 2: Reverse engineer the MC protocol and log in as an actual user**

From there we can get basically everything:
* Plugin channel (`minecraft:brand`) -> server software name
* Full playerlist with UUID, gamemode, ping and display name
* Dimension, gamemode, difficulty, position
* Time and weather
* Scoreboard
* Resource pack push info

**We will not collect(because of privacy and abuse):**
* Chat or system messages
* The world