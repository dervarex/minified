package com.dervarex.minified.utils.nbt.tag;

import java.util.ArrayList;
import java.util.List;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public final class NbtList implements NbtTag {
    private final byte elementId;
    private final List<NbtTag> elements = new ArrayList<>();

    public NbtList(byte elementId) {
        this.elementId = elementId;
    }

    public byte id() { return 9; }
    public byte elementId() { return elementId; }

    public void add(NbtTag tag) {
        if (tag.id() != elementId) {
            throw new IllegalArgumentException("Tag type mismatch in list");
        }
        elements.add(tag);
    }

    public List<NbtTag> elements() {
        return List.copyOf(elements);
    }

    public int size() { return elements.size(); }

    @Override
    public NbtList copy() {
        NbtList copy = new NbtList(elementId);
        for (NbtTag element : elements) copy.elements.add(element.copy());
        return copy;
    }
}