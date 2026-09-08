package com.dervarex.minified.worlds.manual;

import java.util.Optional;
import java.util.function.Consumer;

public class NbtDataPrinter {

    public static <T> void printIfPresent(Optional<T> data, String name, Consumer<T> printer) {
        System.out.println();
        System.out.println("[" + name + "]");
        data.ifPresentOrElse(
                printer,
                () -> System.out.println("    not present")
        );
    }

    public static <T> void printIfNotNull(T data, String name, Consumer<T> printer) {
        printIfPresent(Optional.ofNullable(data), name, printer);
    }

    public static <T> void printSimple(Optional<T> data, String name) {
        printIfPresent(data, name, value -> System.out.println("    " + value));
    }

    public static <T> void printSimple(T data, String name) {
        printIfNotNull(data, name, value -> System.out.println("    " + value));
    }
}
