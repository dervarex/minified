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
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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

    @Test
    void keepsThePreviousFileAsBackup() throws IOException {
        Path folder = Files.createDirectories(tempDir.resolve("backup"));
        Path file = folder.resolve("level.dat");
        Path backup = folder.resolve("level.dat_old");
        NbtCompound first = new NbtCompound();
        first.setInt("Time", 1);
        NbtCompound second = new NbtCompound();
        second.setInt("Time", 2);

        Writer.writeFile(file.toFile(), first, backup.toFile());
        assertFalse(Files.exists(backup), "there was nothing to back up yet");
        Writer.writeFile(file.toFile(), second, backup.toFile());

        assertEquals(2, Parser.readFile(file.toFile()).getInt("Time"));
        assertEquals(1, Parser.readFile(backup.toFile()).getInt("Time"));
    }

    @Test
    void failingHalfwayLeavesTheOldFileAlone() throws IOException {
        Path folder = Files.createDirectories(tempDir.resolve("failing"));
        Path file = folder.resolve("level.dat");
        NbtCompound good = new NbtCompound();
        good.setInt("Time", 1);
        Writer.writeFile(file.toFile(), good);

        NbtCompound broken = new NbtCompound();
        broken.setInt("Time", 2);
        broken.put("end", NbtEnd.INSTANCE); // can't be written, so this blows up halfway through
        assertThrows(IOException.class, () -> Writer.writeFile(file.toFile(), broken));

        assertEquals(1, Parser.readFile(file.toFile()).getInt("Time"));
        try (Stream<Path> files = Files.list(folder)) {
            assertEquals(List.of(file), files.toList(), "the temporary file should be gone");
        }
    }

    @Test
    void replacedFilesKeepTheirPermissions() throws IOException {
        Path file = tempDir.resolve("permissions.dat");
        Writer.writeFile(file.toFile(), new NbtCompound());
        assumeTrue(Files.getFileAttributeView(file, PosixFileAttributeView.class) != null, "no POSIX permissions on this file system");
        Set<PosixFilePermission> readableByEveryone = PosixFilePermissions.fromString("rw-r--r--");
        Files.setPosixFilePermissions(file, readableByEveryone);

        Writer.writeFile(file.toFile(), new NbtCompound());

        assertEquals(readableByEveryone, Files.getPosixFilePermissions(file));
    }
}
