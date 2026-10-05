---
title: API Endpoints
description: The upstream API endpoints Minified talks to, exposed as public constants
---

`ApiEndpoints` is a small holder class of `public static final String` constants for every upstream URL Minified's launch/java logic depends on. They're exposed publicly so you can reuse them if you need to talk to the same APIs directly (for a diagnostics page, a custom loader implementation, etc.) without hardcoding URLs that might change.

<details class="fields-details" open>
  <summary class="fields-summary">
    <span>
      <strong>ApiEndpoints Constants</strong>
      <small>10 available constants</small>
    </span>

    <span class="fields-chevron" aria-hidden="true">›</span>
  </summary>

  <div class="fields-grid">

    <div class="field-card">
      <div class="field-header">
        <code>VERSION_MANIFEST_URL</code>
      </div>
      <p>Mojang's version manifest, used by <code>VersionManifestClient</code>.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>RESOURCES_URL</code>
      </div>
      <p>Base URL for downloading Minecraft assets (sound, language files, textures referenced by hash) via <code>AssetDownloader</code>.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>FABRIC_LOADER_META_URL</code>
      </div>
      <p>Fabric's loader metadata endpoint, used to resolve available Fabric loader versions.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>QUILT_LOADER_META_URL</code>
      </div>
      <p>Quilt's loader metadata endpoint, mirroring Fabric's.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>FORGE_INSTALLER_BASE_URL</code> / <code>FORGE_MAVEN_METADATA_URL</code> / <code>FORGE_PROMOTIONS_URL</code>
      </div>
      <p>Forge's Maven repository and promotions feed, used to resolve and download the Forge installer for a given Minecraft version.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>NEOFORGE_INSTALLER_BASE_URL</code> / <code>NEOFORGE_MAVEN_METADATA_URL</code>
      </div>
      <p>NeoForge's Maven repository, mirroring Forge's.</p>
    </div>

    <div class="field-card">
      <div class="field-header">
        <code>ADOPTIUM_ASSET_URL_TEMPLATE</code>
      </div>
      <p>A <code>String.format</code> template for Eclipse Adoptium's release-asset API, used by <code>JavaManager</code> to find a downloadable JDK/JRE for a required major version, architecture, image type and OS.</p>
    </div>

  </div>
</details>

:::note
These are Mojang/Fabric/Quilt/Forge/NeoForge/Adoptium's own public endpoints, not anything hosted by Minified. Availability and response shape are outside Minified's control - if one of these services changes its API, the corresponding Minified feature (version listing, Fabric/Quilt/Forge/NeoForge installs, managed Java runtimes) may need a library update to match.
:::
