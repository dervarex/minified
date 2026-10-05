---
title: Opening a World
description: Load a Minecraft world and read its basic data
---


Before we get into chunks, entities and blocks, let's start with the basics: opening a world folder and reading the data that sits right at its root, like `level.dat`.

## 1. What is a world?

If you looked at the <a href="/minified-docs/guides/minified-worlds/introduction" class="link">Introduction</a> guide already, you know a world is not just a single file. It's a whole folder full of `.dat` files, region files and player data. Most of that data only matters once you start looking at a specific dimension or a specific chunk, which we'll cover in later guides.

For now, think of a world as two things:

1. A handful of files that describe the world itself: its name, its difficulty, its spawn point, whether it's hardcore, and so on. This lives in `level.dat`.
2. A `session.lock` file that Minecraft uses to make sure only one thing is writing to the world at a time.

`minified-worlds` wraps both of these in one class: `WorldSave`.

## 2. Add the dependency

<Tabs>
  <TabItem label="Gradle (Groovy)">

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    // Replace 3.1.0 with your preferred version
    implementation 'com.github.dervarex.minified:minified-worlds:3.1.0'
}
```

  </TabItem>

  <TabItem label="Gradle (Kotlin DSL)">

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // Replace 3.1.0 with your preferred version
    implementation("com.github.dervarex.minified:minified-worlds:3.1.0")
}
```

  </TabItem>
</Tabs>

## 3. Opening a world

All you need is a path to the world folder, the same folder that contains the `level.dat`.

<Tabs>
  <TabItem label="Java">

```java
Path worldDirectory = Path.of("saves/My World");
WorldSave world = new WorldSave(worldDirectory);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val worldDirectory = Path.of("saves/My World")
val world = WorldSave(worldDirectory)
```

  </TabItem>
</Tabs>

When you construct `WorldSave`, it immediately reads `level.dat` and a couple of other small `.dat` files from the `data/` folder, so the constructor can throw if something is wrong. Two things it checks:

* The given path has to be an existing directory. If it isn't, you'll get a `RuntimeException` saying the world directory doesn't exist.
* That directory has to contain a `level.dat` file. Without it, `minified-worlds` has no idea what world it's even looking at, so it throws too.

Both of these are unchecked exceptions, so you don't need a `try`/`catch` just to open a world, but it's worth catching them if you're opening a folder the user picked or one that might not be a real world.

## 4. Reading level.dat

Once you have a `WorldSave`, its `getLevel()` method gives you a `Level` object with the world's core settings already parsed for you.

<Tabs>
  <TabItem label="Java">

```java
Level level = world.getLevel();

System.out.println(level.getLevelName());
System.out.println(level.getDifficulty());
System.out.println(level.isHardcore());
System.out.println(level.getSpawn().getPos());
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val level = world.level

println(level.levelName)
println(level.difficulty)
println(level.hardcore)
println(level.spawn.pos)
```

  </TabItem>
</Tabs>

A few of the fields you'll find on `Level`:

<details class="fields-details" open>
  <summary class="fields-summary">
    <span>
      <strong>Level fields</strong>
      <small>The most commonly used ones</small>
    </span>
    <span class="fields-chevron" aria-hidden="true">›</span>
  </summary>

  <div class="fields-grid">

    <div class="field-card">
      <div class="field-header">
        <code>levelName</code>
        <span class="type-badge">String</span>
      </div>
      <p>The world's name, as shown on the "Select World" screen.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>difficulty</code>
        <a href="#difficulty" class="type-link">Difficulty</a>
      </div>
      <p>The world's difficulty: <code>peaceful</code>, <code>easy</code>, <code>normal</code> or <code>hard</code>.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>hardcore</code>
        <span class="type-badge">boolean</span>
      </div>
      <p>Whether hardcore mode is on. This is separate from <code>difficulty</code>, a hardcore world still reports <code>hard</code> there.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>spawn</code>
        <span class="type-badge">Spawn</span>
      </div>
      <p>The world spawn point: dimension, position, pitch and yaw.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>version</code>
        <span class="type-badge">Version</span>
      </div>
      <p>The Minecraft version this world was last saved with.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>gameType</code>
        <span class="type-badge">int</span>
      </div>
      <p>The default game mode: <code>0</code> survival, <code>1</code> creative, <code>2</code> adventure, <code>3</code> spectator.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>enabledDatapacks</code>
        <span class="type-badge">String[]</span>
      </div>
      <p>Which datapacks are turned on for this world. <code>disabledDatapacks</code> lists the rest.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>allowCommands</code>
        <span class="type-badge">boolean</span>
      </div>
      <p>Whether cheats are enabled.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>time</code>
        <span class="type-badge">int</span>
      </div>
      <p>The current world time, in ticks.</p>
    </div>

  </div>
</details>

`Level` is a plain data class with getters and setters for everything, so if you want to change something, like renaming the world or nudging the spawn point, just call the matching setter.

### Difficulty

<div id="difficulty" class="type-definition">
  <div class="type-definition-header">
    <h3>Difficulty</h3>
  </div>

  <div class="enum-rows">
    <div class="enum-row">
      <code>peaceful</code>
      <span>No hostile mobs spawn, and hunger doesn't drain.</span>
    </div>
    <div class="enum-row">
      <code>easy</code>
      <span>Hostile mobs deal less damage, hunger drains slowly.</span>
    </div>
    <div class="enum-row">
      <code>normal</code>
      <span>The default, balanced difficulty.</span>
    </div>
    <div class="enum-row">
      <code>hard</code>
      <span>Hostile mobs deal more damage, hunger drains faster. Also what hardcore worlds report here.</span>
    </div>
  </div>
</div>

## 5. Saving your changes

`WorldSave` has a `save()` method that writes the `Level` object back to `level.dat`.

<Tabs>
  <TabItem label="Java">

```java
level.setLevelName("My Renamed World");
world.save();
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
level.levelName = "My Renamed World"
world.save()
```

  </TabItem>
</Tabs>

Keep in mind that `save()` only writes `level.dat`. It doesn't touch chunks, entities or point of interest data, since those live in their own region files per dimension. We'll get to saving those in the next guide.

## 6. Checking the session lock

Minecraft uses `session.lock` to prevent two processes (say, a running server and your tool) from writing to the same world at the same time. `WorldSave` reads this for you through `getLock()`.

<Tabs>
  <TabItem label="Java">

```java
if (world.getLock().isLocked()) {
    System.out.println("This world is currently open somewhere else!");
}
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
if (world.lock.locked) {
    println("This world is currently open somewhere else!")
}
```

  </TabItem>
</Tabs>

It's a good habit to check this before writing to a world your tool didn't open itself, editing a world while Minecraft has it loaded is a great way to lose progress.

## 7. Optional data

A couple of files in the `data/` folder aren't always there, since Minecraft only creates them once they're actually used. `WorldSave` exposes those as `Optional`:

<Tabs>
  <TabItem label="Java">

```java
world.getGameRules().ifPresent(rules -> {
    // do something with the game rules
});

world.getCustomBossEvents().ifPresent(bossEvents -> {
    // do something with the boss bars
});
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
world.gameRules.ifPresent { rules ->
    // do something with the game rules
}

world.customBossEvents.ifPresent { bossEvents ->
    // do something with the boss bars
}
```

  </TabItem>
</Tabs>

## Next steps

Now that you can open a world and read its top level settings, it's time to look at the actual terrain.

<div class="fields-details" open>
  <div class="fields-grid">

    <a class="field-card-link" href="/minified-docs/guides/minified-worlds/dimensions-and-chunks">
      <div class="field-card">
        <div class="field-header">
          <code>Dimensions and Chunks</code>
        </div>
        <p>Learn how terrain is split into dimensions, chunks and blocks, and how to read or write them.</p>
      </div>
    </a>

  </div>
</div>
