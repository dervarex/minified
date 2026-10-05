---
title: HTTP
description: The lightweight HTTP client Minified uses internally, built on java.net.HttpURLConnection.
---


## 1. HttpUtil

`HttpUtil` is a small static wrapper around `java.net.HttpURLConnection`. It's what almost every network call inside Minified goes through, version manifests, Modrinth API requests, Forge/NeoForge metadata lookups and it's exported for you to use too, so you don't need to add a separate HTTP client dependency just to call a JSON API from your launcher.

<Tabs>
  <TabItem label="Java">

```java
String body = HttpUtil.get("https://example.com/data.json");
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val body = HttpUtil.get("https://example.com/data.json")
```

  </TabItem>
</Tabs>

For anything beyond a plain GET, use `request(...)` directly:

<Tabs>
  <TabItem label="Java">

```java
HttpResponse response = HttpUtil.request(
        "POST",
        "https://example.com/api",
        Map.of("Authorization", "Bearer " + token),
        payloadBytes,
        10_000, // connect timeout ms
        10_000  // read timeout ms
);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val response = HttpUtil.request(
    "POST",
    "https://example.com/api",
    mapOf("Authorization" to "Bearer $token"),
    payloadBytes,
    10_000, // connect timeout ms
    10_000  // read timeout ms
)
```

  </TabItem>
</Tabs>

There's also `requestJson(method, url, jsonBody)`, a shortcut that sets `Content-Type`/`Accept` to `application/json` for you and encodes the body as UTF-8.

<details class="fields-details" closed>
  <summary class="fields-summary">
    <span>
      <strong>HttpUtil Methods</strong>
      <small>4 available methods</small>
    </span>

    <span class="fields-chevron" aria-hidden="true">›</span>
  </summary>

  <div class="fields-grid">

    <div class="field-card">
      <div class="field-header">
        <code>get(url)</code>
      </div>
      <p>Performs a GET request with a 10 second timeout and returns the response body as a UTF-8 string.</p>
      <span class="field-meta">Returns: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>get(url, timeoutMs)</code>
      </div>
      <p>Same as <code>get(url)</code> but with a custom connect/read timeout.</p>
      <span class="field-meta">Returns: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>request(method, url, headers, body, connectTimeoutMs, readTimeoutMs)</code>
      </div>
      <p>Performs an arbitrary HTTP request with custom headers, an optional request body, and separate connect/read timeouts.</p>
      <span class="field-meta">Returns: <code>HttpResponse</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>requestJson(method, url, jsonBody)</code>
      </div>
      <p>Convenience wrapper around <code>request()</code> that sets JSON content-type/accept headers automatically.</p>
      <span class="field-meta">Returns: <code>HttpResponse</code></span>
    </div>

  </div>
</details>

## 2. HttpResponse

`HttpResponse` is a small record: `statusCode`, `statusMessage`, `headers` (an unmodifiable, insertion ordered map, note that multivalue headers are flattened into a single `,`-joined string per key, not a `List<String>`), and `body`.

<Tabs>
  <TabItem label="Java">

```java
HttpResponse response = HttpUtil.request("GET", url, Map.of(), null, 10_000, 10_000);
String text = response.getBodyAsString();
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val response = HttpUtil.request("GET", url, mapOf(), null, 10_000, 10_000)
val text = response.getBodyAsString()
```

  </TabItem>
</Tabs>

:::note
`body()` returns a defensive **copy** of the underlying byte array on every call (so does `HttpException.getResponseBody()`) - if you need it more than once, store the result in a local variable rather than calling `body()` repeatedly in a hot loop.
:::

## 3. Error handling

`HttpUtil.request(...)` (and everything built on it) throws `HttpException` for any response outside the `2xx` range, it does not return an `HttpResponse` with an error status code for you to check manually. See the <a href="/guides/minified-utils/exceptions/exceptions" class="link">Exceptions guide</a> for the full `HttpException` API, including `toUserFriendlyMessage()` for displaying failures to end users and `isTransientFailure()` for deciding whether a retry makes sense (true for `429`/`502`/`503`/`504`).
