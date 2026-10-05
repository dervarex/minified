---
title: SaveEncryptedSessionEvent
description: Fired after a login session has been encrypted and written to disk.
---

## When it fires

`Encryptor.saveEncryptedSession(...)` is called from `AuthManager.persistSession(...)`, which runs after **every successful login or session refresh**, not just the first one. Concretely, this event fires after:

* a fresh device-code login completes (`AuthManager.login()` or `startDeviceCodeLoginAsync()`), and
* a saved session is successfully refreshed via `AuthManager.loginWithSavedSession()` (Minecraft/Xbox tokens are re-persisted even if only the access token changed).

The session content itself (the raw Microsoft/Xbox/Minecraft auth chain as JSON) is AES-encrypted with the master key from <a href="/events/auth/createmasterkeyevent" class="link">`CreateMasterKeyEvent`</a> before being base64-encoded and written to `SessionFile`. This event fires after that write completes, so by the time you receive it the file on disk is already up to date.

Posted to the `EventBus` passed to `AuthManager.init(baseDir, eventBus)` - see the <a href="/events/introduction" class="link">Events introduction</a> for how this bus relates to `LaunchConfiguration`'s.
