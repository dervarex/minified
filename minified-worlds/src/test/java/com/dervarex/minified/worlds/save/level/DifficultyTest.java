package com.dervarex.minified.worlds.save.level;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DifficultyTest {

    @Test
    void testFindDifficultyByName() {
        for (Difficulty difficulty : Difficulty.values()) {
            assertEquals(difficulty, Difficulty.valueOf(difficulty.name()));
        }
    }
}
