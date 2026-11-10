package com.dervarex.minified.worlds.save.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import com.dervarex.minified.utils.nbt.tag.NbtTag;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apiguardian.api.API;

/**
 * The scoreboard of a world, from scoreboard.dat
 */
@Getter
@Setter
@API(status = API.Status.STABLE)
public class Scoreboard {
    /**
     * Creates an empty scoreboard
     */
    public Scoreboard() {
    }


    private int dataVersion;
    private final Map<String, String> displaySlots = new LinkedHashMap<>();
    private final List<Objective> objectives = new ArrayList<>();
    private final List<PlayerScore> playerScores = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private NbtCompound raw;

    /**
     * A scoreboard objective, made with /scoreboard objectives
     */
    @Getter
    @Setter
    public static class Objective {
        /**
         * Creates an empty objective
         */
        public Objective() {
        }

        private String name;
        /** The display name, null if it isn't plain text */
        @Nullable
        private String displayName;
        private String criteriaName;
        @Nullable
        private String renderType;

        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private NbtCompound raw;

        /**
         * Reads an objective from NBT
         * @param compound the objective's NBT
         * @return the objective
         */
        public static Objective fromNbt(NbtCompound compound) {
            Objective obj = new Objective();
            obj.raw = compound.copy();
            obj.setName(compound.getString("Name"));
            obj.setDisplayName(RawNbt.stringOrNull(compound, "DisplayName"));
            obj.setCriteriaName(compound.getString("CriteriaName"));
            if (compound.has("RenderType")) {
                obj.setRenderType(compound.getString("RenderType"));
            }
            return obj;
        }

        /** {@return the objective as NBT} */
        public NbtCompound toNbt() {
            NbtCompound compound = RawNbt.copyOrNew(raw);
            compound.setString("Name", name);
            RawNbt.setStringOrKeep(compound, "DisplayName", displayName);
            compound.setString("CriteriaName", criteriaName);
            if (renderType != null) {
                compound.setString("RenderType", renderType);
            } else {
                compound.remove("RenderType");
            }
            return compound;
        }
    }

    /**
     * The score of a player in one objective
     */
    @Getter
    @Setter
    public static class PlayerScore {
        /**
         * Creates an empty score
         */
        public PlayerScore() {
        }

        private String name;
        private String objective;
        private int score;
        private boolean locked;

        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private NbtCompound raw;

        /**
         * Reads a score from NBT
         * @param compound the score's NBT
         * @return the score
         */
        public static PlayerScore fromNbt(NbtCompound compound) {
            PlayerScore ps = new PlayerScore();
            ps.raw = compound.copy();
            ps.setName(compound.getString("Name"));
            ps.setObjective(compound.getString("Objective"));
            ps.setScore(compound.getInt("Score"));
            ps.setLocked(compound.getByte("Locked") != 0);
            return ps;
        }

        /** {@return the score as NBT} */
        public NbtCompound toNbt() {
            NbtCompound compound = RawNbt.copyOrNew(raw);
            compound.setString("Name", name);
            compound.setString("Objective", objective);
            compound.setInt("Score", score);
            compound.setByte("Locked", (byte) (locked ? 1 : 0));
            return compound;
        }
    }

    /**
     * Reads the scoreboard from NBT
     * @param nbt the content of scoreboard.dat
     * @return the scoreboard
     */
    public static Scoreboard fromNbt(NbtCompound nbt) {
        Scoreboard result = new Scoreboard();
        result.raw = nbt.copy();
        result.dataVersion = nbt.getInt("DataVersion");

        NbtCompound data = nbt.has("data") ? nbt.getCompound("data") : new NbtCompound();

        if (data.has("DisplaySlots")) {
            NbtCompound slots = data.getCompound("DisplaySlots");
            for (Map.Entry<String, NbtTag> entry : slots.asMap().entrySet()) {
                result.displaySlots.put(entry.getKey(), slots.getString(entry.getKey()));
            }
        }

        if (data.has("Objectives")) {
            NbtList objectivesList = data.getList("Objectives");
            for (NbtTag tag : objectivesList.elements()) {
                if (tag instanceof NbtCompound objCompound) {
                    result.objectives.add(Objective.fromNbt(objCompound));
                }
            }
        }

        if (data.has("PlayerScores")) {
            NbtList scoresList = data.getList("PlayerScores");
            for (NbtTag tag : scoresList.elements()) {
                if (tag instanceof NbtCompound scoreCompound) {
                    result.playerScores.add(PlayerScore.fromNbt(scoreCompound));
                }
            }
        }

        return result;
    }

    /** {@return the scoreboard as NBT, like in scoreboard.dat} */
    public NbtCompound toNbt() {
        NbtCompound root = RawNbt.copyOrNew(raw);
        NbtCompound data = RawNbt.child(root, "data");

        NbtCompound displaySlotsCompound = RawNbt.child(data, "DisplaySlots");
        RawNbt.removeMissing(displaySlotsCompound, displaySlots.keySet(), tag -> true);
        for (Map.Entry<String, String> entry : displaySlots.entrySet()) {
            displaySlotsCompound.setString(entry.getKey(), entry.getValue());
        }

        NbtList objectivesList = new NbtList((byte) 10);
        for (Objective obj : objectives) {
            objectivesList.add(obj.toNbt());
        }
        data.setList("Objectives", objectivesList);

        NbtList playerScoresList = new NbtList((byte) 10);
        for (PlayerScore score : playerScores) {
            playerScoresList.add(score.toNbt());
        }
        data.setList("PlayerScores", playerScoresList);

        root.setInt("DataVersion", dataVersion);

        return root;
    }

    /**
     * Shows an objective in a display slot
     * @param key the display slot
     * @param value the objective's name
     */
    public void putDisplaySlot(String key, String value) {
        displaySlots.put(key, value);
    }

    /**
     * Clears a display slot
     * @param key the display slot
     */
    public void removeDisplaySlot(String key) {
        displaySlots.remove(key);
    }

    /**
     * Adds an objective
     * @param objective the objective
     */
    public void addObjective(Objective objective) {
        objectives.add(objective);
    }

    /**
     * Removes an objective
     * @param objective the objective
     */
    public void removeObjective(Objective objective) {
        objectives.remove(objective);
    }

    /**
     * Adds a player's score
     * @param playerScore the score
     */
    public void addPlayerScore(PlayerScore playerScore) {
        playerScores.add(playerScore);
    }

    /**
     * Removes a player's score
     * @param playerScore the score
     */
    public void removePlayerScore(PlayerScore playerScore) {
        playerScores.remove(playerScore);
    }
}