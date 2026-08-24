package com.dervarex.minified.utils.nbt;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Deep equality check for parsed nbt trees (arrays,
 * lists and such would be something like "I@35399441"
 * else, which changes without the data actually changing)
 */
public class NbtEquals {

    public static boolean deepEquals(Object a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (!a.getClass().equals(b.getClass())) return false;

        if (a instanceof byte[]) return Arrays.equals((byte[]) a, (byte[]) b);
        if (a instanceof int[]) return Arrays.equals((int[]) a, (int[]) b);
        if (a instanceof long[]) return Arrays.equals((long[]) a, (long[]) b);

        if (a instanceof Map) {
            Map<?, ?> mapA = (Map<?, ?>) a;
            Map<?, ?> mapB = (Map<?, ?>) b;
            if (!mapA.keySet().equals(mapB.keySet())) return false;
            for (Object key : mapA.keySet()) {
                if (!deepEquals(mapA.get(key), mapB.get(key))) return false;
            }
            return true;
        }

        if (a instanceof List) {
            List<?> listA = (List<?>) a;
            List<?> listB = (List<?>) b;
            if (listA.size() != listB.size()) return false;
            for (int i = 0; i < listA.size(); i++) {
                if (!deepEquals(listA.get(i), listB.get(i))) return false;
            }
            return true;
        }

        // Byte, Short, Integer, Long, Float, Double, String - regular equals is okay here
        return a.equals(b);
    }
}