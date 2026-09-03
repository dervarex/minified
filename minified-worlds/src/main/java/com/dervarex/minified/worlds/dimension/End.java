package com.dervarex.minified.worlds.dimension;

import com.dervarex.minified.utils.nbt.Parser;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.dervarex.minified.worlds.world.worlds.EnderDragonFight;
import org.jetbrains.annotations.Nullable;

public class End extends Dimension {

    @Nullable
    private final EnderDragonFight enderDragonFight;

    public End(File worldFolder) {
        super(worldFolder, DimensionType.End);

        File fightFile = new File(
                new File(dimensionFolder("data"), "minecraft"),
                "ender_dragon_fight.dat"
        );

        try {
            this.enderDragonFight = fightFile.exists()
                    ? EnderDragonFight.fromNbt(Parser.readFile(fightFile))
                    : null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<EnderDragonFight> getEnderDragonFight() {
        return Optional.ofNullable(enderDragonFight);
    }
}