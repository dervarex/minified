package com.dervarex.minified.worlds.manual;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtTag;
import com.dervarex.minified.worlds.block.BlockState;
import com.dervarex.minified.worlds.chunk.Chunk;
import com.dervarex.minified.worlds.chunk.ChunkSection;
import com.dervarex.minified.worlds.dimension.Dimension;
import com.dervarex.minified.worlds.entity.Entity;
import com.dervarex.minified.worlds.entity.EntityData;
import com.dervarex.minified.worlds.entity.LivingEntity;
import com.dervarex.minified.worlds.poi.PoiData;
import com.dervarex.minified.worlds.poi.PoiRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

final class ChunkScan {

    static final String CHUNKS = "chunks";
    static final String BLOCKS = "blocks";
    static final String BIOMES = "biomes";
    static final String ENTITIES = "entities";
    static final String POIS = "points of interest";

    private static final int MAX_REGIONS = 16;
    private static final int GRID = 5;

    record ChunkPos(int x, int z) {
        Path regionFile(Path dimensionFolder, String category) {
            return dimensionFolder.resolve(category).resolve("r." + (x >> 5) + "." + (z >> 5) + ".mca");
        }

        @Override
        public String toString() {
            return "chunk(" + x + "," + z + ")";
        }
    }

    int regions;
    int slots;
    int chunks;
    int emptySlots;
    long blockReads;
    long nonAirBlocks;
    int entities;
    int livingEntities;
    int chunksWithEntities;
    int pois;
    int chunksWithPois;
    final Set<String> blockNames = new TreeSet<>();
    final Set<String> biomeNames = new TreeSet<>();
    final Set<String> entityTypes = new TreeSet<>();
    final Set<String> poiTypes = new TreeSet<>();
    final List<ChunkPos> readableChunks = new ArrayList<>();
    ChunkPos firstEntityChunk;
    ChunkPos firstPoiChunk;

    private final Map<String, List<String>> problems = new LinkedHashMap<>();

    static ChunkScan of(Dimension dimension, Path regionFolder) throws IOException {
        ChunkScan scan = new ChunkScan();
        if (!Files.isDirectory(regionFolder)) return scan;

        List<Path> regionFiles;
        try (Stream<Path> files = Files.list(regionFolder)) {
            regionFiles = files.filter(file -> file.getFileName().toString().matches("r\\.-?\\d+\\.-?\\d+\\.mca"))
                    .sorted()
                    .limit(MAX_REGIONS)
                    .toList();
        }

        for (Path regionFile : regionFiles) {
            String[] parts = regionFile.getFileName().toString().split("\\.");
            int regionX = Integer.parseInt(parts[1]);
            int regionZ = Integer.parseInt(parts[2]);
            scan.regions++;

            // a 5x5 grid spread over the 32x32 region, enough to notice when something is off without reading all 1024
            for (int gridX = 0; gridX < GRID; gridX++) {
                for (int gridZ = 0; gridZ < GRID; gridZ++) {
                    int step = 32 / GRID;
                    scan.chunk(dimension, new ChunkPos((regionX << 5) + gridX * step + step / 2, (regionZ << 5) + gridZ * step + step / 2));
                }
            }
        }
        return scan;
    }

    List<String> problems(String category) {
        return problems.getOrDefault(category, List.of());
    }

    private void chunk(Dimension dimension, ChunkPos pos) {
        slots++;
        Chunk chunk;
        try {
            chunk = dimension.readChunk(pos.x(), pos.z());
        } catch (Exception e) {
            problem(CHUNKS, pos + ": " + e);
            return;
        }
        if (chunk == null) {
            emptySlots++;
            return;
        }
        chunks++;
        if (chunk.chunkX() != pos.x() || chunk.chunkZ() != pos.z()) {
            problem(CHUNKS, pos + " claims to be chunk(" + chunk.chunkX() + "," + chunk.chunkZ() + ")");
        }
        readableChunks.add(pos);

        for (NbtTag tag : chunk.raw().getList("sections").elements()) {
            NbtCompound sectionTag = (NbtCompound) tag;
            if (sectionTag.has("block_states")) {
                section(chunk, new ChunkSection(sectionTag), pos);
            }
        }
        entities(dimension, pos);
        pois(dimension, pos);
    }

    private void section(Chunk chunk, ChunkSection section, ChunkPos pos) {
        int baseY = section.sectionY() * 16;
        try {
            for (int y = baseY; y < baseY + 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        BlockState state = chunk.getBlock(x, y, z);
                        blockReads++;
                        if (state == null || state.name() == null) {
                            throw new IllegalStateException("no block state at " + x + "," + y + "," + z);
                        }
                        if (!state.name().endsWith(":air")) {
                            nonAirBlocks++;
                            blockNames.add(state.name());
                        }
                    }
                }
            }
        } catch (Exception e) {
            problem(BLOCKS, pos + " section " + section.sectionY() + ": " + e);
        }

        try {
            for (int y = baseY; y < baseY + 16; y += 4) {
                for (int z = 0; z < 16; z += 4) {
                    for (int x = 0; x < 16; x += 4) {
                        String biome = section.getBiome(x, y, z);
                        if (biome == null) throw new IllegalStateException("no biome at " + x + "," + y + "," + z);
                        biomeNames.add(biome);
                    }
                }
            }
            if (section.predominantBiome() == null) throw new IllegalStateException("no predominant biome");
        } catch (Exception e) {
            problem(BIOMES, pos + " section " + section.sectionY() + ": " + e);
        }
    }

    private void entities(Dimension dimension, ChunkPos pos) {
        try {
            EntityData data = dimension.readEntities(pos.x(), pos.z());
            if (data == null || data.entities().isEmpty()) return;

            chunksWithEntities++;
            if (firstEntityChunk == null) firstEntityChunk = pos;
            for (Entity entity : data.entities()) {
                entities++;
                entityTypes.add(entity.id());
                entity.uuid();
                entity.pos();
                entity.motion();
                entity.rotation();
                if (entity instanceof LivingEntity living) {
                    livingEntities++;
                    living.health();
                }
            }
        } catch (Exception e) {
            problem(ENTITIES, pos + ": " + e);
        }
    }

    private void pois(Dimension dimension, ChunkPos pos) {
        try {
            PoiData data = dimension.readPoi(pos.x(), pos.z());
            if (data == null || data.records().isEmpty()) return;

            chunksWithPois++;
            if (firstPoiChunk == null) firstPoiChunk = pos;
            for (PoiRecord record : data.records()) {
                pois++;
                poiTypes.add(record.type());
                int[] at = record.pos();
                if (at.length != 3 || at[0] >> 4 != pos.x() || at[2] >> 4 != pos.z()) {
                    problem(POIS, pos + " has a " + record.type() + " outside of it at " + Arrays.toString(at));
                }
            }
        } catch (Exception e) {
            problem(POIS, pos + ": " + e);
        }
    }

    private void problem(String category, String message) {
        problems.computeIfAbsent(category, key -> new ArrayList<>()).add(message);
    }
}
