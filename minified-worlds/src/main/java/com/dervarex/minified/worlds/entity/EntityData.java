package com.dervarex.minified.worlds.entity;

import com.dervarex.minified.utils.nbt.tag.NbtCompound;
import com.dervarex.minified.utils.nbt.tag.NbtList;
import com.dervarex.minified.utils.nbt.tag.NbtTag;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps an entities/*.mca chunk's raw NBT
 * Each element of Entities is wrapped as a {@link LivingEntity} if it carries the
 * "Health" field (the simplest reliable marker that separates living entities from
 * items/minecarts/...), or a plain {@link Entity} otherwise.
 */
public class EntityData {
    private final NbtCompound root;
    private final NbtList entitiesList;
    private final List<Entity> entities = new ArrayList<>();

    public EntityData(NbtCompound root) {
        this.root = root;
        this.entitiesList = root.getList("Entities");

        for (NbtTag tag : entitiesList.elements()) {
            NbtCompound entityTag = (NbtCompound) tag;
            entities.add(entityTag.has("Health")
                    ? new LivingEntity(entityTag)
                    : new Entity(entityTag));
        }
    }

    public NbtCompound raw() { return root; }

    public List<Entity> entities() { return List.copyOf(entities); }

    /**
     * Adds a new entity to this chunk's entity list, wiring it into
     * both the in-memory list and the underlying NBT.
     * @return entity wrapped as an Entity or LivingEntity depending on whether it
     * carries "Health".
     */
    public Entity addEntity(NbtCompound entityData) {
        entitiesList.add(entityData);
        Entity wrapped = entityData.has("Health")
                ? new LivingEntity(entityData)
                : new Entity(entityData);
        entities.add(wrapped);
        return wrapped;
    }
}