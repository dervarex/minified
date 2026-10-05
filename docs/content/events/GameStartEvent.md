---
title: GameStartEvent
description: Fired right before the Minecraft process is spawned.
---

## When it fires

This is the last event posted before Minified actually calls `ProcessBuilder.start()`. By the time you receive it, everything else has already happened: connectivity was checked, Forge/NeoForge (if applicable) was installed, the required Java runtime was resolved, assets/libraries/the client JAR were downloaded, and the full JVM/game argument list was built. It fires exactly once per `Launcher.launchMinecraft(...)` call, right after the X11 environment fix-up (see <a href="/events/environment/configurex11environmentevent" class="link">`ConfigureX11EnvironmentEvent`</a>) and immediately before the process starts.

`online` reflects the result of the connectivity check from earlier in the same call (see <a href="/events/connection/checkconnectionevent" class="link">`CheckConnectionEvent`</a>) - `user` is whatever you passed into `launchMinecraft(...)`, which is `null` for a fully offline launch.

Once this event fires, Minified blocks on `Process.waitFor()` until the game exits - see <a href="/events/launch/gamestoppedevent" class="link">`GameStoppedEvent`</a> for what happens next.
