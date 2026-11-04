package com.dervarex.minified.worlds.save.level;

import org.apiguardian.api.API;

/**
 * The difficulty of a world
 */
@API(status = API.Status.STABLE)
public enum Difficulty {
    peaceful,
    easy,
    normal,
    hard
    // no hardcore here, since hardcore is hard mode + additional field, not a separate Difficulty, even if Minecraft sometime treats it as one
}
