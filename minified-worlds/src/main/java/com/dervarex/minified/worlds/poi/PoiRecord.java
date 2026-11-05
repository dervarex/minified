package com.dervarex.minified.worlds.poi;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.apiguardian.api.API;

/**
 * Wraps a single POI record
 */
@API(status = API.Status.STABLE)
public class PoiRecord {
    private final NbtCompound raw;

    /**
     * Wraps a POI record's NBT
     * @param raw the record's NBT
     */
    public PoiRecord(NbtCompound raw) {
        this.raw = raw;
    }

    /** {@return the record's NBT} */
    public NbtCompound raw() { return raw; }

    /** {@return the block position, x, y, z} */
    public int[] pos() { return raw.getIntArray("pos"); }

    /**
     * Sets the block position
     * @param x the x
     * @param y the y
     * @param z the z
     */
    public void setPos(int x, int y, int z) { raw.setIntArray("pos", new int[]{x, y, z}); }

    /** {@return the POI type} */
    public String type() { return raw.getString("type"); }

    /**
     * Sets the POI type
     * @param value the POI type
     */
    public void setType(String value) { raw.setString("type", value); }

    /** {@return how many more villagers can claim it} */
    public int freeTickets() { return raw.getInt("free_tickets"); }

    /**
     * Sets how many more villagers can claim it
     * @param value the free tickets
     */
    public void setFreeTickets(int value) { raw.setInt("free_tickets", value); }
}