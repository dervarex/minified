package com.dervarex.minified.worlds.dimension.data;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtInt;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnderDragonFight {
    private int dataVersion;
    private int[] gateways;
    private int[] dragonUUID;
    private int[] exitPortalLocation; // x, y, z
    private boolean needsStateScanning;
    private int respawnTime;
    private boolean dragonKilled;
    private boolean previouslyKilled;

    public static EnderDragonFight fromNbt(NbtCompound nbt) {
        EnderDragonFight dragonFight = new EnderDragonFight();

        dragonFight.dataVersion = nbt.getInt("DataVersion");
        NbtCompound data = nbt.getCompound("data");
        dragonFight.previouslyKilled = data.getBoolean("previously_killed");
        dragonFight.needsStateScanning = data.getBoolean("needs_state_scanning");
        dragonFight.respawnTime = data.getInt("respawn_time");
        dragonFight.dragonKilled = data.getBoolean("dragon_killed");

        NbtList gatewaysList = data.getList("gateways");
        dragonFight.gateways = new int[gatewaysList.size()];
        for (int i = 0; i < gatewaysList.size(); ++i) {
            NbtInt entry = (NbtInt) gatewaysList.elements().get(i);
            dragonFight.gateways[i] = entry.value();
        }
        dragonFight.dragonUUID = data.getIntArray("dragon_uuid").value();
        dragonFight.exitPortalLocation = data.getIntArray("exit_portal_location").value();

        return dragonFight;
    }

    public NbtCompound toNbt() {
        NbtCompound compound = new NbtCompound();
        compound.setInt("DataVersion", dataVersion);

        NbtCompound data = new NbtCompound();
        data.setBoolean("previously_killed", previouslyKilled);
        data.setBoolean("needs_state_scanning", needsStateScanning);
        data.setInt("respawn_time", respawnTime);
        data.setBoolean("dragon_killed", dragonKilled);
        data.setIntArray("dragon_uuid", dragonUUID);
        data.setIntArray("exit_portal_location", exitPortalLocation);

        NbtList gatewaysList = new NbtList((byte) 3); // 3 = TAG_Int
        for (int gateway : gateways) {
            gatewaysList.add(new NbtInt(gateway));
        }
        data.setList("gateways", gatewaysList);

        compound.setCompound("data", data);
        return compound;
    }
}