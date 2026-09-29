package com.dervarex.minified.worlds.manual;

import com.dervarex.minified.worlds.manual.NbtChecks.Codec;
import com.dervarex.minified.worlds.save.WorldSave;
import com.dervarex.minified.worlds.save.data.BossEvent;
import com.dervarex.minified.worlds.save.data.CustomBossEvents;
import com.dervarex.minified.worlds.save.data.GameRules;
import com.dervarex.minified.worlds.save.data.RandomSequences;
import com.dervarex.minified.worlds.save.data.Scoreboard;
import com.dervarex.minified.worlds.save.data.StopWatches;
import com.dervarex.minified.worlds.save.data.WanderingTrader;
import com.dervarex.minified.worlds.save.data.Weather;
import com.dervarex.minified.worlds.save.data.WorldClocks;
import com.dervarex.minified.worlds.save.data.WorldGenSettings;
import com.dervarex.minified.worlds.save.level.Level;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

final class SaveChecks {

    private static final String TEST_ID = "minified:manual_test";
    private static final Path LEVEL_DAT = Path.of("level.dat");
    private static final Path DATA = Path.of("data", "minecraft");

    private final Report report;
    private final NbtChecks nbt;
    private final Path world;
    private final ScratchWorld scratch;

    SaveChecks(Report report, Path world, ScratchWorld scratch) {
        this.report = report;
        this.nbt = new NbtChecks(report);
        this.world = world;
        this.scratch = scratch;
    }

    void run(WorldSave save) {
        level(save.getLevel());
        saveThroughWorldSave();

        long present = Stream.of(save.getCustomBossEvents(), save.getGameRules(), save.getRandomSequences(),
                save.getScoreboard(), save.getStopWatches(), save.getWanderingTrader(), save.getWeather(),
                save.getWorldClocks(), save.getWorldGenSettings()).filter(Optional::isPresent).count();
        report.fact("data files", present + " of 9 present");
        report.fact("session lock", save.getLock().isLocked() ? "locked, the game is probably running" : "free");

        ifPresent("CustomBossEvents", save.getCustomBossEvents(), this::customBossEvents);
        ifPresent("GameRules", save.getGameRules(), this::gameRules);
        ifPresent("RandomSequences", save.getRandomSequences(), this::randomSequences);
        ifPresent("Scoreboard", save.getScoreboard(), this::scoreboard);
        ifPresent("StopWatches", save.getStopWatches(), this::stopWatches);
        ifPresent("WanderingTrader", save.getWanderingTrader(), this::wanderingTrader);
        ifPresent("Weather", save.getWeather(), this::weather);
        ifPresent("WorldClocks", save.getWorldClocks(), this::worldClocks);
        ifPresent("WorldGenSettings", save.getWorldGenSettings(), this::worldGenSettings);
    }

    private void level(Level level) {
        report.section("Level");
        report.log("levelName=" + level.getLevelName());
        report.log("dataVersion=" + level.getDataVersion() + " nbtVersion=" + level.getNbtVersion());
        report.log("gameType=" + gameType(level.getGameType()) + " difficulty=" + level.getDifficulty()
                + " hardcore=" + level.isHardcore() + " difficultyLocked=" + level.isLocked());
        report.log("allowCommands=" + level.isAllowCommands() + " modded=" + level.isModded()
                + " initialized=" + level.getInitialized());
        report.log("time=" + level.getTime() + " lastPlayed=" + Instant.ofEpochMilli(level.getLastPlayed()));
        report.log("singleplayerUuid=" + level.getSingleplayerUuid());
        report.log("enabledDatapacks=" + Arrays.toString(level.getEnabledDatapacks()));
        report.log("disabledDatapacks=" + Arrays.toString(level.getDisabledDatapacks()));
        report.log("enabledFeatures=" + Arrays.toString(level.getEnabledFeatures()));
        report.log("serverBrands=" + Arrays.toString(level.getServerBrands()));
        if (level.getSpawn() != null) {
            report.log("spawn: dimension=" + level.getSpawn().getDimension()
                    + " pos=" + Arrays.toString(level.getSpawn().getPos())
                    + " yaw=" + level.getSpawn().getYaw() + " pitch=" + level.getSpawn().getPitch());
        }
        if (level.getVersion() != null) {
            report.log("version: id=" + level.getVersion().getId() + " name=" + level.getVersion().getName()
                    + " series=" + level.getVersion().getSeries() + " snapshot=" + level.getVersion().isSnapshot());
        }

        report.fact("world", level.getLevelName());
        report.fact("version", (level.getVersion() == null ? "?" : level.getVersion().getName())
                + " (data version " + level.getDataVersion() + ")");
        report.fact("mode", gameType(level.getGameType()) + ", " + level.getDifficulty()
                + (level.isHardcore() ? ", hardcore" : "") + (level.isAllowCommands() ? ", cheats on" : ""));
        report.fact("last played", Instant.ofEpochMilli(level.getLastPlayed()));
        report.fact("datapacks", level.getEnabledDatapacks().length + " enabled, "
                + level.getDisabledDatapacks().length + " disabled");

        nbt.roundtrips(level, new Codec<>(Level::toNbt, Level::fromNbt), world.resolve(LEVEL_DAT), scratch.resolve(LEVEL_DAT));
    }

    private void saveThroughWorldSave() {
        report.check("WorldSave.save() keeps edits", () -> {
            scratch.copy(LEVEL_DAT);
            WorldSave copy = new WorldSave(scratch.root());
            String renamed = copy.getLevel().getLevelName() + " (minified was here)";
            copy.getLevel().setLevelName(renamed);
            copy.getLevel().setTime(copy.getLevel().getTime() + 1);
            copy.save();

            Level reloaded = new WorldSave(scratch.root()).getLevel();
            if (!renamed.equals(reloaded.getLevelName())) {
                throw new AssertionError("expected name '" + renamed + "' but got '" + reloaded.getLevelName() + "'");
            }
            return NbtDiff.between(copy.getLevel().toNbt(), reloaded.toNbt()).orFail();
        });
    }

    private void customBossEvents(CustomBossEvents events) {
        report.log(events.getEvents().size() + " boss events");
        report.list(events.getEvents().entrySet().stream()
                .map(entry -> entry.getKey() + " name=" + entry.getValue().getName() + " visible=" + entry.getValue().isVisible())
                .toList());

        Codec<CustomBossEvents> codec = new Codec<>(CustomBossEvents::toNbt, CustomBossEvents::fromNbt);
        dataRoundtrips(events, codec, "custom_boss_events.dat");
        nbt.edit("add boss event", events, codec, scratchData("custom_boss_events.dat"),
                copy -> {
                    BossEvent event = new BossEvent();
                    event.setName("{\"text\":\"minified\"}");
                    event.setVisible(true);
                    copy.putEvent(TEST_ID, event);
                },
                reread -> reread.getEvents().containsKey(TEST_ID) && reread.getEvents().get(TEST_ID).isVisible());
    }

    private void gameRules(GameRules rules) {
        report.log(rules.getBooleanGamerules().size() + " boolean rules, " + rules.getIntegerGamerules().size() + " integer rules");
        report.list(Stream.concat(rules.getBooleanGamerules().entrySet().stream(), rules.getIntegerGamerules().entrySet().stream())
                .map(Map.Entry::toString)
                .sorted()
                .toList());
        report.fact("gamerules", rules.getBooleanGamerules().size() + rules.getIntegerGamerules().size());

        Codec<GameRules> codec = new Codec<>(GameRules::toNbt, GameRules::fromNbt);
        dataRoundtrips(rules, codec, "game_rules.dat");
        nbt.edit("add gamerules", rules, codec, scratchData("game_rules.dat"),
                copy -> {
                    copy.putBooleanGamerule(TEST_ID, true);
                    copy.putIntegerGamerule(TEST_ID + "_count", 42);
                },
                reread -> Boolean.TRUE.equals(reread.getBooleanGamerules().get(TEST_ID))
                        && Integer.valueOf(42).equals(reread.getIntegerGamerules().get(TEST_ID + "_count")));
    }

    private void randomSequences(RandomSequences sequences) {
        report.log("salt=" + sequences.getSalt() + ", " + sequences.getSequences().size() + " sequences");
        report.list(sequences.getSequences().entrySet().stream()
                .map(entry -> entry.getKey() + " " + Arrays.toString(entry.getValue()))
                .sorted()
                .toList());

        Codec<RandomSequences> codec = new Codec<>(RandomSequences::toNbt, RandomSequences::fromNbt);
        dataRoundtrips(sequences, codec, "random_sequences.dat");
        nbt.edit("add sequence", sequences, codec, scratchData("random_sequences.dat"),
                copy -> copy.putSequence(TEST_ID, new long[]{4, 2}),
                reread -> Arrays.equals(reread.getSequences().get(TEST_ID), new long[]{4, 2}));
    }

    private void scoreboard(Scoreboard scoreboard) {
        report.log(scoreboard.getObjectives().size() + " objectives, " + scoreboard.getPlayerScores().size()
                + " player scores, " + scoreboard.getDisplaySlots().size() + " display slots");
        report.list(scoreboard.getObjectives().stream()
                .map(objective -> "objective " + objective.getName() + " (" + objective.getCriteriaName() + ")")
                .toList());
        report.list(scoreboard.getPlayerScores().stream()
                .map(score -> "score " + score.getName() + " " + score.getObjective() + "=" + score.getScore())
                .toList());
        report.list(scoreboard.getDisplaySlots().entrySet().stream().map(entry -> "slot " + entry).toList());

        Codec<Scoreboard> codec = new Codec<>(Scoreboard::toNbt, Scoreboard::fromNbt);
        dataRoundtrips(scoreboard, codec, "scoreboard.dat");
        nbt.edit("add objective and score", scoreboard, codec, scratchData("scoreboard.dat"),
                copy -> {
                    Scoreboard.Objective objective = new Scoreboard.Objective();
                    objective.setName("minified");
                    objective.setDisplayName("{\"text\":\"minified\"}");
                    objective.setCriteriaName("dummy");
                    copy.addObjective(objective);

                    Scoreboard.PlayerScore score = new Scoreboard.PlayerScore();
                    score.setName("Steve");
                    score.setObjective("minified");
                    score.setScore(1337);
                    copy.addPlayerScore(score);
                    copy.putDisplaySlot("sidebar", "minified");
                },
                reread -> reread.getObjectives().stream().anyMatch(objective -> "minified".equals(objective.getName()))
                        && reread.getPlayerScores().stream().anyMatch(score -> score.getScore() == 1337)
                        && "minified".equals(reread.getDisplaySlots().get("sidebar")));
    }

    private void stopWatches(StopWatches stopWatches) {
        report.log(stopWatches.getStopwatches().size() + " stopwatches");
        report.list(stopWatches.getStopwatches().entrySet().stream().map(Map.Entry::toString).toList());

        Codec<StopWatches> codec = new Codec<>(StopWatches::toNbt, StopWatches::fromNbt);
        dataRoundtrips(stopWatches, codec, "stopwatches.dat");
        nbt.edit("add stopwatch", stopWatches, codec, scratchData("stopwatches.dat"),
                copy -> copy.getStopwatches().put(TEST_ID, 1234L),
                reread -> Long.valueOf(1234L).equals(reread.getStopwatches().get(TEST_ID)));
    }

    private void wanderingTrader(WanderingTrader trader) {
        report.log("spawnChance=" + trader.getSpawnChance() + " spawnDelay=" + trader.getSpawnDelay());

        Codec<WanderingTrader> codec = new Codec<>(WanderingTrader::toNbt, WanderingTrader::fromNbt);
        dataRoundtrips(trader, codec, "wandering_trader.dat");
        nbt.edit("change spawn chance", trader, codec, scratchData("wandering_trader.dat"),
                copy -> copy.setSpawnChance(75),
                reread -> Integer.valueOf(75).equals(reread.getSpawnChance()));
    }

    private void weather(Weather weather) {
        report.log("raining=" + weather.getRaining() + " rainTime=" + weather.getRainTime());
        report.log("thundering=" + weather.getThundering() + " thunderTime=" + weather.getThunderTime());
        report.log("clearWeatherTime=" + weather.getClearWeatherTime());
        report.fact("weather", Boolean.TRUE.equals(weather.getThundering()) ? "thunder"
                : Boolean.TRUE.equals(weather.getRaining()) ? "rain" : "clear");

        Codec<Weather> codec = new Codec<>(Weather::toNbt, Weather::fromNbt);
        dataRoundtrips(weather, codec, "weather.dat");
        nbt.edit("make it rain", weather, codec, scratchData("weather.dat"),
                copy -> {
                    copy.setRaining(true);
                    copy.setRainTime(4242);
                },
                reread -> Boolean.TRUE.equals(reread.getRaining()) && Integer.valueOf(4242).equals(reread.getRainTime()));
    }

    private void worldClocks(WorldClocks clocks) {
        report.log(clocks.getClocks().size() + " clocks");
        report.list(clocks.getClocks().entrySet().stream().map(Map.Entry::toString).toList());

        Codec<WorldClocks> codec = new Codec<>(WorldClocks::toNbt, WorldClocks::fromNbt);
        dataRoundtrips(clocks, codec, "world_clocks.dat");
        nbt.edit("add clock", clocks, codec, scratchData("world_clocks.dat"),
                copy -> copy.getClocks().put(TEST_ID, 24000L),
                reread -> Long.valueOf(24000L).equals(reread.getClocks().get(TEST_ID)));
    }

    private void worldGenSettings(WorldGenSettings settings) {
        report.log("seed=" + settings.getSeed() + " bonusChest=" + settings.isBonusChest()
                + " generateStructures=" + settings.isGenerateStructures());
        report.list(settings.getDimensions().entrySet().stream()
                .map(entry -> entry.getKey() + " type=" + entry.getValue().getType()
                        + (entry.getValue().getGenerator() == null ? "" : " generator=" + entry.getValue().getGenerator().getType()
                        + " settings=" + entry.getValue().getGenerator().getSettings()))
                .toList());
        report.fact("seed", settings.getSeed());

        Codec<WorldGenSettings> codec = new Codec<>(WorldGenSettings::toNbt, WorldGenSettings::fromNbt);
        dataRoundtrips(settings, codec, "world_gen_settings.dat");
        nbt.edit("change seed", settings, codec, scratchData("world_gen_settings.dat"),
                copy -> copy.setSeed(copy.getSeed() + 1),
                reread -> reread.getSeed() == settings.getSeed() + 1);
    }

    private <T> void ifPresent(String section, Optional<T> data, Consumer<T> checks) {
        report.section(section);
        data.ifPresentOrElse(checks, () -> report.skip("parse", "file not present"));
    }

    private <T> void dataRoundtrips(T value, Codec<T> codec, String fileName) {
        nbt.roundtrips(value, codec, world.resolve(DATA).resolve(fileName), scratchData(fileName));
    }

    private Path scratchData(String fileName) {
        return scratch.resolve(DATA).resolve(fileName);
    }

    static String gameType(Integer type) {
        if (type == null) return "unknown";
        return switch (type) {
            case 0 -> "survival";
            case 1 -> "creative";
            case 2 -> "adventure";
            case 3 -> "spectator";
            default -> "gameType " + type;
        };
    }
}
