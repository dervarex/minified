---
title: StopGameEvent
description: Post it to stop the running game
---

## When it's handled

`StopGameEvent` is a [reverse event](/events/introduction#reverse-events): nothing in Minified posts it, your launcher does, for a "Stop Game" button for example. `Launcher.launchMinecraft(...)` listens to it from the moment the game process is spawned (right before <a href="/events/launch/gameprocessstartedevent" class="link">`GameProcessStartedEvent`</a>) until the process has exited. Posting it at any other time, while assets are still downloading for example, does nothing.

`force` decides how the game gets stopped:

* `false` (the default of `new StopGameEvent()`) asks the game to close, like closing its window. Minecraft saves and shuts down on its own.
* `true` kills the process right away, for a game that hangs. Unsaved progress is lost.

Every game launched with the same `EventBus` listens, so posting it stops all of them.

## What happens afterwards

Don't mix it up with <a href="/events/launch/gamestoppedevent" class="link">`GameStoppedEvent`</a>, that one is posted by Minified once the game has exited, and it still fires after a `StopGameEvent` with the real exit code (usually not `0`, you just nuked the process). Since your launcher asked for it, `launchMinecraft(...)` doesn't throw a `NonZeroExitCodeException` and no `NonZeroExitCodeEvent` is posted

```java
// "Stop Game" button
stopButton.setOnAction(e -> eventBus.post(new StopGameEvent()));

eventBus.subscribe(GameStoppedEvent.class, event -> {
    // the game is gone, show the "Play" button again
});
```
