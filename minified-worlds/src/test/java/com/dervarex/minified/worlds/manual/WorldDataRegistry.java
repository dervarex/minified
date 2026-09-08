package com.dervarex.minified.worlds.manual;

import com.dervarex.minified.worlds.save.WorldSave;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class WorldDataRegistry {

    private final List<DataEntry<?>> entries = new ArrayList<>();

    private record DataEntry<T>(String name, Function<WorldSave, Optional<T>> supplier, Consumer<T> printer) {
        void print(WorldSave worldSave) {
            Optional<T> data = supplier.apply(worldSave);
            NbtDataPrinter.printIfPresent(data, name, printer);
        }
    }

    public <T> void register(String name, Function<WorldSave, Optional<T>> supplier, Consumer<T> printer) {
        entries.add(new DataEntry<>(name, supplier, printer));
    }

    public <T> void registerSimple(String name, Function<WorldSave, Optional<T>> supplier) {
        register(name, supplier, value -> System.out.println(value));
    }

    public void printAll(WorldSave worldSave) {
        if (entries.isEmpty()) {
            System.out.println("No data registered to print");
            return;
        }

        System.out.println("========================================");
        System.out.println(" World Data Analysis");
        System.out.println("========================================");
        entries.forEach(entry -> entry.print(worldSave));
        System.out.println();
        System.out.println("========================================");
        System.out.println(" Analysis Complete (" + entries.size() + " categories)");
        System.out.println("========================================");
    }
}