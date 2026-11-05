/**
 * Reading and writing Minecraft worlds, 26.1 and above only
 * <br><br>
 * Start with {@link com.dervarex.minified.worlds.save.WorldSave} for level.dat, the data folder and the players, and with
 * {@link com.dervarex.minified.worlds.dimension.Overworld}, {@link com.dervarex.minified.worlds.dimension.Nether} or {@link com.dervarex.minified.worlds.dimension.End}
 * for chunks, entities and POIs. World folder structure:
 * <pre class="text">
 * someworld/
 * ├── level.dat                   world settings
 * ├── level.dat_old               backup of level.dat
 * ├── session.lock                taken while the world is open
 * ├── icon.png
 * ├── data/minecraft/
 * │   ├── custom_boss_events.dat  boss bars
 * │   ├── game_rules.dat
 * │   ├── random_sequences.dat    for blocks that need the same randomness every time, like spawners
 * │   ├── scheduled_events.dat    functions scheduled with /schedule, not read yet
 * │   ├── scoreboard.dat
 * │   ├── stopwatches.dat         /stopwatch, in real time instead of ticks
 * │   ├── wandering_trader.dat
 * │   ├── weather.dat
 * │   ├── world_clocks.dat
 * │   └── world_gen_settings.dat  the seed and the generators
 * ├── datapacks/
 * ├── dimensions/minecraft/
 * │   ├── overworld/
 * │   │   ├── data/minecraft/     chunk_tickets.dat, raids.dat and world_border.dat
 * │   │   ├── entities/
 * │   │   ├── poi/
 * │   │   └── region/
 * │   ├── the_nether/             same as overworld
 * │   └── the_end/                same as overworld, plus ender_dragon_fightdat
 * └── players/
 *     ├── advancements/&lt;uuid&gt;.json
 *     ├── data/&lt;uuid&gt;.dat      inventory, position, xp and so on, with a .dat_old backup
 *     └── stats/&lt;uuid&gt;.json
 * </pre>
 */
@API(status = API.Status.STABLE, since = "v3.1.0")
package com.dervarex.minified.worlds;

import org.apiguardian.api.API;