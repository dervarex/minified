---
title: Java Runtimes
description: Resolve, download and manage Java runtimes with Minified Java.
---


## 1. Add the Dependency

Add `minified-java` to your `build.gradle` file:

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.dervarex.minified:minified-java:3.0.0'
}
```

---

## 2. Do you need this module directly?

`Launcher.launchMinecraft()` already calls `JavaManager` internally - before starting the game, it reads the required Java feature version from the version JSON and calls `JavaManager.ensureJavaVersion(...)` for you. If you only care about launching Minecraft, `minified-launch` pulls this in as a dependency and you don't need to touch `minified-java` at all.

Reach for `JavaManager` directly when you want to resolve or pre-download a runtime *before* launching - for example, to show a "downloading Java 21…" step in your own UI ahead of time, or to check compatibility without starting the game.

---

## 3. Initializing the Java Manager

`JavaManager` keeps its runtime cache under a static, class-wide directory - by default a user-specific application data folder (`%APPDATA%\Minified\java` on Windows, `~/Library/Application Support/Minified/java` on macOS, `~/.local/share/Minified/java` on Linux). Call `init(...)` to override it:

<Tabs>
  <TabItem label="Java">

```java
Path javaDirectory = Path.of("<java-directory>");

JavaManager.init(javaDirectory);
```

  </TabItem>
  <TabItem label="Kotlin">

```kotlin
val javaDirectory = Path.of("<java-directory>")

JavaManager.init(javaDirectory)
```

  </TabItem>
</Tabs>

:::caution[Sharing an EventBus with AuthManager]
`JavaManager.init(baseDir, eventBus)` also exists, but if you call `AuthManager.init(...)` afterwards it will silently reset `JavaManager` back to its own private `EventBus`. If you want both managers posting to the same bus, always call `JavaManager.init(...)` *last*. See the <a href="/events/introduction" class="link">Events introduction</a> for the full explanation and a working order of calls.
:::

Calling `init(...)` is entirely optional - if you skip it, `JavaManager` lazily uses its default directory the first time it needs one.

---

## 4. Ensuring a Compatible Runtime

The main entry point is `ensureJavaForMinecraftVersion(...)`. It resolves the Java feature version required by a Minecraft version, compares it against the JVM currently running your launcher, and only downloads a managed runtime if the current one is too old:

<Tabs>
  <TabItem label="Java">

```java
JavaInstallation installation = JavaManager.ensureJavaForMinecraftVersion("1.21.11");

Path javaExecutable = installation.executable();
boolean wasDownloaded = installation.managed();
```

  </TabItem>
  <TabItem label="Kotlin">

```kotlin
val installation = JavaManager.ensureJavaForMinecraftVersion("1.21.11")

val javaExecutable = installation.executable()
val wasDownloaded = installation.managed()
```

  </TabItem>
</Tabs>

If you already know the required feature version (say, `21`), use `ensureJavaVersion(int)` instead and skip the version-manifest lookup. Both come with a `...Executable(...)` shorthand that returns just the `Path` to the `java` binary instead of the full `JavaInstallation`:

<Tabs>
  <TabItem label="Java">

```java
Path javaExecutable = JavaManager.ensureJavaExecutable(21);
```

  </TabItem>
  <TabItem label="Kotlin">

```kotlin
val javaExecutable = JavaManager.ensureJavaExecutable(21)
```

  </TabItem>
</Tabs>

Managed runtimes are downloaded from [Eclipse Temurin](https://adoptium.net/) and cached under the configured base directory per platform and feature version, so repeated calls for the same version reuse the existing install instead of downloading it again. Concurrent calls for the *same* runtime are serialized internally, so it's safe to call `ensureJavaVersion(...)` from multiple threads without downloading the same archive twice.

### JavaInstallation

<details class="fields-details" open>
  <summary class="fields-summary">
    <span>
      <strong>JavaInstallation Fields</strong>
      <small>5 available fields</small>
    </span>

    <span class="fields-chevron" aria-hidden="true">›</span>
  </summary>

  <div class="fields-grid">

    <div class="field-card">
      <div class="field-header">
        <code>majorVersion</code>
      </div>
      <p>The Java feature version, such as <code>17</code>, <code>21</code>, or <code>25</code>.</p>
      <span class="field-meta">Type: <code>int</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>home</code>
      </div>
      <p>The runtime's home directory.</p>
      <span class="field-meta">Type: <code>Path</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>executable</code>
      </div>
      <p>The Java executable that should be launched.</p>
      <span class="field-meta">Type: <code>Path</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>managed</code>
      </div>
      <p>Whether this runtime was downloaded and is managed by <code>JavaManager</code>, as opposed to being the JVM already running your launcher.</p>
      <span class="field-meta">Type: <code>boolean</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>releaseName</code>
      </div>
      <p>The vendor release name for managed runtimes, or <code>null</code> for the current JVM.</p>
      <span class="field-meta">Type: <code>String</code></span>
    </div>

  </div>
</details>

---

## 5. Checking Required Java Versions

If you just need the required Java feature version without downloading anything, `getRequiredJavaVersion(...)` is overloaded for every stage a version JSON might be in - a Minecraft version id (fetches and caches the manifest itself), an already-loaded `JsonFile`/`JsonValue`, or a cached version JSON on disk:

<Tabs>
  <TabItem label="Java">

```java
int required = JavaManager.getRequiredJavaVersion("1.21.11");

if (required > JavaPlatform.majorVersion()) {
    System.out.println("Current JVM is too old, Minified will download Java " + required);
}
```

  </TabItem>
  <TabItem label="Kotlin">

```kotlin
val required = JavaManager.getRequiredJavaVersion("1.21.11")

if (required > JavaPlatform.majorVersion()) {
    println("Current JVM is too old, Minified will download Java $required")
}
```

  </TabItem>
</Tabs>

All overloads return `-1` if the required version couldn't be determined, rather than throwing.

---

## 6. The Current Runtime

`currentRuntime()` resolves the JVM your launcher is already running on as a `JavaInstallation` (with `managed` set to `false`), and `JavaPlatform` offers quick feature-version checks without going through `JavaManager` at all:

<Tabs>
  <TabItem label="Java">

```java
JavaInstallation current = JavaManager.currentRuntime();

boolean atLeast17 = JavaPlatform.isAtLeast(17);
```

  </TabItem>
  <TabItem label="Kotlin">

```kotlin
val current = JavaManager.currentRuntime()

val atLeast17 = JavaPlatform.isAtLeast(17)
```

  </TabItem>
</Tabs>

---

## 7. Events

While a managed runtime is being resolved, `JavaManager` posts progress events to the `EventBus` it was configured with - `EnsureJavaVersionEvent` when a download starts, `JavaArchiveDownloadEvent` while the archive is downloading, and `ExtractArchiveEvent` while it's being unpacked. See the <a href="/events/java/ensurejavaversionevent" class="link">Java events</a> section for the full details on each one, including the same shared-`EventBus` caveat mentioned above.
