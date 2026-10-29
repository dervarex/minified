---
title: LaunchCancelledEvent
description: Fired when a StopGameEvent stopped the launch before the game was started
---

## When it fires

Your launcher posted a <a href="/events/launch/stopgameevent" class="link">`StopGameEvent`</a> while `Launcher.launchMinecraft(...)` was still preparing the game.

The game was never started, so <a href="/events/launch/gamestoppedevent" class="link">`GameStoppedEvent`</a> doesn't fire.
If you just want to know when it Stopped, no matter what reason, listen to both.
