---
title: Migrating to 4.0
description: Migrating to Minified v4.0.0
---

This migration guide covers everything from 3.2.3 to 4.0.0.

## Dependency

Minified moved from JitPack to Maven Central, the group is `io.github.dervarex.minified` and versions have no `v` anymore.

```groovy
// before
implementation 'com.github.dervarex.minified:minified-launch:v3.2.3'
// now
implementation 'io.github.dervarex.minified:minified-launch:4.0.0'
```

## General

- The library doesn't print anymore, everything that used to go to the console is an event now, see below.
- Static helper classes have private constructors now, call their static methods.
- A listener that throws doesn't get its stack trace printed anymore, the exception goes to the thread's `UncaughtExceptionHandler`.
- Most `RuntimeException`s and `IOException`s are now specific exceptions, see [Exceptions](/exceptions/introduction).

| Printed before                  | Event now                    |
|---------------------------------|------------------------------|
| Device code login failed        | `DeviceCodeLoginFailedEvent` |
| Session refresh failed          | `SessionRefreshFailedEvent`  |
| Headless game stopped           | `HeadlessStopEvent`          |
| Forge/NeoForge installer output | `LoaderInstallerOutputEvent` |


## minified-auth

- `AuthManager.init(...)` throws `AuthInitException` if it fails.
- A login without internet throws `NoConnectionLoginFailedException` (a `LoginFailedException`) instead of a `RuntimeException`.
- `User.toString()` doesn't contain the access token and the session anymore.
- `Encryptor` methods throw `IOException` and `GeneralSecurityException` instead of `Exception`.

## minified-java

- Failing Java downloads and installs throw `JavaRuntimeDownloadException`, `JavaRuntimeInstallException` or `NoJavaRuntimeAvailableException`.

## minified-launch

- `ClientDownloader.downloadClient(...)` throws `VersionNotFoundException` for unknown versions instead of printing and returning.
- Loader fetchers throw `NoLoadersFoundException` when there's no loader for a Minecraft version.
- A failing launch throws `LaunchFailedException`, a game that exits with an error posts `NonZeroExitCodeEvent` and throws `NonZeroExitCodeException`.
- `UnknownLoaderTypeException` is gone, it's `UnexpectedLoaderException` now.
- `CustomLoader` works in every loader switch instead of throwing `UnexpectedLoaderException`.
- Profiles have a new format with metadata and files, see [Profiles](/guides/minified-launch/profiles).

## minified-utils

- `DownloadProgress` is removed.
- `DownloadHelper` throws `DownloadFailedException`, a wrong SHA-1 is a `ChecksumMismatchException`.
- Archives with entries pointing outside the target folder throw `UnsafeArchiveEntryException`.
- Broken NBT throws `NbtFormatException`.

## minified-worlds

- Saving a world that's open in Minecraft throws `WorldLockedException`.
- Game rules, scoreboard, world gen settings and the other data files keep everything they don't model when saved now, before we had serious data loss.

## minified-modrinth

The whole module got cleaned up, I recommend viewing the new docs, as you might have to rewrite it anyway, and this guide might not be complete.

### Client

- `new Modrinth(...)` -> `Modrinth.connect()`, `connect(baseUrl)` or `connect(baseUrl, eventBus)`
- `new ProjectsClient(modrinth)` and the other clients -> `modrinth.projects()`, `modrinth.versions()`, ...

### Everywhere

- Arrays -> `List`
- `ModrinthStateException` is gone, remove the catch blocks
- String loaders -> `ModLoader.FABRIC`, `PluginLoader...`, `ShaderLoader...`, they all implement `Loader`
- `enum.toString()` / `String.valueOf(enum)` -> `enum.getApiValue()`, `toString()` is the constant name now

### Search

- `SearchOptions` -> `SearchRequest`
- `index("downloads")` -> `index(SearchIndex.DOWNLOADS)`
- `projectType(...)` -> `projectTypes(...)`
- `environment(...)`, `clientSide(...)`, `serverSide(...)` -> `environments(Environment...)`
- `facets(list)` -> one `facet(key, values...)` per group
- `result.hits` -> `result.getHits()`, same for `offset`, `limit` and `totalHits`

### Project

| Before                                | Now                                           |
|---------------------------------------|-----------------------------------------------|
| `projectId` / `id`                    | `getId()`                                     |
| `follows` / `followers`               | `getFollowers()`                              |
| `organizationId`                      | `getOrganization()`                           |
| `getTags()` / `getDisplayTags()`      | `getCategories()`                             |
| `getAdditionalTags()`                 | `getAdditionalCategories()`                   |
| `hasTag(...)` / `hasDisplayTag(...)`  | `hasCategory(...)`                            |
| `hasAdditionalTag(t)`                 | `getAdditionalCategories().contains(t)`       |
| `supportsVersion(...)`                | `supportsGameVersion(...)`                    |
| `supportsEnvironment(e)`              | `getEnvironment().contains(e)`                |
| `clientSide` / `serverSide`           | `getEnvironment()`                            |
| `getFeaturedGallery()`                | `getFeaturedImage()`                          |
| `ProjectLicense`                      | `projects.License`                            |
| `author` / `authorId`                 | `modrinth.teams().members(project.getTeam())` |
| `getLatestVersion(...)`               | `modrinth.versions().getLatest(id, options)`  |
| `projects().getVersions(id, options)` | `versions().getByProject(id, options)`        |
| `bodyUrl`                             | `getBody()`                                   |


No replacement: `threadId`, `disclosureTypes`, `setTags(...)`, `setLicense(...)`

### Versions

| Before                               | Now                                                                |
|--------------------------------------|--------------------------------------------------------------------|
| `getVersionByNumber(...)`            | `getByNumber(...)`                                                 |
| `resolveLatest(...)`                 | `getLatest(...)`                                                   |
| `latestFromHashes(hashes)`           | `latestFromHashes(VersionSearchOptions.builder().build(), hashes)` |
| `versions().dependencies(id)`        | `version.getDependencies()`                                        |
| `version.getProject()`               | `modrinth.projects().get(version.getProjectId())`                  |
| `version.changelog` / `changelogUrl` | `getChangelog()`                                                   |
| `hasLoader("fabric")`                | `hasLoader(ModLoader.FABRIC)`                                      |
| `supportsVersion(...)`               | `supportsGameVersion(...)`                                         |
| `file.getHash("sha1")`               | `file.getHashes().get("sha1")`                                     |
| `file.hasHash("sha1")`               | `file.getHashes().containsKey("sha1")`                             |
| `dependency.isOptional()`            | `getDependencyType() == DependencyType.OPTIONAL`                   |

- `getByNumber` and `fromHash` throw `ModrinthNotFoundException` for unknown versions or files
- `VersionSearchOptions` returns every match unless you set a limit (was 10 before)
- `getMany(...)` keeps the order of the IDs you pass
- `requestedStatus` is removed

### Tags

- `categories()` returns `Category`, `loaders()` returns `LoaderTag`, `donationPlatforms()` returns `DonationPlatform`
- `versions()` -> `gameVersions()`, returns `GameVersion`
- `licenses()`, `sideTypes()`, `users()`, `teams()`, `versionFiles()` and `tags()` are removed, most of them never worked anyway

### Users and teams

- `teams().get(...)` / `getMany(...)` -> `members(teamId)`
- `member.getPermissions()` and `getJoined()` are removed
- `user.getName()` -> `getUsername()`
- `Team`, `Tag`, `tags.License` and `SideSupport` are removed
