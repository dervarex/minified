package com.dervarex.minified.utils.nbt;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtEnd;
import com.dervarex.minified.utils.nbt.tag.NbtInt;
import com.dervarex.minified.utils.nbt.tag.NbtList;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;

public class WriterTest {
    @TempDir
    static Path tempDir;

    static Path nbtFile;
    @BeforeAll
    static void setup() {
        nbtFile = tempDir.resolve("test.dat");
        // copy nbt file content to test.dat in temporary directory
        try (InputStream in = WriterTest.class.getResourceAsStream("/nbt/test.dat")) {
            Files.copy(in, nbtFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void testRoundTrip() throws IOException {
        NbtCompound nbt = Parser.readFile(nbtFile.toFile());

        Path output = tempDir.resolve("level_out.dat");
        Writer.writeFile(output.toFile(), nbt);

        NbtCompound reparsed = Parser.readFile(output.toFile());
        assertTrue(NbtEquals.deepEquals(nbt, reparsed));
    }

    @Test
    void everyTagTypeSurvives() throws IOException {
        NbtCompound player = new NbtCompound();
        player.setString("name", "Notch");
        NbtList players = new NbtList((byte) 10);
        players.add(player);
        NbtList scores = new NbtList((byte) 3);
        scores.add(new NbtInt(1));
        scores.add(new NbtInt(-1));

        NbtCompound nbt = new NbtCompound();
        nbt.setByte("byte", (byte) -1);
        nbt.setShort("short", Short.MAX_VALUE);
        nbt.setInt("int", Integer.MIN_VALUE);
        nbt.setLong("long", Long.MAX_VALUE);
        nbt.setFloat("float", 0.5f);
        nbt.setDouble("double", Math.PI);
        nbt.setString("string", "äöü 😀");
        nbt.setByteArray("bytes", new byte[]{1, 2, 3});
        nbt.setIntArray("ints", new int[]{4, 5});
        nbt.setLongArray("longs", new long[]{6L});
        nbt.setList("players", players);
        nbt.setList("scores", scores);
        nbt.setList("empty", new NbtList((byte) 0));
        nbt.setCompound("nested", player);

        Path compressed = tempDir.resolve("everything.dat");
        Path uncompressed = tempDir.resolve("everything.nbt");
        Writer.writeFile(compressed.toFile(), nbt);
        Writer.writeFileUncompressed(uncompressed.toFile(), nbt);

        assertTrue(NbtEquals.deepEquals(nbt, Parser.readFile(compressed.toFile())));
        assertTrue(NbtEquals.deepEquals(nbt, Parser.readFile(uncompressed.toFile())));
    }

    @Test
    void booleansComeBackAsBytes() throws IOException {
        NbtCompound nbt = new NbtCompound();
        nbt.setBoolean("hardcore", true);

        Path file = tempDir.resolve("boolean.dat");
        Writer.writeFile(file.toFile(), nbt);

        assertTrue(Parser.readFile(file.toFile()).getBoolean("hardcore"));
    }

    @Test
    void refusesToWriteAnEndTag() {
        NbtCompound nbt = new NbtCompound();
        nbt.put("end", NbtEnd.INSTANCE);

        assertThrows(IOException.class, () -> Writer.writeFile(tempDir.resolve("end.dat").toFile(), nbt));
    }
}
