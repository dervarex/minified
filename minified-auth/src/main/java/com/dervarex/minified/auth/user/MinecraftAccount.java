package com.dervarex.minified.auth.user;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface MinecraftAccount {

    /**
     * @return {@link MinecraftUUID}, contains dashed and non dashed uuid
     */
    MinecraftUUID getMinecraftUUID();

    /**
     * @return the display name of the account
     */
    String username();
}
