---
title: JavaArchiveDownloadEvent
description: Fired while a managed Java runtime archive is downloading from Adoptium.
---

## When it fires

This only fires when <a href="/events/java/ensurejavaversionevent" class="link">`EnsureJavaVersionEvent`</a> also fired (your JVM is too old) **and** no already-extracted managed runtime exists yet on disk for the required major version/platform combination. If Minified previously downloaded and extracted a matching runtime, it's reused directly and this event doesn't fire at all. The archive itself is always downloaded fresh into a temporary file (deleted again after extraction) - it's the extracted runtime, not the archive, that's cached between launches. It's fetched from Eclipse Adoptium (`api.adoptium.net`).

Progress is throttled to roughly **one event per 100ms** while bytes are streamed in, followed by a guaranteed final event at `progress: 1.0` so listeners always see completion even if the download finishes between throttle windows. If the server doesn't report a `Content-Length` header, `progress` is reported as `-1.0` for every event except the final one - fall back to showing `downloadedBytes` as a raw counter in that case.

After this event stops firing, the checksum of the downloaded archive is verified against Adoptium's metadata before extraction begins (see <a href="/events/java/extractarchiveevent" class="link">`ExtractArchiveEvent`</a>). A checksum mismatch throws an `IOException` and no extraction event fires.

Posted to the `EventBus` given to `JavaManager.init(...)` - see the note on <a href="/events/java/ensurejavaversionevent" class="link">`EnsureJavaVersionEvent`</a>.
