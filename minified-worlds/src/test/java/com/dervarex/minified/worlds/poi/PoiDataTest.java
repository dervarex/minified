package com.dervarex.minified.worlds.poi;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PoiDataTest {

    private static NbtCompound rootWithSections() {
        NbtCompound root = new NbtCompound();
        root.setCompound("Sections", new NbtCompound());
        return root;
    }

    @Test
    void parsesEmptySections() {
        PoiData data = new PoiData(rootWithSections());
        assertTrue(data.records().isEmpty());
        assertTrue(data.records(0).isEmpty());
    }

    @Test
    void testAddRecord() {
        PoiData data = new PoiData(rootWithSections());

        PoiRecord record = data.addRecord(2, 1, 33, -4, "minecraft:home", 1);

        assertEquals("minecraft:home", record.type());
        assertArrayEquals(new int[]{1, 33, -4}, record.pos());
        assertEquals(1, record.freeTickets());

        assertEquals(1, data.records().size());
        assertEquals(1, data.records(2).size());
        assertSame(record, data.records(2).get(0));
        assertTrue(data.records(3).isEmpty());

        // reflected into raw NBT too
        assertTrue(data.raw().getCompound("Sections").has("2"));
    }

    @Test
    void testAddMultipleRecordsToSameSection() {
        PoiData data = new PoiData(rootWithSections());
        data.addRecord(0, 0, 0, 0, "minecraft:meeting", 1);
        data.addRecord(0, 1, 0, 0, "minecraft:nether_portal", 0);

        List<PoiRecord> records = data.records(0);
        assertEquals(2, records.size());
        assertEquals("minecraft:meeting", records.get(0).type());
        assertEquals("minecraft:nether_portal", records.get(1).type());
    }

    @Test
    void testSetAndGetPoiRecordData() {
        PoiData data = new PoiData(rootWithSections());
        PoiRecord record = data.addRecord(0, 0, 0, 0, "minecraft:home", 1);

        record.setType("minecraft:meeting");
        record.setFreeTickets(3);
        record.setPos(9, 10, 11);

        assertEquals("minecraft:meeting", record.type());
        assertEquals(3, record.freeTickets());
        assertArrayEquals(new int[]{9, 10, 11}, record.pos());
    }

    @Test
    void testSaveAndReloadPoiRecord() {
        NbtCompound root = rootWithSections();
        PoiData writer = new PoiData(root);
        writer.addRecord(-1, 5, 6, 7, "minecraft:beehive", 2);

        // reparse the same underlying NBT tree from scratch
        PoiData reader = new PoiData(root);
        List<PoiRecord> records = reader.records(-1);
        assertEquals(1, records.size());
        assertEquals("minecraft:beehive", records.get(0).type());
        assertArrayEquals(new int[]{5, 6, 7}, records.get(0).pos());
        assertEquals(2, records.get(0).freeTickets());
    }
}
