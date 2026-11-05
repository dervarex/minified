package com.dervarex.minified.worlds.dimension;

import java.io.File;
import org.apiguardian.api.API;

/**
 * The Overworld
 */
@API(status = API.Status.STABLE)
public final class Overworld extends Dimension {

    /**
     * Opens the Overworld
     * @param worldFolder the world's folder
     */
    public Overworld(File worldFolder) {
        super(worldFolder, DimensionType.Overworld);
    }
}
