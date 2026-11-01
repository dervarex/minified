package com.dervarex.minified.worlds.dimension.region;

import org.apiguardian.api.API;

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