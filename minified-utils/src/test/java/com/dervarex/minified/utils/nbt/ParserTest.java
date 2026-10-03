package com.dervarex.minified.utils.nbt;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.DeflaterOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParserTest {

    @TempDir
    Path tempDir;

    @Test
    void readsZlibToo() throws IOException {
        NbtCompound nbt = new NbtCompound();
        nbt.setString("LevelName", "New World");
        Path raw = tempDir.resolve("level.nbt");
        Writer.writeFileUncompressed(raw.toFile(), nbt);

        Path zlib = tempDir.resolve("level.zlib");
        try (OutputStream out = new DeflaterOutputStream(Files.newOutputStream(zlib))) {
            out.write(Files.readAllBytes(raw));
        }

        assertEquals("New World", Parser.readFile(zlib.toFile()).getString("LevelName"));
    }

    @Test
    void rootHasToBeACompound() throws IOException {
        Path file = Files.write(tempDir.resolve("string.nbt"), new byte[]{8, 0, 0, 0, 0}); // not a compound

        assertThrows(IOException.class, () -> Parser.readFile(file.toFile()));
    }

    @Test
    void unknownTagTypesAreRejected() throws IOException {
        // compound "" containing a tag of type 99 called "?", we don't know what this is so it should reject it
        Path file = Files.write(tempDir.resolve("future.nbt"), new byte[]{10, 0, 0, 99, 0, 1, '?'});

        IOException e = assertThrows(IOException.class, () -> Parser.readFile(file.toFile()));
        assertEquals("Unknown Tag Type: 99", e.getMessage());
    }
}
