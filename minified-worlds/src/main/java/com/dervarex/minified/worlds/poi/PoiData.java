package com.dervarex.minified.worlds.poi;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import com.dervarex.minified.utils.nbt.tag.NbtTag;

import java.util.*;

/**
 * Wraps a poi/*.mca chunk's raw NBT
 */
public class PoiData {
    private final NbtCompound root;
    private final Map<Integer, NbtCompound> sectionTags = new HashMap<>();
    private final Map<Integer, List<PoiRecord>> records = new HashMap<>();

    public PoiData(NbtCompound root) {
        this.root = root;
        NbtCompound sections = root.getCompound("Sections");

        for (Map.Entry<String, NbtTag> entry : sections.asMap().entrySet()) {
            int sectionY = Integer.parseInt(entry.getKey());
            NbtCompound sectionTag = (NbtCompound) entry.getValue();
            sectionTags.put(sectionY, sectionTag);

            List<PoiRecord> sectionRecords = new ArrayList<>();
            if (sectionTag.has("Records")) {
                for (NbtTag recordTag : sectionTag.getList("Records").elements()) {
                    sectionRecords.add(new PoiRecord((NbtCompound) recordTag));
                }
            }
            records.put(sectionY, sectionRecords);
        }
    }

    public NbtCompound raw() { return root; }

    /** All POI records across every section in this chunk. */
    public List<PoiRecord> records() {
        List<PoiRecord> all = new ArrayList<>();
        for (List<PoiRecord> sectionRecords : records.values()) all.addAll(sectionRecords);
        return all;
    }

    /** POI records within just one section Y, or an empty list if that section has none */
    public List<PoiRecord> records(int sectionY) {
        return List.copyOf(records.getOrDefault(sectionY, List.of()));
    }

    /**
     * Adds a new POI record to the given section y, creating that section
     * (with Valid=true) if it doesn't exist in this chunk.
     * Links the POI to both in memory list and to the NBT.
     */
    public PoiRecord addRecord(int sectionY, int x, int y, int z, String type, int freeTickets) {
        NbtCompound recordTag = new NbtCompound();
        recordTag.setIntArray("pos", new int[]{x, y, z});
        recordTag.setString("type", type);
        recordTag.setInt("free_tickets", freeTickets);
        PoiRecord record = new PoiRecord(recordTag);

        NbtCompound sectionTag = sectionTags.get(sectionY);
        if (sectionTag == null) {
            sectionTag = new NbtCompound();
            sectionTag.setBoolean("Valid", true);
            sectionTag.setList("Records", new NbtList((byte) 10)); // TAG_Compound
            sectionTags.put(sectionY, sectionTag);
            root.getCompound("Sections").setCompound(String.valueOf(sectionY), sectionTag);
            records.put(sectionY, new ArrayList<>());
        }

        sectionTag.getList("Records").add(recordTag);
        records.get(sectionY).add(record);
        return record;
    }
}