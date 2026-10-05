---
title: ExtractArchiveEvent
description: Fired while a downloaded Java runtime archive is being unpacked.
---

## When it fires

This fires immediately after <a href="/events/java/javaarchivedownloadevent" class="link">`JavaArchiveDownloadEvent`</a> finishes and the archive's checksum has been verified. Which archive format is used - `zip` or `tarGz` - depends on the Adoptium asset for the current platform (Windows typically ships `.zip`, Linux/macOS typically ship `.tar.gz`).

Both formats always post a `progress: 0` event right at the start. After that:

* **`zip`** extraction reports whole-percent progress (`0`-`100`) throttled to roughly every 100ms, based on the number of ZIP entries processed - the last entry always forces a final event even if it lands inside the 100ms window, so you're guaranteed to see `progress: 100`.
* **`tar.gz`** extraction estimates progress from *compressed* bytes read through the gzip stream (capped at `99` until fully done, since tar entries don't have a fixed upfront total), and posts a guaranteed final `progress: 100` event once the whole stream has been consumed. If the archive size can't be determined, `progress` is reported as `-1` for the in-between events.
