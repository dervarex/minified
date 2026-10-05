---
title: JSON
description: Minifieds dependency free JSON parser and object model
---


## 1. Why Minified has its own JSON library

Version manifests, profile JSONs, Modrinth API responses and more are all basic JSON. Rather than pulling in Gson or Jackson as a huge dependency for consumers of `minified-utils` alone, Minified has its very own dependency free JSON parser and object model under `com.dervarex.minified.utils.json`. It's what `VersionManifestClient`, `AssetDownloader`, `LibraryDownloader` and others use, and it's fully public, so you can use it directly for your own launcher's config files too.

It is **not** a replacement for a full-featured library: no annotations, no reflection-based (de)serialization, no streaming parser. It reads a whole document into memory as a tree of `JsonValue` nodes and writes it back out as a compact string (no pretty-printing), but it's more than enough for minified's use cases.

## 2. Parsing JSON

<Tabs>
  <TabItem label="Java">

```java
JsonValue root = JsonParser.parse(jsonString);
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val root = JsonParser.parse(jsonString)
```

  </TabItem>
</Tabs>

For files on disk, `JsonFile` is usually easier, it reads and parses in one step:

<Tabs>
  <TabItem label="Java">

```java
JsonFile config = new JsonFile(Path.of("config.json"));
String name = config.getString("launcherName");
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val config = JsonFile(Path.of("config.json"))
val name = config.getString("launcherName")
```

  </TabItem>
</Tabs>

If parsing fails, `JsonParser.parse(...)` (and by extension `JsonFile`'s constructors) throw `JsonParseException`, an unchecked exception carrying the character `position` where parsing broke, this is useful for pointing at the exact offset in a malformed file rather than just "invalid JSON".

## 3. The JsonValue hierarchy

Everything parsed or built with this library implements `JsonValue`. There is one implementation per JSON type:

<details class="fields-details" open>
  <summary class="fields-summary">
    <span>
      <strong>JsonValue implementations</strong>
      <small>6 types</small>
    </span>

    <span class="fields-chevron" aria-hidden="true">›</span>
  </summary>

  <div class="fields-grid">

    <div class="field-card">
      <div class="field-header">
        <code>JsonObject</code>
        <span class="type-badge">OBJECT</span>
      </div>
      <p>An insertion-ordered string keyed map. Mutable, you can <code>put()</code>/<code>remove()</code>/<code>clear()</code> after parsing.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>JsonArray</code>
        <span class="type-badge">ARRAY</span>
      </div>
      <p>An ordered list of values, iterable directly with a for-each loop. Mutable via <code>add()</code>/<code>remove()</code>/<code>clear()</code>.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>JsonString</code>
        <span class="type-badge">STRING</span>
      </div>
      <p>Wraps a <code>String</code>. A <code>null</code> passed to the constructor is normalized to <code>""</code>, never a real null.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>JsonNumber</code>
        <span class="type-badge">NUMBER</span>
      </div>
      <p>Backed by <code>BigDecimal</code> to prevent precision loss for large IDs or exact decimals. <code>asInt()</code> truncates via <code>BigDecimal.intValue()</code>.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>JsonBoolean</code>
        <span class="type-badge">BOOLEAN</span>
      </div>
      <p>Wraps a primitive <code>boolean</code>.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>JsonNull</code>
        <span class="type-badge">NULL</span>
      </div>
      <p>A singleton (<code>JsonNull.INSTANCE</code>) representing JSON <code>null</code> - distinct from a missing key, which parses to a real Java <code>null</code> reference instead.</p>
    </div>

  </div>
</details>

Every `JsonValue` exposes `getType()`, the `isObject()`/`isArray()`/`isString()`/`isNumber()`/`isBoolean()`/`isNull()` predicates, and the matching `asObject()`/`asArray()`/`asString()`/`asNumber()`/`asInt()`/`asBoolean()` casts.

:::caution[The `asX()` casts throw, they don't return null]
Calling e.g. `.asString()` on a value that isn't a `JsonString` throws `IllegalStateException("Not a JSON string")`, it does not return `null` or attempt a conversion. Always check `isX()` first, or prefer the null-safe convenience getters on `JsonObject`/`JsonArray`/`JsonFile` described below when a key might be absent or of an unexpected type.
:::

## 4. Reading values safely

`JsonObject`, `JsonArray` and `JsonFile` all provide typed convenience getters (`getString`, `getNumber`, `getBoolean`, `getInt`, `getLong`, `getDouble`, `getObject`, `getArray`) that return `null` instead of throwing when the key is missing **or** its value is JSON `null`, but still throw `IllegalStateException` if the key exists with a genuinely different type (a string where a number was expected, for example).

<Tabs>
  <TabItem label="Java">

```java
JsonObject profile = versionJson.getObject("javaVersion");
Integer majorVersion = profile != null ? profile.getInt("majorVersion") : null;
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val profile = versionJson.getObject("javaVersion")
val majorVersion = profile?.getInt("majorVersion")
```

  </TabItem>
</Tabs>

`JsonArray` also implements `Iterable<JsonValue>`, so you can iterate it directly with a for-each loop (this is exactly how `VersionListProvider` walks the version manifest, see the <a href="/guides/minified-launch/versions/versions" class="link">Versions guide</a>):

<Tabs>
  <TabItem label="Java">

```java
for (JsonValue entry : someArray) {
    System.out.println(entry.asObject().get("id").asString());
}
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
for (entry in someArray) {
    println(entry.asObject().get("id").asString())
}
```

  </TabItem>
</Tabs>

## 5. Building and writing JSON

`JsonObject`/`JsonArray` can be constructed and populated directly, `put`/`add` overloads exist for `String`, `Number`, `boolean` and nested `JsonValue`'s, so you rarely need to manually wrap values in `JsonString`/`JsonNumber`/`JsonBoolean` yourself:

<Tabs>
  <TabItem label="Java">

```java
JsonObject session = new JsonObject();
session.put("username", "Player");
session.put("expiresAt", System.currentTimeMillis());
session.put("valid", true);

String json = session.toJson();
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val session = JsonObject()
session.put("username", "Player")
session.put("expiresAt", System.currentTimeMillis())
session.put("valid", true)

val json = session.toJson()
```

  </TabItem>
</Tabs>

`toJson()` is available on every `JsonValue` and produces compact, escaped JSON (string escaping (quotes, backslashes, control characters, `\uXXXX` for anything below `0x20`) is handled automatically). To persist a whole document to disk, build (or load) it as a `JsonFile` and call `save()`:

<Tabs>
  <TabItem label="Java">

```java
JsonFile file = new JsonFile(); // empty JsonObject root, no path yet
file.asObject().put("launcherName", "MyLauncher");
file.save(Path.of("config.json"));
```

  </TabItem>

  <TabItem label="Kotlin">

```kotlin
val file = JsonFile() // empty JsonObject root, no path yet
file.asObject().put("launcherName", "MyLauncher")
file.save(Path.of("config.json"))
```

  </TabItem>
</Tabs>

:::note
`JsonFile.save()` (no args) requires the `JsonFile` to have been constructed from a `Path` in the first place - it throws `IllegalStateException` otherwise. Use `save(Path)` for a `JsonFile` you built from scratch or loaded from a raw string, see above for an example.
:::
