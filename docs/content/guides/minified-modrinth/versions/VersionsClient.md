---
title: Versions
description: Fetch, filter, and resolve Modrinth project versions.
---


## 1. The Versions Client

The `VersionsClient` provides access to Modrinth project versions, files, and dependencies.

You can fetch individual versions, retrieve versions for a project, resolve the latest compatible version, and look up versions by file hashes.

Access it through the main `Modrinth` client:

```java
modrinth.versions()
```

## 2. Getting a Version

Fetch a version by its ID:

<Tabs>
  <TabItem label="Java">

```java
Version version = modrinth.versions().get("version-id");
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val version = modrinth.versions().get("version-id")
```

  </TabItem>
</Tabs>

## 3. Getting Multiple Versions

Fetch multiple versions at once:

<Tabs>
  <TabItem label="Java">

```java
List<Version> versions = modrinth.versions().getMany(
        "version-id-1",
        "version-id-2"
);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val versions = modrinth.versions().getMany(
    "version-id-1",
    "version-id-2"
)
```

  </TabItem>
</Tabs>

## 4. Getting Project Versions

Use `getByProject()` to fetch all versions of a project:

<Tabs>
  <TabItem label="Java">

```java
List<Version> versions = modrinth.versions()
        .getByProject("sodium");
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val versions = modrinth.versions()
    .getByProject("sodium")
```

  </TabItem>
</Tabs>

### Filtering Project Versions

Pass a `VersionSearchOptions` object to filter versions:

<Tabs>
  <TabItem label="Java">

```java
VersionSearchOptions options = VersionSearchOptions.builder()
        .gameVersions("1.21.11")
        .loaders(ModLoader.FABRIC)
        .featured(true)
        .build();

List<Version> versions = modrinth.versions()
        .getByProject("sodium", options);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val options = VersionSearchOptions.builder()
    .gameVersions("1.21.11")
    .loaders(ModLoader.FABRIC)
    .featured(true)
    .build()

val versions = modrinth.versions()
    .getByProject("sodium", options)
```

  </TabItem>
</Tabs>

## 5. Resolving Versions

### By Version Number

Use `getVersionByNumber()` to find a project version by its version number:

<Tabs>
  <TabItem label="Java">

```java
Version version = modrinth.versions()
        .getVersionByNumber("sodium", "mc1.21.11-0.8.0");
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val version = modrinth.versions()
    .getVersionByNumber("sodium", "mc1.21.11-0.8.0")
```

  </TabItem>
</Tabs>

:::note
This method returns `null` if no matching version is found.
:::

### Latest Matching Version

Use `resolveLatest()` to get the first version matching the provided filters:

<Tabs>
  <TabItem label="Java">

```java
VersionSearchOptions options = VersionSearchOptions.builder()
        .gameVersions("1.21.11")
        .loaders(ModLoader.FABRIC)
        .build();

Version latest = modrinth.versions()
        .resolveLatest("sodium", options);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val options = VersionSearchOptions.builder()
    .gameVersions("1.21.11")
    .loaders(ModLoader.FABRIC)
    .build()

val latest = modrinth.versions()
    .resolveLatest("sodium", options)
```

  </TabItem>
</Tabs>

:::note
`resolveLatest()` returns `null` when no matching version is found.
:::

## 6. Looking Up Versions by Hash

Versions can also be resolved from file hashes.

### Single Hash

<Tabs>
  <TabItem label="Java">

```java
Version version = modrinth.versions()
        .fromHash("file-hash");
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val version = modrinth.versions()
    .fromHash("file-hash")
```

  </TabItem>
</Tabs>

### Multiple Hashes

<Tabs>
  <TabItem label="Java">

```java
List<Version> versions = modrinth.versions().fromHashes(
        "file-hash-1",
        "file-hash-2"
);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val versions = modrinth.versions().fromHashes(
    "file-hash-1",
    "file-hash-2"
)
```

  </TabItem>
</Tabs>

## 7. Getting Dependencies

Fetch the dependencies of a version:

<Tabs>
  <TabItem label="Java">

```java
List<VersionDependency> dependencies = modrinth.versions()
        .dependencies("version-id");
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val dependencies = modrinth.versions()
    .dependencies("version-id")
```

  </TabItem>
</Tabs>

## 8. Methods

<details class="fields-details">
  <summary class="fields-summary">
    <span>
      <strong>VersionsClient Methods</strong>
      <small>9 available methods</small>
    </span>

    <span class="fields-chevron" aria-hidden="true">›</span>
  </summary>

  <div class="fields-grid">

    <div class="field-card">
      <div class="field-header">
        <code>get()</code>
      </div>
      <p>Fetches a version by its ID.</p>
      <span class="field-meta">Returns: <code>Version</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getMany()</code>
      </div>
      <p>Fetches multiple versions by their IDs.</p>
      <span class="field-meta">Returns: <code>List&lt;Version&gt;</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getByProject()</code>
      </div>
      <p>Fetches versions belonging to a project, optionally using filters.</p>
      <span class="field-meta">Returns: <code>List&lt;Version&gt;</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getVersionByNumber()</code>
      </div>
      <p>Finds a project version by its version number.</p>
      <span class="field-meta">Returns: <code>Version</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>resolveLatest()</code>
      </div>
      <p>Returns the latest version matching the provided search options.</p>
      <span class="field-meta">Returns: <code>Version</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>fromHashes()</code>
      </div>
      <p>Resolves multiple versions from file hashes.</p>
      <span class="field-meta">Returns: <code>List&lt;Version&gt;</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>fromHash()</code>
      </div>
      <p>Resolves a version from a file hash.</p>
      <span class="field-meta">Returns: <code>Version</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>latestFromHashes()</code>
      </div>
      <p>Resolves the latest versions associated with multiple file hashes.</p>
      <span class="field-meta">Returns: <code>List&lt;Version&gt;</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>dependencies()</code>
      </div>
      <p>Fetches the dependencies of a version.</p>
      <span class="field-meta">Returns: <code>List&lt;VersionDependency&gt;</code></span>
    </div>

  </div>
</details>

{/* This block has been copy pasted so many times that Ctrl+C and Ctrl+V are now considered npm dependencies:
    <div class="field-card">
      <div class="field-header">
        <code>yes</code>
      </div>
      <p>Does stuff.</p>
      <span class="field-meta">Returns: <code>String</code></span>
    </div>
*/}