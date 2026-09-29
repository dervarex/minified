package com.dervarex.minified.worlds.manual;

import com.dervarex.minified.worlds.dimension.Dimension;
import com.dervarex.minified.worlds.dimension.End;
import com.dervarex.minified.worlds.dimension.Nether;
import com.dervarex.minified.worlds.dimension.Overworld;
import com.dervarex.minified.worlds.dimension.data.EnderDragonFight;
import com.dervarex.minified.worlds.manual.NbtChecks.Codec;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

final class DimensionChecks {

    private static final Path DIMENSIONS = Path.of("dimensions", "minecraft");
    private static final Path DRAGON_FIGHT = DIMENSIONS.resolve(Path.of("the_end", "data", "minecraft", "ender_dragon_fight.dat"));

    private final Report report;
    private final Path world;
    private final ScratchWorld scratch;

    DimensionChecks(Report report, Path world, ScratchWorld scratch) {
        this.report = report;
        this.world = world;
        this.scratch = scratch;
    }

    void run() throws IOException {
        dimension("Overworld", "overworld", Overworld::new);
        dimension("Nether", "the_nether", Nether::new);
        dimension("End", "the_end", End::new);
        enderDragonFight();
    }

    private void dimension(String name, String folderName, Function<File, Dimension> open) throws IOException {
        report.section(name);
        Path relativeFolder = DIMENSIONS.resolve(folderName);
        Path folder = world.resolve(relativeFolder);
        if (!Files.isDirectory(folder)) {
            report.skip("dimension", "folder not present");
            report.fact(name, "not generated yet");
            return;
        }

        String regionSummary = folderSummary(folder.resolve("region"));
        report.log("region: " + regionSummary);
        report.log("entities: " + folderSummary(folder.resolve("entities")));
        report.log("poi: " + folderSummary(folder.resolve("poi")));

        Dimension dimension = report.load("open", () -> open.apply(world.toFile()));
        if (dimension == null) return;
        ChunkScan scan = report.load("scan chunks", () -> ChunkScan.of(dimension, folder.resolve("region")));
        report.check("close", () -> {
            dimension.close();
            return "";
        });
        if (scan == null) return;
        if (scan.chunks == 0) {
            report.skip("chunk reads", "no chunks found in " + scan.regions + " region files");
            report.fact(name, regionSummary + ", no chunks");
            return;
        }

        report.log(scan.chunks + " chunks read, " + scan.emptySlots + " sampled slots were empty");
        report.log(scan.nonAirBlocks + " of " + scan.blockReads + " blocks are not air ("
                + (100 * scan.nonAirBlocks / Math.max(1, scan.blockReads)) + "%), " + scan.blockNames.size() + " distinct");
        report.list(scan.blockNames);
        report.log(scan.biomeNames.size() + " biomes");
        report.list(scan.biomeNames);
        report.log(scan.entities + " entities (" + scan.livingEntities + " living) in " + scan.chunksWithEntities + " chunks");
        report.list(scan.entityTypes);
        report.log(scan.pois + " points of interest in " + scan.chunksWithPois + " chunks");
        report.list(scan.poiTypes);

        problems(scan, ChunkScan.CHUNKS, scan.chunks + " chunks from " + scan.regions + " regions");
        problems(scan, ChunkScan.BLOCKS, scan.blockReads + " blocks, " + scan.blockNames.size() + " distinct");
        problems(scan, ChunkScan.BIOMES, scan.biomeNames.size() + " distinct");
        problems(scan, ChunkScan.ENTITIES, scan.entities + " entities");
        problems(scan, ChunkScan.POIS, scan.pois + " records");

        report.fact(name, regionSummary + ", " + scan.chunks + " chunks sampled, "
                + scan.blockNames.size() + " block types, " + scan.biomeNames.size() + " biomes, "
                + scan.entities + " entities, " + scan.pois + " pois");

        new ChunkWriteChecks(report, scratch, relativeFolder, open).run(scan);
    }

    private void problems(ChunkScan scan, String category, String summary) {
        report.check("read " + category, () -> {
            List<String> problems = scan.problems(category);
            if (problems.isEmpty()) return summary;
            report.list(problems.stream().map(problem -> "! " + problem).toList());
            throw new AssertionError(problems.size() + " problems, first one: " + problems.get(0));
        });
    }

    private void enderDragonFight() {
        report.section("EnderDragonFight");
        End end = report.load("open End", () -> new End(world.toFile()));
        if (end == null) return;

        end.getEnderDragonFight().ifPresentOrElse(fight -> {
            report.log("dragonKilled=" + fight.isDragonKilled() + " previouslyKilled=" + fight.isPreviouslyKilled()
                    + " needsStateScanning=" + fight.isNeedsStateScanning() + " respawnTime=" + fight.getRespawnTime());
            report.log("dragonUUID=" + Arrays.toString(fight.getDragonUUID()));
            report.log("gateways=" + Arrays.toString(fight.getGateways()));
            report.log("exitPortalLocation=" + Arrays.toString(fight.getExitPortalLocation()));
            report.fact("dragon", fight.isDragonKilled() ? "dead" : fight.isPreviouslyKilled() ? "respawned" : "alive");

            Codec<EnderDragonFight> codec = new Codec<>(EnderDragonFight::toNbt, EnderDragonFight::fromNbt);
            NbtChecks nbt = new NbtChecks(report);
            nbt.roundtrips(fight, codec, world.resolve(DRAGON_FIGHT), scratch.resolve(DRAGON_FIGHT));
            nbt.edit("revive the dragon", fight, codec, scratch.resolve(DRAGON_FIGHT),
                    copy -> {
                        copy.setDragonKilled(false);
                        copy.setRespawnTime(200);
                    },
                    reread -> !reread.isDragonKilled() && reread.getRespawnTime() == 200);
        }, () -> {
            report.skip("parse", "ender_dragon_fight.dat not present");
            report.fact("dragon", "never met");
        });
    }

    private static String folderSummary(Path folder) throws IOException {
        if (!Files.isDirectory(folder)) return "not present";
        try (Stream<Path> files = Files.list(folder)) {
            List<Path> regions = files.filter(file -> file.toString().endsWith(".mca")).toList();
            long bytes = 0;
            for (Path region : regions) {
                bytes += Files.size(region);
            }
            return regions.size() + " region files, " + (bytes / 1024 / 1024) + " MiB";
        }
    }
}
