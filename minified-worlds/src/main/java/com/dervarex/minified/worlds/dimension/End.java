package com.dervarex.minified.worlds.dimension;

import com.dervarex.minified.utils.nbt.Parser;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.dervarex.minified.worlds.dimension.region.RegionCategory;
import com.dervarex.minified.worlds.dimension.data.EnderDragonFight;
import com.dervarex.minified.worlds.exceptions.FailedToLoadWorldException;
import org.jetbrains.annotations.Nullable;
import org.apiguardian.api.API;

/**
 * The End dimension with its ender dragon fight
 */
@API(status = API.Status.STABLE)
public final class End extends Dimension {

    @Nullable
    private final EnderDragonFight enderDragonFight;

    /**
     * Opens the End and reads its ender dragon fight
     * @param worldFolder the world's folder
     * @throws FailedToLoadWorldException if the ender dragon fight can't be read
     */
    public End(File worldFolder) {
        super(worldFolder, DimensionType.End);

        File fightFile = new File(
                new File(dimensionFolder(RegionCategory.DATA), "minecraft"),
                "ender_dragon_fight.dat"
        );

        try {
            this.enderDragonFight = fightFile.exists()
                    ? EnderDragonFight.fromNbt(Parser.readFile(fightFile))
                    : null;
        } catch (IOException e) {
            throw new FailedToLoadWorldException("Failed to read " + fightFile, fightFile.toPath(), e);
        }
    }

    /** {@return the ender dragon fight} */
    public Optional<EnderDragonFight> getEnderDragonFight() {
        return Optional.ofNullable(enderDragonFight);
    }
}