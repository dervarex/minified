package com.dervarex.minified.worlds.dimension.region;

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