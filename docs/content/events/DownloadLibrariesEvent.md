---
title: DownloadLibrariesEvent
description: Fired repeatedly while Minecraft's libraries are being downloaded.
---

## When it fires

Like <a href="/events/download/assets/downloadassetsevent" class="link">`DownloadAssetsEvent`</a>, `LibraryDownloader` runs on a fixed thread pool sized by `LaunchConfiguration.downloadThreads`, so this event fires from multiple threads concurrently - one post per finished library, with the running `progress`/`downloadedBytes` totals shared across the whole download.

`LibraryDownloader` performs its own connectivity check before starting, independently of `Launcher`'s. If that check fails, it does **not** post `OfflineEvent` - it silently falls back to `OfflineLibraryValidator`, which verifies previously downloaded libraries already exist on disk instead of downloading anything. In that fallback path, `DownloadLibrariesEvent` never fires at all.
