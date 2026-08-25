package com.dervarex.minified.worlds;

import com.dervarex.minified.worlds.world.*;
import com.dervarex.minified.worlds.world.data.*;
import com.dervarex.minified.worlds.world.level.Level;
import com.dervarex.minified.worlds.world.playerdata.Player;
import com.dervarex.minified.worlds.world.worlds.End;
import com.dervarex.minified.worlds.world.worlds.Nether;
import com.dervarex.minified.worlds.world.worlds.Overworld;

public class World {
    Level level;
    SessionLock lock;
    Icon icon;
    Raids raids;
    Map[] maps;
    IdCounts idCounts;
    RandomSequences randomSequences;
    ScoreBoard scoreBoard;
    CommandStorage commandStorage;
    Player[] players;
    Overworld overworld;
    Nether nether;
    End end;
}
