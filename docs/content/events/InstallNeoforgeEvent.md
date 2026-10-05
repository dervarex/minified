---
title: InstallNeoforgeEvent
description: Fired continuously while NeoForge is installing.
---

## When it fires

`NeoInstallerInjector` mirrors `ForgeInstallerInjector` almost exactly and fires from `Launcher.launchMinecraft(...)` whenever `LaunchConfiguration.loader` is a `NeoforgeLoader` and the launch is online. The stage order is the same as <a href="/events/loader/installforgeevent" class="link">`InstallForgeEvent`</a>, minus `EXTRACTING` (which NeoForge's `Stage` enum doesn't even define):

1. `PREPARING`: always first.
2. `WRITING_PROFILE`: only if `launcher_profiles.json` doesn't already exist; skipped on repeat launches.
3. `DOWNLOADING_INSTALLER`: before resolving the NeoForge version and downloading its installer JAR.
4. `RUNNING_INSTALLER`: before invoking the NeoForge installer via reflection.
5. `FINISHED` or `FAILED`, exactly one of these fires last, mirroring Forge's behavior.

Unlike Forge, NeoForge's installer is invoked with a `Predicate<String>` for "optional" components that always returns `true`, so all optional install steps are accepted automatically - there's no event for that decision either.
