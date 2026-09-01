package com.dervarex.minified.worlds.poi;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;

/**
 * Wraps a single POI record
 */
public class PoiRecord {
    private final NbtCompound raw;

    public PoiRecord(NbtCompound raw) {
        this.raw = raw;
    }

    public NbtCompound raw() { return raw; }

    public int[] pos() { return raw.getIntArray("pos").value(); }
    public void setPos(int x, int y, int z) { raw.setIntArray("pos", new int[]{x, y, z}); }

    public String type() { return raw.getString("type"); }
    public void setType(String value) { raw.setString("type", value); }

    public int freeTickets() { return raw.getInt("free_tickets"); }
    public void setFreeTickets(int value) { raw.setInt("free_tickets", value); }
}