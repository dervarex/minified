package com.dervarex.minified.launch.arguments;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LegacyMinecraftArgumentsParserTest {

    @Test
    void splitsOnAnyWhitespace() {
        assertEquals(
                List.of("--username", "${auth_player_name}", "--session", "${auth_session}"),
                LegacyMinecraftArgumentsParser.parse("  --username ${auth_player_name}\t--session   ${auth_session} ")
        );
    }

    @Test
    void keepsQuotedValuesTogether() {
        assertEquals(
                List.of("--gameDir", "/home/someone/my game", "--demo"),
                LegacyMinecraftArgumentsParser.parse("--gameDir \"/home/someone/my game\" \"\" --demo")
        );
    }

    @Test
    void returnsNothingForNothing() {
        assertEquals(List.of(), LegacyMinecraftArgumentsParser.parse(null));
        assertEquals(List.of(), LegacyMinecraftArgumentsParser.parse("   "));
    }
}
