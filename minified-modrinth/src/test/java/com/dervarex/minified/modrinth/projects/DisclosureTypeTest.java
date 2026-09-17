package com.dervarex.minified.modrinth.projects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisclosureTypeTest {

    @Test
    void fromApiValue_returnsEnum_whenMatch() {
        assertEquals(DisclosureType.AI_CONTENT, DisclosureType.fromApiValue("ai_content"));
        assertEquals(DisclosureType.TELEMETRY, DisclosureType.fromApiValue("telemetry"));
    }

    @Test
    void fromApiValue_ignoresCase() {
        assertEquals(DisclosureType.AI_CONTENT, DisclosureType.fromApiValue("AI_CONTENT"));
    }

    @Test
    void fromApiValue_returnsUnknown_whenNoMatch() {
        assertEquals(DisclosureType.UNKNOWN, DisclosureType.fromApiValue("not_real"));
    }

    @Test
    void fromApiValue_returnsNull_whenNull() {
        assertNull(DisclosureType.fromApiValue(null));
    }

    @Test
    void getApiValue_returnsValue() {
        assertEquals("ai_content", DisclosureType.AI_CONTENT.getApiValue());
    }

    @Test
    void toString_returnsApiValue() {
        assertEquals("telemetry", DisclosureType.TELEMETRY.toString());
    }
}