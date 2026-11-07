package com.dervarex.minified.utils;

import org.apiguardian.api.API;

/**
 * Contains API Endpoints used across minified.
 */
@API(status = API.Status.STABLE)
public final class ApiEndpoints {

    private ApiEndpoints() {
    }

    // Vanilla
    /**
     * The Vanilla Version Manifest. <br>
     * Every version of Minecraft since rd-132211 is listed here.
     * <p>
     * Example of how one entry looks like in the version manifest:
     * <pre>{@code
     * {
     *   "id": "26.4-snapshot-7",
     *   "type": "snapshot",
     *   "url": "https://piston-meta.mojang.com/v1/packages/6be84ec668c4c843c87f80f2a8efa3095f2989d2/26.4-snapshot-7.json",
     *   "time": "2026-11-03T13:18:26+00:00",
     *   "releaseTime": "2026-11-03T13:08:56+00:00",
     *   "sha1": "6be84ec668c4c843c87f80f2a8efa3095f2989d2",
     *   "complianceLevel": 1
     * }
     * }</pre>
     *
     * As you can see, the <code>url</code> field contains another URL.<br>
     * This is the "version json", it contains the <code>arguments</code> required for launching the game, <br>
     * the <code>assetIndex</code> which contains an url to the assets' metadata, <br>
     * the <code>downloads</code> for client and server JAR downloads, the list of <code>libraries</code> needed, <br>
     * which version of java it wants and a few more essentials like the <code>mainClass</code>.
     */
    public static final String VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json";
    /**
     * This is the resources base url.
     * <p>
     * The hashes come from the {@code assetIndex} in the version JSON, the needed files can be
     * fetched by appending the subdirectory (first two characters of the hash), "/" and the hash
     * of the asset.
     * <p>
     * Example:
     * <a href="https://resources.download.minecraft.net/10/1058cf72714c48b154e7883cb9d7ec918e0816b3">
     * https://resources.download.minecraft.net/10/1058cf72714c48b154e7883cb9d7ec918e0816b3 - cave1.ogg</a>
     * <p>
     * There you'll get a file named after the hash, not after what it actually is. You need the
     * assets' metadata again, since this is the only source where the real name is linked to the
     * hash, in this case, {@code minecraft/sounds/ambient/cave/cave1.ogg} to
     * {@code 1058cf72714c48b154e7883cb9d7ec918e0816b3}
     */
    public static final String RESOURCES_URL = "https://resources.download.minecraft.net/";

    // Fabric
    /**
     * The Fabric Loader index.
     * <p>
     * Every Fabric Loader Version is listed here.
     * <p>
     * Example:
     * <pre>{@code
     *   {
     *     "separator": ".",
     *     "build": 1,
     *     "maven": "net.fabricmc:fabric-loader:0.20.1",
     *     "version": "0.20.1",
     *     "stable": true
     *   }
     * }</pre>
     */
    public static final String FABRIC_LOADER_META_URL = "https://meta.fabricmc.net/v2/versions/loader";

    // Quilt
    /**
     * Mostly the same as the {@link FABRIC_LOADER_META_URL}, but for Quilt with file sizes and hashes.
     * <pre>{@code
     *   {
     *     "maven": "org.quiltmc:quilt-loader:0.31.0-beta.6",
     *     "version": "0.31.0-beta.6",
     *     "build": 6,
     *     "separator": ".",
     *     "file_size": 3221767,
     *     "hashes": {
     *       "sha1": "d6f287f4620e9973c3ca7694a180586bb79092e7",
     *       "sha256": "f14bf8d5ec41be892f161526d2bc509ef3f7eea4184ce02d071b818fe56ab5ee",
     *       "sha512": "5cbbe32437ad5ccd45f550f84dac06be9673b39a5579f840229b9e506548247ef6d22b58c3206217f9a20a2b1f30c75e16b7b82a572c86ed1073852126c43dbf"
     *     }
     *   }
     * }</pre>
     */
    public static final String QUILT_LOADER_META_URL = "https://meta.quiltmc.org/v3/versions/loader";

    // Forge
    /**
     * File Host for all Forge Loaders (not directly browsable). <br>
     * One subdirectory for every Loader Version (e.g. {@code 1.20.1-47.3.39}). <br>
     * Every of those Files has Maven checksums: md5, sha1, sha256 and sha512.
     * <h4>Changelog</h4>
     * <ul>
     *   <li>{@code forge-{version}-changelog.txt} Changelog for the Loader Version</li>
     * </ul>
     *
     * <h4>Installer</h4>
     * <ul>
     *   <li>{@code forge-{version}-installer.jar} the Forge installer jar</li>
     * </ul>
     *
     * <h4>POM (Maven)</h4>
     * <ul>
     *   <li>{@code forge-{version}.pom} Maven POM file</li>
     * </ul>
     *
     * <h4>Sources</h4>
     * <ul>
     *   <li>{@code forge-{version}-sources.jar} Forge sources jar</li>
     * </ul>
     *
     * <h4>Universal</h4>
     * <ul>
     *   <li>{@code forge-{version}-universal.jar} universal jar for Forge</li>
     * </ul>
     *
     * <h4>UserDev (Development)</h4>
     * <ul>
     *   <li>{@code forge-{version}-userdev.jar} dev kit for ForgeGradle, containing Access Transformers, patches, Forge Sided Annotation Stripper (SAS), and joined Minecraft sources</li>
     * </ul>
     *
     * <h4>MDK (Mod Development Kit)</h4>
     * <ul>
     *   <li>{@code forge-{version}-mdk.zip} example mod workspace</li>
     * </ul>
     */
    public static final String FORGE_INSTALLER_BASE_URL = "https://maven.minecraftforge.net/net/minecraftforge/forge/";
    /**
     * Forge Maven Metadata. <br>
     * Contains all Forge Loader Versions, just like the Fabric Loader Index does, but here, it's only the versions, no download links or hashes, since you'll get those from the {@link FORGE_INSTALLER_BASE_URL}.
     */
    public static final String FORGE_MAVEN_METADATA_URL = "https://maven.minecraftforge.net/net/minecraftforge/forge/maven-metadata.xml";
    /**
     * Forge Promotions, for knowing which Loader is the Latest and which one the Recommended for a Minecraft Version. <br>
     * Also contains a link to the Forge Homepage.
     */
    public static final String FORGE_PROMOTIONS_URL = "https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json";
    //
    /**
     * The libraries FML up to 1.5.2 downloads at startup, %s is the file name. <br>
     * Since Forge doesn't host them anymore, we'll get those from PrismLauncher, thanks for hosting it!
     */
    public static final String FML_LIBRARIES_MIRROR_URL = "https://files.prismlauncher.org/fmllibs/%s";

    // NeoForge
    /**
     * File Host for all Neoforge Loaders.
     * One subdirectory is one Loader Version (beta versions get their own ones too). <br>
     * Every of those Files has hashes: md5, sha1, sha256 and sha512.
     * <h4>Changelog</h4>
     * <ul>
     *   <li>{@code neoforge-20.4.167-changelog.txt} Changelog for the Loader Version</li>
     *   <li>{@code ...changelog.txt.asc} PGP Signature for the Changelog </li>
     * </ul>
     *
     * <h4>Installer</h4>
     * <ul>
     *   <li>{@code neoforge-20.4.167-installer.jar} the neoforge installer jar </li>
     *   <li>{@code ...installer.jar.asc} PGP Signature for the Installer </li>
     * </ul>
     *
     * <h4>POM (Maven)</h4>
     * <ul>
     *   <li>{@code neoforge-20.4.167.pom} maven pom file </li>
     *   <li>{@code ...pom.asc} PGP Signature for the pom file </li>
     * </ul>
     *
     * <h4>Sources</h4>
     * <ul>
     *   <li>{@code neoforge-20.4.167-sources.jar} neoforge sources jar </li>
     *   <li>{@code ...sources.jar.asc} PGP Signature for the sources jar </li>
     * </ul>
     *
     * <h4>Universal</h4>
     * <ul>
     *   <li>{@code neoforge-20.4.167-universal.jar} universal installer for neoforge </li>
     *   <li>{@code ...universal.jar.asc} PGP Signature for the universal installer </li>
     * </ul>
     *
     * <h4>UserDev</h4>
     * <ul>
     *   <li>{@code neoforge-20.4.167-userdev.jar} dev kit containing access transformers, patches, and joined Minecraft sources for modding</li>
     *   <li>{@code ...userdev.jar.asc} PGP Signature for the dev kit </li>
     * </ul>
     *<p>
     *
     * <b>Dev Kit Explanation:</b>
     * <p>Inside the {@code userdev.jar} it looks about like this:
     * <ul>
     *     <li>{@code ats} - Access Transformers. These files change the visibility (access) of
     *     Minecraft classes and methods so mods can access them</li>
     *
     *     <li>{@code META-INF} - Manifest data, usually not a lot</li>
     *
     *     <li>{@code patches} - The NeoForge source patches. These are human-readable unified diffs that modify
     *     vanilla Minecraft classes. They add Neoforge features, hooks and APIs.</li>
     *
     *     <li>{@code config.json} - Configuration file for the dev environment setup</li>
     *
     *     <li>{@code joined.lzma} - An LZMA compressed file. It contains the binary patches (.binpatch). The Gradle plugin
     *     decompresses this automatically in the background during project sync/import, please don't try to do it yourself ;)</li>
     * </ul>
     * <br>
     * Why <code>.lzma</code> and not <code>.zip</code>? <br>
     * LZMA compresses a lot better than .zip, especially in already compressed archives (.jar is an archive already!)
     * <br>
     */
    public static final String NEOFORGE_INSTALLER_BASE_URL = "https://maven.neoforged.net/releases/net/neoforged/neoforge/";
    /**
     * Neoforge Maven Metadata. <br>
     * Contains all Neoforge Loader Versions, just like the Fabric Loader Index does, but here, it's only the versions, no download links or hashes, since you'll get those from the {@link NEOFORGE_INSTALLER_BASE_URL}.
     */
    public static final String NEOFORGE_MAVEN_METADATA_URL = "https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml";
    /**
     * Legacy Version of the {@link NEOFORGE_INSTALLER_BASE_URL}. <br>
     * NeoForge for 1.20.1 was published as net.neoforged:forge, with versions like 1.20.1-47.1.106, which later changed, so this is only used for 1.20.1
     */
    public static final String NEOFORGE_LEGACY_INSTALLER_BASE_URL = "https://maven.neoforged.net/releases/net/neoforged/forge/";
    /**
     * Legacy Version of the {@link NEOFORGE_MAVEN_METADATA_URL}. <br>
     * NeoForge for 1.20.1 was published as net.neoforged:forge, with versions like 1.20.1-47.1.106, which later changed, so this is only used for 1.20.1
     */
    public static final String NEOFORGE_LEGACY_MAVEN_METADATA_URL = "https://maven.neoforged.net/releases/net/neoforged/forge/maven-metadata.xml";
    /**
     * Fallback for fetching Neoforge Versions when the CDN serves us shit
     */
    public static final String NEOFORGE_VERSIONS_API_URL = "https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoforge";
    /**
     * Legacy Version of {@link NEOFORGE_VERSIONS_API_URL}. <br>
     * NeoForge for 1.20.1 was published as net.neoforged:forge, with versions like 1.20.1-47.1.106, which later changed, so this is only used for 1.20.1
     */
    public static final String NEOFORGE_LEGACY_VERSIONS_API_URL = "https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/forge";
    //
    /**
     * The folders that exist on the maven, for when both of the above only list the newest versions or do some other weird stuff
     */
    public static final String NEOFORGE_DIRECTORY_API_URL = "https://maven.neoforged.net/api/maven/details/releases/net/neoforged/neoforge";
    /**
     * Legacy Version of {@link NEOFORGE_DIRECTORY_API_URL}
     * NeoForge for 1.20.1 was published as net.neoforged:forge, with versions like 1.20.1-47.1.106, which later changed, so this is only used for 1.20.1
     */
    public static final String NEOFORGE_LEGACY_DIRECTORY_API_URL = "https://maven.neoforged.net/api/maven/details/releases/net/neoforged/forge";

    // Java
    /**
     * Template for the Adoptium asset endpoint. Contains placeholders resolved in
     * {@code JavaManager.adoptiumAssetUrl}.
     */
    public static final String ADOPTIUM_ASSET_URL_TEMPLATE =
            "https://api.adoptium.net/v3/assets/feature_releases/%d/ga?architecture=%s&heap_size=normal&image_type=%s&jvm_impl=hotspot&os=%s&vendor=eclipse";

    // Profile
    /**
     * The Base URL for fetching Minecraft Accounts. <br>
     * <p>
     * Append "/" and the trimmed (without dashes) UUID of the minecraft player, and you'll get something similar to this:
     * <pre>{@code
     * {
     *   "id" : "a31ccf3000e44928a590e366c90af710",
     *   "name" : "dervarex",
     *   "properties" : [ {
     *     "name" : "textures",
     *     "value" : "ewogICJ0aW1lc3RhbXAiIDogMTc5Mzk2MTQyNjk1NiwKICAicHJvZmlsZUlkIiA6ICJhMzFjY2YzMDAwZTQ0OTI4YTU5MGUzNjZjOTBhZjcxMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJkZXJ2YXJleCIsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jYWQ1MjZhMzlmMTMyNzcyZThhMzgxODRkNmViMDZiYmEzN2Q1ZGUyODk1MDY0MzI3ZDRjMWE4ZjdjOGNkZjQ1IgogICAgfQogIH0KfQ=="
     *   } ],
     *   "profileActions" : [ ]
     * }
     * }</pre>
     * <p>
     * The interesting part here is <code>value</code>, it's base64 encoded and when decoded looks like this:
     * <pre>{@code
     * {
     *   "timestamp" : 1793961426956,
     *   "profileId" : "a31ccf3000e44928a590e366c90af710",
     *   "profileName" : "dervarex",
     *   "textures" : {
     *     "SKIN" : {
     *       "url" : "http://textures.minecraft.net/texture/cad526a39f132772e8a38184d6eb06bba37d5de2895064327d4c1a8f7c8cdf45"
     *     }
     *   }
     * }
     * }</pre>
     * <p>
     * <code>textures.SKIN.url</code> is the direct URL to the current skin
     */
    public static final String PROFILE_ENDPOINT = "https://sessionserver.mojang.com/session/minecraft/profile/";
}