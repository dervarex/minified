/**
 * A Minecraft World isn't just a level.dat and some .mca chunk files - it's more:
 *
 * <pre>
 * level.dat           - obviously the most important and well known .dat file, contains world metadata and settings
 * level.dat_old       - backup of level.dat
 * session.lock        - for preventing simultaneous access to a world
 * icon.png            - world icon
 *
 * data/
 *     raids.dat               - status of currently active raids
 *     map_*.dat               - one file per map, for example map_0.dat
 *     idcounts.dat            - counts used id's
 *     random_sequences.dat    - 1.19+, saves the state of random sequences (loot tables, mob drops, ...)
 *     scoreboard.dat          - scoreboard data
 *     command_storage_*.dat   - command storage (for example for functions and datapacks)
 *
 * playerdata/
 *     a31ccf30-00e4-4928-a590-e366c90af710.dat - one .dat file per player, the uuid in the name is the player uuid,
 *                                                 contains inventory, position, xp, effects and so on, additionally
 *                                                 has an .dat_old as backup
 *
 * stats/
 *     contains one .json file (name = uuid), with statistics of the player
 *
 * advancements/
 *     contains one .json file (name = uuid), with advancements of the player
 *
 * datapacks/
 *     contains active datapacks (folders inside of this directory)
 *
 * region/
 *     .mca data of the overworld (chunks)
 *
 * entities/
 *     .mca data for entities (since 1.17)
 *
 * poi/
 *     .mca data for "points of interest"
 *
 * DIM-1/   -  that's the nether
 *     region/
 *         .mca chunk data
 *     data/
 *         map_*.dat - dimension-specific maps
 *
 * DIM1/    -  that's the end
 *     region/
 *         .mca chunk data
 *     data/
 *         map_*.dat - dimension-specific maps
 *
 * todo: remove this here and make a seperate section in the documentation
 * </pre>
 */
package com.dervarex.minified.worlds;