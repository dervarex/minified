package com.dervarex.minified.launch.launch.internal;

import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.utils.download.DownloadHelper;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;
import com.dervarex.minified.utils.sha.Hasher;
import org.apiguardian.api.API;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The log4j config from the version JSON, Mojang's fix for Log4Shell (CVE-2021-44228) for 1.7 to 1.18
 */
@API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
public final class LoggingConfig {

    private LoggingConfig() {}

    static final String CONFIG_PROPERTY = "-Dlog4j.configurationFile=";
    static final String NO_LOOKUPS_PROPERTY = "-Dlog4j2.formatMsgNoLookups=true";

    private static final Pattern XML_CONSOLE_LAYOUT = Pattern.compile("<(Legacy)?XMLLayout\\s*/>");
    private static final Pattern PATTERN_LAYOUT = Pattern.compile("<PatternLayout\\s+pattern=\"[^\"]*\"\\s*/>");
    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9._-]+");

    private static final String PLAIN_CONSOLE_LAYOUT = "<PatternLayout pattern=\"[%d{HH:mm:ss}] [%t/%level]: %msg%n\" />";

    // fallback config for when the user's offline or something
    static final String FALLBACK_CONFIG = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Configuration status="WARN">
                <Appenders>
                    <Console name="SysOut" target="SYSTEM_OUT">
                        <PatternLayout pattern="[%d{HH:mm:ss}] [%t/%level]: %msg%n" />
                    </Console>
                    <RollingRandomAccessFile name="File" fileName="logs/latest.log" filePattern="logs/%d{yyyy-MM-dd}-%i.log.gz">
                        <PatternLayout pattern="[%d{HH:mm:ss}] [%t/%level]: %msg%n" />
                        <Policies>
                            <TimeBasedTriggeringPolicy />
                            <OnStartupTriggeringPolicy />
                        </Policies>
                    </RollingRandomAccessFile>
                </Appenders>
                <Loggers>
                    <Root level="info">
                        <filters>
                            <MarkerFilter marker="NETWORK_PACKETS" onMatch="DENY" onMismatch="NEUTRAL" />
                            <RegexFilter regex="(?s).*\\$\\{[^}]*\\}.*" onMatch="DENY" onMismatch="NEUTRAL"/>
                        </filters>
                        <AppenderRef ref="SysOut"/>
                        <AppenderRef ref="File"/>
                    </Root>
                </Loggers>
            </Configuration>
            """;

    /**
     * {@return the JVM arguments for the log4j config, empty if the version has none}
     * @param versionJson the vanilla version JSON
     * @param launchConfig the launch configuration
     * @param online whether the file may be downloaded
     * @param existingJvmArgs the JVM arguments so far
     */
    public static List<String> buildJvmArguments(
            JsonFile versionJson,
            LaunchConfiguration launchConfig,
            boolean online,
            List<String> existingJvmArgs
    ) {
        List<String> args = new ArrayList<>();
        JsonObject client = clientLogging(versionJson);
        if (client == null) {
            return args;
        }
        args.add(NO_LOOKUPS_PROPERTY);
        if (existingJvmArgs.stream().anyMatch(arg -> arg.startsWith(CONFIG_PROPERTY))) {
            return args;
        }

        Path configDir = launchConfig.getAssetsDirectory().resolve("log_configs");
        JsonValue file = client.get("file");
        String id = file != null && file.isObject() ? file.asObject().getString("id") : null;
        if (id == null || !SAFE_ID.matcher(id).matches()) {
            id = "client.xml";
        }

        String content = mojangConfig(client, configDir.resolve(id), online);
        String patched = content == null ? FALLBACK_CONFIG : plainConsole(content);

        Path target = configDir.resolve("minified-" + id);
        try {
            Files.createDirectories(configDir);
            Files.writeString(target, patched, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write the log4j config to " + target, e);
        }

        String argument = client.getString("argument");
        if (argument == null || !argument.contains("${path}")) {
            argument = CONFIG_PROPERTY + "${path}";
        }
        args.add(argument.replace("${path}", target.toAbsolutePath().toString()));
        return args;
    }

    private static JsonObject clientLogging(JsonFile versionJson) {
        JsonValue logging = versionJson.get("logging");
        if (logging == null || !logging.isObject()) {
            return null;
        }
        JsonValue client = logging.asObject().get("client");
        return client != null && client.isObject() ? client.asObject() : null;
    }

    private static String mojangConfig(JsonObject client, Path original, boolean online) {
        JsonObject file = client.get("file").asObject();
        String url = file.getString("url");
        String sha1 = file.getString("sha1");
        if (url == null || sha1 == null) {
            return null;
        }
        try {
            if (!matches(original, sha1)) {
                if (!online) {
                    return null;
                }
                Files.createDirectories(original.getParent());
                HttpClient httpClient = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();
                if (!DownloadHelper.download(url, original, sha1, httpClient) || !matches(original, sha1)) {
                    return null;
                }
            }
            return Files.readString(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean matches(Path path, String sha1) throws IOException {
        return Files.isRegularFile(path) && Hasher.sha1(path).equalsIgnoreCase(sha1);
    }

    static String plainConsole(String config) {
        Matcher console = XML_CONSOLE_LAYOUT.matcher(config);
        if (!console.find()) {
            return config;
        }
        Matcher pattern = PATTERN_LAYOUT.matcher(config);
        String layout = pattern.find() ? pattern.group() : PLAIN_CONSOLE_LAYOUT;
        return console.replaceAll(Matcher.quoteReplacement(layout));
    }
}
