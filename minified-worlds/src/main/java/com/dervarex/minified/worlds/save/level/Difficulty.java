package com.dervarex.minified.worlds.save.level;

import org.apiguardian.api.API;

/**
 * The difficulty of a world
 */
@API(status = API.Status.STABLE)
public enum Difficulty {
    /** No hostile mobs, and hunger doesn't drain */
    peaceful,
    /** Mobs deal less damage, hunger drains slowly */
    easy,
    /** The default */
    normal,
    /** Mobs deal more damage, hunger drains faster */
    hard
    // no hardcore here, since hardcore is hard mode + additional field, not a separate Difficulty, even if Minecraft sometime treats it as one
}
