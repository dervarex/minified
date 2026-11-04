package com.dervarex.minified.worlds.poi;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import com.dervarex.minified.utils.nbt.tag.NbtTag;

import java.util.*;
import org.apiguardian.api.API;

/**
 * Wraps a poi/*.mca chunk's raw NBT
 */
@API(status = API.Status.STABLE)
public class PoiData {
    private final NbtCompound root;
    private final Map<Integer, NbtCompound> sectionTags = new HashMap<>();
    private final Map<Integer, List<PoiRecord>> records = new HashMap<>();

    /**
     * Wraps a chunk's POI NBT
     * @param root the chunk's POI NBT
     */
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

    /** {@return the chunk's POI NBT with every change made so far} */
    public NbtCompound raw() { return root; }

    /** {@return all POI records across every section in this chunk} */
    public List<PoiRecord> records() {
        List<PoiRecord> all = new ArrayList<>();
        for (List<PoiRecord> sectionRecords : records.values()) all.addAll(sectionRecords);
        return all;
    }

    /**
     * {@return the POI records within just one section Y, or an empty list if that section has none}
     * @param sectionY the Y of the section, so the block y divided by 16
     */
    public List<PoiRecord> records(int sectionY) {
        return List.copyOf(records.getOrDefault(sectionY, List.of()));
    }

    /**
     * Adds a new POI record to the given section y, creating that section
     * (with Valid=true) if it doesn't exist in this chunk.
     * Links the POI to both in memory list and to the NBT.
     * @param sectionY the Y of the section, so the block y divided by 16
     * @param x the block x
     * @param y the block y
     * @param z the block z
     * @param type the POI type
     * @param freeTickets how many more villagers can claim it
     * @return the new record
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