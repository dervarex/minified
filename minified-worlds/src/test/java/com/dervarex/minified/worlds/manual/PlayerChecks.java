package com.dervarex.minified.worlds.manual;

import com.dervarex.minified.worlds.manual.NbtChecks.Codec;
import com.dervarex.minified.worlds.save.playerdata.Advancements;
import com.dervarex.minified.worlds.save.playerdata.Player;
import com.dervarex.minified.worlds.save.playerdata.Stats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

final class PlayerChecks {

    private static final int MAX_PLAYERS = 10;
    private static final Path PLAYER_DATA = Path.of("players", "data");

    private final Report report;
    private final NbtChecks nbt;
    private final Path world;
    private final ScratchWorld scratch;

    PlayerChecks(Report report, Path world, ScratchWorld scratch) {
        this.report = report;
        this.nbt = new NbtChecks(report);
        this.world = world;
        this.scratch = scratch;
    }

    void run(List<Player> players) throws IOException {
        report.section("Players");
        report.fact("players", players.size());

        List<Path> files = playerFiles();
        report.check("one player per file", () -> {
            if (files.size() != players.size()) {
                throw new AssertionError(files.size() + " files in players/data but " + players.size() + " players parsed");
            }
            return players.size() + " players";
        });
        if (players.isEmpty() || files.size() != players.size()) return;

        for (int i = 0; i < Math.min(players.size(), MAX_PLAYERS); i++) {
            player(players.get(i), files.get(i));
        }
        if (players.size() > MAX_PLAYERS) {
            report.section("Players");
            report.skip("remaining players", (players.size() - MAX_PLAYERS) + " more players not checked, one sample is enough of a crowd");
        }
    }

    private void player(Player player, Path file) {
        String fileUuid = file.getFileName().toString().replace(".dat", "");
        report.section("Player " + fileUuid);
        print(player);

        report.check("UUID matches file name", () -> {
            if (player.getUUID() == null) throw Report.skipped("player has no UUID tag");
            UUID uuid = toUuid(player.getUUID());
            if (!uuid.toString().equals(fileUuid)) throw new AssertionError("UUID tag says " + uuid);
            return "";
        });

        Codec<Player> codec = new Codec<>(Player::toNbt, raw -> Player.fromNbt(raw, null, null));
        Path scratchFile = scratch.resolve(PLAYER_DATA.resolve(file.getFileName()));
        nbt.roundtrips(player, codec, file, scratchFile);
        nbt.edit("change health and xp", player, codec, scratchFile,
                copy -> {
                    copy.setHealth(4.5f);
                    copy.setXPLevel(1337);
                },
                reread -> Float.valueOf(4.5f).equals(reread.getHealth()) && Integer.valueOf(1337).equals(reread.getXPLevel()));

        report.check("advancements roundtrip", () -> {
            Advancements advancements = player.getAdvancements();
            if (advancements == null) throw Report.skipped("no advancements file");
            String json = advancements.toJson().toJson();
            if (!json.equals(Advancements.fromJson(advancements.toJson()).toJson().toJson())) {
                throw new AssertionError("advancements json changed after a roundtrip");
            }
            return advancements.getAdvancements().size() + " entries";
        });
        report.check("stats roundtrip", () -> {
            Stats stats = player.getStatistics();
            if (stats == null) throw Report.skipped("no stats file");
            String json = stats.toJson().toJson();
            if (!json.equals(Stats.fromJson(stats.toJson()).toJson().toJson())) {
                throw new AssertionError("stats json changed after a roundtrip");
            }
            return stats.getCategories().size() + " categories";
        });
    }

    private void print(Player player) {
        report.log("dataVersion=" + player.getDataVersion() + " dimension=" + player.getDimension()
                + " gameType=" + SaveChecks.gameType(player.getPlayerGameType()));
        report.log("position=" + player.getPosition() + " rotation=" + player.getRotation() + " motion=" + player.getMotion());
        report.log("health=" + player.getHealth() + " absorption=" + player.getAbsorptionAmount()
                + " food=" + player.getFoodLevel() + " saturation=" + player.getFoodSaturationLevel()
                + " exhaustion=" + player.getFoodExhaustionLevel());
        report.log("xpLevel=" + player.getXPLevel() + " xpTotal=" + player.getXPTotal() + " xpProgress=" + player.getXpP()
                + " score=" + player.getScore());
        report.log("air=" + player.getAir() + " fire=" + player.getFire() + " fallDistance=" + player.getFallDistance()
                + " selectedSlot=" + player.getSelectedItemSlot() + " seenCredits=" + player.getSeenCredits());

        if (player.getAbilities() != null) {
            report.log("abilities: mayfly=" + player.getAbilities().isMayfly() + " flying=" + player.getAbilities().isFlying()
                    + " instabuild=" + player.getAbilities().isInstabuild() + " invulnerable=" + player.getAbilities().isInvulnerable()
                    + " walkSpeed=" + player.getAbilities().getWalkSpeed() + " flySpeed=" + player.getAbilities().getFlySpeed());
        }
        if (player.getWardenSpawnTracker() != null) {
            report.log("wardenSpawnTracker: warningLevel=" + player.getWardenSpawnTracker().getWarningLevel()
                    + " cooldownTicks=" + player.getWardenSpawnTracker().getCooldownTicks());
        }
        if (player.getInventory() != null) {
            report.log("inventory: " + player.getInventory().getItems().length + " stacks");
        }
        if (player.getAttributes() != null) {
            report.log("attributes: " + player.getAttributes().getAttributes().length);
        }
        if (player.getRecipeBook() != null) {
            report.log("recipeBook: " + player.getRecipeBook().getRecipes().length + " recipes, "
                    + player.getRecipeBook().getToBeDisplayed().length + " to be displayed");
        }
        if (player.getAdvancements() != null) {
            long done = player.getAdvancements().getAdvancements().values().stream()
                    .filter(Advancements.Advancement::isDone)
                    .count();
            report.log("advancements: " + done + " done of " + player.getAdvancements().getAdvancements().size() + " tracked");
        }
        if (player.getStatistics() != null) {
            Stats stats = player.getStatistics();
            report.log("stats: " + stats.getCategories().size() + " categories, play time "
                    + stats.getStat("minecraft:custom", "minecraft:play_time") / 20 / 60 + " min, "
                    + stats.getStat("minecraft:custom", "minecraft:deaths") + " deaths, "
                    + stats.getStat("minecraft:custom", "minecraft:jump") + " jumps");
        }
    }

    private List<Path> playerFiles() throws IOException {
        Path folder = world.resolve(PLAYER_DATA);
        if (!Files.isDirectory(folder)) return List.of();
        try (Stream<Path> files = Files.list(folder)) {
            return files.filter(file -> file.toString().endsWith(".dat"))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        }
    }

    private static UUID toUuid(List<Integer> parts) {
        long most = ((long) parts.get(0) << 32) | (parts.get(1) & 0xFFFFFFFFL);
        long least = ((long) parts.get(2) << 32) | (parts.get(3) & 0xFFFFFFFFL);
        return new UUID(most, least);
    }
}
