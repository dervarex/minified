package com.dervarex.minified.worlds.dimension.region;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegionCategoryTest {

    @Test
    void testFolderStructureIsCorrect() {
        assertEquals("region", RegionCategory.REGION.folderName);
        assertEquals("poi", RegionCategory.POI.folderName);
        assertEquals("entities", RegionCategory.ENTITIES.folderName);
        assertEquals("data", RegionCategory.DATA.folderName);
    }
}
