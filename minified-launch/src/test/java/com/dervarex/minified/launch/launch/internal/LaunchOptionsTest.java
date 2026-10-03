package com.dervarex.minified.launch.launch.internal;

import com.dervarex.minified.auth.user.User;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.modding.Loader;
import com.dervarex.minified.launch.launch.modding.forge.ForgeLoader;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;
import com.dervarex.minified.utils.json.JsonFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LaunchOptionsTest {

    @TempDir
    Path tempDir;

    @Test
    void offlinePlayersGetTheUuidEveryOfflineServerExpects() {
        Map<String, String> variables = build(null, new VanillaLoader("1.21.11")).getVariables();

        assertEquals("Notch", variables.get("auth_player_name"));
        assertEquals("b50ad385-829d-3141-a216-7e7d7539ba7f", variables.get("auth_uuid"));
        assertEquals("0", variables.get("auth_access_token"));
        assertEquals("-", variables.get("auth_session"));
        assertEquals("legacy", variables.get("user_type"));
    }

    @Test
    void loggedInPlayersPassTheirSessionToOldVersionsToo() {
        User user = new User(UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"), "Notch", "token", null);

        Map<String, String> variables = build(user, new VanillaLoader("1.21.11")).getVariables();

        assertEquals("069a79f444e94726a5befca90e38aaf5", variables.get("auth_uuid"));
        assertEquals("token", variables.get("auth_access_token"));
        assertEquals("token:token:069a79f444e94726a5befca90e38aaf5", variables.get("auth_session"));
        assertEquals("msa", variables.get("user_type"));
    }

    @Test
    void fillsInWhatTheVersionJsonLeavesOut() {
        LaunchOptions options = build(null, new VanillaLoader("1.21.11"));

        assertEquals("", options.getVariables().get("assets_index_name"));
        assertEquals("release", options.getVariables().get("version_type"));
        assertEquals(Map.of("has_custom_resolution", false, "is_demo_user", false), options.getFeatures());
    }

    @Test
    void forgeLibrariesLiveInTheGameDirectory() {
        assertEquals(tempDir.resolve("jar/libraries").toString(),
                build(null, new ForgeLoader("1.21.11", "1.21.11-61.1.8")).getVariables().get("library_directory"));
        assertEquals(tempDir.resolve("libraries").toString(),
                build(null, new VanillaLoader("1.21.11")).getVariables().get("library_directory"));
    }

    private LaunchOptions build(User user, Loader loader) {
        LaunchConfiguration config = new LaunchConfiguration.Builder()
                .jarFile(tempDir.resolve("jar/client.jar"))
                .assetsDirectory(tempDir.resolve("assets"))
                .librariesDirectory(tempDir.resolve("libraries"))
                .offlineUsername("Notch")
                .loader(loader)
                .build();

        return LaunchOptions.buildLaunchOptions(user, "1.21.11", config, new JsonFile("{}"), "client.jar");
    }
}
