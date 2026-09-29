package com.dervarex.minified.worlds.manual;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.worlds.block.BlockState;
import com.dervarex.minified.worlds.chunk.Chunk;
import com.dervarex.minified.worlds.chunk.ChunkSection;
import com.dervarex.minified.worlds.dimension.Dimension;
import com.dervarex.minified.worlds.entity.Entity;
import com.dervarex.minified.worlds.entity.EntityData;
import com.dervarex.minified.worlds.entity.LivingEntity;
import com.dervarex.minified.worlds.manual.ChunkScan.ChunkPos;
import com.dervarex.minified.worlds.poi.PoiData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

final class ChunkWriteChecks {

    private static final BlockState LOG = new BlockState("minecraft:oak_log", Map.of("axis", "x"));
    private static final BlockState GLASS = BlockState.of("minecraft:glass");
    private static final String VOID = "minecraft:the_void";

    @FunctionalInterface
    private interface DimensionAction<T> {
        T apply(Dimension dimension) throws IOException;
    }

    private final Report report;
    private final ScratchWorld scratch;
    private final Path dimensionFolder;
    private final Function<File, Dimension> open;

    ChunkWriteChecks(Report report, ScratchWorld scratch, Path dimensionFolder, Function<File, Dimension> open) {
        this.report = report;
        this.scratch = scratch;
        this.dimensionFolder = dimensionFolder;
        this.open = open;
    }

    void run(ChunkScan scan) throws IOException {
        ChunkPos pos = scan.readableChunks.get(0);
        for (String category : List.of("region", "entities", "poi")) {
            Files.createDirectories(scratch.resolve(dimensionFolder.resolve(category)));
        }
        scratch.copy(pos.regionFile(dimensionFolder, "region"));
        report.log("write tests use " + pos + " on the scratch copy");

        rewriteUnchanged(pos);
        setBlocks(pos);
        createSection(pos);
        setBiome(pos);
        growChunk(pos, scan.readableChunks);
        newRegionFile(pos);
        entities(scan.firstEntityChunk);
        pointsOfInterest(scan.firstPoiChunk != null ? scan.firstPoiChunk : pos);
    }

    private void rewriteUnchanged(ChunkPos pos) {
        report.check("rewrite chunk unchanged", () -> {
            NbtCompound before = withDimension(dimension -> {
                Chunk chunk = dimension.readChunk(pos.x(), pos.z());
                dimension.saveChunkData(chunk);
                return chunk.raw();
            });
            return NbtDiff.between(before, readRaw(pos)).orFail();
        });
    }

    private void setBlocks(ChunkPos pos) {
        report.check("set blocks", () -> {
            Chunk chunk = readChunk(pos);
            List<ChunkSection> sections = blockSections(chunk);
            int minY = sections.getFirst().sectionY() * 16;
            int maxY = sections.getLast().sectionY() * 16 + 15;
            int lowY = minY + 5;
            int highY = maxY - 5;

            List<BlockState> before = blocks(chunk, minY, maxY);
            chunk.setBlock(7, lowY, 7, LOG);
            chunk.setBlock(7, highY, 7, LOG);
            save(chunk);
            Chunk reloaded = readChunk(pos);

            int index = 0;
            int mismatches = 0;
            String firstMismatch = null;
            for (int y = minY; y <= maxY; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        boolean edited = x == 7 && z == 7 && (y == lowY || y == highY);
                        BlockState expected = edited ? LOG : before.get(index);
                        BlockState actual = reloaded.getBlock(x, y, z);
                        if (!expected.equals(actual)) {
                            mismatches++;
                            if (firstMismatch == null) firstMismatch = x + "," + y + "," + z + " is " + actual + " instead of " + expected;
                        }
                        index++;
                    }
                }
            }
            if (mismatches > 0) throw new AssertionError(mismatches + " blocks wrong, e.g. " + firstMismatch);
            return "2 logs placed at y=" + lowY + " and y=" + highY + ", " + (index - 2) + " other blocks untouched";
        });
    }

    private void createSection(ChunkPos pos) {
        report.check("create section", () -> {
            Chunk chunk = readChunk(pos);
            ChunkSection top = blockSections(chunk).getLast();
            int sectionY = top.sectionY() + 1;
            int y = sectionY * 16;

            chunk.setBlock(0, y, 0, GLASS);
            save(chunk);
            Chunk reloaded = readChunk(pos);

            if (!GLASS.equals(reloaded.getBlock(0, y, 0))) {
                throw new AssertionError("expected glass at y=" + y + " but found " + reloaded.getBlock(0, y, 0));
            }
            long copies = sectionTags(reloaded).stream().filter(tag -> tag.getByte("Y") == sectionY).count();
            if (copies != 1) {
                throw new AssertionError("section Y=" + sectionY + " is in the chunk " + copies + " times");
            }
            String biome = new ChunkSection(blockSectionTag(reloaded, sectionY)).getBiome(0, y, 0);
            if (!top.predominantBiome().equals(biome)) {
                throw new AssertionError("new section got biome " + biome + ", the one below is " + top.predominantBiome());
            }
            return "section " + sectionY + " inherited " + biome;
        });
    }

    private void setBiome(ChunkPos pos) {
        report.check("set biome", () -> {
            Chunk chunk = readChunk(pos);
            ChunkSection section = new ChunkSection(blockSectionTag(chunk, blockSections(chunk).getFirst().sectionY()));
            int baseY = section.sectionY() * 16;
            if (section.getBiome(0, baseY, 0) == null) throw Report.skipped("section has no biomes");

            List<String> before = biomes(section, baseY);
            section.setBiome(0, baseY, 0, VOID);
            save(chunk);
            List<String> after = biomes(new ChunkSection(blockSectionTag(readChunk(pos), section.sectionY())), baseY);

            List<String> expected = new ArrayList<>(before);
            expected.set(0, VOID);
            if (!expected.equals(after)) throw new AssertionError("biomes after save: " + after);
            return "one cell of section " + section.sectionY() + " is now the void, 63 others untouched";
        });
    }

    // pads a chunk with junk until it no longer fits its old sectors, the region file has to move it somewhere else
    private void growChunk(ChunkPos pos, List<ChunkPos> readable) {
        report.check("grow chunk beyond its sectors", () -> {
            List<ChunkPos> neighbours = readable.stream()
                    .filter(other -> !other.equals(pos) && other.x() >> 5 == pos.x() >> 5 && other.z() >> 5 == pos.z() >> 5)
                    .toList();
            List<NbtCompound> neighboursBefore = new ArrayList<>();
            for (ChunkPos neighbour : neighbours) {
                neighboursBefore.add(readRaw(neighbour));
            }

            long[] junk = new Random(42).longs(16_384).toArray();
            Chunk chunk = readChunk(pos);
            chunk.raw().setLongArray("minified_padding", junk);
            save(chunk);

            if (!Arrays.equals(junk, readRaw(pos).getLongArray("minified_padding"))) {
                throw new AssertionError("padding came back different");
            }
            for (int i = 0; i < neighbours.size(); i++) {
                NbtDiff diff = NbtDiff.between(neighboursBefore.get(i), readRaw(neighbours.get(i)));
                if (!diff.identical()) throw new AssertionError(neighbours.get(i) + " got damaged: " + diff.summary());
            }
            return "128 KiB padding, " + neighbours.size() + " neighbours in the region survived";
        });
    }

    private void newRegionFile(ChunkPos pos) {
        report.check("write into a new region file", () -> {
            ChunkPos far = new ChunkPos(pos.x() + 32 * 1000, pos.z() + 32 * 1000);
            Path file = scratch.resolve(far.regionFile(dimensionFolder, "region"));
            if (Files.exists(file)) throw new AssertionError(file + " should not exist yet");

            NbtCompound raw = readRaw(pos);
            raw.setInt("xPos", far.x());
            raw.setInt("zPos", far.z());
            save(new Chunk(raw));

            if (!Files.exists(file)) throw new AssertionError(file + " was not created");
            NbtDiff.between(raw, readRaw(far)).orFail();
            return Files.size(file) / 1024 + " KiB " + file.getFileName();
        });
    }

    private void entities(ChunkPos pos) throws IOException {
        if (pos == null) {
            report.skip("edit and add entities", "no entities in the scanned chunks");
            return;
        }
        scratch.copy(pos.regionFile(dimensionFolder, "entities"));
        report.check("edit and add entities", () -> {
            EntityData data = withDimension(dimension -> dimension.readEntities(pos.x(), pos.z()));
            Entity first = data.entities().getFirst();
            UUID firstUuid = first.uuid();
            first.setInvulnerable(true);
            first.setFire((short) 42);
            if (first instanceof LivingEntity living) living.setHealth(1.5f);

            NbtCompound armorStand = new NbtCompound();
            armorStand.setString("id", "minecraft:armor_stand");
            Entity added = data.addEntity(armorStand);
            UUID addedUuid = UUID.randomUUID();
            added.setUuid(addedUuid);
            added.setPos(pos.x() * 16 + 8.5, 64, pos.z() * 16 + 8.5);
            added.setMotion(0, 0, 0);
            added.setRotation(90f, 0f);

            withDimension(dimension -> {
                dimension.saveEntityData(pos.x(), pos.z(), data);
                return null;
            });
            List<Entity> reread = withDimension(dimension -> dimension.readEntities(pos.x(), pos.z())).entities();

            if (reread.size() != data.entities().size()) {
                throw new AssertionError(reread.size() + " entities after saving, expected " + data.entities().size());
            }
            Entity firstAgain = find(reread, firstUuid);
            if (!firstAgain.invulnerable() || firstAgain.fire() != 42) throw new AssertionError(first.id() + " lost its edits");
            if (firstAgain instanceof LivingEntity living && living.health() != 1.5f) {
                throw new AssertionError(first.id() + " has " + living.health() + " health instead of 1.5");
            }
            if (!Arrays.equals(added.pos(), find(reread, addedUuid).pos())) throw new AssertionError("armor stand moved");
            return "edited a " + first.id() + ", added an armor stand, " + reread.size() + " entities now";
        });
    }

    private void pointsOfInterest(ChunkPos pos) throws IOException {
        scratch.copy(pos.regionFile(dimensionFolder, "poi"));
        report.check("add point of interest", () -> {
            PoiData existing = withDimension(dimension -> dimension.readPoi(pos.x(), pos.z()));
            PoiData data = existing != null ? existing : emptyPoiData();

            int[] at = {pos.x() * 16 + 3, 64, pos.z() * 16 + 3};
            data.addRecord(at[1] >> 4, at[0], at[1], at[2], "minecraft:home", 0);
            int expected = data.records().size();
            withDimension(dimension -> {
                dimension.savePoiData(pos.x(), pos.z(), data);
                return null;
            });

            PoiData reread = withDimension(dimension -> dimension.readPoi(pos.x(), pos.z()));
            if (reread.records().size() != expected) {
                throw new AssertionError(reread.records().size() + " records after saving, expected " + expected);
            }
            boolean found = reread.records(at[1] >> 4).stream()
                    .anyMatch(record -> "minecraft:home".equals(record.type()) && Arrays.equals(at, record.pos()));
            if (!found) throw new AssertionError("the new bed went missing");
            return expected + " records" + (existing == null ? " in a brand new poi chunk" : "");
        });
    }

    private <T> T withDimension(DimensionAction<T> action) throws IOException {
        Dimension dimension = open.apply(scratch.root().toFile());
        try {
            return action.apply(dimension);
        } finally {
            dimension.close();
        }
    }

    private Chunk readChunk(ChunkPos pos) throws IOException {
        return withDimension(dimension -> dimension.readChunk(pos.x(), pos.z()));
    }

    private NbtCompound readRaw(ChunkPos pos) throws IOException {
        return withDimension(dimension -> dimension.readChunkData(pos.x(), pos.z()));
    }

    private void save(Chunk chunk) throws IOException {
        withDimension(dimension -> {
            dimension.saveChunkData(chunk);
            return null;
        });
    }

    private static List<NbtCompound> sectionTags(Chunk chunk) {
        return chunk.raw().getList("sections").elements().stream().map(NbtCompound.class::cast).toList();
    }

    private static List<ChunkSection> blockSections(Chunk chunk) {
        return sectionTags(chunk).stream()
                .filter(tag -> tag.has("block_states"))
                .map(ChunkSection::new)
                .sorted(Comparator.comparingInt(ChunkSection::sectionY))
                .toList();
    }

    private static NbtCompound blockSectionTag(Chunk chunk, int sectionY) {
        return sectionTags(chunk).stream()
                .filter(tag -> tag.has("block_states") && tag.getByte("Y") == sectionY)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no section Y=" + sectionY));
    }

    private static List<BlockState> blocks(Chunk chunk, int minY, int maxY) {
        List<BlockState> blocks = new ArrayList<>();
        for (int y = minY; y <= maxY; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    blocks.add(chunk.getBlock(x, y, z));
                }
            }
        }
        return blocks;
    }

    private static List<String> biomes(ChunkSection section, int baseY) {
        List<String> biomes = new ArrayList<>();
        for (int y = 0; y < 16; y += 4) {
            for (int z = 0; z < 16; z += 4) {
                for (int x = 0; x < 16; x += 4) {
                    biomes.add(section.getBiome(x, baseY + y, z));
                }
            }
        }
        return biomes;
    }

    private static Entity find(List<Entity> entities, UUID uuid) {
        return entities.stream()
                .filter(entity -> entity.uuid().equals(uuid))
                .findFirst()
                .orElseThrow(() -> new AssertionError("entity " + uuid + " vanished"));
    }

    private static PoiData emptyPoiData() {
        NbtCompound root = new NbtCompound();
        root.setCompound("Sections", new NbtCompound());
        return new PoiData(root);
    }
}
