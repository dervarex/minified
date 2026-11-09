package com.dervarex.minified.launch.profile;

import com.dervarex.minified.launch.exceptions.loader.UnexpectedLoaderException;
import com.dervarex.minified.launch.exceptions.profile.FailedToLoadProfileException;
import com.dervarex.minified.launch.exceptions.profile.FailedToSaveProfileException;
import com.dervarex.minified.launch.exceptions.profile.UnsupportedProfileFormatException;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.Launcher;
import com.dervarex.minified.launch.launch.modding.Loader;
import com.dervarex.minified.launch.launch.modding.custom.CustomLoader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoader;
import com.dervarex.minified.launch.launch.modding.forge.ForgeLoader;
import com.dervarex.minified.launch.launch.modding.neoforge.NeoforgeLoader;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoader;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;
import com.dervarex.minified.utils.json.JsonFile;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProfileFactoryTest {
    @TempDir
    Path tempDir;

    @Test
    void canSaveAndLoadConfiguration() {
        LaunchConfiguration[] pair = createConfigurationPair();

        LaunchConfiguration baseConfig = pair[0];
        LaunchConfiguration config = pair[1];

        assertEquals(baseConfig.getMinRam(), config.getMinRam());
        assertEquals(baseConfig.getMaxRam(), config.getMaxRam());
        assertEquals(baseConfig.getDownloadThreads(), config.getDownloadThreads());
        assertEquals(baseConfig.getResolutionWidth(), config.getResolutionWidth());
        assertEquals(baseConfig.getResolutionHeight(), config.getResolutionHeight());
        assertEquals(baseConfig.isCustomResolution(), config.isCustomResolution());
        assertEquals(baseConfig.getLauncherName(), config.getLauncherName());
        assertEquals(baseConfig.getLauncherVersion(), config.getLauncherVersion());
        assertEquals(baseConfig.isDemoUser(), config.isDemoUser());
        assertEquals(baseConfig.getExtraJvmArgs(), config.getExtraJvmArgs());
        assertEquals(baseConfig.getJarFile(), config.getJarFile());
        assertEquals(baseConfig.getAssetsDirectory(), config.getAssetsDirectory());
        assertEquals(baseConfig.getLibrariesDirectory(), config.getLibrariesDirectory());
        assertEquals(baseConfig.getNativesDirectory(), config.getNativesDirectory());
        assertEquals(baseConfig.getCustomJavaExecutable(), config.getCustomJavaExecutable());
        assertEquals(baseConfig.getOfflineUsername(), config.getOfflineUsername());
        assertEquals(baseConfig.isFullscreen(), config.isFullscreen());
        assertEquals(baseConfig.getExtraGameArgs(), config.getExtraGameArgs());
        assertEquals(baseConfig.getEnvironmentVariables(), config.getEnvironmentVariables());
        assertEquals(baseConfig.getServerAddress(), config.getServerAddress());

    /*
      Don't be confused: assertEquals() uses .equals(), while assertNotSame() checks reference equality.
      We want the loader to be a different instance while still being equal.
     */
        assertEquals(baseConfig.getLoader(), config.getLoader());
        assertNotSame(baseConfig.getLoader(), config.getLoader());
    }

    @ParameterizedTest
    @MethodSource("loaders")
    void keepsEveryLoaderType(Loader loader) {
        Path profile = tempDir.resolve("profile.json");
        ProfileFactory.save(minimalConfiguration(loader), profile);

        assertEquals(loader, ProfileFactory.load(profile).getLoader());
    }

    @Test
    void unknownLoaderTypesAreNotWrapped() throws IOException {
        Path profile = tempDir.resolve("profile.json");
        ProfileFactory.save(minimalConfiguration(new VanillaLoader("1.21.11")), profile);
        Files.writeString(profile, Files.readString(profile).replace("\"VANILLA\"", "\"LITELOADER\""));

        assertThrows(UnexpectedLoaderException.class, () -> ProfileFactory.load(profile));
    }

    @Test
    void brokenProfilesFailToLoad() throws IOException {
        Path profile = Files.writeString(tempDir.resolve("profile.json"), "{}");

        assertThrows(FailedToLoadProfileException.class, () -> ProfileFactory.load(profile));
        assertThrows(FailedToLoadProfileException.class, () -> ProfileFactory.load(tempDir.resolve("does-not-exist.json")));
    }

    @Test
    void failsToSaveIntoADirectory() {
        assertThrows(FailedToSaveProfileException.class,
                () -> ProfileFactory.save(minimalConfiguration(new VanillaLoader("1.21.11")), tempDir));
    }

    @Test
    void writesTheFormatVersion() throws IOException {
        Path profile = tempDir.resolve("profile.json");
        ProfileFactory.save(minimalConfiguration(new VanillaLoader("1.21.11")), profile);

        assertEquals(ProfileFactory.FORMAT_VERSION, new JsonFile(profile).asObject().getInt("formatVersion"));
        assertEquals(2, ProfileFactory.FORMAT_VERSION);
    }

    @Test
    void keepsMetadataAndFiles() {
        ProfileMetadata metadata = ProfileMetadata.builder()
                .name("Survival with friends")
                .description("Sodium and a few resource packs")
                .author("Notch")
                .iconUrl("https://example.com/icon.png")
                .createdAt(Instant.parse("2026-10-01T12:00:00Z"))
                .updatedAt(Instant.parse("2026-10-07T08:30:00Z"))
                .build();
        Profile original = new Profile(metadata, minimalConfiguration(new FabricLoader("1.21.11", "0.16.14")), List.of(SODIUM, LOCAL_PACK));

        Path path = tempDir.resolve("profile.json");
        ProfileFactory.save(original, path);
        Profile loaded = ProfileFactory.loadProfile(path);

        assertEquals(metadata, loaded.metadata());
        assertEquals(List.of(SODIUM, LOCAL_PACK), loaded.files());
        assertEquals(original.configuration().getJarFile(), loaded.configuration().getJarFile());
    }

    @Test
    void exportedProfilesLeaveOutWhatOnlyMakesSenseOnThisMachine() throws IOException {
        LaunchConfiguration config = new LaunchConfiguration.Builder()
                .jarFile(tempDir.resolve("mine/client.jar"))
                .assetsDirectory(tempDir.resolve("assets"))
                .librariesDirectory(tempDir.resolve("libraries"))
                .customJavaExecutable(tempDir.resolve("java/bin/java"))
                .offlineUsername("Notch")
                .maxRam(8192)
                .serverAddress("play.example.com")
                .loader(new FabricLoader("1.21.11", "0.16.14"))
                .build();
        Path shared = tempDir.resolve("shared.json");
        ProfileFactory.export(new Profile(ProfileMetadata.builder().name("Shared").build(), config, List.of(SODIUM)), shared);

        String json = Files.readString(shared);
        assertFalse(json.contains(tempDir.toString()), json);
        assertFalse(json.contains("Notch"), json);
        assertThrows(FailedToLoadProfileException.class, () -> ProfileFactory.loadProfile(shared));

        ProfileEnvironment friend = ProfileEnvironment.of(
                tempDir.resolve("friend/client.jar"), tempDir.resolve("friend-libraries"), tempDir.resolve("friend-assets")
        ).withOfflineUsername("Jeb");
        Profile loaded = ProfileFactory.loadProfile(shared, friend);

        assertEquals("Shared", loaded.metadata().name());
        assertEquals(List.of(SODIUM), loaded.files());
        assertEquals(config.getLoader(), loaded.configuration().getLoader());
        assertEquals(8192, loaded.configuration().getMaxRam());
        assertEquals("play.example.com", loaded.configuration().getServerAddress());
        assertEquals(tempDir.resolve("friend/client.jar"), loaded.configuration().getJarFile());
        assertEquals(tempDir.resolve("friend-libraries"), loaded.configuration().getLibrariesDirectory());
        assertNull(loaded.configuration().getCustomJavaExecutable());
        assertEquals("Jeb", loaded.configuration().getOfflineUsername());
        assertEquals(tempDir.resolve("friend").toAbsolutePath(), loaded.gameDirectory());
    }

    @Test
    void theEnvironmentWinsOverTheSavedPaths() {
        Path path = tempDir.resolve("profile.json");
        ProfileFactory.save(minimalConfiguration(new VanillaLoader("1.21.11")), path);

        LaunchConfiguration loaded = ProfileFactory.loadProfile(path,
                ProfileEnvironment.of(tempDir.resolve("other/client.jar"), tempDir.resolve("l"), tempDir.resolve("a"))).configuration();

        assertEquals(tempDir.resolve("other/client.jar"), loaded.getJarFile());
        assertEquals("Player", loaded.getOfflineUsername());
    }

    @Test
    void loadsFormatVersionOne() throws IOException {
        Path profile = Files.writeString(tempDir.resolve("v1.json"), """
                {
                  "minRam": 1024, "maxRam": 6144, "downloadThreads": 12,
                  "resolution": { "width": 1280, "height": 720 },
                  "launcher": { "name": "MinifiedLauncher", "version": "2.0.0" },
                  "flags": { "demoUser": false, "customResolution": true },
                  "paths": { "jarFile": "/instances/a/client.jar", "librariesDirectory": "/libraries", "assetsDirectory": "/assets" },
                  "launch": { "extraJvmArgs": ["-Dtest=true"], "loader": { "type": "FABRIC", "mcVersion": "1.21.11", "loaderVersion": "0.16.14", "iconUrl": null } },
                  "user": { "offlineUsername": "Notch" }
                }
                """);

        Profile loaded = ProfileFactory.loadProfile(profile);
        LaunchConfiguration config = loaded.configuration();

        assertEquals(6144, config.getMaxRam());
        assertEquals(1280, config.getResolutionWidth());
        assertEquals(List.of("-Dtest=true"), config.getExtraJvmArgs());
        assertEquals(new FabricLoader("1.21.11", "0.16.14"), config.getLoader());
        assertEquals("Notch", config.getOfflineUsername());
        assertFalse(config.isFullscreen());
        assertEquals(List.of(), config.getExtraGameArgs());
        assertEquals(Map.of(), config.getEnvironmentVariables());
        assertNull(config.getServerAddress());
        assertEquals(List.of(), loaded.files());
        assertNotNull(loaded.metadata().id());
    }

    @Test
    void newerFormatsAreRejected() throws IOException {
        Path profile = tempDir.resolve("profile.json");
        ProfileFactory.save(minimalConfiguration(new VanillaLoader("1.21.11")), profile);
        Files.writeString(profile, Files.readString(profile).replace("\"formatVersion\":2", "\"formatVersion\":3"));

        UnsupportedProfileFormatException e = assertThrows(UnsupportedProfileFormatException.class, () -> ProfileFactory.load(profile));
        assertEquals(3, e.getFormatVersion());
    }

    @Test
    void filesCantEscapeTheGameDirectory() throws IOException {
        Path profile = tempDir.resolve("profile.json");
        ProfileFactory.save(new Profile(ProfileMetadata.builder().build(), minimalConfiguration(new VanillaLoader("1.21.11")),
                List.of(SODIUM)), profile);
        Files.writeString(profile, Files.readString(profile).replace("mods/sodium.jar", "../../.bashrc"));

        assertThrows(FailedToLoadProfileException.class, () -> ProfileFactory.loadProfile(profile));
    }

    private static final ProfileFile SODIUM = ProfileFile.builder("mods/sodium.jar")
            .name("Sodium")
            .hash("sha1", "da39a3ee5e6b4b0d3255bfef95601890afd80709")
            .download("https://cdn.modrinth.com/data/AANobbMI/versions/abc/sodium.jar")
            .size(1234)
            .server(SideRequirement.UNSUPPORTED)
            .source(ProfileFileSource.modrinth("AANobbMI", "abc"))
            .build();

    private static final ProfileFile LOCAL_PACK = ProfileFile.builder("resourcepacks/our-pack.zip")
            .client(SideRequirement.OPTIONAL)
            .enabled(false)
            .build();

    @Tag("manual")
    @Test
    void canLaunchLoadedConfiguration() {
        assertDoesNotThrow(() ->
                Launcher.launchMinecraft(
                        null,
                        createConfigurationPair()[1]
                )
        );
    }

    static List<Loader> loaders() {
        return List.of(
                new FabricLoader("1.21.11", "0.16.14"),
                new QuiltLoader("1.21.11", "0.29.2"),
                new ForgeLoader("1.21.11", "1.21.11-61.1.8"),
                new NeoforgeLoader("1.21.11", "21.11.20"),
                new CustomLoader("Homebrew", "1.21.11", "0.0.1", "https://example.com/icon.png", "net.example.Main",
                        List.of("-Dhomebrew=true"), List.of("--brew"), List.of("/opt/homebrew.jar"))
        );
    }

    private LaunchConfiguration minimalConfiguration(Loader loader) {
        return new LaunchConfiguration.Builder()
                .jarFile(tempDir.resolve("client.jar"))
                .assetsDirectory(tempDir.resolve("assets"))
                .librariesDirectory(tempDir.resolve("libraries"))
                .loader(loader)
                .build();
    }

    private LaunchConfiguration createLaunchConfiguration() {
        return new LaunchConfiguration.Builder()
                .minRam(1024)
                .maxRam(6144)
                .downloadThreads(12)
                .resolution(1280, 720)
                .launcherName("MinifiedLauncher")
                .launcherVersion("2.0.0")
                .isDemoUser(true)
                //.extraJvmArg("-XX:+UseG1GC")
                .extraJvmArgs(List.of("-Dtest=true", "-Dlauncher.name=minified"))
                .jarFile(tempDir.resolve("test.jar"))
                .assetsDirectory(tempDir.resolve("assets"))
                .librariesDirectory(tempDir.resolve("libraries"))
                .nativesDirectory(tempDir.resolve("natives"))
                .customJavaExecutable(tempDir.resolve("java/bin/java"))
                .offlineUsername("Notch")
                .fullscreen(true)
                .extraGameArgs(List.of("--quickPlayPath", "quickplay.json"))
                .environmentVariable("MESA_GL_VERSION_OVERRIDE", "4.5")
                .serverAddress("play.example.com:25566")
                .loader(new VanillaLoader("26.1.2"))
                .isDemoUser(false)
                .build();
    }

    private LaunchConfiguration[] createConfigurationPair() {
        LaunchConfiguration original = createLaunchConfiguration();
        Path profile = tempDir.resolve("profile.json");
        ProfileFactory.save(original, profile);
        LaunchConfiguration loaded = ProfileFactory.load(profile);
        return new LaunchConfiguration[]{original, loaded};
    }

}
