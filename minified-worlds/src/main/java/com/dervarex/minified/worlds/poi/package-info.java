/**
 * POIs (points of interest) of a chunk, from the poi folder
 * <ul>
 *     <li>{@link com.dervarex.minified.worlds.poi.PoiData}: all POIs of one chunk</li>
 *     <li>{@link com.dervarex.minified.worlds.poi.PoiRecord}: one POI</li>
 * </ul>
 * Read them with {@link com.dervarex.minified.worlds.dimension.Dimension#readPoi(int, int)},
 * change and write them with {@link com.dervarex.minified.worlds.dimension.Dimension#savePoiData(int, int, PoiData)}
 */
@API(status = API.Status.STABLE)
package com.dervarex.minified.worlds.poi;

import org.apiguardian.api.API;
