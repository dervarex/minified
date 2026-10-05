---

title: Quilt Loader

---

The Quilt Loader is the foundation of the Quilt ecosystem. It loads Quilt mods and provides the runtime environment, which can be extended through the <a href="./qsl" class="link">Quilt Standard Libraries (QSL)</a>.

### What the Quilt Loader does

**Mod Initialization**<br />
It integrates with Minecraft's startup process to discover, load, and initialize Quilt mods in the correct order.

**Dependency Management**<br />
It verifies that all required dependencies (such as <a href="./qsl" class="link">QSL</a>) are installed, resolves version requirements, and prevents incompatible mod combinations from loading.

**Class Transformation**<br />
It uses <a href="./mixins" class="link">Mixins</a> to safely modify Minecraft's bytecode at runtime without directly changing the game's files.

**Fabric Compatibility**<br />
Quilt Loader maintains compatibility with most Fabric mods, allowing many existing Fabric projects to run on Quilt without modification.
