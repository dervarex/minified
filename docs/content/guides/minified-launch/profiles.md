---
title: Profiles
description: Save launch configurations with their mods and share them with friends
---


## 1. Saving and Loading

`ProfileFactory` saves a `LaunchConfiguration` as JSON and loads it again:

<Tabs>
  <TabItem label="Java">

```java
ProfileFactory.save(config, Path.of("profile.json"));
LaunchConfiguration loaded = ProfileFactory.load(Path.of("profile.json"));
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
ProfileFactory.save(config, Path.of("profile.json"))
val loaded = ProfileFactory.load(Path.of("profile.json"))
```

  </TabItem>
</Tabs>

A `Profile` adds a name, description, author and icon (`ProfileMetadata`) and the files that go into the game directory, like mods, resource packs and shader packs (`ProfileFile`). Save it with `ProfileFactory.save(profile, path)` and load it with `ProfileFactory.loadProfile(path)`

<Tabs>
  <TabItem label="Java">

```java
Profile profile = new Profile(
        ProfileMetadata.builder()
                .name("Survival with friends")
                .author("me")
                .build(),
        config,
        List.of(
                ProfileFile.builder("mods/sodium.jar")
                        .name("Sodium")
                        .hash("sha512", "<sha512>")
                        .download("https://cdn.modrinth.com/data/...")
                        .source(ProfileFileSource.modrinth("AANobbMI", "<version id>"))
                        .build(),
                ProfileFile.fromFile(gameDirectory, gameDirectory.resolve("resourcepacks/our-pack.zip"))
        )
);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val profile = Profile(
    ProfileMetadata.builder()
        .name("Survival with friends")
        .author("not_me")
        .build(),
    config,
    listOf(
        ProfileFile.builder("mods/sodium.jar")
            .name("Sodium")
            .hash("sha512", "<sha512>")
            .download("https://cdn.modrinth.com/data/...")
            .source(ProfileFileSource.modrinth("AANobbMI", "<version id>"))
            .build(),
        ProfileFile.fromFile(gameDirectory, gameDirectory.resolve("resourcepacks/our-pack.zip"))
    )
)
```

  </TabItem>
</Tabs>

`ProfileFile.fromFile` reads the hashes and size of a file that is already in the game directory. It has no download URL, so whoever gets the profile has to copy it over by hand

## 2. Sharing a Profile

The paths and the offline username only make sense on your machine. `ProfileFactory.export` leaves them out:

```java
ProfileFactory.export(profile, Path.of("survival.json"));
```

Whoever gets the file passes their own paths with a `ProfileEnvironment` and downloads the files with `ProfileInstaller`:

<Tabs>
  <TabItem label="Java">

```java
Profile shared = ProfileFactory.loadProfile(
        Path.of("survival.json"),
        ProfileEnvironment.of(
                Path.of("<instance>/client.jar"),
                Path.of("<libraries-directory>"),
                Path.of("<assets-directory>")
        )
);

List<ProfileFile> missing = ProfileInstaller.install(shared);
Launcher.launchMinecraft(user, shared.configuration());
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val shared = ProfileFactory.loadProfile(
    Path.of("survival.json"),
    ProfileEnvironment.of(
        Path.of("<instance>/client.jar"),
        Path.of("<libraries-directory>"),
        Path.of("<assets-directory>")
    )
)

val missing = ProfileInstaller.install(shared)
Launcher.launchMinecraft(user, shared.configuration())
```

  </TabItem>
</Tabs>

`ProfileInstaller.install` puts every file into the game directory (`LaunchConfiguration.resolveGameDirectory()`, the client jar's parent directory unless you set one):

* Files that are already there with the right hashes are skipped
* The download URLs are tried in order, the first one that works is used
* Disabled files get a `.disabled` suffix, since most launchers use that
* Files with `client` set to `UNSUPPORTED` are skipped
* Missing files without a download URL are returned, everything else that fails throws `FailedToInstallProfileFileException`

:::caution[File paths are checked]
Paths have to be relative with forward slashes and must not contain `..`, profiles that break this will fail to load
:::

## 3. The Format

Profiles have a `formatVersion`, this version of minified writes `2` and still reads version 1 files (the ones without `formatVersion`). Newer versions throw `UnsupportedProfileFormatException`.

The entries in `files` use the same fields as Modrinth's `.mrpack` format (`path`, `hashes`, `downloads`, `fileSize`, `env`), so modpacks can be converted into profiles and back.

```json
{
  "formatVersion": 2,
  "profile": { "id": "…", "name": "Survival with friends", "author": "Notch", "createdAt": "2026-10-07T08:30:00Z", "updatedAt": "2026-10-07T08:30:00Z" },
  "minRam": 2048,
  "maxRam": 4096,
  "downloadThreads": 5,
  "resolution": { "width": 1920, "height": 1080 },
  "launcher": { "name": "Launcher", "version": "1.0.0" },
  "flags": { "demoUser": false, "customResolution": false, "fullscreen": false },
  "paths": { "jarFile": "…", "librariesDirectory": "…", "assetsDirectory": "…" },
  "launch": {
    "extraJvmArgs": [],
    "extraGameArgs": [],
    "environment": {},
    "serverAddress": "play.example.com",
    "loader": { "type": "FABRIC", "mcVersion": "1.21.11", "loaderVersion": "0.16.14", "iconUrl": null }
  },
  "user": { "offlineUsername": "Player" },
  "files": [
    {
      "path": "mods/sodium.jar",
      "name": "Sodium",
      "hashes": { "sha512": "…" },
      "downloads": ["https://cdn.modrinth.com/data/…"],
      "fileSize": 1234,
      "env": { "client": "required", "server": "unsupported" },
      "enabled": true,
      "source": { "platform": "modrinth", "projectId": "AANobbMI", "versionId": "…" }
    }
  ]
}
```

`paths` and `user` are missing in exported profiles, since they are only relevant for this machine
