/**
 * Blocks and biomes of a chunk
 * <ul>
 *     <li>{@link com.dervarex.minified.worlds.chunk.Chunk}: one 16x16 column of a world, use that for setting and getting blocks across all of its sections</li>
 *     <li>{@link com.dervarex.minified.worlds.chunk.ChunkSection}: a 16x16x16 piece of a chunk with its blocks and biomes</li>
 * </ul>
 * Read a chunk with {@link com.dervarex.minified.worlds.dimension.Dimension#readChunk(int, int)},
 * change and write it with {@link com.dervarex.minified.worlds.dimension.Dimension#saveChunkData(Chunk)}
 */
@API(status = API.Status.STABLE)
package com.dervarex.minified.worlds.chunk;

import org.apiguardian.api.API;
