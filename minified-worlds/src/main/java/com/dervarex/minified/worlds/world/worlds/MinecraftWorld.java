package com.dervarex.minified.worlds.world.worlds;

import com.dervarex.minified.utils.nbt.RegionFile;
import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.worlds.chunk.Chunk;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Base Minecraft World.
 * Implemented by {@link Overworld}, {@link Nether} and {@link End}
 */
public class MinecraftWorld {
    private Type type;
    private Versioned<ChunkTicket[]> chunkTickets;
    private Raids raids;
    private Versioned<WorldBorder> worldBorder;
    private final File worldFolder;
    private final Map<Long, RegionFile> regionFiles = new HashMap<>();
    private final Map<Long, RegionFile> poiFiles = new HashMap<>();
    private final Map<Long, RegionFile> entityFiles = new HashMap<>();
    public MinecraftWorld(File worldFolder, Type type) {
        this.worldFolder = worldFolder;
        this.type = type;
    }
    public record Versioned<T>(int dataVersion, T data) {}
    public enum Type {
        Overworld("overworld"),
        Nether("the_nether"),
        End("the_end");
        private final String dimensionName;
        Type(String dimensionName) {
            this.dimensionName = dimensionName;
        }
    }
    public static class ChunkTicket {
        int[] chunkPos; // two integers, chunk x and chunk z (there's no chunk y since chunks range from -64 to the height limit)
        String type; // usually minecraft:forced
        int level;
    }
    public static class WorldBorder {
        int damagePerBlock;
        int centerZ;
        int centerX;
        int lerpTarget;
        int safeZone;
        int size;
        int lerpTime;
        int warningBlocks;
        int warningTime;
    }
    public static class Raids {
        int nextId;
        int tick;
    }
    private static long regionKey(int regionX, int regionZ) {
        return ((long) regionX << 32) | (regionZ & 0xFFFFFFFFL);
    }
    private File dimensionFolder(String category) {
        return new File(
                new File(new File(new File(worldFolder, "dimensions"), "minecraft"), type.dimensionName),
                category
        );
    }
    private RegionFile getRegionFile(Map<Long, RegionFile> cache, String category, int regionX, int regionZ) throws IOException {
        long key = regionKey(regionX, regionZ);
        RegionFile existing = cache.get(key);
        if (existing != null) return existing;
        File file = new File(dimensionFolder(category), "r." + regionX + "." + regionZ + ".mca");
        if (!file.exists()) return null;
        RegionFile opened = RegionFile.open(file);
        cache.put(key, opened);
        return opened;
    }

    /**
     * Like {@link getRegionFile}, but for writing: if the region file doesn't exist yet
     * (e.g. writing into a not generated region), it's created instead of
     * returning null.
     */
    private RegionFile getOrCreateRegionFile(Map<Long, RegionFile> cache, String category, int regionX, int regionZ) throws IOException {
        long key = regionKey(regionX, regionZ);
        RegionFile existing = cache.get(key);
        if (existing != null) return existing;
        File file = new File(dimensionFolder(category), "r." + regionX + "." + regionZ + ".mca");
        RegionFile opened = file.exists() ? RegionFile.open(file) : RegionFile.create(file);
        cache.put(key, opened);
        return opened;
    }

    public NbtCompound readChunkData(int chunkX, int chunkZ) throws IOException {
        RegionFile region = getRegionFile(regionFiles, "region", chunkX >> 5, chunkZ >> 5);
        return region == null ? null : region.readChunk(chunkX, chunkZ);
    }
    public NbtCompound readPoiData(int chunkX, int chunkZ) throws IOException {
        RegionFile poi = getRegionFile(poiFiles, "poi", chunkX >> 5, chunkZ >> 5);
        return poi == null ? null : poi.readChunk(chunkX, chunkZ);
    }
    public NbtCompound readEntityData(int chunkX, int chunkZ) throws IOException {
        RegionFile entities = getRegionFile(entityFiles, "entities", chunkX >> 5, chunkZ >> 5);
        return entities == null ? null : entities.readChunk(chunkX, chunkZ);
    }

    /**
     * Serializes and writes a chunk's current data to its region file, creating
     * the region file if it doesn't exist. The chunk's coordinates
     * (chunk.chunkX()/chunkZ()) determine where it's written.
     */
    public void saveChunkData(Chunk chunk) throws IOException {
        RegionFile region = getOrCreateRegionFile(regionFiles, "region", chunk.chunkX() >> 5, chunk.chunkZ() >> 5);
        region.writeChunk(chunk.chunkX(), chunk.chunkZ(), chunk.raw());
    }

    public void close() throws IOException {
        for (RegionFile r : regionFiles.values()) r.close();
        for (RegionFile r : poiFiles.values()) r.close();
        for (RegionFile r : entityFiles.values()) r.close();
    }
}