package com.dervarex.minified.utils.nbt;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.util.zip.GZIPOutputStream;

public class Writer {

    /**
     * Writes the given NBT tree to a file, Gzip compressed (like the original Minecraft nbt files)
     * Goes through a temp file, so a crash halfway through doesn't leave you with half a level.dat
     * @param file target file
     * @param nbt root compound, e.g. as returned by Parser.readFile
     * @throws IOException if writing fails, the old file is still fine then
     */
    public static void writeFile(File file, NbtCompound nbt) throws IOException {
        write(file, nbt, true, null);
    }

    /**
     * Like {@link #writeFile(File, NbtCompound)}, but the old file is copied to {@code backup} first (think level.dat_old)
     * @throws IOException if writing or copying fails, the old file is still fine then
     */
    public static void writeFile(File file, NbtCompound nbt, File backup) throws IOException {
        write(file, nbt, true, backup);
    }

    /**
     * Writes the given NBT tree uncompressed. Useful for debugging or
     * formats that don't expect Gzip (e.g. individual chunk payloads
     * that are compressed at the region-file level instead).
     * Also goes through a temp file
     */
    public static void writeFileUncompressed(File file, NbtCompound nbt) throws IOException {
        write(file, nbt, false, null);
    }

    private static void write(File file, NbtCompound nbt, boolean compressed, File backup) throws IOException {
        Path target = file.toPath().toAbsolutePath();
        Path temp = Files.createTempFile(target.getParent(), target.getFileName() + "-", ".tmp");
        try {
            try (DataOutputStream out = new DataOutputStream(compressed
                    ? new GZIPOutputStream(new BufferedOutputStream(Files.newOutputStream(temp)))
                    : new BufferedOutputStream(Files.newOutputStream(temp)))) {
                out.writeByte(Parser.TAG_Compound);
                out.writeUTF(""); // root name, empty for level.dat
                NbtWriter.writeCompound(out, nbt);
            }
            // actually on the disk, not just "written" into some cache
            try (FileChannel channel = FileChannel.open(temp, StandardOpenOption.WRITE)) {
                channel.force(true);
            }
            if (Files.exists(target)) {
                keepPermissions(target, temp);
                if (backup != null) Files.copy(target, backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    // createTempFile makes it owner only, the file we replace might not be
    private static void keepPermissions(Path from, Path to) throws IOException {
        PosixFileAttributeView view = Files.getFileAttributeView(from, PosixFileAttributeView.class);
        if (view != null) Files.setPosixFilePermissions(to, view.readAttributes().permissions());
    }
}
