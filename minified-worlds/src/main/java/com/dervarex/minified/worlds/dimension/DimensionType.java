package com.dervarex.minified.worlds.dimension;

import java.io.File;

public enum DimensionType {
    Overworld("overworld"),
    Nether("the_nether"),
    End("the_end");

    protected final String dimensionName;

    DimensionType(String dimensionName) {
        this.dimensionName = dimensionName;
    }

    /**
     * @param worldFolder world folder
     * @return {@link DimensionType} based on the folder name
     */
    public static DimensionType fromFolder(File worldFolder) {
        for (DimensionType type : values()) {
            if (type.dimensionName.equalsIgnoreCase(worldFolder.getName())) {
                return type;
            }
        }
        return null;
    }
}