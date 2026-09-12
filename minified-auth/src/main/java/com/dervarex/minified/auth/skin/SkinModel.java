package com.dervarex.minified.auth.skin;

import org.apiguardian.api.API;

import java.util.UUID;

/**
 * The player model a skin is rendered with
 */
@API(status = API.Status.STABLE, since = "v3.2.0")
public enum SkinModel {
    /** The wide/Steve arm model. */
    CLASSIC,
    /** The slim/Alex arm model. */
    SLIM;

    public static SkinModel fromString(String model) {
        return "slim".equalsIgnoreCase(model) ? SLIM : CLASSIC;
    }

    /**
     * Determines the default player model Minecraft assigns to a player that has no
     * skin set, based on their UUID. <br> This mirrors vanilla's own skin
     * selection ({@code uuid.hashCode() & 1}) and is used to pick a model
     * for offline-mode players.
     *
     * @param uuid the player's dashed UUID
     * @return the default {@link SkinModel} for the given UUID
     */
    public static SkinModel getDefault(UUID uuid) {
        return (uuid.hashCode() & 1) == 1 ? SLIM : CLASSIC;
    }
}
