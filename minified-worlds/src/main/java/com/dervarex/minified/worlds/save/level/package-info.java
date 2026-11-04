/**
 * A world's level.dat
 * <ul>
 *     <li>{@link com.dervarex.minified.worlds.save.level.Level}: the whole file</li>
 *     <li>{@link com.dervarex.minified.worlds.save.level.Spawn}: the world spawn</li>
 *     <li>{@link com.dervarex.minified.worlds.save.level.Version}: the version the world was last played in</li>
 *     <li>{@link com.dervarex.minified.worlds.save.level.Difficulty}: the difficulty</li>
 * </ul>
 * Read it with {@link com.dervarex.minified.worlds.save.WorldSave}, change it and write it with {@link com.dervarex.minified.worlds.save.WorldSave#save()}
 */
@API(status = API.Status.STABLE)
package com.dervarex.minified.worlds.save.level;

import org.apiguardian.api.API;
