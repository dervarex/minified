---
title: Projects
description: Work with Modrinth projects and their metadata.
---


## 1. The Project Object

The `Project` class represents a project returned by the Modrinth API.

A project can contain metadata such as its title, description, supported Minecraft versions, loaders, download count, gallery images, links, and more.

<Tabs>
  <TabItem label="Java">

```java
Project project = modrinth.projects().get("sodium");
````

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val project = modrinth.projects().get("sodium")
```

  </TabItem>
</Tabs>

You can access project metadata directly:

<Tabs>
  <TabItem label="Java">

```java
String title = project.title;
String description = project.description;
long downloads = project.downloads;
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val title = project.title
val description = project.description
val downloads = project.downloads
```

  </TabItem>
</Tabs>

## 2. Fields

The available fields depend on whether the project was returned by a search endpoint or a detail endpoint.

<details class="fields-details">
  <summary class="fields-summary">
    <span>
      <strong>Project Fields</strong>
      <small>Project metadata</small>
    </span>


<span class="fields-chevron" aria-hidden="true">›</span>

  </summary>

  <div class="fields-grid">


<div class="field-card">
  <div class="field-header">
    <code>id</code>
    <span class="badge optional">Detail</span>
  </div>
  <p>The unique Modrinth project ID returned by the detail endpoint.</p>
  <span class="field-meta">Type: <code>String</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>projectId</code>
    <span class="badge optional">Search</span>
  </div>
  <p>The unique Modrinth project ID returned by the search endpoint.</p>
  <span class="field-meta">Type: <code>String</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>slug</code>
  </div>
  <p>The human-readable identifier of the project.</p>
  <span class="field-meta">Type: <code>String</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>title</code>
  </div>
  <p>The display name of the project.</p>
  <span class="field-meta">Type: <code>String</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>description</code>
  </div>
  <p>A short description of the project.</p>
  <span class="field-meta">Type: <code>String</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>body</code>
    <span class="badge optional">Detail</span>
  </div>
  <p>The full project description.</p>
  <span class="field-meta">Type: <code>String</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>bodyUrl</code>
  </div>
  <p>Legacy field, always <code>null</code> according to the Modrinth API.</p>
  <span class="field-meta">Type: <code>String</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>projectType</code>
  </div>
  <p>The type of content provided by the project.</p>
  <span class="field-meta">Type: <code>ProjectType</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>monetizationStatus</code>
  </div>
  <p>The monetization status of the project.</p>
  <span class="field-meta">Type: <code>String</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>author</code>
  </div>
  <p>The display name of the project author.</p>
  <span class="field-meta">Type: <code>String</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>authorId</code>
  </div>
  <p>The unique ID of the project author.</p>
  <span class="field-meta">Type: <code>String</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>organization</code>
  </div>
  <p>The organization that owns the project, if available.</p>
  <span class="field-meta">Type: <code>String</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>organizationId</code>
  </div>
  <p>The unique ID of the organization that owns the project, if available.</p>
  <span class="field-meta">Type: <code>String</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>team</code>
  </div>
  <p>The ID of the team associated with the project.</p>
  <span class="field-meta">Type: <code>String</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>clientSide</code>
  </div>
  <p>Describes the project's client-side support.</p>
  <span class="field-meta">Type: <code>SideSupport</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>serverSide</code>
  </div>
  <p>Describes the project's server-side support.</p>
  <span class="field-meta">Type: <code>SideSupport</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>environment</code>
  </div>
  <p>Replaces <code>clientSide</code>/<code>serverSide</code> as the preferred way to describe where the project runs. Both are still populated, but prefer this field going forward.</p>
  <span class="field-meta">Type: <code>Environment[]</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>categories</code>
  </div>
  <p>The categories and tags assigned to the project.</p>
  <span class="field-meta">Type: <code>String[]</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>displayCategories</code>
  </div>
  <p>The subset of categories shown on the project's search card.</p>
  <span class="field-meta">Type: <code>String[]</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>additionalCategories</code>
  </div>
  <p>Categories assigned to the project that aren't primary categories.</p>
  <span class="field-meta">Type: <code>String[]</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>versions</code>
  </div>
  <p>The IDs of every version published under the project.</p>
  <span class="field-meta">Type: <code>String[]</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>gameVersions</code>
  </div>
  <p>The Minecraft versions supported by the project.</p>
  <span class="field-meta">Type: <code>String[]</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>loaders</code>
  </div>
  <p>The mod loaders and platforms supported by the project.</p>
  <span class="field-meta">Type: <code>String[]</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>disclosureTypes</code>
    <span class="badge optional">Search</span>
  </div>
  <p>Content disclosure categories (AI content, telemetry, advertisements, and so on) the project is tagged with, returned by the search endpoint only.</p>
  <span class="field-meta">Type: <code>DisclosureType[]</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>downloads</code>
  </div>
  <p>The total number of project downloads.</p>
  <span class="field-meta">Type: <code>long</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>follows</code>
    <span class="badge optional">Search</span>
  </div>
  <p>The number of project followers returned by the search endpoint.</p>
  <span class="field-meta">Type: <code>long</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>followers</code>
    <span class="badge optional">Detail</span>
  </div>
  <p>The number of project followers returned by the detail endpoint.</p>
  <span class="field-meta">Type: <code>long</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>iconUrl</code>
  </div>
  <p>The URL of the project's icon.</p>
  <span class="field-meta">Type: <code>String</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>rawIconUrl</code>
    <span class="badge optional">Detail</span>
  </div>
  <p>The unoptimized icon, returned by the detail endpoint only.</p>
  <span class="field-meta">Type: <code>String</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>color</code>
  </div>
  <p>The project's display color.</p>
  <span class="field-meta">Type: <code>Integer</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>threadId</code>
  </div>
  <p>The ID of the moderation thread associated with the project.</p>
  <span class="field-meta">Type: <code>String</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>issuesUrl</code>
  </div>
  <p>The URL of the project's issue tracker, if any.</p>
  <span class="field-meta">Type: <code>String</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>sourceUrl</code>
  </div>
  <p>The URL of the project's source code repository, if any.</p>
  <span class="field-meta">Type: <code>String</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>wikiUrl</code>
  </div>
  <p>The URL of the project's wiki page, if any.</p>
  <span class="field-meta">Type: <code>String</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>discordUrl</code>
  </div>
  <p>The invite URL of the project's Discord server, if any.</p>
  <span class="field-meta">Type: <code>String</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>donationUrls</code>
  </div>
  <p>The donation platforms and links listed for the project.</p>
  <span class="field-meta">Type: <code>DonationUrl[]</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>license</code>
  </div>
  <p>License information for the project.</p>
  <span class="field-meta">Type: <code>ProjectLicense</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>gallery</code>
  </div>
  <p>The images available in the project's gallery.</p>
  <span class="field-meta">Type: <code>GalleryImage[]</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>published</code>
  </div>
  <p>The time at which the project was published.</p>
  <span class="field-meta">Type: <code>Instant</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>updated</code>
  </div>
  <p>The time at which the project was last updated.</p>
  <span class="field-meta">Type: <code>Instant</code></span>
</div>

  </div>
</details>

## 3. Convenience Methods

The `Project` class provides convenience methods for common project checks and metadata access.

<details class="fields-details" closed>
  <summary class="fields-summary">
    <span>
      <strong>Project Methods</strong>
      <small>Metadata and compatibility helpers</small>
    </span>

<span class="fields-chevron" aria-hidden="true">›</span>

  </summary>

  <div class="fields-grid">


<div class="field-card">
  <div class="field-header">
    <code>getTags()</code>
  </div>
  <p>Returns the project's categories as tags.</p>
  <span class="field-meta">Returns: <code>String[]</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>getDisplayTags()</code>
  </div>
  <p>Returns the subset of tags shown on the project's search card.</p>
  <span class="field-meta">Returns: <code>String[]</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>getAdditionalTags()</code>
  </div>
  <p>Returns tags assigned to the project that aren't primary categories.</p>
  <span class="field-meta">Returns: <code>String[]</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>hasTag()</code>
  </div>
  <p>Checks whether the project contains a specific tag.</p>
  <span class="field-meta">Returns: <code>boolean</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>hasDisplayTag()</code>
  </div>
  <p>Checks whether the project contains a specific display tag.</p>
  <span class="field-meta">Returns: <code>boolean</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>hasAdditionalTag()</code>
  </div>
  <p>Checks whether the project contains a specific additional tag.</p>
  <span class="field-meta">Returns: <code>boolean</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>hasLoader()</code>
  </div>
  <p>Checks whether the project supports a loader.</p>
  <span class="field-meta">
    Accepts: <code>String</code> or <code>ModLoader</code>
  </span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>supportsVersion()</code>
  </div>
  <p>Checks whether the project supports a Minecraft version.</p>
  <span class="field-meta">Returns: <code>boolean</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>supportsEnvironment()</code>
  </div>
  <p>Checks whether the project supports a specific <code>Environment</code> value.</p>
  <span class="field-meta">Returns: <code>boolean</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>hasDisclosure()</code>
  </div>
  <p>Checks whether the project is tagged with a specific <code>DisclosureType</code>.</p>
  <span class="field-meta">Returns: <code>boolean</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>getFeaturedGallery()</code>
  </div>
  <p>Returns the featured gallery image, if one exists.</p>
  <span class="field-meta">Returns: <code>GalleryImage</code></span>
</div>

<div class="field-card">
  <div class="field-header">
    <code>getLatestVersion()</code>
  </div>
  <p>Resolves the latest matching version of the project.</p>
  <span class="field-meta">Returns: <code>Version</code></span>
</div>


  </div>
</details>

### Checking Compatibility

<Tabs>
  <TabItem label="Java">

```java
boolean supportsFabric = project.hasLoader(ModLoader.FABRIC);
boolean supportsVersion = project.supportsVersion("1.21.11");

if (supportsFabric && supportsVersion) {
    // The project supports Fabric on Minecraft 1.21.11
}
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val supportsFabric = project.hasLoader(ModLoader.FABRIC)
val supportsVersion = project.supportsVersion("1.21.11")

if (supportsFabric && supportsVersion) {
    // The project supports Fabric on Minecraft 1.21.11
}
```

  </TabItem>
</Tabs>

### Working with Tags

<Tabs>
  <TabItem label="Java">

```java
boolean optimization = project.hasTag("optimization");
boolean technology = project.hasDisplayTag("technology");
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val optimization = project.hasTag("optimization")
val technology = project.hasDisplayTag("technology")
```

  </TabItem>
</Tabs>

### Checking Environment and Disclosures

<Tabs>
  <TabItem label="Java">

```java
boolean clientOnly = project.supportsEnvironment(Environment.CLIENT_ONLY);
boolean usesTelemetry = project.hasDisclosure(DisclosureType.TELEMETRY);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val clientOnly = project.supportsEnvironment(Environment.CLIENT_ONLY)
val usesTelemetry = project.hasDisclosure(DisclosureType.TELEMETRY)
```

  </TabItem>
</Tabs>

:::note
`disclosureTypes` is only populated by the search endpoint - a `Project` fetched with `ProjectsClient.get()` will have `hasDisclosure()` always return `false`, since the field is `null`.
:::

### Getting the Featured Gallery Image

`getFeaturedGallery()` returns the gallery image marked as featured, or `null` if the project has no featured image.

<Tabs>
  <TabItem label="Java">

```java
GalleryImage featured = project.getFeaturedGallery();

if (featured != null) {
    System.out.println(featured.url);
}
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val featured = project.getFeaturedGallery()

if (featured != null) {
    println(featured.url)
}
```

  </TabItem>
</Tabs>

## 4. Resolving the Latest Version

A project returned by a `Modrinth` client is automatically attached to that client. This allows you to resolve the latest matching version directly from the project.

### By Game Version and Loader

<Tabs>
  <TabItem label="Java">

```java
Version latest = project.getLatestVersion(
        "1.21.11",
        ModLoader.FABRIC
);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val latest = project.getLatestVersion(
    "1.21.11",
    ModLoader.FABRIC
)
```

  </TabItem>
</Tabs>

### With Version Search Options

For more control, pass a `VersionSearchOptions` object:

<Tabs>
  <TabItem label="Java">

```java
VersionSearchOptions options = VersionSearchOptions.builder()
        .gameVersions("1.21.11")
        .loaders("fabric")
        .build();

Version latest = project.getLatestVersion(options);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val options = VersionSearchOptions.builder()
    .gameVersions("1.21.11")
    .loaders("fabric")
    .build()

val latest = project.getLatestVersion(options)
```

  </TabItem>
</Tabs>