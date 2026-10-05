---

title: Versions
description: Get available versions of Minecraft and their metadata.
--------------------------------------------------------------------


## 1. Fetching Available Versions

You can fetch all available Minecraft versions using `VersionListProvider`:

<Tabs>
  <TabItem label="Java">

```java
ArrayList<String> versions = VersionListProvider.getVersions();
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val versions = VersionListProvider.getVersions()
```

  </TabItem>
</Tabs>

### Filtering Versions

`VersionListProvider` also provides methods for fetching specific version types:

* `getReleaseVersions()` - Returns all available release versions.
* `getLatestReleaseVersion()` - Returns the latest release version.
* `getLatestSnapshotVersion()` - Returns the latest snapshot version.

## 2. Getting Version Metadata as JSON

If you need the metadata of a specific version because you need something more specific, you can use `VersionManifestClient`:

:::caution[VersionManifestClient is lower-level]
`VersionManifestClient` is marked `INTERNAL` by Minified's API status annotations - it's exported and safe to call, but it's the raw building block `VersionListProvider` and `VersionMetadataProvider` (section 3 below) are built on top of, not the recommended entry point. Prefer those two for anything they already cover, and only reach for `VersionManifestClient` when you need the full manifest or version JSON yourself.
:::

<Tabs>
  <TabItem label="Java">

```java
JsonFile manifest = VersionManifestClient.getManifest();
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val manifest = VersionManifestClient.getManifest()
```

  </TabItem>
</Tabs>

We also provide ways to get specific data, so you mostly don't have to parse the manifest yourself:

<Tabs>
  <TabItem label="Java">

```java
Iterable<JsonValue> versions = VersionManifestClient.getVersions();
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val versions = VersionManifestClient.getVersions()
```

  </TabItem>
</Tabs>

To get the manifest entry for a specific version:

<Tabs>
  <TabItem label="Java">

```java
JsonValue versionEntry = VersionManifestClient.getVersionEntry("1.21.11");
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val versionEntry = VersionManifestClient.getVersionEntry("1.21.11")
```

  </TabItem>
</Tabs>

## 3. Convenience Metadata Methods

For common version metadata, use `VersionMetadataProvider` instead of fetching and parsing the version JSON manually.

<details class="fields-details">
  <summary class="fields-summary">
    <span>
      <strong>Metadata Methods</strong>
      <small>8 available methods</small>
    </span>

    <span class="fields-chevron" aria-hidden="true">›</span>
  </summary>

  <div class="fields-grid">

    <div class="field-card">
      <div class="field-header">
        <code>getVersionJsonUrl()</code>
      </div>
      <p>Returns the URL of the full metadata JSON for a Minecraft version.</p>
      <span class="field-meta">Returns: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getMainClass()</code>
      </div>
      <p>Returns the main class used to launch the Minecraft client.</p>
      <span class="field-meta">Returns: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getVersionType()</code>
      </div>
      <p>Returns the type of the Minecraft version.</p>
      <span class="field-meta">
        Examples: <code>release</code>, <code>snapshot</code>
      </span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getReleaseTime()</code>
      </div>
      <p>Returns the release timestamp of the Minecraft version.</p>
      <span class="field-meta">Returns: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getClientSha1()</code>
      </div>
      <p>Returns the SHA-1 checksum of the Minecraft client JAR.</p>
      <span class="field-meta">Returns: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getClientUrl()</code>
      </div>
      <p>Returns the download URL of the Minecraft client JAR.</p>
      <span class="field-meta">Returns: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getServerSha1()</code>
      </div>
      <p>Returns the SHA-1 checksum of the Minecraft server JAR.</p>
      <span class="field-meta">Returns: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getServerUrl()</code>
      </div>
      <p>Returns the download URL of the Minecraft server JAR.</p>
      <span class="field-meta">Returns: <code>String</code></span>
    </div>

  </div>
</details>
