package com.dervarex.minified.worlds.dimension.region;

import org.apiguardian.api.API;

/**
 * The kinds of region files a dimension has
 */
@API(status = API.Status.STABLE)
public enum RegionCategory {
    REGION("region"),
    POI("poi"),
    ENTITIES("entities"),
    DATA("data");

    public final String folderName;

    RegionCategory(String folderName) {
        this.folderName = folderName;
    }
}