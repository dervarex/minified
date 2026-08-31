package com.dervarex.minified.worlds.world.region;

public enum RegionCategory {
    REGION("region"),
    POI("poi"),
    ENTITIES("entities");

    public final String folderName;

    RegionCategory(String folderName) {
        this.folderName = folderName;
    }
}