---
title: DownloadClientJarEvent
description: Fired while the Minecraft client JAR is being downloaded.
---

## When it fires

`ClientDownloader` posts this event on every chunk read from the HTTP response body (there's no throttling like the Java runtime downloader has), plus one guaranteed final call with `progress: 1.0`. Since it comes from a single sequential `InputStream` read loop, unlike the asset/library events, these always arrive in order on one thread.

Two cases where you *won't* see this event fire during a normal launch:
* **The cached JAR is already valid.** If `path` exists and its SHA-1 matches the expected checksum from `VersionMetadataProvider`, `ClientDownloader` skips straight to a single `progress: 1.0` event without downloading anything.
* **Offline mode.** `Launcher.launchMinecraft(...)` only calls `downloadClient(...)` when `context.isOnline()` is true; in offline mode it either reuses the cached JAR silently or throws `OfflineModeNeedsNetworkException` if none exists - neither path posts this event.
