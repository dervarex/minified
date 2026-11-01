---
title: Events
description: Introduction to Events
---

Events are an essential part of building your own launcher.
They allow you to receive updates from the backend, such as the current launch status, the game exit code, and many other events during the launch process.
[Reverse events](#reverse-events) work the other way round and let your launcher control the backend, like stopping the game.

## Where events live

Events aren't all posted by one class, different modules post them, but they all follow the same `EventBus`/`subscribe()`/`unsubscribe()` pattern.
Every event is listed in [All events](#all-events), grouped by modules

Every module posts its events to the `EventBus` you give it: `LaunchConfiguration.Builder.eventBus(...)`, `AuthManager.init(...)`, `JavaManager.init(...)` or `new Modrinth(baseUrl, eventBus)`. I'd recommend passing the same EventBus to every module, it's simpler like that.

## Reverse events

Most events go from Minified to your launcher. Reverse events go the other way: your launcher posts them and Minified listens, to control the backend. They implement `ReverseEvent` and are marked with "reverse" in the List below

Pleas don't mix up the two types, <a href="/events/launch/gamestoppedevent" class="link">`GameStoppedEvent`</a> tells you when the game has stopped, <a href="/events/launch/stopgameevent" class="link">`StopGameEvent`</a> asks Minified to stop it.

<Tabs>
  <TabItem label="Java">

```java
// on the same EventBus you passed to LaunchConfiguration.Builder.eventBus(...)
eventBus.post(new StopGameEvent());
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
// on the same EventBus you passed to LaunchConfiguration.Builder.eventBus(...)
eventBus.post(StopGameEvent())
```

  </TabItem>
</Tabs>

Posting a reverse event while nothing listens to it, for example when no launch is currently running, does nothing

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