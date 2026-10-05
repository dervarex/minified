---
title: OfflineEvent
description: Fired when Minified could not detect a working internet connection.
---

## When it fires

`OfflineEvent` fires whenever the connectivity check started by <a href="/events/connection/checkconnectionevent" class="link">`CheckConnectionEvent`</a> comes back negative, i.e. when `NetworkUtil.ensureOnline(...)` throws `NoConnectionException`. Just like `CheckConnectionEvent`, this happens in two places:

1. **In `Launcher.launchMinecraft()`**, if this fires, the launch continues in offline mode: no Forge/NeoForge installation step runs, and the client JAR download is skipped in favor of a cached copy. If no cached client JAR exists in that case, `OfflineModeNeedsNetworkException` is thrown instead.
2. **In `ClientDownloader.downloadClient()`**, if this fires there, the download is aborted immediately (the method returns without downloading anything).

Note that `NetworkUtil.ensureOnline(...)` itself never posts these events, it only throws. It's the calling code (`Launcher`, `ClientDownloader`) that translates the check into `CheckConnectionEvent`/`OfflineEvent`. If you call `NetworkUtil.ensureOnline(...)` directly in your own code, you won't get this event automatically; you'd need to post it yourself.

<Tabs>
  <TabItem label="Java">

```java
eventBus.subscribe(OfflineEvent.class, event -> {
    System.out.println("No connection - launching offline");
});
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
eventBus.subscribe(OfflineEvent::class.java) {
    println("No connection - launching offline")
}
```

  </TabItem>
</Tabs>
