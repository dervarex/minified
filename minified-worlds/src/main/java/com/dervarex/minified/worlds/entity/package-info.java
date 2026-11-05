/**
 * Entities of a chunk, from the entities folder
 * <ul>
 *     <li>{@link com.dervarex.minified.worlds.entity.EntityData}: all entities of one chunk</li>
 *     <li>{@link com.dervarex.minified.worlds.entity.Entity}: the fields every entity has</li>
 *     <li>{@link com.dervarex.minified.worlds.entity.LivingEntity}: the fields every living entity has</li>
 * </ul>
 * Read them with {@link com.dervarex.minified.worlds.dimension.Dimension#readEntities(int, int)},
 * change and write them with {@link com.dervarex.minified.worlds.dimension.Dimension#saveEntityData(int, int, EntityData)}
 */
@API(status = API.Status.STABLE)
package com.dervarex.minified.worlds.entity;

import org.apiguardian.api.API;
