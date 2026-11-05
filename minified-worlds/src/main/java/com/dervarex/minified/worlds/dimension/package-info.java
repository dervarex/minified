/**
 * The dimensions of a minecraft world. <br>
 * Vanilla Dimensions:
 * <ul>
 *     <li>{@link com.dervarex.minified.worlds.dimension.Overworld}, {@link com.dervarex.minified.worlds.dimension.Nether}
 *     and {@link com.dervarex.minified.worlds.dimension.End}</li>
 * </ul>
 *
 * Terrain is split into chunks (16x16 columns). Chunks are Split into Sections (16x16x16).
 * Chunk coordinates are basically block coordinates divided by 16, rounded down, block (35, 70, -12) is in chunk (2, -1) for example <br>
 * 32x32 chunks share one region file, like r.0.0.mca, for each {@link com.dervarex.minified.worlds.dimension.region.RegionCategory}
 * <br><br>
 * {@link com.dervarex.minified.worlds.dimension.Dimension} for reading and writing chunks, POIs and entities
 */
@API(status = API.Status.STABLE)
package com.dervarex.minified.worlds.dimension;

import org.apiguardian.api.API;
