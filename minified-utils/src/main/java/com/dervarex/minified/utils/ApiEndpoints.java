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

    // NeoForge
    public static final String NEOFORGE_INSTALLER_BASE_URL = "https://maven.neoforged.net/releases/net/neoforged/neoforge/";
    public static final String NEOFORGE_MAVEN_METADATA_URL = "https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml";

    // Java
    /**
     * Template for the Adoptium asset endpoint. Contains placeholders resolved in
     * {@code JavaManager.adoptiumAssetUrl}.
     */
    public static final String ADOPTIUM_ASSET_URL_TEMPLATE =
            "https://api.adoptium.net/v3/assets/feature_releases/%d/ga?architecture=%s&heap_size=normal&image_type=%s&jvm_impl=hotspot&os=%s&vendor=eclipse";
}