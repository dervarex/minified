---
title: Tags
description: Work with Modrinth tags.
---


## 1. The Tag Object

The `Tag` class represents a tag returned by the Modrinth tag endpoints. Tags can be used to categorize projects and provide metadata for search filters.

Depending on the tag type, different fields may be available.

<details class="fields-details" closed>
  <summary class="fields-summary">
    <span>
      <strong>Tag Fields</strong>
      <small>9 available fields</small>
    </span>

    <span class="fields-chevron" aria-hidden="true">›</span>
  </summary>

  <div class="fields-grid">

    <div class="field-card">
      <div class="field-header">
        <code>id</code>
      </div>
      <p>The unique identifier of the tag.</p>
      <span class="field-meta">Type: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>name</code>
      </div>
      <p>The display name of the tag.</p>
      <span class="field-meta">Type: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>description</code>
      </div>
      <p>A description of the tag.</p>
      <span class="field-meta">Type: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>icon</code>
      </div>
      <p>The icon associated with the tag.</p>
      <span class="field-meta">Type: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>header</code>
      </div>
      <p>The header associated with the tag.</p>
      <span class="field-meta">Type: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>value</code>
      </div>
      <p>The API value used to identify the tag.</p>
      <span class="field-meta">Type: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>featured</code>
      </div>
      <p>Whether the tag is featured by Modrinth.</p>
      <span class="field-meta">Type: <code>boolean</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>projectType</code>
      </div>
      <p>The project type associated with the tag.</p>
      <span class="field-meta">Type: <code>ProjectType</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>projectTypes</code>
      </div>
      <p>The project types supported by the tag.</p>
      <span class="field-meta">Type: <code>String[]</code></span>
    </div>

  </div>
</details>

## 2. The Tags Client

The `TagsClient` provides access to the tags and metadata available through the Modrinth API.

Access it through the main `Modrinth` client:

```java
modrinth.tags()
```

## 3. Getting Tags

Each method returns a different type of tag or metadata.

<Tabs>
  <TabItem label="Java">

```java
List<Tag> categories = modrinth.tags().categories();
List<Tag> loaders = modrinth.tags().loaders();
List<Tag> versions = modrinth.tags().versions();
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val categories = modrinth.tags().categories()
val loaders = modrinth.tags().loaders()
val versions = modrinth.tags().versions()
```

  </TabItem>
</Tabs>

<details class="fields-details" closed>
  <summary class="fields-summary">
    <span>
      <strong>TagsClient Methods</strong>
      <small>11 available methods</small>
    </span>

    <span class="fields-chevron" aria-hidden="true">›</span>
  </summary>

  <div class="fields-grid">

    <div class="field-card">
      <div class="field-header">
        <code>categories()</code>
      </div>
      <p>Returns all available project categories.</p>
      <span class="field-meta">Returns: <code>List&lt;Tag&gt;</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>loaders()</code>
      </div>
      <p>Returns all available mod loaders and platforms.</p>
      <span class="field-meta">Returns: <code>List&lt;Tag&gt;</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>versions()</code>
      </div>
      <p>Returns all available Minecraft versions.</p>
      <span class="field-meta">Returns: <code>List&lt;Tag&gt;</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>users()</code>
      </div>
      <p>Returns the available user tags.</p>
      <span class="field-meta">Returns: <code>List&lt;Tag&gt;</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>teams()</code>
      </div>
      <p>Returns the available team tags.</p>
      <span class="field-meta">Returns: <code>List&lt;Tag&gt;</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>versionFiles()</code>
      </div>
      <p>Returns the available version file tags.</p>
      <span class="field-meta">Returns: <code>List&lt;Tag&gt;</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>tags()</code>
      </div>
      <p>Returns general Modrinth tags.</p>
      <span class="field-meta">Returns: <code>List&lt;Tag&gt;</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>licenses()</code>
      </div>
      <p>Returns all available project licenses.</p>
      <span class="field-meta">Returns: <code>List&lt;License&gt;</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>projectTypes()</code>
      </div>
      <p>Returns all available project types.</p>
      <span class="field-meta">Returns: <code>List&lt;ProjectType&gt;</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>sideTypes()</code>
      </div>
      <p>Returns all available client and server side support types.</p>
      <span class="field-meta">Returns: <code>List&lt;SideSupport&gt;</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>donationPlatforms()</code>
      </div>
      <p>Returns all available donation platforms.</p>
      <span class="field-meta">Returns: <code>List&lt;Tag&gt;</code></span>
    </div>

  </div>
</details>

## 4. Example

For example, you can retrieve all loaders and inspect their API values:

<Tabs>
  <TabItem label="Java">

```java
List<Tag> loaders = modrinth.tags().loaders();

for (Tag loader : loaders) {
    System.out.println(loader.name + ": " + loader.value);
}
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val loaders = modrinth.tags().loaders()

for (loader in loaders) {
    println("${loader.name}: ${loader.value}")
}
```

  </TabItem>
</Tabs>

{/* I need to find some funny comment to put here, but I can't think of anything right now, so I'll just leave this here. */}