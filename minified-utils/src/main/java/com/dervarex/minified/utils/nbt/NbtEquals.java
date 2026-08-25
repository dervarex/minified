package com.dervarex.minified.utils.nbt;

import com.dervarex.minified.utils.nbt.tag.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Deep equality check for parsed nbt trees (arrays,
 * lists and such would be something like "I@35399441"
 * else, which changes without the data actually changing)
 */
public class NbtEquals {

    public static boolean deepEquals(NbtTag a, NbtTag b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (!a.getClass().equals(b.getClass())) return false;

        if (a instanceof NbtByteArray) return Arrays.equals(((NbtByteArray) a).value(), ((NbtByteArray) b).value());
        if (a instanceof NbtIntArray) return Arrays.equals(((NbtIntArray) a).value(), ((NbtIntArray) b).value());
        if (a instanceof NbtLongArray) return Arrays.equals(((NbtLongArray) a).value(), ((NbtLongArray) b).value());

        if (a instanceof NbtCompound) {
            Map<String, NbtTag> mapA = ((NbtCompound) a).asMap();
            Map<String, NbtTag> mapB = ((NbtCompound) b).asMap();
            if (!mapA.keySet().equals(mapB.keySet())) return false;
            for (String key : mapA.keySet()) {
                if (!deepEquals(mapA.get(key), mapB.get(key))) return false;
            }
            return true;
        }

        if (a instanceof NbtList) {
            List<NbtTag> listA = ((NbtList) a).elements();
            List<NbtTag> listB = ((NbtList) b).elements();
            if (listA.size() != listB.size()) return false;
            for (int i = 0; i < listA.size(); i++) {
                if (!deepEquals(listA.get(i), listB.get(i))) return false;
            }
            return true;
        }

        // Byte, Boolean, Short, Integer, Long, Float, Double, String, End - regular equals is okay here
        return a.equals(b);
    }
}