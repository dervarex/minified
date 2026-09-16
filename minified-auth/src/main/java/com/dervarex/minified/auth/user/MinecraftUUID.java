package com.dervarex.minified.auth.user;

import java.util.Objects;
import java.util.UUID;

public class MinecraftUUID {
    private final UUID dashedUUID;
    private final String uuid;

    public MinecraftUUID(UUID dashedUUID) {
        this.dashedUUID = dashedUUID;
        this.uuid = dashedUUID.toString().replace("-", "");
    }

    public UUID getDashed() {
        return dashedUUID;
    }

    public String getUndashed() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MinecraftUUID)) return false;
        MinecraftUUID other = (MinecraftUUID) o;
        return Objects.equals(dashedUUID, other.dashedUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dashedUUID);
    }

    @Override
    public String toString() {
        return "MinecraftUUID[" + dashedUUID + ']';
    }
}