---
title: LoadEncryptedSessionEvent
description: Fired after a saved session has been decrypted and loaded from disk.
---

## When it fires

`Encryptor.loadEncryptedSession(...)` runs inside `AuthManager.loginWithSavedSession()`, which you'd typically call once at launcher startup (see the <a href="/guides/minified-auth/authentication" class="link">Authentication guide</a>). This event fires only after the file is successfully decrypted with the AES master key, **not** merely when a saved session file is found.

Two cases where it does **not** fire, even though `loginWithSavedSession()` was called:
* **No session file exists yet** (first run, or after `hasSessionSaved()` returned `false`): the method returns `null` immediately, before any decryption is attempted.
* **Decryption or refreshing fails**, e.g. the master key changed, the file is corrupted, or the Microsoft/Xbox token refresh call throws. `loginWithSavedSession()` catches the exception, logs it, and returns `null` without posting this event.

So in practice, this event firing is a reliable signal that the on-disk session was valid and readable. It does not by itself guarantee the session was still *authorized* by Microsoft, since token refreshing happens afterward and can still fail separately.

Posted to the `EventBus` passed to `AuthManager.init(baseDir, eventBus)` - see the <a href="/events/introduction" class="link">Events introduction</a> for how this bus relates to `LaunchConfiguration`'s.
