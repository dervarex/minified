---
title: Launching Configuration
description: Learn how to configure the Minecraft launch process with Minified.
---


## 1. The LaunchConfiguration
The `LaunchConfiguration` is the central configuration object for launching Minecraft with Minified.
It can be built using the `LaunchConfiguration.Builder` class, which provides a fluent API for setting various launch parameters.

Example usage:

<Tabs>
  <TabItem label="Java">

```java
LaunchConfiguration config = new LaunchConfiguration.Builder()
        .downloadThreads(10)
        .launcherName("YourLauncherName")
        .launcherVersion("1.0.0")
        .assetsDirectory(Path.of("<assets-directory>"))
        .librariesDirectory(Path.of("<libraries-directory>"))
        .jarFile(Path.of("<client.jar>"))
        .isDemoUser(false)
        .loader(new VanillaLoader("1.21.11"))
        .build();
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val config = LaunchConfiguration.Builder()
    .downloadThreads(10)
    .launcherName("YourLauncherName")
    .launcherVersion("1.0.0")
    .assetsDirectory(Path.of("<assets-directory>"))
    .librariesDirectory(Path.of("<libraries-directory>"))
    .jarFile(Path.of("<client.jar>"))
    .isDemoUser(false)
    .loader(VanillaLoader("1.21.11"))
    .build()
```

  </TabItem>
</Tabs>

## 2. Fields

Configure how Minecraft is downloaded, prepared, and launched.

<details class="fields-details">
  <summary class="fields-summary">
    <span>
      <strong>Configuration Fields</strong>
      <small>17 available fields</small>
    </span>

    <span class="fields-chevron" aria-hidden="true">›</span>
  </summary>

  <div class="fields-grid">

    <div class="field-card">
      <div class="field-header">
        <code>minRam</code>
        <span class="badge optional">Optional</span>
      </div>
      <p>The minimum amount of RAM allocated to the Minecraft process.</p>
      <span class="field-meta">Megabytes · Default: <code>2048</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>maxRam</code>
        <span class="badge optional">Optional</span>
      </div>
      <p>The maximum amount of RAM allocated to the Minecraft process.</p>
      <span class="field-meta">Megabytes · Default: <code>4096</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>downloadThreads</code>
        <span class="badge optional">Optional</span>
      </div>
      <p>The number of threads used for downloading assets and libraries.</p>
      <span class="field-meta">Default: <code>5</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>resolutionWidth</code>
        <span class="badge optional">Optional</span>
      </div>
      <p>The width of the Minecraft window. Only takes effect if set together with <code>resolutionHeight</code> via the <code>resolution(width, height)</code> builder method - see the note below.</p>
      <span class="field-meta">Pixels · Default: <code>1920</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>resolutionHeight</code>
        <span class="badge optional">Optional</span>
      </div>
      <p>The height of the Minecraft window. Only takes effect if set together with <code>resolutionWidth</code> via the <code>resolution(width, height)</code> builder method - see the note below.</p>
      <span class="field-meta">Pixels · Default: <code>1080</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>launcherName</code>
        <span class="badge optional">Optional</span>
      </div>
      <p>The name of the launcher. May be displayed in-game or in logs.</p>
      <span class="field-meta">Default: <code>"Launcher"</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>launcherVersion</code>
        <span class="badge optional">Optional</span>
      </div>
      <p>The launcher version. May be displayed in-game or in logs.</p>
      <span class="field-meta">Default: <code>"1.0.0"</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>demoUser</code>
        <span class="badge optional">Optional</span>
      </div>
      <p>Controls whether Minecraft launches in demo mode.</p>
      <span class="field-meta">Default: <code>false</code></span>
    </div>

    <div class="field-card required-card">
      <div class="field-header">
        <code>jarFile</code>
        <span class="badge required">Required</span>
      </div>
      <p>The path to the Minecraft client JAR file.</p>
    </div>

    <div class="field-card required-card">
      <div class="field-header">
        <code>librariesDirectory</code>
        <span class="badge required">Required</span>
      </div>
      <p>The directory where Minecraft libraries are stored.</p>
    </div>

    <div class="field-card required-card">
      <div class="field-header">
        <code>assetsDirectory</code>
        <span class="badge required">Required</span>
      </div>
      <p>The directory where Minecraft assets are stored.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>nativesDirectory</code>
        <span class="badge optional">Optional</span>
      </div>
      <p>The directory where native libraries are extracted.</p>
      <span class="field-meta">
        Default: <code>&lt;jarFile parent&gt;/natives</code>
      </span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>customJavaExecutable</code>
        <span class="badge optional">Optional</span>
      </div>
      <p>A custom Java executable used to launch Minecraft.</p>
      <span class="field-meta">
        Default: Recommended Java version via <code>minified-java</code>
      </span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>extraJvmArgs</code>
        <span class="badge optional">Optional</span>
      </div>
      <p>Additional JVM arguments passed to the Minecraft process.</p>
    </div>

    <div class="field-card required-card">
      <div class="field-header">
        <code>loader</code>
        <span class="badge required">Required</span>
      </div>
      <p>
        The loader used to launch Minecraft. Must implement the
        <code>Loader</code> interface.
      </p>
      <span class="field-meta">
        Examples: <code>VanillaLoader</code>, <code>FabricLoader</code>
      </span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>offlineUsername</code>
        <span class="badge optional">Optional</span>
      </div>
      <p>The username used when launching Minecraft in offline mode.</p>
      <span class="field-meta">Default: <code>"Player"</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>eventBus</code>
        <span class="badge optional">Optional</span>
      </div>
      <p>The <code>EventBus</code> used to dispatch events during the launch process.</p>
      <span class="field-meta">Default: a new <code>EventBus</code></span>
    </div>

  </div>
</details>

:::caution[There's no resolutionWidth()/resolutionHeight() builder method]
`resolutionWidth` and `resolutionHeight` can't be set individually - the only way to change them is `.resolution(width, height)`, which sets both at once and also flips an internal `customResolution` flag to `true`. Without calling `.resolution(...)`, that flag stays `false` and the 1920×1080 defaults are used regardless of what you might expect the fields to contain.
:::