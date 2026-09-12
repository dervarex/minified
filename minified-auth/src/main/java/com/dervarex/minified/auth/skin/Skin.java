package com.dervarex.minified.auth.skin;

import org.apiguardian.api.API;

import java.util.Objects;

/**
 * A resolved skin (and optional cape) for a Minecraft account, fetched from Mojang's session servers.
 * <br><br>
 * {@link #skinUrl} is the direct URL to the skin texture (hosted on textures.minecraft.net) <br>
 * {@link #model} is the player model the skin should be rendered with <br>
 * {@link #capeUrl} is the direct URL to the cape texture, or {@code null} if the account has no cape <br>
 */
@API(status = API.Status.STABLE, since = "v3.2.0")
public final class Skin {
    private final String skinUrl;
    private final SkinModel model;
    private final String capeUrl;

    public Skin(String skinUrl, SkinModel model, String capeUrl) {
        this.skinUrl = skinUrl;
        this.model = model;
        this.capeUrl = capeUrl;
    }

    public String skinUrl() {
        return skinUrl;
    }

    public SkinModel model() {
        return model;
    }

    public String capeUrl() {
        return capeUrl;
    }

    public boolean hasCape() {
        return capeUrl != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Skin)) return false;
        Skin other = (Skin) o;
        return Objects.equals(skinUrl, other.skinUrl)
                && model == other.model
                && Objects.equals(capeUrl, other.capeUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skinUrl, model, capeUrl);
    }

    @Override
    public String toString() {
        return "Skin[" +
                "skinUrl=" + skinUrl + ", " +
                "model=" + model + ", " +
                "capeUrl=" + capeUrl + ']';
    }
}
