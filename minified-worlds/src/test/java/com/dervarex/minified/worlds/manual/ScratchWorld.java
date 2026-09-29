package com.dervarex.minified.worlds.manual;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

final class ScratchWorld implements AutoCloseable {

    private final Path source;
    private final Path root;

    private ScratchWorld(Path source, Path root) {
        this.source = source;
        this.root = root;
    }

    static ScratchWorld copyOf(Path source) throws IOException {
        ScratchWorld scratch = new ScratchWorld(source, Files.createTempDirectory("minified-manual-"));
        try (Stream<Path> files = Files.walk(source)) {
            List<Path> small = files.filter(Files::isRegularFile)
                    .filter(file -> !file.toString().endsWith(".mca"))
                    .toList();
            for (Path file : small) {
                scratch.copy(source.relativize(file));
            }
        }
        return scratch;
    }

    Path root() {
        return root;
    }

    Path resolve(Path relative) {
        return root.resolve(relative);
    }

    void copy(Path relative) throws IOException {
        Path from = source.resolve(relative);
        if (!Files.exists(from)) return;

        Path target = root.resolve(relative);
        Files.createDirectories(target.getParent());
        Files.copy(from, target, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void close() throws IOException {
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : files.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(file);
            }
        }
    }
}
