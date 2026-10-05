---
title: InstallForgeEvent
description: Fired continuously while Minecraft Forge is installing.
---

## When it fires

`ForgeInstallerInjector` runs from `Launcher.launchMinecraft(...)` whenever `LaunchConfiguration.loader` is a `ForgeLoader` **and** the connectivity check earlier in the launch succeeded (Forge is never installed in offline mode - the launch just proceeds assuming Forge is already installed from a previous run). The stages fire in this order:

1. `PREPARING`: always first; creates the `versions`/`libraries` directories.
2. `WRITING_PROFILE`: only if `launcher_profiles.json` doesn't already exist in the game directory. On repeat launches where it's already there, this stage is skipped entirely.
3. `DOWNLOADING_INSTALLER`: before fetching the Forge installer JAR and verifying its SHA-1.
4. `RUNNING_INSTALLER`: before invoking the official Forge installer via reflection to do the actual client install.
5. Either `FINISHED` (install succeeded) or `FAILED` (a reflective invocation error occurred, followed by a thrown `RuntimeException`). Exactly one of these always fires last.

:::note
`EXTRACTING` is defined in the `Stage` enum but is **not currently posted anywhere** - unpacking Forge's own libraries happens inside the official Forge installer, which Minified invokes as an opaque step during `RUNNING_INSTALLER` rather than reimplementing itself. Don't rely on `EXTRACTING` firing.
:::
