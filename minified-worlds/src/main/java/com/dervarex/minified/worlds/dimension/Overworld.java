package com.dervarex.minified.worlds.dimension;

import java.io.File;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public final class Overworld extends Dimension {

    public Overworld(File worldFolder) {
        super(worldFolder, DimensionType.Overworld);
    }
}
