package com.dervarex.minified.launch.arguments;

import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonNumber;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonString;
import com.dervarex.minified.utils.os.OS;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JvmArgumentsParserTest {

    @Test
    void parseReplacesMemoryArgumentsAndPreservesRegularArgs() {
        JsonArray jvm = new JsonArray();
        jvm.add(new JsonString("-Xms256M"));
        jvm.add(new JsonString("-Xmx2G"));
        jvm.add(new JsonString("-XX:+UseG1GC"));

        List<String> parsed = JvmArgumentsParser.parse(jvm, 768, 4096);

        assertEquals(List.of("-Xms768M", "-Xmx4096M", "-XX:+UseG1GC"), parsed);
    }

    @Test
    void parseHonorsRulesForCurrentOperatingSystem() {
        JsonArray rules = new JsonArray();
        rules.add(new JsonObject(Map.of(
                "action", new JsonString("allow"),
                "os", new JsonObject(Map.of("name", new JsonString(OS.getCurrentOS().getName())))
        )));

        JsonObject conditionalArg = new JsonObject(Map.of(
                "rules", rules,
                "value", new JsonString("-Dtest.allowed=true")
        ));

        JsonArray jvm = new JsonArray();
        jvm.add(conditionalArg);

        List<String> parsed = JvmArgumentsParser.parse(jvm, 256, 1024);

        assertEquals(List.of("-Dtest.allowed=true"), parsed);
    }

    @Test
    void parseSkipsEntriesWhenRulesDoNotMatch() {
        JsonArray rules = new JsonArray();
        rules.add(new JsonObject(Map.of(
                "action", new JsonString("allow"),
                "os", new JsonObject(Map.of("name", new JsonString(nonCurrentOsToken())))
        )));

        JsonObject conditionalArg = new JsonObject(Map.of(
                "rules", rules,
                "value", new JsonString("-Dtest.not.allowed=true")
        ));

        JsonArray jvm = new JsonArray();
        jvm.add(conditionalArg);

        List<String> parsed = JvmArgumentsParser.parse(jvm, 256, 1024);

        assertEquals(List.of(), parsed);
    }

    @Test
    void parseIgnoresNonStringAndNonObjectEntries() {
        JsonArray jvm = new JsonArray();
        jvm.add(new JsonNumber(BigDecimal.ONE));
        jvm.add(new JsonString("-Dvalid=true"));

        List<String> parsed = JvmArgumentsParser.parse(jvm, 256, 1024);

        assertEquals(List.of("-Dvalid=true"), parsed);
    }

    @Test
    void parseFlattensArrayValues() {
        JsonArray jvm = json("""
                [{ "value": ["-Xss1M", "-Dfoo=bar"] }]
                """);

        assertEquals(List.of("-Xss1M", "-Dfoo=bar"), JvmArgumentsParser.parse(jvm, 256, 1024));
    }

    @Test
    void parseRespectsOsVersionRanges() {
        JsonArray jvm = json("""
                [
                  { "rules": [{ "action": "allow", "os": { "versionRange": { "min": "0" } } }], "value": "-Dold.enough=true" },
                  { "rules": [{ "action": "allow", "os": { "versionRange": { "min": "99999" } } }], "value": "-Dfrom.the.future=true" }
                ]
                """);

        assertEquals(List.of("-Dold.enough=true"), JvmArgumentsParser.parse(jvm, 256, 1024));
    }

    @Test
    void parseReadsDefaultUserJvmFromObjects() {
        JsonObject arguments = new JsonFile("""
                { "default-user-jvm": [{ "value": ["-Xmx2G", "-XX:+UseZGC"] }] }
                """).asObject();

        assertEquals(List.of("-Xmx4096M", "-XX:+UseZGC"), JvmArgumentsParser.parse(arguments, 512, 4096));
    }

    private static JsonArray json(String content) {
        return new JsonFile(content).asArray();
    }

    private static String nonCurrentOsToken() {
        return OS.getCurrentOS() == OS.WINDOWS ? "linux" : "windows";
    }
}
