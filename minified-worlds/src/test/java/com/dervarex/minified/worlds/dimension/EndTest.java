package com.dervarex.minified.worlds.dimension;

import com.dervarex.minified.utils.nbt.Writer;
import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class EndTest {

    private static NbtCompound fightNbt() {
        NbtCompound data = new NbtCompound();
        data.setBoolean("previously_killed", true);
        data.setBoolean("needs_state_scanning", false);
        data.setInt("respawn_time", 0);
        data.setBoolean("dragon_killed", true);
        data.setIntArray("dragon_uuid", new int[]{1, 2, 3, 4});
        data.setIntArray("exit_portal_location", new int[]{0, 64, 0});
        data.setList("gateways", new NbtList((byte) 3));

        NbtCompound nbt = new NbtCompound();
        nbt.setInt("DataVersion", 3953);
        nbt.setCompound("data", data);
        return nbt;
    }

    @Test
    void testDragonFightIsEmptyWhenFileIsMissing(@TempDir Path worldDir) {
        End end = new End(worldDir.toFile());
        assertTrue(end.getEnderDragonFight().isEmpty());
    }

    @Test
    void testDragonFightIsLoadedFromFile(@TempDir Path worldDir) throws IOException {
        Path fightDir = worldDir.resolve("dimensions").resolve("minecraft").resolve("the_end")
                .resolve("data").resolve("minecraft");
        Files.createDirectories(fightDir);
        Writer.writeFile(fightDir.resolve("ender_dragon_fight.dat").toFile(), fightNbt());

        End end = new End(worldDir.toFile());

        assertTrue(end.getEnderDragonFight().isPresent());
        assertTrue(end.getEnderDragonFight().get().isDragonKilled());
        assertEquals(3953, end.getEnderDragonFight().get().getDataVersion());
    }
}
