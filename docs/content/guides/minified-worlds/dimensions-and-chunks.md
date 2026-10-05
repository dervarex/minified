---
title: Dimensions and Chunks
description: Understand how terrain is stored and read chunks and blocks
---


This guide covers the part of a world that everyone thinks of first: the actual terrain. It's here to help to understand how Minecraft organizes that terrain on disk, since the API mirrors it closely.

## 1. How terrain is organized

A world is split into up to three dimensions: the overworld, the nether and the end. Each one lives in its own folder under `dimensions/minecraft/`, and each is completely independent, they have their own terrain, their own force loaded chunks, and so on.

Inside a dimension, terrain is split into **chunks**. A chunk is a 16 by 16 column of blocks, going from the bottom of the world to the top. Chunks are identified by chunk coordinates, which are just block coordinates divided by 16, rounded down. So block `(35, 70, -12)` sits inside chunk `(2, -1)`.

A chunk itself is further split vertically into **sections**, each one a 16x16x16 cube of blocks. A tall chunk might have dozens of sections stacked on top of each other, each with its own `Y` value (section `0` covers world Y 0 to 15, section `-1` covers Y -16 to -1, and so on).

Finally, chunks aren't stored one file per chunk, that would be a lot of tiny files. Instead, groups of 32x32 chunks are bundled together into a single **region file**, named like `r.0.0.mca`. There are actually three kinds of region files per dimension:

<div class="fields-details" open>
  <div class="fields-grid">

    <div class="field-card">
      <div class="field-header">
        <code>region/</code>
      </div>
      <p>Stores the chunk's blocks and biomes.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>entities/</code>
      </div>
      <p>Stores everything living or moving in that chunk: mobs, dropped items, minecarts, and so on.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>poi/</code>
      </div>
      <p>Stores points of interest, like job sites, beds and bells, mostly used by villagers.</p>
    </div>

  </div>
</div>

`minified-worlds` handles picking the right region file and the right offset inside it for you, you just ask for a chunk by its chunk coordinates.

## 2. Opening a dimension

Each dimension is represented by a small subclass of `Dimension`: `Overworld`, `Nether` and `End`. They all take the world folder in their constructor.

<Tabs>
  <TabItem label="Java">

```java
Overworld overworld = new Overworld(worldDirectory.toFile());
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val overworld = Overworld(worldDirectory.toFile())
```

  </TabItem>
</Tabs>

If you don't know or don't care which dimension you're dealing with, you can also construct a plain `Dimension` from a dimension folder directly, and it figures out the type from the folder's name (`overworld`, `the_nether` or `the_end`):

<Tabs>
  <TabItem label="Java">

```java
File dimensionFolder = new File(worldDirectory.toFile(), "dimensions/minecraft/the_nether");
Dimension nether = new Dimension(dimensionFolder);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val dimensionFolder = File(worldDirectory.toFile(), "dimensions/minecraft/the_nether")
val nether = Dimension(dimensionFolder)
```

  </TabItem>
</Tabs>

`End` also loads `ender_dragon_fight.dat` for you, available through `getEnderDragonFight()`, which tells you things like how many times the dragon has been defeated in that world.

## 3. Reading a chunk

Call `readChunk(chunkX, chunkZ)` with chunk coordinates, not block coordinates. It reads the right region file, finds the chunk inside it, and wraps it in a `Chunk` object. If that chunk hasn't been generated yet, you get `null` back instead of an error, so always check for that.

<Tabs>
  <TabItem label="Java">

```java
Chunk chunk = overworld.readChunk(2, -1);
if (chunk == null) {
    System.out.println("That chunk hasn't been generated yet.");
    return;
}
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val chunk = overworld.readChunk(2, -1)
if (chunk == null) {
    println("That chunk hasn't been generated yet.")
    return
}
```

  </TabItem>
</Tabs>

## 4. Reading and writing blocks

Once you have a `Chunk`, `getBlock` and `setBlock` take plain block coordinates (not local, not chunk relative, the same coordinates you'd see in game with F3 open).

<Tabs>
  <TabItem label="Java">

```java
BlockState block = chunk.getBlock(35, 70, -12);
System.out.println(block.name()); // e.g. "minecraft:stone"

chunk.setBlock(35, 70, -12, BlockState.of("minecraft:diamond_block"));
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val block = chunk.getBlock(35, 70, -12)
println(block.name) // e.g. "minecraft:stone"

chunk.setBlock(35, 70, -12, BlockState.of("minecraft:diamond_block"))
```

  </TabItem>
</Tabs>

A `BlockState` is just a block name plus a map of properties, for blocks that have them, like `facing`, `waterlogged` or `half`.

<Tabs>
  <TabItem label="Java">

```java
Map<String, String> properties = Map.of("facing", "north", "waterlogged", "false");
BlockState state = new BlockState("minecraft:oak_stairs", properties);
chunk.setBlock(10, 64, 10, state);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val properties = mapOf("facing" to "north", "waterlogged" to "false")
val state = BlockState("minecraft:oak_stairs", properties)
chunk.setBlock(10, 64, 10, state)
```

  </TabItem>
</Tabs>

If you set a block in a section that doesn't exist yet (say, you're building far above the existing terrain), the chunk quietly creates that section for you as air first, so you never have to create sections manually just to place a block.

### Why blocks aren't stored as one id per block

It's worth knowing this, even if the library hides it from you. Minecraft doesn't store a block id for every single one of the 4096 blocks in a section. Most sections only use a handful of different block types (stone, dirt, air, and maybe one or two ores), so storing a full id for every block would waste a lot of space.

Instead, each section keeps a **palette**: a small list of the block states actually used in that section. Every block position then just stores an index into that palette, packed as tightly as possible (using only as many bits as needed to cover the palette's size). If a whole section is just one block, like a section of pure stone, the palette has a single entry and no index data is stored at all, since there's nothing to choose between.

This is also why adding a brand new block type to a section can be a bit of work behind the scenes: the palette grows, and if it crosses a size that needs one more bit per entry, the whole packed array has to be rebuilt with the wider bit width. `minified-worlds` does all of that for you inside `setBlock`.

## 5. Biomes

Biomes work almost the same way as blocks, palette and all, except they're stored per 4x4x4 block area instead of per block, since biomes rarely need to change at a finer resolution than that.

`Chunk` doesn't expose biome reading and writing directly right now, that lives one level down on `ChunkSection`. What `Chunk` does handle for you is picking a sensible biome whenever a brand new section gets created: it looks at the sections directly above and below it and reuses whichever biome shows up most between the two, falling back to `minecraft:plains` if the chunk has no sections at all yet. So as long as you're just placing blocks with `setBlock`, new sections still end up with a reasonable biome without any extra work.

## 6. Saving chunk changes

Changes you make with `setBlock` happen in memory. To write them back to disk, pass the chunk to `saveChunkData`.

<Tabs>
  <TabItem label="Java">

```java
overworld.saveChunkData(chunk);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
overworld.saveChunkData(chunk)
```

  </TabItem>
</Tabs>

This writes into the chunk's region file, creating that file if it didn't exist yet (useful if you're generating brand new chunks rather than editing existing ones).

## 7. Closing the dimension

`Dimension` keeps region files open while you're reading and writing, to avoid reopening the same file over and over. Once you're done, call `close()` to release those file handles.

<Tabs>
  <TabItem label="Java">

```java
overworld.close();
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
overworld.close()
```

  </TabItem>
</Tabs>

## Next steps

Chunks also carry entities and points of interest, stored separately from the terrain itself.

<div class="fields-details" open>
  <div class="fields-grid">

    <a class="field-card-link" href="/minified-docs/guides/minified-worlds/entities-and-poi">
      <div class="field-card">
        <div class="field-header">
          <code>Entities and Points of Interest</code>
        </div>
        <p>Learn how to read and add the mobs, items and villager points of interest living inside a chunk.</p>
      </div>
    </a>

  </div>
</div>
