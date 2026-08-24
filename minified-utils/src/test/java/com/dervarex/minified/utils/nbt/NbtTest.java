package com.dervarex.minified.utils.nbt;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class NbtTest {
    @TempDir
    static Path tempDir;

    static Path nbtFile;
    @BeforeAll
    static void setup() {
        nbtFile = tempDir.resolve("test.dat");
        // copy nbt file content to test.dat in temporary directory
        try (InputStream in = NbtTest.class.getResourceAsStream("/nbt/test.dat")) {
            Files.copy(in, nbtFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void testRoundTrip() throws IOException {
        LinkedHashMap<String, Object> nbt = Parser.readFile(nbtFile.toFile());

        Path output = tempDir.resolve("level_out.dat");
        Writer.writeFile(output.toFile(), nbt);

        LinkedHashMap<String, Object> reparsed = Parser.readFile(output.toFile());
        assertTrue(NbtEquals.deepEquals(nbt, reparsed));
    }
}
