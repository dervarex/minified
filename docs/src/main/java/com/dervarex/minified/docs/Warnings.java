package com.dervarex.minified.docs;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Collects problems found while generating, like stale guide references or unresolved links
 */
final class Warnings {

    private final Set<String> messages = new LinkedHashSet<>();
    private final boolean strict;

    Warnings(boolean strict) {
        this.strict = strict;
    }

    void add(String where, String message) {
        messages.add(where + ": " + message);
    }

    void print() {
        if (messages.isEmpty()) {
            System.out.println("docs: no warnings");
            return;
        }
        System.out.println("docs: " + messages.size() + " warning(s)");
        messages.forEach(m -> System.out.println("  warning: " + m));
    }

    boolean ok() {
        return !strict || messages.isEmpty();
    }
}
