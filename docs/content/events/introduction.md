---
title: Events
description: Introduction to Events
---

Events are an essential part of building your own launcher.
They allow you to receive updates from the backend, such as the current launch status, the game exit code, and many other events during the launch process.

## Where events live

Events aren't all posted by one class - different modules post to different categories, but they all follow the same `EventBus`/`subscribe()`/`unsubscribe()` pattern described on this page:

* **<a href="/events/connection/checkconnectionevent" class="link">Connection</a>**: online/offline detection, posted by `minified-launch` before network-dependent work.
* **<a href="/events/download/assets/downloadassetsevent" class="link">Download</a>**: asset, library and client JAR download progress.
* **<a href="/events/environment/configurex11environmentevent" class="link">Environment</a>**: graphics environment setup on Linux.
* **<a href="/events/launch/gamestartevent" class="link">Launch</a>**: the Minecraft process starting and stopping.
* **<a href="/events/loader/installforgeevent" class="link">Loader</a>**: Forge/NeoForge installer progress.
* **<a href="/events/java/ensurejavaversionevent" class="link">Java</a>**: managed Java runtime resolution, download and extraction (`minified-java`).
* **<a href="/events/auth/createmasterkeyevent" class="link">Auth</a>**: session encryption (`minified-auth`).

:::caution[Two different event buses]
Only the **Connection**, **Download**, **Environment**, **Launch** and **Loader** events are posted to the `EventBus` you pass to `LaunchConfiguration.Builder.eventBus(...)` (via `LaunchContext`).

The **Java** and **Auth** events are posted by `JavaManager` and `AuthManager` respectively, and each keeps its *own* internal, static `EventBus` that has nothing to do with `LaunchConfiguration`'s. `AuthManager.init(baseDir, eventBus)` does **not** forward that bus to `JavaManager` either - internally it always calls the single-argument `JavaManager.init(path)`, which resets Java's bus back to a fresh, private one.

To actually receive `EnsureJavaVersionEvent`, `JavaArchiveDownloadEvent`, `ExtractArchiveEvent`, or any of the Auth encryption events, you need to explicitly (re-)initialize `JavaManager` (and `AuthManager`, if you want its events too) with the same `EventBus` instance - and do it *after* `AuthManager.init(...)`, since that call would otherwise overwrite `JavaManager`'s bus back to a private one:

```java
EventBus eventBus = new EventBus();

AuthManager.init(authDirectory, eventBus);
// AuthManager.init() just reset JavaManager to a private bus internally,
// so re-point it at the shared bus afterwards:
JavaManager.init(javaDirectory, eventBus);

LaunchConfiguration config = new LaunchConfiguration.Builder()
        // ...
        .eventBus(eventBus)
        .build();
```

Skip this and subscribers on the `LaunchConfiguration`'s bus will simply never see Java or Auth events - there's no error, they just go to a different, private `EventBus` instance.
:::

## All events

{{events}}

## Usage

Firstly, create your EventBus Object:
<Tabs>
  <TabItem label="Java">

```java
EventBus eventBus = new EventBus();
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val eventBus = EventBus()
```

  </TabItem>
</Tabs>

Then, in your LaunchConfiguration, add
```java
.eventBus(eventBus)
```

### Subscribing to Events
<Tabs>
  <TabItem label="Java">

```java
// replace SomeEvent with any event you'd like to listen to
eventBus.subscribe(SomeEvent.class, event -> {
    // Code here will get executed when the event gets triggered
});
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
// replace SomeEvent with any event you'd like to listen to
eventBus.subscribe(SomeEvent::class.java) { event -> 
    // Code here will get executed when the event gets triggered
}
```

  </TabItem>
</Tabs>

### Unsubscribing to Events
This might not seem useful, but it's a requirement for a clean and fast launcher.
For example, if the user closes some GUI that displays it, listening to an event that triggers often would waste resources.
And generally, forgetting to unsubscribe can eventually lead to memory leaks, which you should avoid.

Unsubscribing from an event prevents the code you registered during subscription from being triggered when the event is fired.

<Tabs>
  <TabItem label="Java">

```java
// replace SomeEvent with the event you want to unsubscribe from
eventBus.unsubscribe(SomeEvent.class, listener);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
// replace SomeEvent with the event you want to unsubscribe from
eventBus.unsubscribe(SomeEvent::class.java, listener)
```

  </TabItem>
</Tabs>

## Example

<Tabs>
  <TabItem label="Java">

```java
EventBus eventBus = new EventBus();

LaunchConfiguration config = new LaunchConfiguration.Builder()
        .downloadThreads(10)
        .launcherName("MinifiedLauncher")
        .launcherVersion("1.0.0")
        .assetsDirectory(Path.of("<assets-directory>"))
        .librariesDirectory(Path.of("<libraries-directory>"))
        .jarFile(Path.of("<client.jar>"))
        .isDemoUser(false)
        .loader(new VanillaLoader("1.21.11"))
        .eventBus(eventBus) // EventBus used by the API to dispatch events
        .build();

Launcher.launchMinecraft(
        user,
        config
);

// Subscribe to the Event
eventBus.subscribe(DownloadAssetsEvent.class, event -> {
    System.out.printf(
            "\rDownloading assets | %.2f%% | %s | %d/%d bytes",
            event.progress() * 100,
            event.currentFile(),
            event.downloadedBytes(),
            event.totalBytes()
    );
    System.out.flush();
});
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val eventBus = EventBus()

val config = LaunchConfiguration.Builder()
    .downloadThreads(10)
    .launcherName("MinifiedLauncher")
    .launcherVersion("1.0.0")
    .assetsDirectory(Path.of("<assets-directory>"))
    .librariesDirectory(Path.of("<libraries-directory>"))
    .jarFile(Path.of("<client.jar>"))
    .isDemoUser(false)
    .loader(VanillaLoader("1.21.11"))
    .eventBus(eventBus) // EventBus used by the API to dispatch events
    .build()

Launcher.launchMinecraft(
    user,
    config
)

// Subscribe to the event
eventBus.subscribe(DownloadAssetsEvent::class.java) { event ->
    print(
        "\rDownloading assets | %.2f%% | %s | %d/%d bytes".format(
            event.progress() * 100,
            event.currentFile(),
            event.downloadedBytes(),
            event.totalBytes()
        )
    )
    System.out.flush()
}
```

  </TabItem>
</Tabs>