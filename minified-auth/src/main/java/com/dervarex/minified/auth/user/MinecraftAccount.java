package com.dervarex.minified.auth.user;

import org.apiguardian.api.API;

/**
 * A Minecraft account, online or offline
 */
@API(status = API.Status.STABLE)
public interface MinecraftAccount {

    /**
     * {@return the account's UUID}
     */
    MinecraftUUID getMinecraftUUID();

    /**
     * {@return the display name}
     */
    String username();
}
