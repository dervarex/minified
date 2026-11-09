package com.dervarex.minified.auth.user;

import org.apiguardian.api.API;

import java.util.Objects;
import java.util.UUID;

/**
 * A Minecraft UUID, saved with and without dashes.
 * The Official Minecraft API takes {@link uuid}, but for processing it's better to use {@link dashedUUID}
 */
@API(status = API.Status.STABLE)
public class MinecraftUUID {
    private final UUID dashedUUID;
    private final String uuid;

    /**
     * A Minecraft UUID
     *
     * @param dashedUUID the UUID
     */
    public MinecraftUUID(UUID dashedUUID) {
        this.dashedUUID = dashedUUID;
        this.uuid = dashedUUID.toString().replace("-", "");
    }

    /**
     * {@return the UUID with dashes}
     */
    public UUID getDashed() {
        return dashedUUID;
    }

    /**
     * {@return the UUID without dashes}
     */
    public String getUndashed() {
        return uuid;
    }

    /**
     * {@return true if it's the same UUID}
     *
     * @param o the other object
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MinecraftUUID)) return false;
        MinecraftUUID other = (MinecraftUUID) o;
        return Objects.equals(dashedUUID, other.dashedUUID);
    }

    /**
     * {@return the hash code of the UUID}
     */
    @Override
    public int hashCode() {
        return Objects.hash(dashedUUID);
    }

    /**
     * {@return the dashed UUID}
     */
    @Override
    public String toString() {
        return "MinecraftUUID[" + dashedUUID + ']';
    }
}