package com.dervarex.minified.auth.user;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MinecraftUUIDTest {

    private static final UUID SAMPLE =
            UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");

    @Test
    void constructor_storesDashedAndUndashedCorrectly() {
        MinecraftUUID uuid = new MinecraftUUID(SAMPLE);

        assertEquals(SAMPLE, uuid.getDashed());
        assertEquals("069a79f444e94726a5befca90e38aaf5", uuid.getUndashed());
    }

    @Test
    void getUndashed_hasNoDashes() {
        MinecraftUUID uuid = new MinecraftUUID(SAMPLE);
        assertFalse(uuid.getUndashed().contains("-"));
    }

    @Test
    void getUndashed_has32Chars() {
        MinecraftUUID uuid = new MinecraftUUID(SAMPLE);
        assertEquals(32, uuid.getUndashed().length());
    }
}