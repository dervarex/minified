---
title: GameStoppedEvent
description: Fired once the Minecraft process has exited.
---

## When it fires

`Launcher.launchMinecraft(...)` blocks on `Process.waitFor()` after posting <a href="/events/launch/gamestartevent" class="link">`GameStartEvent`</a>, so this event fires exactly once, synchronously, whenever the Minecraft process exits for any reason - a normal quit, a crash, or the process being killed externally. Since `launchMinecraft(...)` itself is blocking, code after the call only resumes once this event has already fired.

`exitCode` is the raw exit code returned by the JVM process (`0` on a clean exit; a non-zero value on a crash, forced termination, or Java throwing an uncaught error before the game loop could start). Minified currently does **not** throw an exception or treat a non-zero exit code specially - it's left entirely up to the listener to decide what a given code means for their launcher.
