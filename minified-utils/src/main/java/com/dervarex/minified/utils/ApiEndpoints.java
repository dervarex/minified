package com.dervarex.minified.utils;

public final class ApiEndpoints {

    private ApiEndpoints() {
    }

    // Vanilla
    public static final String VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json";
    public static final String RESOURCES_URL = "https://resources.download.minecraft.net/";

    // Fabric
    public static final String FABRIC_LOADER_META_URL = "https://meta.fabricmc.net/v2/versions/loader";

    // Quilt
    public static final String QUILT_LOADER_META_URL = "https://meta.quiltmc.org/v3/versions/loader";

    // Forge
    public static final String FORGE_INSTALLER_BASE_URL = "https://maven.minecraftforge.net/net/minecraftforge/forge/";
    public static final String FORGE_MAVEN_METADATA_URL = "https://maven.minecraftforge.net/net/minecraftforge/forge/maven-metadata.xml";
    public static final String FORGE_PROMOTIONS_URL = "https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json";
    // the libraries FML up to 1.5.2 downloads at startup, Forge doesn't host them anymore. %s is the file name. thanks to prismlauncher here
    public static final String FML_LIBRARIES_MIRROR_URL = "https://files.prismlauncher.org/fmllibs/%s";

    // NeoForge
    public static final String NEOFORGE_INSTALLER_BASE_URL = "https://maven.neoforged.net/releases/net/neoforged/neoforge/";
    public static final String NEOFORGE_MAVEN_METADATA_URL = "https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml";
    // NeoForge for 1.20.1 was published as net.neoforged:forge, with versions like 1.20.1-47.1.106
    public static final String NEOFORGE_LEGACY_INSTALLER_BASE_URL = "https://maven.neoforged.net/releases/net/neoforged/forge/";
    public static final String NEOFORGE_LEGACY_MAVEN_METADATA_URL = "https://maven.neoforged.net/releases/net/neoforged/forge/maven-metadata.xml";
    // fallback when the CDN serves us shit
    public static final String NEOFORGE_VERSIONS_API_URL = "https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoforge";
    public static final String NEOFORGE_LEGACY_VERSIONS_API_URL = "https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/forge";

    // Java
    /**
     * Template for the Adoptium asset endpoint. Contains placeholders resolved in
     * {@code JavaManager.adoptiumAssetUrl}.
     */
    public static final String ADOPTIUM_ASSET_URL_TEMPLATE =
            "https://api.adoptium.net/v3/assets/feature_releases/%d/ga?architecture=%s&heap_size=normal&image_type=%s&jvm_impl=hotspot&os=%s&vendor=eclipse";

    // Profile
    public static final String PROFILE_ENDPOINT = "https://sessionserver.mojang.com/session/minecraft/profile/";
}