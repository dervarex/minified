/**
 * A whole world, without the chunks
 * <ul>
 *     <li>{@link com.dervarex.minified.worlds.save.WorldSave}: level.dat, the data folder and the players</li>
 *     <li>{@link com.dervarex.minified.worlds.save.SessionLock}: the session.lock Minecraft uses while the world is open</li>
 * </ul>
 * Read a world with {@link com.dervarex.minified.worlds.save.WorldSave#WorldSave(java.nio.file.Path)},
 * change and write it with {@link com.dervarex.minified.worlds.save.WorldSave#save()}
 */
@API(status = API.Status.STABLE)
package com.dervarex.minified.worlds.save;

import org.apiguardian.api.API;
