package com.dervarex.minified.worlds.dimension.data;

public class ChunkTicket {
    int[] chunkPos; // two integers, chunk x and chunk z (there's no chunk y since chunks range from -64 to the height limit)
    String type; // usually minecraft:forced
    int level;
}