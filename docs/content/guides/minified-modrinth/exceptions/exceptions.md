---
title: Exceptions
description: The exception hierarchy thrown by Minified Modrinth.
---


Every exception `minified-modrinth` throws is an unchecked `RuntimeException` rooted at `ModrinthException`, so you're never forced to wrap every client call in a `try`/`catch` - but catching `ModrinthException` gets you all of them at once when you want to.

## 1. ModrinthException

The base type for everything on this page. Carries a message and, optionally, a cause - nothing else.

## 2. ModrinthApiException

Thrown whenever a request to the Modrinth API fails at the HTTP/protocol level - a non-2xx response that isn't specifically a 404 or a 429 (those get their own subclasses below), or the request couldn't even be sent (e.g. no connection). Every `ProjectsClient`/`VersionsClient`/`TagsClient`/`UsersClient`/`TeamsClient` method that talks to the network can throw this.

<details class="fields-details" closed>
  <summary class="fields-summary">
    <span>
      <strong>ModrinthApiException Methods</strong>
      <small>2 available methods</small>
    </span>

    <span class="fields-chevron" aria-hidden="true">›</span>
  </summary>

  <div class="fields-grid">

    <div class="field-card">
      <div class="field-header">
        <code>getStatusCode()</code>
      </div>
      <p>The HTTP status code returned by the API, or <code>-1</code> if the request never reached the server.</p>
      <span class="field-meta">Returns: <code>int</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getRequestUrl()</code>
      </div>
      <p>The full URL that was requested, including query parameters.</p>
      <span class="field-meta">Returns: <code>String</code></span>
    </div>

  </div>
</details>

<Tabs>
  <TabItem label="Java">

```java
try {
    modrinth.projects().get("this-does-not-exist-either");
} catch (ModrinthApiException e) {
    System.out.println("Modrinth request to " + e.getRequestUrl() + " failed: " + e.getStatusCode());
}
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
try {
    modrinth.projects().get("this-does-not-exist-either")
} catch (e: ModrinthApiException) {
    println("Modrinth request to ${e.requestUrl} failed: ${e.statusCode}")
}
```

  </TabItem>
</Tabs>

## 3. ModrinthNotFoundException

A `ModrinthApiException` subclass thrown specifically for HTTP `404` responses - a project, version, user, or team ID/slug that doesn't exist. Catch this separately when you want to treat "not found" differently from other API failures (e.g. falling back to a search instead of surfacing an error):

<Tabs>
  <TabItem label="Java">

```java
Project project;
try {
    project = modrinth.projects().get(slugOrId);
} catch (ModrinthNotFoundException e) {
    project = null;
}
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val project = try {
    modrinth.projects().get(slugOrId)
} catch (e: ModrinthNotFoundException) {
    null
}
```

  </TabItem>
</Tabs>

## 4. ModrinthRateLimitedException

A `ModrinthApiException` subclass thrown for HTTP `429` responses. Adds `getRetryAfterSeconds()`, parsed from the response's `Retry-After` header (`0` if the header was missing or unparsable):

<Tabs>
  <TabItem label="Java">

```java
try {
    modrinth.projects().search(request);
} catch (ModrinthRateLimitedException e) {
    Thread.sleep(e.getRetryAfterSeconds() * 1000L);
}
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
try {
    modrinth.projects().search(request)
} catch (e: ModrinthRateLimitedException) {
    Thread.sleep(e.retryAfterSeconds * 1000L)
}
```

  </TabItem>
</Tabs>

## 5. ModrinthSerializationException

Thrown when a response body couldn't be parsed as the JSON shape a client method expected - normally a sign the API changed the response format, or the body itself wasn't valid JSON to begin with. Carries the underlying parse failure as its cause.

## 6. ModrinthStateException

Thrown when a model method needs a `Modrinth` client but the model instance isn't attached to one - most commonly, calling `Project.getLatestVersion(...)` on a `Project` you constructed yourself instead of getting back from `ProjectsClient`. See the <a href="/guides/minified-modrinth/projects/project" class="link">Projects guide</a> for how attachment works.

## 7. ModrinthDownloadException

Thrown by `VersionFile.download(directory)` (and, by extension, `Version.download(directory)`/`Version.downloadDependencies(directory)`, which delegate to it) when a file download fails - a missing download URL, a non-2xx response while downloading, an I/O failure, or a hash mismatch against the checksum Modrinth published for the file.

## 8. ModrinthDependencyResolutionException

Thrown by `Version.download(directory)` when the version has no downloadable files at all (an empty `files` array), so there's nothing to resolve a primary file from.
