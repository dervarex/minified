package com.dervarex.minified.worlds.world.level;

import com.dervarex.minified.worlds.world.Datapack;

import java.util.UUID;

// level.dat
public class Level {
    Difficulty difficulty;
    boolean hardcore;
    boolean locked;
    Datapack[] enabledDatapacks;
    Datapack[] disabledDatapacks;
    Spawn spawn;
    Version version;
    String[] enabled_features;
    String[] ServerBrands;
    boolean allowCommands;
    int dataVersion;
    int GameType;
    int initialized;
    long lastPlayed;
    String levelName;
    int time;
    int nbtVersion; // named "version" in the nbt, shows which save format the file is, for example Anvil Format
    boolean modded;
    UUID[] singleplayerUuid;
}
// /home/dervarex/Development/temp/minified-worlds-testdata/some world 1234 & üäö °!