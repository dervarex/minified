---
title: Network
description: Connectivity checks used before Minified attempts anything network dependent
---


## 1. NetworkUtil

`NetworkUtil.ensureOnline(action)` is the connectivity check Minified runs before download heavy or authentication related operations. It runs three independent probes and only reports "offline" if **all three** fail:

1. **DNS**: resolves `example.com`.
2. **TCP**: opens a raw socket to `1.1.1.1:53` with a 1.2 second timeout.
3. **HTTP**: sends a `HEAD` request to `https://api.mojang.com` with a 1.5 second timeout; any response code (even a 4xx) counts as reachable, since it proves the network is working, even if the site rejects the request.

<Tabs>
  <TabItem label="Java">

```java
try {
    NetworkUtil.ensureOnline("download client jar");
    // proceed with the network operation
} catch (NoConnectionException e) {
    System.out.println(e.toUserFriendlyMessage());
}
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
try {
    NetworkUtil.ensureOnline("download client jar")
    // proceed with the network operation
} catch (e: NoConnectionException) {
    println(e.toUserFriendlyMessage())
}
```

  </TabItem>
</Tabs>

The `action` string becomes part of the thrown exception's diagnostic output, so pass something specific ("download client jar", "login") rather than a generic label, since it's the main thing that tells you *what* was being attempted when connectivity failed.

:::note
`ensureOnline(...)` **only throws**, it never posts an event itself. Places in Minified that want to notify listeners (like `Launcher.launchMinecraft(...)`) wrap the call and post <a href="/events/connection/checkconnectionevent" class="link">`CheckConnectionEvent`</a>/<a href="/events/connection/offlineevent" class="link">`OfflineEvent`</a> around it manually. If you call `ensureOnline(...)` directly in your own code, no such events fire automatically.
:::

:::caution[Experimental package]
`com.dervarex.minified.utils.network` is annotated `@API(status = API.Status.EXPERIMENTAL)`. It's exported and works fine usually, but treat the exact behavior (probe targets, timeouts, the specific suggestions returned) as subject to change between releases more commonly than the rest of `minified-utils`.
:::

There's also a lighter-weight static helper for call sites that already have their own connectivity signal and just want the same exception shape:

<Tabs>
  <TabItem label="Java">

```java
NoConnectionException.throwIfOffline("download client jar", () -> myOwnQuickCheck());
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
NoConnectionException.throwIfOffline("download client jar") { myOwnQuickCheck() }
```

  </TabItem>
</Tabs>

`throwIfOffline(action, quickCheck)` runs your `Supplier<Boolean>` (any thrown exception counts as `false`) and, if it returns `false`, throws a `NoConnectionException` with all three probe flags set to `false` and a single synthetic `"quickCheck"` probe entry. It does **not** run the DNS/TCP/HTTP probes itself.

## 2. NoConnectionException

Unlike a typical "no internet" exception, `NoConnectionException` carries full diagnostics: which of the three probes succeeded, their latencies, the OS, and a list of human-readable suggestions.

<details class="fields-details" closed>
  <summary class="fields-summary">
    <span>
      <strong>NoConnectionException Methods</strong>
      <small>13 available methods</small>
    </span>

    <span class="fields-chevron" aria-hidden="true">›</span>
  </summary>

  <div class="fields-grid">

    <div class="field-card">
      <div class="field-header">
        <code>getActionContext()</code>
      </div>
      <p>The <code>action</code> string passed to <code>ensureOnline(...)</code>.</p>
      <span class="field-meta">Returns: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>wasDnsResolved()</code> / <code>wasAnyTcpReachable()</code> / <code>wasAnyHttpReachable()</code>
      </div>
      <p>Individual results of the three connectivity probes.</p>
      <span class="field-meta">Returns: <code>boolean</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getProbeResults()</code>
      </div>
      <p>Raw per-probe results, keyed by probe id (e.g. <code>"dns:example.com"</code>), including latency and a short detail string.</p>
      <span class="field-meta">Returns: <code>Map&lt;String, ProbeResult&gt;</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getSuggestions()</code>
      </div>
      <p>Auto-generated, human-readable troubleshooting suggestions based on which probes failed (e.g. "Check your firewall").</p>
      <span class="field-meta">Returns: <code>List&lt;String&gt;</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getActiveOs()</code>
      </div>
      <p>The value of <code>os.name</code> when the exception was built.</p>
      <span class="field-meta">Returns: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getTimestampInstant()</code> / <code>getTimestampEpochMillis()</code>
      </div>
      <p>When the failure was recorded, as an <code>Instant</code> or raw epoch milliseconds.</p>
      <span class="field-meta">Returns: <code>Instant</code> / <code>long</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getNetworkInterfaceSummary()</code>
      </div>
      <p>A free-form summary of the active network interfaces, if one was attached via the builder's <code>netIfaces(...)</code>. Empty by default, since <code>ensureOnline(...)</code> doesn't populate it.</p>
      <span class="field-meta">Returns: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>toUserFriendlyMessage()</code>
      </div>
      <p>A multi-line, ready-to-display summary: action, OS, probe results and suggestions.</p>
      <span class="field-meta">Returns: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>toJson()</code>
      </div>
      <p>A hand-rolled JSON serialization of the whole diagnostic report, useful for bug reports or telemetry.</p>
      <span class="field-meta">Returns: <code>String</code></span>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>getRootCause()</code>
      </div>
      <p>The underlying <code>Throwable</code> that triggered the failure, if any was attached via the builder.</p>
      <span class="field-meta">Returns: <code>Throwable</code></span>
    </div>

  </div>
</details>

`getSuggestions()` is populated automatically if you don't add your own via the builder: no DNS → "Check DNS and Router"; no TCP → "Check your firewall (and vpn if present)"; TCP reachable but no HTTP → "You might be on a captive portal (hotels often have that)"; plus two generic suggestions ("Disconnect from the wifi and reconnect", "Try again later") that are always appended.

<Tabs>
  <TabItem label="Java">

```java
try {
    NetworkUtil.ensureOnline("Login");
} catch (NoConnectionException e) {
    for (String suggestion : e.getSuggestions()) {
        System.out.println("- " + suggestion);
    }
}
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
try {
    NetworkUtil.ensureOnline("Login")
} catch (e: NoConnectionException) {
    e.suggestions.forEach { println("- $it") }
}
```

  </TabItem>
</Tabs>
