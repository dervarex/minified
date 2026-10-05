---
title: Downloads & Hashing
description: Checksum-verified file downloads and hashing utilities shared across Minified's modules.
---


## 1. DownloadHelper

`DownloadHelper` downloads a single file and verifies it against an expected SHA-1 checksum, this is the primitive that `AssetDownloader`, `LibraryDownloader`, `ClientDownloader` and the Forge/NeoForge installers are all built on.

<Tabs>
  <TabItem label="Java">

```java
boolean ok = DownloadHelper.download(url, targetPath, expectedSha1);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val ok = DownloadHelper.download(url, targetPath, expectedSha1)
```

  </TabItem>
</Tabs>

If `targetPath` already exists **and** its SHA-1 matches `expectedSha1`, `download(...)` skips the network call entirely and returns `true` immediately, this is why re-launching with the same assets/libraries directory is fast on subsequent runs. If the file exists with a *different* hash, it's deleted and re-downloaded. If the download completes but the resulting checksum still doesn't match, the partial file is discarded and `download(...)` returns `false` rather than throwing.

For progress reporting, pass a `LongConsumer` that receives the number of bytes read per chunk (not a running total or percentage, you accumulate that yourself, as `AssetDownloader`/`LibraryDownloader` do internally):

<Tabs>
  <TabItem label="Java">

```java
AtomicLong downloaded = new AtomicLong();
DownloadHelper.download(url, targetPath, expectedSha1, httpClient, bytes -> {
    long total = downloaded.addAndGet(bytes);
    System.out.println("Downloaded " + total + " bytes so far");
});
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val downloaded = AtomicLong()
DownloadHelper.download(url, targetPath, expectedSha1, httpClient) { bytes ->
    val total = downloaded.addAndGet(bytes)
    println("Downloaded $total bytes so far")
}
```

  </TabItem>
</Tabs>

There's also an async overload that submits the download to an `ExecutorService` and returns a `Future<?>`, useful if you're building your own parallel downloader the way `AssetDownloader` does.

:::caution[Experimental API]
`DownloadHelper` and `DownloadProgress` are marked `@API(status = EXPERIMENTAL)` and their Javadoc notes they were "recently moved over from the Launch module" - the shape of this API may still change in a future Minified release.
:::

`prepareClientRequest(manifestUrl, type)` builds an `HttpRequest` for the `client`/`server` download entry of a version manifest JSON (fetching and parsing the manifest itself), a low level building block exposed alongside `download(...)` in case you want to drive the actual HTTP send yourself.

## 2. DownloadProgress

A tiny helper for turning a running byte count into a `0.0`–`1.0` progress fraction. It's not used internally as of this version (the download classes track progress with a plain `AtomicLong` themselves), but it's exported as a convenience if you want the same pattern in your own code:

<Tabs>
  <TabItem label="Java">

```java
DownloadProgress progress = new DownloadProgress(totalBytes, value -> {
    System.out.printf("%.1f%%%n", value * 100);
});

progress.addBytes(chunkSize); // call once per chunk read
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val progress = DownloadProgress(totalBytes) { value ->
    println("%.1f%%".format(value * 100))
}

progress.addBytes(chunkSize) // call once per chunk read
```

  </TabItem>
</Tabs>

## 3. Hasher

Computes SHA-1 checksums for files used everywhere Minified needs to verify a download or detect that a cached file is stale.

<Tabs>
  <TabItem label="Java">

```java
String sha1 = Hasher.sha1(path);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val sha1 = Hasher.sha1(path)
```

  </TabItem>
</Tabs>

`bytesToHex(byte[])` is the shared hex-encoding helper used by both `Hasher` and `JavaManager`'s SHA-256 verification for downloaded Java runtimes, handy any time you compute your own `MessageDigest` and need a lowercase hex string out of it.
