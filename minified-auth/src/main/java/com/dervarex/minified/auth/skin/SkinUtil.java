package com.dervarex.minified.auth.skin;

import com.dervarex.minified.auth.user.MinecraftAccount;
import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.http.HttpUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apiguardian.api.API;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * Utilities for fetching the skin (and cape) of a Minecraft account using Mojang's session servers.
 */
@API(status = API.Status.STABLE, since = "v3.2.0")
public final class SkinUtil {
    private static final String PROFILE_ENDPOINT = "https://sessionserver.mojang.com/session/minecraft/profile/"; // todo move to minified-utils

    private SkinUtil() {}

    /**
     * Fetches the skin currently used by the given account.
     *
     * @param account the account to look up
     * @return the player's {@link Skin}, or {@code null} if the account has no skin set
     * @throws IOException   if the request could not be made
     * @throws HttpException if the session server responded with an error
     */
    public static Skin fetchSkin(MinecraftAccount account) throws IOException, HttpException {
        return fetchSkin(account.getMinecraftUUID().getUndashed());
    }

    /**
     * Fetches the skin currently used by the player with the given UUID
     *
     * @param uuid the player's UUID
     * @return the resolved {@link Skin}, or {@code null} if the account has no skin set
     * @throws IOException   if the request could not be made
     * @throws HttpException if the session server responded with an error
     */
    public static Skin fetchSkin(UUID uuid) throws IOException, HttpException {
        return fetchSkin(uuid.toString().replace("-", ""));
    }

    /**
     * Fetches the skin currently used by the player with the given UUID
     *
     * @param uuid the player's UUID (can be dashed or undashed)
     * @return the resolved {@link Skin}, or {@code null} if the account has no skin set,
     * no matching profile was found, or the profile carries no readable texture data
     * @throws IOException   if the request could not be made
     * @throws HttpException if the session server responded with an error
     */
    public static Skin fetchSkin(String uuid) throws IOException, HttpException {
        String undashed = uuid.replace("-", "");
        String json = HttpUtil.get(PROFILE_ENDPOINT + undashed + "?unsigned=false");
        JsonObject profile = JsonParser.parseString(json).getAsJsonObject();
        JsonArray properties = profile.getAsJsonArray("properties");
        if (properties == null) return null;

        for (JsonElement element : properties) {
            JsonObject property = element.getAsJsonObject();
            JsonElement name = property.get("name");
            if (name == null || !"textures".equals(name.getAsString())) continue;

            return parseTexturesProperty(property.get("value").getAsString());
        }
        return null;
    }

    private static Skin parseTexturesProperty(String base64Value) {
        String decoded = new String(Base64.getDecoder().decode(base64Value), StandardCharsets.UTF_8);
        JsonObject textures = JsonParser.parseString(decoded).getAsJsonObject().getAsJsonObject("textures");
        if (textures == null) return null;

        JsonObject skin = textures.getAsJsonObject("SKIN");
        if (skin == null || !skin.has("url")) return null;

        SkinModel model = SkinModel.CLASSIC;
        if (skin.has("metadata")) {
            JsonObject metadata = skin.getAsJsonObject("metadata");
            if (metadata.has("model")) {
                model = SkinModel.fromString(metadata.get("model").getAsString());
            }
        }

        JsonObject cape = textures.getAsJsonObject("CAPE");
        String capeUrl = (cape != null && cape.has("url")) ? cape.get("url").getAsString() : null;

        return new Skin(skin.get("url").getAsString(), model, capeUrl);
    }

    /**
     * @param account the account to fetch the default model for
     * @return the {@link SkinModel} Minecraft would assign the account by default
     * (if it never set a skin), based on its UUID
     */
    public static SkinModel getDefaultModel(MinecraftAccount account) {
        return SkinModel.getDefault(account.getMinecraftUUID().getDashed());
    }

    /**
     * @param uuid the UUID to fetch the default model for
     * @return the {@link SkinModel} Minecraft would assign that UUID by default
     * (if it never set a skin)
     */
    public static SkinModel getDefaultModel(UUID uuid) {
        return SkinModel.getDefault(uuid);
    }
}
