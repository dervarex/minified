package com.dervarex.minified.worlds.manual;

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
import com.dervarex.minified.worlds.save.level.Spawn;
import com.dervarex.minified.worlds.save.level.Version;
import com.dervarex.minified.worlds.save.SessionLock;
import com.dervarex.minified.worlds.save.playerdata.Player;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Consumer;

public class DataPrinters {

    private static final String ITEM = "        - ";

    public static final Consumer<CustomBossEvents> PRINT_BOSS_EVENTS = events -> {
        System.out.println("    " + events.getEvents().size() + " entries");
        new TreeMap<>(events.getEvents()).forEach((id, event) ->
                System.out.println(ITEM + id + ": visible=" + event.isVisible())
        );
    };

    public static final Consumer<GameRules> PRINT_GAME_RULES = rules -> {
        System.out.println("    " + rules.getBooleanGamerules().size() + " boolean rules, "
                + rules.getIntegerGamerules().size() + " integer rules");
        new TreeMap<>(rules.getBooleanGamerules()).forEach((key, value) ->
                System.out.println(ITEM + key + "=" + value)
        );
        new TreeMap<>(rules.getIntegerGamerules()).forEach((key, value) ->
                System.out.println(ITEM + key + "=" + value)
        );
    };

    public static final Consumer<Level> PRINT_LEVEL = level -> {
        System.out.println("    levelName=" + level.getLevelName());
        System.out.println("    dataVersion=" + level.getDataVersion() + " nbtVersion=" + level.getNbtVersion());
        System.out.println("    gameType=" + level.getGameType() + " difficulty=" + level.getDifficulty());
        System.out.println("    hardcore=" + level.isHardcore() + " locked=" + level.isLocked()
                + " allowCommands=" + level.isAllowCommands() + " modded=" + level.isModded());
        System.out.println("    time=" + level.getTime() + " lastPlayed=" + level.getLastPlayed()
                + " initialized=" + level.getInitialized());
        System.out.println("    singleplayerUuid=" + level.getSingleplayerUuid());
        System.out.println(ITEM + "enabledDatapacks=" + Arrays.toString(level.getEnabledDatapacks()));
        System.out.println(ITEM + "disabledDatapacks=" + Arrays.toString(level.getDisabledDatapacks()));
        System.out.println(ITEM + "enabledFeatures=" + Arrays.toString(level.getEnabledFeatures()));
        System.out.println(ITEM + "serverBrands=" + Arrays.toString(level.getServerBrands()));
        Spawn spawn = level.getSpawn();
        if (spawn != null) {
            System.out.println(ITEM + "spawn: dimension=" + spawn.getDimension()
                    + " pitch=" + spawn.getPitch()
                    + " yaw=" + spawn.getYaw()
                    + " pos=" + Arrays.toString(spawn.getPos()));
        }
        Version version = level.getVersion();
        if (version != null) {
            System.out.println(ITEM + "version: id=" + version.getId()
                    + " name=" + version.getName()
                    + " series=" + version.getSeries()
                    + " snapshot=" + version.isSnapshot());
        }
    };

    public static final Consumer<SessionLock> PRINT_SESSION_LOCK = lock -> {
        System.out.println("    locked=" + lock.isLocked());
    };

    public static final Consumer<RandomSequences> PRINT_RANDOM_SEQUENCES = sequences -> {
        System.out.println("    salt=" + sequences.getSalt() + " " + sequences.getSequences().size() + " sequences");
        new TreeMap<>(sequences.getSequences()).forEach((key, value) ->
                System.out.println(ITEM + key + ": " + Arrays.toString(value))
        );
    };

    public static final Consumer<Scoreboard> PRINT_SCOREBOARD = scoreboard -> {
        System.out.println("    " + scoreboard.getObjectives().size() + " objectives, "
                + scoreboard.getPlayerScores().size() + " player scores");
        scoreboard.getObjectives().forEach(objective ->
                System.out.println(ITEM + "objective: " + objective.getName() + " (" + objective.getCriteriaName() + ")")
        );
        scoreboard.getPlayerScores().forEach(score ->
                System.out.println(ITEM + "score: " + score.getName() + " " + score.getObjective() + "=" + score.getScore())
        );
    };

    public static final Consumer<StopWatches> PRINT_STOPWATCHES = stopWatches -> {
        System.out.println("    " + stopWatches.getStopwatches().size() + " stopwatches");
        new TreeMap<>(stopWatches.getStopwatches()).forEach((key, value) ->
                System.out.println(ITEM + key + ": " + value)
        );
    };

    public static final Consumer<WanderingTrader> PRINT_WANDERING_TRADER = trader -> {
        System.out.println("    dataVersion=" + trader.getDataVersion()
                + " spawnChance=" + trader.getSpawnChance()
                + " spawnDelay=" + trader.getSpawnDelay());
    };

    public static final Consumer<Weather> PRINT_WEATHER = weather -> {
        System.out.println("    dataVersion=" + weather.getDataVersion());
        System.out.println("    raining=" + weather.getRaining() + " rainTime=" + weather.getRainTime());
        System.out.println("    thundering=" + weather.getThundering() + " thunderTime=" + weather.getThunderTime());
        System.out.println("    clearWeatherTime=" + weather.getClearWeatherTime());
    };

    public static final Consumer<WorldClocks> PRINT_WORLD_CLOCKS = clocks -> {
        System.out.println("    " + clocks.getClocks().size() + " clocks");
        new TreeMap<>(clocks.getClocks()).forEach((key, value) ->
                System.out.println(ITEM + key + ": " + value)
        );
    };

    public static final Consumer<WorldGenSettings> PRINT_WORLD_GEN_SETTINGS = settings -> {
        System.out.println("    seed=" + settings.getSeed()
                + " bonusChest=" + settings.isBonusChest()
                + " generateStructures=" + settings.isGenerateStructures());
        new TreeMap<>(settings.getDimensions()).forEach((key, dimension) ->
                System.out.println(ITEM + key + ": type=" + dimension.getType())
        );
    };

    public static final Consumer<List<Player>> PRINT_PLAYERS = players -> {
        System.out.println("    " + players.size() + " players");
        players.forEach(player -> {
            System.out.println(ITEM + "uuid=" + player.getUUID()
                    + " dimension=" + player.getDimension());
            System.out.println("            dataVersion=" + player.getDataVersion()
                    + " health=" + player.getHealth()
                    + " foodLevel=" + player.getFoodLevel()
                    + " gameType=" + player.getPlayerGameType());
            System.out.println("            xpLevel=" + player.getXPLevel()
                    + " xpTotal=" + player.getXPTotal());
            System.out.println("            position=" + player.getPosition()
                    + " rotation=" + player.getRotation());
            System.out.println("            advancements=" + (player.getAdvancements() != null)
                    + " statistics=" + (player.getStatistics() != null)
                    + " abilities=" + (player.getAbilities() != null)
                    + " inventoryItems=" + (player.getInventory() != null ? player.getInventory().getItems().length : 0)
                    + " attributes=" + (player.getAttributes() != null ? player.getAttributes().getAttributes().length : 0));
        });
    };
}
