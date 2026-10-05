---
title: Exceptions
description: The structured exception types shared across Minified's modules.
---


Most of Minified's exceptions aren't plain `new Exception("message")`, they're built with a `Builder` and carry structured, machine-readable diagnostic data alongside a human-readable message. This page covers the general-purpose ones exported by `minified-utils`; module-specific exceptions (Forge/NeoForge installer failures, profile loading errors, etc.) live in `minified-launch` and aren't covered here.

## 1. HttpException

Thrown by <a href="/guides/minified-utils/http/http" class="link">`HttpUtil`</a> for any HTTP response outside the `2xx` range. Carries the status code, method, URL, response headers, and a snippet of the response body, enough to debug an API failure without re-running the request with a debugger attached.

<details class="fields-details" closed>
  <summary class="fields-summary">
    <span>
      <strong>HttpException Methods</strong>
      <small>13 available methods</small>
    </span>

    <span class="fields-chevron" aria-hidden="true">›</span>
  </summary>

  <div class="fields-grid">

    <div class="field-card">
      <div class="field-header">
        <code>getStatusCode()</code>
      </div>
      <p>The HTTP status code returned by the server.</p>
      <span class="field-meta">Returns: <code>int</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getStatusMessage()</code>
      </div>
      <p>The HTTP reason phrase, if any (e.g. <code>"Not Found"</code>).</p>
      <span class="field-meta">Returns: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getMethod()</code>
      </div>
      <p>The HTTP method that was used.</p>
      <span class="field-meta">Returns: <code>HttpException.Method</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getUrl()</code>
      </div>
      <p>The URL that was requested.</p>
      <span class="field-meta">Returns: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getResponseHeaders()</code>
      </div>
      <p>An unmodifiable, case-preserved map of the response headers.</p>
      <span class="field-meta">Returns: <code>Map&lt;String, String&gt;</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getResponseBody()</code>
      </div>
      <p>A defensive copy of the raw response body bytes.</p>
      <span class="field-meta">Returns: <code>byte[]</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>responseSnippet(maxChars)</code>
      </div>
      <p>The response body decoded as UTF-8 and truncated to <code>maxChars</code> (with a trailing <code>…</code> if cut off), safe to log without risking megabytes of HTML in your console.</p>
      <span class="field-meta">Returns: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>isTransientFailure()</code>
      </div>
      <p>Whether the status code suggests retrying later might succeed. <code>true</code> for <code>429</code>, <code>502</code>, <code>503</code> and <code>504</code>.</p>
      <span class="field-meta">Returns: <code>boolean</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getRequestId()</code>
      </div>
      <p>The value of the response's <code>X-Request-Id</code> header, if present pass this along when reporting API issues to the server operator.</p>
      <span class="field-meta">Returns: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getTimestampInstant()</code> / <code>getTimestampEpochMillis()</code>
      </div>
      <p>When the exception was built, as an <code>Instant</code> or raw epoch milliseconds.</p>
      <span class="field-meta">Returns: <code>Instant</code> / <code>long</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>toUserFriendlyMessage()</code> / <code>toJson()</code>
      </div>
      <p>A ready-to-display multi-line summary, or a hand-rolled JSON serialization of the same data.</p>
      <span class="field-meta">Returns: <code>String</code></span>
    </div>

  </div>
</details>

<Tabs>
  <TabItem label="Java">

```java
try {
    HttpUtil.get("https://example.com/missing");
} catch (HttpException e) {
    if (e.isTransientFailure()) {
        // safe to retry after a short delay
    }
    System.out.println(e.toUserFriendlyMessage());
}
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
try {
    HttpUtil.get("https://example.com/missing")
} catch (e: HttpException) {
    if (e.isTransientFailure) {
        // safe to retry after a short delay
    }
    println(e.toUserFriendlyMessage())
}
```

  </TabItem>
</Tabs>

## 2. NoConnectionException

Thrown by `NetworkUtil.ensureOnline(...)` when none of its DNS/TCP/HTTP connectivity probes succeed. It's covered in full on the <a href="/guides/minified-utils/network/network" class="link">Network guide</a>, since it's tightly coupled to how `NetworkUtil` works.

## 3. OfflineModeNeedsNetworkException

A `RuntimeException` thrown when a launch was started in offline mode (`user == null` or connectivity failed) but a required file simply isn't cached locally - most commonly, no cached client JAR exists for `Launcher.launchMinecraft(...)` to fall back to.

```java
if (!Files.exists(launchConfig.getJarFile())) {
    throw new OfflineModeNeedsNetworkException(
            "Missing cached client jar: " + launchConfig.getJarFile()
    );
}
```

:::note
This exception type is marked `@API(status = EXPERIMENTAL)` with a Javadoc note that it "will get expanded with Minified v3.0" (not directly the 3.0.0 release, instead a subrelease), expect it to gain structured fields (similar to `HttpException`/`NoConnectionException`) rather than just a plain message in a future release.
:::

## 4. StopException

A general-purpose, structured "controlled abort" signal for your **own** code, not currently thrown anywhere inside Minified itself. It's exported as a building block for launchers that want a consistent way to represent "the operation was intentionally stopped" (user cancellation, a failed precondition, a safety abort) as opposed to an unexpected bug.

It carries a `reason`, a machine-readable `code`, a `Severity` (`INFO`/`WARNING`/`ERROR`/`FATAL`), whether the operation is `recoverable`, and an arbitrary string-keyed `metadata` map plus `toUserFriendlyMessage()`/`toJson()` in the same style as `HttpException`. Three factory methods cover the common cases:

<Tabs>
  <TabItem label="Java">

```java
throw StopException.userCancel("User closed the download dialog");
// or: StopException.preconditionFailed("No internet connection")
// or: StopException.fatal("Corrupted install directory")
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
throw StopException.userCancel("User closed the download dialog")
// or: StopException.preconditionFailed("No internet connection")
// or: StopException.fatal("Corrupted install directory")
```

  </TabItem>
</Tabs>

For anything else, use `new StopException.Builder()` directly to set a custom `code`/`severity`/`metadata`.

## 5. JsonParseException

An unchecked exception thrown by `JsonParser`/`JsonFile` when a document isn't valid JSON. It carries `getPosition()`, the character index in the input where parsing failed, combine it with the original string to point a user (or yourself) at exactly what's wrong, rather than just "invalid JSON somewhere in this file". See the <a href="/guides/minified-utils/json/json" class="link">JSON guide</a> for the parser itself.
