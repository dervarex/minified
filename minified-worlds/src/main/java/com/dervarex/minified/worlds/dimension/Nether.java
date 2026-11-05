package com.dervarex.minified.worlds.dimension;

import java.io.File;
import org.apiguardian.api.API;

/**
 * The Nether
 */
@API(status = API.Status.STABLE)
public final class Nether extends Dimension {
    /**
     * Opens the Nether
     * @param worldFolder the world's folder
     */
    public Nether(File worldFolder) {
        super(worldFolder, DimensionType.Nether);
    }
}
