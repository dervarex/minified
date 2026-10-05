---
title: Modrinth
description: Connect to the Modrinth API with Minified Modrinth.
---


## 1. Add the Dependency

Add `minified-modrinth` to your `build.gradle` file:

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.dervarex.minified:minified-modrinth:3.0.0'
}
```

This module is independent of `minified-launch` - it only depends on `minified-utils` for its HTTP and JSON plumbing, so you can use it in a launcher, a mod manager, or a standalone tool without pulling in the rest of Minified.

---

## 2. Connecting

Every other Modrinth doc page assumes you already have a `Modrinth` client. Create one with `Modrinth.connect()`:

<Tabs>
  <TabItem label="Java">

```java
Modrinth modrinth = Modrinth.connect();
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val modrinth = Modrinth.connect()
```

  </TabItem>
</Tabs>

This talks to the public Modrinth API at `https://api.modrinth.com/v2`. If you're pointing at a staging instance or a self-hosted, API-compatible mirror, pass a custom base URL instead:

<Tabs>
  <TabItem label="Java">

```java
Modrinth modrinth = Modrinth.connect("https://staging-api.modrinth.com/v2");
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val modrinth = Modrinth.connect("https://staging-api.modrinth.com/v2")
```

  </TabItem>
</Tabs>

A `Modrinth` instance is cheap to keep around and safe to reuse for the lifetime of your application - each sub-client (`projects()`, `versions()`, `tags()`, `users()`, `teams()`) is created once and shared.

---

## 3. Where to Go Next

* **<a href="/guides/minified-modrinth/projects/project" class="link">Projects</a>**: fetch, search, and inspect Modrinth projects.
* **<a href="/guides/minified-modrinth/versions/versionsclient" class="link">Versions</a>**: resolve project versions, files, and dependencies.
* **<a href="/guides/minified-modrinth/tags/tag" class="link">Tags</a>**: categories, loaders, licenses, and other search metadata.
* **<a href="/guides/minified-modrinth/teams/teamsclient" class="link">Teams</a>** and **<a href="/guides/minified-modrinth/users/usersclient" class="link">Users</a>**: project ownership.
* **<a href="/guides/minified-modrinth/exceptions/exceptions" class="link">Exceptions</a>**: what can go wrong, and how to handle it.
* **<a href="/guides/minified-modrinth/loaders/loaders" class="link">Loaders</a>**: the full list of mod loader, plugin loader, platform, and shader loader identifiers Modrinth recognizes.
