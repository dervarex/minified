/**
 * The players of a world, from the players folder
 * <ul>
 *     <li>{@link com.dervarex.minified.worlds.save.playerdata.Player}: one player, with everything below</li>
 *     <li>{@link com.dervarex.minified.worlds.save.playerdata.Abilities}: what the player is allowed to do</li>
 *     <li>{@link com.dervarex.minified.worlds.save.playerdata.Advancements}: the advancements</li>
 *     <li>{@link com.dervarex.minified.worlds.save.playerdata.Attributes}: the attributes</li>
 *     <li>{@link com.dervarex.minified.worlds.save.playerdata.Inventory}: the inventory</li>
 *     <li>{@link com.dervarex.minified.worlds.save.playerdata.RecipeBook}: the unlocked recipes</li>
 *     <li>{@link com.dervarex.minified.worlds.save.playerdata.Stats}: the statistics</li>
 *     <li>{@link com.dervarex.minified.worlds.save.playerdata.WardenSpawnTracker}: how close the player is to summoning a warden</li>
 * </ul>
 * Read them with {@link com.dervarex.minified.worlds.save.WorldSave}, change them and write them with {@link com.dervarex.minified.worlds.save.WorldSave#save()}
 */
@API(status = API.Status.STABLE)
package com.dervarex.minified.worlds.save.playerdata;

import org.apiguardian.api.API;
