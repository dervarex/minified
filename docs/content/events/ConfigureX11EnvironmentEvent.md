---
title: ConfigureX11EnvironmentEvent
description: Fired when Minified injects an X11 DISPLAY fallback for the Minecraft process on Linux.
---

## When it fires

This fires from `X11Helper.configureGraphicsEnvironment(...)`, called right before the Minecraft process is started, only when **all** of the following are true:

1. The launcher is running on Linux (`os.name` contains `linux`).
2. The child process environment doesn't already have a `DISPLAY` variable set (i.e. you haven't already configured one yourself).
3. Minified can find an active X11 socket in `/tmp/.X11-unix` (e.g. `X0`, `X1`, …) and derive a display string like `:0` from it.

If any of those conditions fail - most commonly on Windows/macOS, or on a pure Wayland session with no Xwayland socket available - this event simply never fires and `DISPLAY` is left untouched.

When it does fire, Minified also sets `DISPLAY` on the child process environment to the resolved `display` value and, if `WAYLAND_DISPLAY` was set, removes it and sets `XDG_SESSION_TYPE=x11` so Minecraft's LWJGL/GLFW windowing picks X11 instead of trying (and often failing) to use Wayland directly.
