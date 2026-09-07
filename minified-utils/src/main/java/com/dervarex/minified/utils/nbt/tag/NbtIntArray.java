package com.dervarex.minified.utils.nbt.tag;

import java.util.ArrayList;
import java.util.List;

public final class NbtIntArray implements NbtTag {
    private final int[] value;
    private final byte elementId;
    private final List<NbtTag> elements = new ArrayList<>();

    public NbtIntArray(int[] value) {
        this.value = value;
        this.elementId = 0;
    }

    public NbtIntArray(byte elementId) {
        this.value = new int[0];
        this.elementId = elementId;
    }

    public int[] value() {
        return value;
    }

    public byte id() {
        return 11;
    }

    public byte elementId() {
        return elementId;
    }

    public void add(NbtTag tag) {
        if (tag.id() != elementId) {
            throw new IllegalArgumentException("Tag type mismatch in list");
        }
        elements.add(tag);
    }

    public List<NbtTag> elements() {
        return List.copyOf(elements);
    }

    public int size() {
        return elements.size();
    }
}