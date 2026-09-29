package com.dervarex.minified.worlds.manual;

import com.dervarex.minified.utils.nbt.Parser;
import com.dervarex.minified.utils.nbt.Writer;
import com.dervarex.minified.utils.nbt.tag.NbtCompound;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

final class NbtChecks {

    record Codec<T>(Function<T, NbtCompound> write, Function<NbtCompound, T> read) {
        T copy(T value) {
            return read.apply(write.apply(value));
        }
    }

    private final Report report;

    NbtChecks(Report report) {
        this.report = report;
    }

    <T> void roundtrips(T value, Codec<T> codec, Path original, Path scratchFile) {
        report.check("roundtrip in memory", () -> {
            NbtCompound written = codec.write().apply(value);
            return NbtDiff.between(written, codec.write().apply(codec.copy(value))).orFail();
        });
        report.check("roundtrip on disk", () -> {
            NbtCompound written = codec.write().apply(value);
            Files.createDirectories(scratchFile.getParent());
            Writer.writeFile(scratchFile.toFile(), written);
            T reread = codec.read().apply(Parser.readFile(scratchFile.toFile()));
            NbtDiff.between(written, codec.write().apply(reread)).orFail();
            return Files.size(scratchFile) + " bytes";
        });
        report.check("matches original file", () ->
                NbtDiff.between(Parser.readFile(original.toFile()), codec.write().apply(value)).orWarn());
    }

    <T> void edit(String name, T value, Codec<T> codec, Path scratchFile, Consumer<T> edit, Predicate<T> survived) {
        report.check(name, () -> {
            T copy = codec.copy(value);
            edit.accept(copy);
            Files.createDirectories(scratchFile.getParent());
            Writer.writeFile(scratchFile.toFile(), codec.write().apply(copy));
            if (!survived.test(codec.read().apply(Parser.readFile(scratchFile.toFile())))) {
                throw new AssertionError("edit got lost somewhere between writing and reading");
            }
            return "";
        });
    }
}
