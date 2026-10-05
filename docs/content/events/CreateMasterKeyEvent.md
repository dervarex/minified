---
title: CreateMasterKeyEvent
description: Fired when a new AES master key is generated for session encryption.
---

## When it fires

`Encryptor.loadOrCreateMasterKey(keyFile, eventBus)` runs once, the first time `AuthManager.init(...)` is called for a given base directory. It checks whether `keyFile` (`<baseDir>/master.key`) already exists:

* **If it exists**, the existing AES key is loaded from disk and this event does **not** fire.
* **If it doesn't exist** (typically the very first time a user runs your launcher), this event fires right before a new 256-bit AES key is generated and written to `keyFile`.

In other words, you'll normally see this exactly once per user, ever (unless `master.key` is deleted or the auth directory changes). If it fires unexpectedly on every startup, something is deleting or relocating the key file between runs, and the session file won't be decryptable with a different key, so `AuthManager.loginWithSavedSession()` will silently fail and return `null`.

:::caution
Posted to the `EventBus` passed to `AuthManager.init(baseDir, eventBus)` - **not** automatically to `LaunchConfiguration`'s bus. See the <a href="/events/introduction" class="link">Events introduction</a> for how the two buses relate.
:::
