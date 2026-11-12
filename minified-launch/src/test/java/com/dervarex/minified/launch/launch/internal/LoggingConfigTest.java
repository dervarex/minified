package com.dervarex.minified.launch.launch.internal;

import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.sha.Hasher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggingConfigTest {

    private static final String CLIENT_1_12 = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Configuration status="WARN">
                <Appenders>
                    <Console name="SysOut" target="SYSTEM_OUT">
                        <LegacyXMLLayout />
                    </Console>
                    <RollingRandomAccessFile name="File" fileName="logs/latest.log" filePattern="logs/%d{yyyy-MM-dd}-%i.log.gz">
                        <PatternLayout pattern="[%d{HH:mm:ss}] [%t/%level]: %msg{nolookups}%n" />
                    </RollingRandomAccessFile>
                </Appenders>
            </Configuration>
            """;

    @TempDir
    Path tempDir;

    @Test
    void versionsWithoutLog4jGetNothing() {
        assertEquals(List.of(), args("{}", false, List.of()));
    }

    @Test
    void offlineWithoutTheFileUsesTheFallback() throws IOException {
        List<String> args = args(versionJson("client-1.7.xml", "0".repeat(40)), false, List.of());

        Path config = tempDir.resolve("assets/log_configs/minified-client-1.7.xml");
        assertEquals(List.of(LoggingConfig.NO_LOOKUPS_PROPERTY, LoggingConfig.CONFIG_PROPERTY + config.toAbsolutePath()), args);
        assertEquals(LoggingConfig.FALLBACK_CONFIG, Files.readString(config));
    }

    @Test
    void cachedFileGetsAPlainConsole() throws IOException {
        Path original = tempDir.resolve("assets/log_configs/client-1.12.xml");
        Files.createDirectories(original.getParent());
        Files.writeString(original, CLIENT_1_12);

        args(versionJson("client-1.12.xml", Hasher.sha1(original)), false, List.of());

        String copy = Files.readString(tempDir.resolve("assets/log_configs/minified-client-1.12.xml"));
        assertFalse(copy.contains("XMLLayout"));
        assertTrue(copy.contains("""
                <Console name="SysOut" target="SYSTEM_OUT">
                            <PatternLayout pattern="[%d{HH:mm:ss}] [%t/%level]: %msg{nolookups}%n" />"""));
        assertEquals(CLIENT_1_12, Files.readString(original));
    }

    @Test
    void tamperedFileIsNotUsed() throws IOException {
        Path original = tempDir.resolve("assets/log_configs/client-1.12.xml");
        Files.createDirectories(original.getParent());
        Files.writeString(original, CLIENT_1_12);
        String sha1 = Hasher.sha1(original);
        Files.writeString(original, CLIENT_1_12.replace("{nolookups}", ""));

        args(versionJson("client-1.12.xml", sha1), false, List.of());

        assertEquals(LoggingConfig.FALLBACK_CONFIG, Files.readString(tempDir.resolve("assets/log_configs/minified-client-1.12.xml")));
    }

    @Test
    void launcherConfigWins() {
        List<String> args = args(versionJson("client-1.12.xml", "0".repeat(40)), false,
                List.of("-Dlog4j.configurationFile=/my/config.xml"));

        assertEquals(List.of(LoggingConfig.NO_LOOKUPS_PROPERTY), args);
    }

    @Test
    void idCantLeaveTheFolder() {
        args(versionJson("../../evil.xml", "0".repeat(40)), false, List.of());

        assertTrue(Files.exists(tempDir.resolve("assets/log_configs/minified-client.xml")));
        assertFalse(Files.exists(tempDir.resolve("evil.xml")));
    }

    @Test
    void downloadsMojangsFile() throws IOException {
        String json = versionJson("client-1.12.xml", "bd65e7d2e3c237be76cfbef4c2405033d7f91521")
                .replace("https://example.invalid/", "https://piston-data.mojang.com/v1/objects/bd65e7d2e3c237be76cfbef4c2405033d7f91521/");

        args(json, true, List.of());

        assertTrue(Files.exists(tempDir.resolve("assets/log_configs/client-1.12.xml")));
        String copy = Files.readString(tempDir.resolve("assets/log_configs/minified-client-1.12.xml"));
        assertFalse(copy.contains("XMLLayout"));
        assertTrue(copy.contains("%msg{nolookups}"));
    }

    @Test
    void consoleOfTheOldFileGetsItsFilePattern() {
        String config = "<Console><XMLLayout /></Console><File><PatternLayout pattern=\"%msg%n\" /></File>";

        assertEquals("<Console><PatternLayout pattern=\"%msg%n\" /></Console><File><PatternLayout pattern=\"%msg%n\" /></File>",
                LoggingConfig.plainConsole(config));
    }

    private List<String> args(String versionJson, boolean online, List<String> existing) {
        LaunchConfiguration config = new LaunchConfiguration.Builder()
                .jarFile(tempDir.resolve("jar/client.jar"))
                .assetsDirectory(tempDir.resolve("assets"))
                .librariesDirectory(tempDir.resolve("libraries"))
                .loader(new VanillaLoader("1.16.5"))
                .build();
        return LoggingConfig.buildJvmArguments(new JsonFile(versionJson), config, online, existing);
    }

    private static String versionJson(String id, String sha1) {
        return """
                { "logging": { "client": {
                    "argument": "-Dlog4j.configurationFile=${path}",
                    "file": { "id": "%s", "sha1": "%s", "size": 888, "url": "https://example.invalid/%s" },
                    "type": "log4j2-xml"
                } } }
                """.formatted(id, sha1, id);
    }
}
