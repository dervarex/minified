package com.dervarex.minified.auth.user;

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
}