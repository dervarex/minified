package com.dervarex.minified.worlds.dimension;

public enum DimensionType {
    Overworld("overworld"),
    Nether("the_nether"),
    End("the_end");
    protected final String dimensionName;
    DimensionType(String dimensionName) {
        this.dimensionName = dimensionName;
    }
}