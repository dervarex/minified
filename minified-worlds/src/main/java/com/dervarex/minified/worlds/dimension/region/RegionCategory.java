package com.dervarex.minified.worlds.dimension.region;

import org.apiguardian.api.API;

/**
 * The kinds of region files a dimension has
 */
@API(status = API.Status.STABLE)
public enum RegionCategory {
    /** Blocks and biomes */
    REGION("region"),
    /** Points of interest, like beds and job sites */
    POI("poi"),
    /** Mobs, dropped items, minecarts, ... */
    ENTITIES("entities"),
    /** The dimension's .dat files, like world_border.dat */
    DATA("data");

    public final String folderName;

    RegionCategory(String folderName) {
        this.folderName = folderName;
    }
}