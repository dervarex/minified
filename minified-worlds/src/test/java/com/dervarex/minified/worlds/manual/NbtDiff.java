package com.dervarex.minified.worlds.manual;

import com.dervarex.minified.utils.nbt.NbtEquals;
import com.dervarex.minified.utils.nbt.tag.NbtBoolean;
import com.dervarex.minified.utils.nbt.tag.NbtByte;
import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import com.dervarex.minified.utils.nbt.tag.NbtTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

final class NbtDiff {

    private static final int SHOWN_PATHS = 5;

    private final List<String> changed = new ArrayList<>();
    private final List<String> dropped = new ArrayList<>();

    private NbtDiff() {}

    static NbtDiff between(NbtTag original, NbtTag written) {
        NbtDiff diff = new NbtDiff();
        diff.compare("", original, written);
        return diff;
    }

    boolean identical() {
        return changed.isEmpty() && dropped.isEmpty();
    }

    String orFail() {
        if (!identical()) throw new AssertionError(summary());
        return "";
    }

    String orWarn() {
        if (!identical()) throw Report.warning(summary());
        return "identical";
    }

    String summary() {
        List<String> parts = new ArrayList<>();
        if (!changed.isEmpty()) parts.add(changed.size() + " changed " + preview(changed));
        if (!dropped.isEmpty()) parts.add(dropped.size() + " dropped " + preview(dropped));
        return parts.isEmpty() ? "identical" : String.join(", ", parts);
    }

    private void compare(String path, NbtTag original, NbtTag written) {
        if (original instanceof NbtCompound a && written instanceof NbtCompound b) {
            Map<String, NbtTag> before = a.asMap();
            Map<String, NbtTag> after = b.asMap();
            for (String key : new TreeSet<>(before.keySet())) {
                if (!after.containsKey(key)) dropped.add(child(path, key));
            }
            for (String key : new TreeSet<>(after.keySet())) {
                if (before.containsKey(key)) {
                    compare(child(path, key), before.get(key), after.get(key));
                } else {
                    changed.add(child(path, key) + " (new)");
                }
            }
        } else if (original instanceof NbtList a && written instanceof NbtList b
                && a.size() == b.size() && a.size() > 0 && a.elementId() == b.elementId()) {
            for (int i = 0; i < a.size(); i++) {
                compare(path + "[" + i + "]", a.elements().get(i), b.elements().get(i));
            }
        } else if (!NbtEquals.deepEquals(normalize(original), normalize(written))) {
            changed.add(path.isEmpty() ? "<root>" : path);
        }
    }

    private static NbtTag normalize(NbtTag tag) {
        return tag instanceof NbtBoolean bool ? new NbtByte((byte) (bool.value() ? 1 : 0)) : tag;
    }

    private static String child(String path, String key) {
        return path.isEmpty() ? key : path + "." + key;
    }

    private static String preview(List<String> paths) {
        List<String> shown = paths.stream().limit(SHOWN_PATHS).toList();
        return shown + (paths.size() > SHOWN_PATHS ? " +" + (paths.size() - SHOWN_PATHS) : "");
    }
}
