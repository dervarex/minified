---
title: Entities and Points of Interest
description: Read and add entities and points of interest for a chunk
---


Terrain isn't the only thing stored per chunk. Every chunk also has its own entities (mobs, dropped items, minecarts) and its own points of interest (job sites, beds, bells and similar blocks villagers care about). Both are stored separately from the chunk's blocks, in their own region files, but they're looked up the same way: by chunk coordinates, through the `Dimension` you already opened in the <a href="/minified-docs/guides/minified-worlds/dimensions-and-chunks" class="link">Dimensions and Chunks</a> guide.

## 1. Reading entities

`readEntities(chunkX, chunkZ)` gives you an `EntityData` object, or `null` if that chunk's entity file doesn't have data for it yet.

<Tabs>
  <TabItem label="Java">

```java
EntityData entityData = overworld.readEntities(2, -1);
if (entityData == null) {
    return;
}

for (Entity entity : entityData.entities()) {
    System.out.println(entity.id() + " at " + Arrays.toString(entity.pos()));
}
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val entityData = overworld.readEntities(2, -1) ?: return

for (entity in entityData.entities()) {
    println("${entity.id()} at ${entity.pos().contentToString()}")
}
```

  </TabItem>
</Tabs>

Each entity comes back wrapped as either an `Entity` or a `LivingEntity`. `minified-worlds` tells them apart by checking whether the entity's NBT has a `Health` field, which every living entity (mobs, players, animals) has and non living ones (dropped items, minecarts, arrows) don't. `LivingEntity` extends `Entity` and adds the extra fields that only make sense for something alive, like health.

<details class="fields-details">
  <summary class="fields-summary">
    <span>
      <strong>Entity fields</strong>
      <small>Common to every entity</small>
    </span>
    <span class="fields-chevron" aria-hidden="true">›</span>
  </summary>

  <div class="fields-grid">

    <div class="field-card">
      <div class="field-header">
        <code>id</code>
        <span class="type-badge">String</span>
      </div>
      <p>The entity's type, like <code>minecraft:zombie</code>.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>uuid</code>
        <span class="type-badge">UUID</span>
      </div>
      <p>The entity's unique id.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>pos</code>
        <span class="type-badge">double[]</span>
      </div>
      <p>The entity's position, as <code>[x, y, z]</code>.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>rotation</code>
        <span class="type-badge">float[]</span>
      </div>
      <p>The entity's facing, as <code>[yaw, pitch]</code>.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>onGround</code>
        <span class="type-badge">boolean</span>
      </div>
      <p>Whether the entity is currently touching the ground.</p>
    </div>

  </div>
</details>

<details class="fields-details">
  <summary class="fields-summary">
    <span>
      <strong>LivingEntity fields</strong>
      <small>Extends Entity</small>
    </span>
    <span class="fields-chevron" aria-hidden="true">›</span>
  </summary>

  <div class="fields-grid">

    <div class="field-card">
      <div class="field-header">
        <code>health</code>
        <span class="type-badge">float</span>
      </div>
      <p>The entity's current health.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>canPickUpLoot</code>
        <span class="type-badge">boolean</span>
      </div>
      <p>Whether the entity can pick up dropped items.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>persistenceRequired</code>
        <span class="type-badge">boolean</span>
      </div>
      <p>Whether the entity is exempt from despawning.</p>
    </div>

  </div>
</details>

Both classes have quite a few more fields than what's listed here, `raw()` gives you the full NBT if you need something that isn't wrapped yet.

## 2. Adding an entity

If you want to spawn something into a chunk yourself, build the entity's NBT compound and pass it to `addEntity`. This adds it both to the in memory list and to the underlying NBT, so it's ready to be saved.

<Tabs>
  <TabItem label="Java">

```java
NbtCompound zombieNbt = new NbtCompound();
zombieNbt.setString("id", "minecraft:zombie");
zombieNbt.setFloat("Health", 20.0f);
// ... position, UUID and other fields would normally go here too

Entity zombie = entityData.addEntity(zombieNbt);
```

  </TabItem>
</Tabs>

Since the `Health` field is what decides how the entity gets wrapped, adding it before calling `addEntity` gets you a `LivingEntity` back instead of a plain `Entity`.

## 3. Saving entity changes

Just like chunks, changes to entity data need to be written back explicitly.

<Tabs>
  <TabItem label="Java">

```java
overworld.saveEntityData(2, -1, entityData);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
overworld.saveEntityData(2, -1, entityData)
```

  </TabItem>
</Tabs>

This creates the entity region file for that chunk if it doesn't exist yet, so it also works when you're populating a freshly generated chunk that never had any entities before.

## 4. Reading points of interest

Points of interest, or POIs, are how villagers find beds to sleep in and job site blocks to work at, and how bells know where to summon villagers to. They're read the same way, through `readPoi(chunkX, chunkZ)`, returning `PoiData` or `null`.

<Tabs>
  <TabItem label="Java">

```java
PoiData poiData = overworld.readPoi(2, -1);
if (poiData == null) {
    return;
}

for (PoiRecord record : poiData.records()) {
    System.out.println(record.type() + " at " + Arrays.toString(record.pos()));
}
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val poiData = overworld.readPoi(2, -1) ?: return

for (record in poiData.records()) {
    println("${record.type()} at ${record.pos().contentToString()}")
}
```

  </TabItem>
</Tabs>

Each `PoiRecord` is small:

<div class="fields-details" open>
  <div class="fields-grid">

    <div class="field-card">
      <div class="field-header">
        <code>pos</code>
        <span class="type-badge">int[]</span>
      </div>
      <p>The block position of the POI, as <code>[x, y, z]</code>.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>type</code>
        <span class="type-badge">String</span>
      </div>
      <p>What kind of POI it is, like <code>minecraft:home</code> or <code>minecraft:armorer</code>.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>freeTickets</code>
        <span class="type-badge">int</span>
      </div>
      <p>How many more villagers can still claim this POI.</p>
    </div>

  </div>
</div>

POIs inside a chunk are grouped by section Y, the same vertical slices chunks use for blocks. If you only care about one section, pass its Y to `records(sectionY)` instead of listing everything in the chunk.

<Tabs>
  <TabItem label="Java">

```java
List<PoiRecord> recordsInSection = poiData.records(4);
```

  </TabItem>
</Tabs>

## 5. Adding a point of interest

`addRecord` takes a section Y, a block position, the POI type, and how many free tickets it has (this is how many villagers can claim it at once, a bed has one, most job sites have one too). It creates the section entry if it doesn't exist yet.

<Tabs>
  <TabItem label="Java">

```java
poiData.addRecord(4, 10, 70, 10, "minecraft:home", 1);
overworld.savePoiData(2, -1, poiData);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
poiData.addRecord(4, 10, 70, 10, "minecraft:home", 1)
overworld.savePoiData(2, -1, poiData)
```

  </TabItem>
</Tabs>

## That's it

That's the core of `minified-worlds`: opening a world, reading its settings, walking through dimensions and chunks, and reading or writing the blocks, entities and points of interest inside them. From here, the `NbtCompound` returned by `raw()` on most of these wrapper classes is still available whenever you need to reach a field that isn't wrapped yet.

<div class="fields-details" open>
  <div class="fields-grid">

    <a class="field-card-link" href="/minified-docs/guides/minified-worlds/introduction">
      <div class="field-card">
        <div class="field-header">
          <code>Introduction</code>
        </div>
        <p>Jump back to the world folder structure this whole guide is based on.</p>
      </div>
    </a>

    <a class="field-card-link" href="/minified-docs/guides/minified-worlds/opening-a-world">
      <div class="field-card">
        <div class="field-header">
          <code>Opening a World</code>
        </div>
        <p>Revisit reading a world's level.dat, spawn point and session lock.</p>
      </div>
    </a>

  </div>
</div>
