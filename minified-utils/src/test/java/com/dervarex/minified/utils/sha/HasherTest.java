package com.dervarex.minified.utils.sha;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HasherTest {

    @TempDir
    Path tempDir;

    @Test
    void hashesFiles() throws Exception {
        Path file = Files.writeString(tempDir.resolve("abc.txt"), "abc");

        assertEquals("a9993e364706816aba3e25717850c26c9cd0d89d", Hasher.sha1(file));
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", Hasher.sha(file, 256));
    }

    @Test
    void hexKeepsLeadingZeros() {
        assertEquals("000fff", Hasher.bytesToHex(new byte[]{0x00, 0x0f, (byte) 0xff}));
    }
}
