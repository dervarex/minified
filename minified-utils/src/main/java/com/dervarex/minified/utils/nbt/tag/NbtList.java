package com.dervarex.minified.utils.nbt.tag;

import java.util.ArrayList;
import java.util.List;
import org.apiguardian.api.API;

/**
 * A list of tags that all have the same id
 */
@API(status = API.Status.STABLE)
public final class NbtList implements NbtTag {
    private final byte elementId;
    private final List<NbtTag> elements = new ArrayList<>();

    /**
     * @param elementId the tag id of the elements
     */
    public NbtList(byte elementId) {
        this.elementId = elementId;
    }

    /**
     * {@return 9}
     */
    public byte id() { return 9; }
    /**
     * {@return the tag id of the elements}
     */
    public byte elementId() { return elementId; }

    /**
     * Adds a tag
     *
     * @param tag the tag
     * @throws IllegalArgumentException if its id isn't the element id
     */
    public void add(NbtTag tag) {
        if (tag.id() != elementId) {
            throw new IllegalArgumentException("Tag type mismatch in list");
        }
        elements.add(tag);
    }

    /**
     * {@return a copy of the elements}
     */
    public List<NbtTag> elements() {
        return List.copyOf(elements);
    }

    /**
     * {@return how many elements there are}
     */
    public int size() { return elements.size(); }

    /**
     * Deep copy, nested stuff included
     *
     * @return the copy
     */
    @Override
    public NbtList copy() {
        NbtList copy = new NbtList(elementId);
        for (NbtTag element : elements) copy.elements.add(element.copy());
        return copy;
    }
}
