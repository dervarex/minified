---
title: DownloadAssetsEvent
description: Fired repeatedly while Minecraft's asset files are being downloaded.
---

## When it fires

`AssetDownloader` downloads assets across a worker pool sized by `LaunchConfiguration.downloadThreads` (5 by default), so this event fires **concurrently from multiple threads** - one post per finished file, plus one final call once every future in the pool has completed to guarantee a `progress: 1.0` event even if the last file was tiny.

Because `progress`/`downloadedBytes`/`totalBytes` describe the *overall* asset download (not just `currentFile`), don't assume events arrive in a strict order by file - two threads can post around the same time with `currentFile` referring to whichever file each thread just finished. If you're rendering a single progress bar, key off `progress`; if you want a live "currently downloading" list, track `currentFile` per listener call rather than assuming monotonic ordering.

This event is only posted when a non-null `LaunchContext` is available, which is always the case when assets are downloaded as part of `Launcher.launchMinecraft(...)`.
