package com.dervarex.minified.worlds.world.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import com.dervarex.minified.utils.nbt.tag.NbtTag;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Scoreboard {

    private int dataVersion;
    private final Map<String, String> displaySlots = new LinkedHashMap<>();
    private final List<Objective> objectives = new ArrayList<>();
    private final List<PlayerScore> playerScores = new ArrayList<>();

    @Getter
    @Setter
    public static class Objective {
        private String name;
        private String displayName;
        private String criteriaName;
        @Nullable
        private String renderType;

        public static Objective fromNbt(NbtCompound compound) {
            Objective obj = new Objective();
            obj.setName(compound.getString("Name"));
            obj.setDisplayName(compound.getString("DisplayName"));
            obj.setCriteriaName(compound.getString("CriteriaName"));
            if (compound.has("RenderType")) {
                obj.setRenderType(compound.getString("RenderType"));
            }
            return obj;
        }

        public NbtCompound toNbt() {
            NbtCompound compound = new NbtCompound();
            compound.setString("Name", name);
            compound.setString("DisplayName", displayName);
            compound.setString("CriteriaName", criteriaName);
            if (renderType != null) {
                compound.setString("RenderType", renderType);
            }
            return compound;
        }
    }

    @Getter
    @Setter
    public static class PlayerScore {
        private String name;
        private String objective;
        private int score;
        private boolean locked;

        public static PlayerScore fromNbt(NbtCompound compound) {
            PlayerScore ps = new PlayerScore();
            ps.setName(compound.getString("Name"));
            ps.setObjective(compound.getString("Objective"));
            ps.setScore(compound.getInt("Score"));
            ps.setLocked(compound.getByte("Locked") != 0);
            return ps;
        }

        public NbtCompound toNbt() {
            NbtCompound compound = new NbtCompound();
            compound.setString("Name", name);
            compound.setString("Objective", objective);
            compound.setInt("Score", score);
            compound.setByte("Locked", (byte) (locked ? 1 : 0));
            return compound;
        }
    }

    public static Scoreboard fromNbt(NbtCompound nbt) {
        Scoreboard result = new Scoreboard();
        result.dataVersion = nbt.getInt("DataVersion");

        NbtCompound data = nbt.getCompound("data");

        if (data.has("DisplaySlots")) {
            NbtCompound slots = data.getCompound("DisplaySlots");
            for (Map.Entry<String, NbtTag> entry : slots.asMap().entrySet()) {
                result.displaySlots.put(entry.getKey(), String.valueOf(entry.getValue()));
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

    public NbtCompound toNbt() {
        NbtCompound data = new NbtCompound();

        NbtCompound displaySlotsCompound = new NbtCompound();
        for (Map.Entry<String, String> entry : displaySlots.entrySet()) {
            displaySlotsCompound.setString(entry.getKey(), entry.getValue());
        }
        data.setCompound("DisplaySlots", displaySlotsCompound);

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

        NbtCompound root = new NbtCompound();
        root.setCompound("data", data);
        root.setInt("DataVersion", dataVersion);

        return root;
    }

    public void putDisplaySlot(String key, String value) {
        displaySlots.put(key, value);
    }

    public void removeDisplaySlot(String key) {
        displaySlots.remove(key);
    }

    public void addObjective(Objective objective) {
        objectives.add(objective);
    }

    public void removeObjective(Objective objective) {
        objectives.remove(objective);
    }

    public void addPlayerScore(PlayerScore playerScore) {
        playerScores.add(playerScore);
    }

    public void removePlayerScore(PlayerScore playerScore) {
        playerScores.remove(playerScore);
    }
}