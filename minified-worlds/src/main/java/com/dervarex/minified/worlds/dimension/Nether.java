package com.dervarex.minified.worlds.dimension;

import java.io.File;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public final class Nether extends Dimension {
    public Nether(File worldFolder) {
        super(worldFolder, DimensionType.Nether);
    }
}
