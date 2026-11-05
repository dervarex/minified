package com.dervarex.minified.worlds.dimension;

import java.io.File;
import org.apiguardian.api.API;

/**
 * Which vanilla dimension type the {@link Dimension} is
 */
@API(status = API.Status.STABLE)
public enum DimensionType {
    Overworld("overworld"),
    Nether("the_nether"),
    End("the_end");

    protected final String dimensionName;

    DimensionType(String dimensionName) {
        this.dimensionName = dimensionName;
    }

    /**
     * @param dimensionFolder the dimension's folder
     * @return {@link DimensionType} based on the folder name, null if it isn't from vanilla
     */
    public static DimensionType fromFolder(File dimensionFolder) {
        for (DimensionType type : values()) {
            if (type.dimensionName.equalsIgnoreCase(dimensionFolder.getName())) {
                return type;
            }
        }
        return null;
    }
}