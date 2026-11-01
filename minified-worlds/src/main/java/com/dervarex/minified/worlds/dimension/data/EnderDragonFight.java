package com.dervarex.minified.worlds.dimension.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtInt;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.apiguardian.api.API;

@Getter
@Setter
@API(status = API.Status.STABLE)
public class EnderDragonFight {
    private int dataVersion;
    @Nullable
    private int[] gateways;
    @Nullable
    private int[] dragonUUID; // only there while a dragon is alive
    @Nullable
    private int[] exitPortalLocation; // x, y, z, missing until the exit portal exists
    private boolean needsStateScanning;
    private int respawnTime;
    private boolean dragonKilled;
    private boolean previouslyKilled;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private NbtCompound raw;

    public static EnderDragonFight fromNbt(NbtCompound nbt) {
        EnderDragonFight dragonFight = new EnderDragonFight();
        dragonFight.raw = nbt.copy();

        dragonFight.dataVersion = nbt.getInt("DataVersion");
        NbtCompound data = nbt.getCompound("data");
        // Minecraft's defaults, for whatever isn't there
        dragonFight.previouslyKilled = data.has("previously_killed") && data.getBoolean("previously_killed");
        dragonFight.needsStateScanning = !data.has("needs_state_scanning") || data.getBoolean("needs_state_scanning");
        dragonFight.respawnTime = data.has("respawn_time") ? data.getInt("respawn_time") : 0;
        dragonFight.dragonKilled = data.has("dragon_killed") && data.getBoolean("dragon_killed");

        if (data.has("gateways")) {
            NbtList gatewaysList = data.getList("gateways");
            dragonFight.gateways = new int[gatewaysList.size()];
            for (int i = 0; i < gatewaysList.size(); ++i) {
                NbtInt entry = (NbtInt) gatewaysList.elements().get(i);
                dragonFight.gateways[i] = entry.value();
            }
        }
        if (data.has("dragon_uuid")) dragonFight.dragonUUID = data.getIntArray("dragon_uuid");
        if (data.has("exit_portal_location")) dragonFight.exitPortalLocation = data.getIntArray("exit_portal_location");

        return dragonFight;
    }

    public NbtCompound toNbt() {
        NbtCompound compound = raw != null ? raw.copy() : new NbtCompound();
        compound.setInt("DataVersion", dataVersion);

        NbtCompound data = compound.get("data").orElse(null) instanceof NbtCompound existing ? existing : new NbtCompound();
        data.setBoolean("previously_killed", previouslyKilled);
        data.setBoolean("needs_state_scanning", needsStateScanning);
        data.setInt("respawn_time", respawnTime);
        data.setBoolean("dragon_killed", dragonKilled);
        if (dragonUUID != null) data.setIntArray("dragon_uuid", dragonUUID);
        else data.remove("dragon_uuid");
        if (exitPortalLocation != null) data.setIntArray("exit_portal_location", exitPortalLocation);
        else data.remove("exit_portal_location");

        if (gateways != null) {
            NbtList gatewaysList = new NbtList((byte) 3); // 3 = TAG_Int
            for (int gateway : gateways) {
                gatewaysList.add(new NbtInt(gateway));
            }
            data.setList("gateways", gatewaysList);
        } else {
            data.remove("gateways");
        }

        compound.setCompound("data", data);
        return compound;
    }
}