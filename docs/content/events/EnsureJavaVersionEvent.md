---
title: EnsureJavaVersionEvent
description: Fired when Minified needs to resolve or provision a Java runtime for the game.
---

## When it fires

`JavaManager.ensureJavaVersion(requiredMajorVersion)` first checks the **JVM Minified itself is running on**. If that JVM's major version already satisfies `requiredMajorVersion`, this event never fires and the current JVM is reused to launch the game - no separate runtime is downloaded. It only fires once your running JVM is *too old* for the target Minecraft version, right before Minified looks for (or downloads) a matching managed runtime under its runtime install root.

`Launcher.launchMinecraft(...)` calls this indirectly via `JavaManager.getRequiredJavaVersion(versionJson)`, unless you set `LaunchConfiguration.customJavaExecutable` - in that case Minified skips Java resolution entirely and none of the Java events fire.

:::caution
This event (and <a href="/events/java/javaarchivedownloadevent" class="link">`JavaArchiveDownloadEvent`</a> / <a href="/events/java/extractarchiveevent" class="link">`ExtractArchiveEvent`</a>) is posted to the `EventBus` passed to `JavaManager.init(...)`, **not** automatically to `LaunchConfiguration`'s bus. See the <a href="/events/introduction" class="link">Events introduction</a> for how to share one bus across both.
:::
