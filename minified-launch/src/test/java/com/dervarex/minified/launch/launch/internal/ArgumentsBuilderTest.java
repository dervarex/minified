package com.dervarex.minified.launch.launch.internal;

import com.dervarex.minified.launch.exceptions.version.MalformedVersionJsonException;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.modding.Loader;
import com.dervarex.minified.launch.launch.modding.custom.CustomLoader;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;
import com.dervarex.minified.utils.json.JsonFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArgumentsBuilderTest {

    private static final VanillaLoader VANILLA = new VanillaLoader("1.21.11");
    private static final CustomLoader HOMEBREW = new CustomLoader("Homebrew", "1.21.11", "0.0.1", null, "net.example.Main",
            List.of("-Dhomebrew=true"), List.of(), List.of());

    private static final LaunchOptions OPTIONS = LaunchOptions.create()
            .setVariable("classpath", "client.jar")
            .setVariable("natives_directory", "/natives")
            .setVariable("auth_player_name", "Notch")
            .setVariable("auth_session", "-");

    @TempDir
    Path tempDir;

    @Test
    void legacyVersionsStillGetAClasspath() {
        assertEquals(List.of("-cp", "client.jar", "-Dextra=true"), jvm("{}", VANILLA));
    }

    @Test
    void mergesJvmArgumentsAndDropsTheOnesThatBreakTheGame() {
        String versionJson = """
                { "arguments": {
                    "default-user-jvm": [{ "value": ["-Xmx2G"] }],
                    "jvm": ["-XX:+UseCompactObjectHeaders", "--sun-misc-unsafe-memory-access=allow", "-Djava.library.path=${natives_directory}"]
                } }
                """;

        assertEquals(List.of("-Xmx4096M", "-Djava.library.path=/natives", "-Dextra=true"), jvm(versionJson, VANILLA));
    }

    @Test
    void customLoaderJvmArgumentsComeLast() {
        assertEquals(List.of("-cp", "client.jar", "-Dextra=true", "-Dhomebrew=true"), jvm("{}", HOMEBREW));
    }

    @Test
    void customLoaderGameArgumentsComeLast() {
        CustomLoader loader = new CustomLoader("Homebrew", "1.21.11", "0.0.1", null, "net.example.Main",
                List.of(), List.of("--brew"), List.of());

        List<String> arguments = ArgumentsBuilder.buildGameArguments(
                new JsonFile("{ \"arguments\": { \"game\": [\"--username\", \"${auth_player_name}\"] } }"),
                OPTIONS, loader, "1.21.11", config(loader), false);

        assertEquals(List.of("--username", "Notch", "--brew"), arguments);
    }

    @Test
    void legacyGameArgumentsGetSplitAndFilledIn() {
        List<String> arguments = ArgumentsBuilder.buildGameArguments(
                new JsonFile("{ \"minecraftArguments\": \"${auth_player_name} ${auth_session}\" }"),
                OPTIONS, VANILLA, "1.5.2", config(VANILLA), false);

        assertEquals(List.of("Notch", "-"), arguments);
    }

    @Test
    void versionJsonsWithoutAnyArgumentsAreMalformed() {
        assertThrows(MalformedVersionJsonException.class, () -> ArgumentsBuilder.buildGameArguments(
                new JsonFile("{}"), OPTIONS, VANILLA, "1.5.2", config(VANILLA), false));
    }

    private List<String> jvm(String versionJson, Loader loader) {
        return ArgumentsBuilder.buildJvmArguments(new JsonFile(versionJson), config(loader), OPTIONS, loader, "1.21.11", false);
    }

    private LaunchConfiguration config(Loader loader) {
        return new LaunchConfiguration.Builder()
                .jarFile(tempDir.resolve("jar/client.jar"))
                .assetsDirectory(tempDir.resolve("assets"))
                .librariesDirectory(tempDir.resolve("libraries"))
                .extraJvmArg("-Dextra=true")
                .loader(loader)
                .build();
    }

    @Test
    void leavesTheVersionJsonAlone() {
        CustomLoader loader = new CustomLoader("Homebrew", "1.21.11", "0.0.1", null, "net.example.Main",
                List.of(), List.of("--brew"), List.of());
        JsonFile versionJson = new JsonFile("{ \"arguments\": { \"game\": [\"--demo\"] } }");

        ArgumentsBuilder.buildGameArguments(versionJson, OPTIONS, loader, "1.21.11", config(loader), false);
        List<String> second = ArgumentsBuilder.buildGameArguments(versionJson, OPTIONS, loader, "1.21.11", config(loader), false);

        assertEquals(List.of("--demo", "--brew"), second);
        assertEquals("{\"arguments\":{\"game\":[\"--demo\"]}}", versionJson.toJson());
    }
}
