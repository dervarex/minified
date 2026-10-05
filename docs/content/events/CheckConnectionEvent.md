---
title: CheckConnectionEvent
description: Fired right before Minified probes for a working internet connection.
---

## When it fires

`CheckConnectionEvent` fires immediately before Minified calls `NetworkUtil.ensureOnline(...)` internally. You'll see it twice during a typical online launch:

1. **Once at the very start of `Launcher.launchMinecraft()`**, before anything else happens. The result decides whether the rest of the launch runs online (Forge/NeoForge installation, live version metadata, downloading the client jar) or falls back to offline mode.
2. **Once inside `ClientDownloader.downloadClient()`**, right before it tries to download the client JAR. This only runs standalone if you call `ClientDownloader` directly with a `LaunchContext`.

The connectivity check itself (DNS lookup, a raw TCP probe, and an HTTPS HEAD request) is not part of this event. `CheckConnectionEvent` only marks that the check is about to happen, which is useful for showing a "Checking connection…" indicator in your UI.

Right after this event, exactly one of two things happens:
* the check succeeds and the launch continues normally, or
* the check fails and <a href="/events/connection/offlineevent" class="link">`OfflineEvent`</a> fires instead.

<Tabs>
  <TabItem label="Java">

```java
eventBus.subscribe(CheckConnectionEvent.class, event -> {
    System.out.println("Checking your connection...");
});
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
eventBus.subscribe(CheckConnectionEvent::class.java) {
    println("Checking your connection...")
}
```

  </TabItem>
</Tabs>
