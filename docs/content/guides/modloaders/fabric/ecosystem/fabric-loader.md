---
title: Fabric Loader

---

The Fabric Loader is the foundation of the Fabric ecosystem. It loads mods and provides the runtime environment, which can be extended through the <a href="./fabric-api" class="link">Fabric API</a>.

### What the Fabric loader does

**Mod Initialization**<br />
It hooks into the game's startup sequence to load and execute mod code properly.

**Dependency Management**<br />
It checks your mods, verifies that required dependencies (like the <a href="./fabric-api" class="link">Fabric API</a>) are present, and prevents version conflicts.

**Class Transformation**<br />
It uses a tool called <a href="./mixins" class="link">Mixins</a>) to safely modify existing game code without heavily altering the core game.
