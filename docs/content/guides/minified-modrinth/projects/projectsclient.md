---
title: Projects
description: Fetch, search, and work with Modrinth projects.
---


## 1. The Projects Client

The `ProjectsClient` provides access to Modrinth projects.

You can fetch individual projects, retrieve multiple projects at once, search for projects, and resolve project versions.

```java
modrinth.projects()
```

## 2. Getting a Project

Use `get()` with a project ID or slug:

<Tabs>
  <TabItem label="Java">

```java
Project project = modrinth.projects().get("sodium");
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val project = modrinth.projects().get("sodium")
```

  </TabItem>
</Tabs>

The returned `Project` is automatically attached to the current `Modrinth` client, allowing client-dependent convenience methods such as `Project.getLatestVersion()` to be used.

## 3. Getting Multiple Projects

Use `getMany()` to fetch multiple projects by ID:

<Tabs>
  <TabItem label="Java">

```java
List<Project> projects = modrinth.projects().getMany(
        "AANobbMI",
        "gvQqBUqZ"
);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val projects = modrinth.projects().getMany(
    "AANobbMI",
    "gvQqBUqZ"
)
```

  </TabItem>
</Tabs>

## 4. Searching Projects

Use `search()` with a `SearchRequest`:

<Tabs>
  <TabItem label="Java">

```java
SearchRequest request = SearchRequest.builder()
        .query("optimization")
        .limit(20)
        .build();

SearchResult<Project> result = modrinth.projects().search(request);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val request = SearchRequest.builder()
    .query("optimization")
    .limit(20)
    .build()

val result = modrinth.projects().search(request)
```

  </TabItem>
</Tabs>

Search requests support queries, sorting indexes, pagination, and facets. The result contains the matching projects and pagination metadata.

## 5. Methods

<details class="fields-details" open>
  <summary class="fields-summary">
    <span>
      <strong>ProjectsClient Methods</strong>
      <small>5 available methods</small>
    </span>

    <span class="fields-chevron" aria-hidden="true">›</span>
  </summary>

  <div class="fields-grid">

    <div class="field-card">
      <div class="field-header">
        <code>get()</code>
      </div>
      <p>Fetches a project by its ID or slug.</p>
      <span class="field-meta">Returns: <code>Project</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getMany()</code>
      </div>
      <p>Fetches multiple projects by their IDs.</p>
      <span class="field-meta">Returns: <code>List&lt;Project&gt;</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>search()</code>
      </div>
      <p>Searches for projects using a configured search request.</p>
      <span class="field-meta">Returns: <code>SearchResult&lt;Project&gt;</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getVersions()</code>
      </div>
      <p>Returns project versions matching the provided search options.</p>
      <span class="field-meta">Returns: <code>List&lt;Version&gt;</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getLatestVersion()</code>
      </div>
      <p>Resolves the latest project version matching a game version and one or more loaders.</p>
      <span class="field-meta">Returns: <code>Version</code></span>
    </div>

  </div>
</details>

## 6. Getting Project Versions

Use `getVersions()` to fetch versions matching a `VersionSearchOptions` configuration:

<Tabs>
  <TabItem label="Java">

```java
VersionSearchOptions options = VersionSearchOptions.builder()
        .gameVersions("1.21.11")
        .loaders(ModLoader.FABRIC)
        .build();

List<Version> versions = modrinth.projects()
        .getVersions("sodium", options);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val options = VersionSearchOptions.builder()
    .gameVersions("1.21.11")
    .loaders(ModLoader.FABRIC)
    .build()

val versions = modrinth.projects()
    .getVersions("sodium", options)
```

  </TabItem>
</Tabs>

## 7. Getting the Latest Version

For the common case, resolve the latest compatible version directly:

<Tabs>
  <TabItem label="Java">

```java
Version latest = modrinth.projects().getLatestVersion(
        "sodium",
        "1.21.11",
        ModLoader.FABRIC
);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val latest = modrinth.projects().getLatestVersion(
    "sodium",
    "1.21.11",
    ModLoader.FABRIC
)
```

  </TabItem>
</Tabs>

You can also provide multiple game versions and loaders using the alternative overload.

{/* Yes, the private half of ProjectsClient is intentionally not documented... */}