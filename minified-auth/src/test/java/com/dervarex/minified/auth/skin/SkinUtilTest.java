package com.dervarex.minified.auth.skin;

import com.dervarex.minified.auth.user.MinecraftAccount;
import com.dervarex.minified.auth.user.MinecraftUUID;
import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.http.HttpUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SkinUtilTest {

    private static final UUID UUID_DASHED =
            UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");
    private static final String UUID_UNDASHED = "069a79f444e94726a5befca90e38aaf5";

    private MinecraftAccount account;

    @BeforeEach
    void setUp() {
        account = mock(MinecraftAccount.class);
        MinecraftUUID uuid = mock(MinecraftUUID.class);
        when(uuid.getUndashed()).thenReturn(UUID_UNDASHED);
        when(uuid.getDashed()).thenReturn(UUID_DASHED);
        when(account.getMinecraftUUID()).thenReturn(uuid);
    }

    private static String encodeTextures(String skinUrl, String model, String capeUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"textures\":{");
        sb.append("\"SKIN\":{\"url\":\"").append(skinUrl).append("\"");
        if (model != null) {
            sb.append(",\"metadata\":{\"model\":\"").append(model).append("\"}");
        }
        sb.append("}");
        if (capeUrl != null) {
            sb.append(",\"CAPE\":{\"url\":\"").append(capeUrl).append("\"}");
        }
        sb.append("}}");
        return Base64.getEncoder().encodeToString(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String profileJson(String base64Value) {
        return """
            {
              "id": "%s",
              "name": "Notch",
              "properties": [
                {
                  "name": "textures",
                  "value": "%s"
                }
              ]
            }
            """.formatted(UUID_UNDASHED, base64Value);
    }

    @Test
    @DisplayName("fetchSkin(account) returns skin with CLASSIC model and cape")
    void fetchSkinAccountClassicWithCape() throws IOException, HttpException {
        String value = encodeTextures(
                "http://textures.minecraft.net/texture/abc",
                null,
                "http://textures.minecraft.net/texture/cape1"
        );
        String json = profileJson(value);

        try (MockedStatic<HttpUtil> http = mockStatic(HttpUtil.class)) {
            http.when(() -> HttpUtil.get(anyString())).thenReturn(json);

            Skin skin = SkinUtil.fetchSkin(account);

            assertNotNull(skin);
            assertEquals("http://textures.minecraft.net/texture/abc", skin.skinUrl());
            assertEquals(SkinModel.CLASSIC, skin.model());
            assertEquals("http://textures.minecraft.net/texture/cape1", skin.capeUrl());
            assertTrue(skin.hasCape());
        }
    }

    @Test
    @DisplayName("fetchSkin(UUID) detects SLIM model")
    void fetchSkinUuidSlimModel() throws IOException, HttpException {
        String value = encodeTextures(
                "http://textures.minecraft.net/texture/slim",
                "slim",
                null
        );
        String json = profileJson(value);

        try (MockedStatic<HttpUtil> http = mockStatic(HttpUtil.class)) {
            http.when(() -> HttpUtil.get(anyString())).thenReturn(json);

            Skin skin = SkinUtil.fetchSkin(UUID_DASHED);

            assertNotNull(skin);
            assertEquals(SkinModel.SLIM, skin.model());
            assertNull(skin.capeUrl());
            assertFalse(skin.hasCape());
        }
    }

    @Test
    @DisplayName("fetchSkin(String) accepts dashed UUID")
    void fetchSkinStringAcceptsDashedUuid() throws IOException, HttpException {
        String value = encodeTextures(
                "http://textures.minecraft.net/texture/abc",
                null,
                null
        );

        try (MockedStatic<HttpUtil> http = mockStatic(HttpUtil.class)) {
            http.when(() -> HttpUtil.get(anyString())).thenReturn(profileJson(value));

            Skin skin = SkinUtil.fetchSkin(UUID_DASHED.toString());
            assertNotNull(skin);
        }
    }

    @Test
    @DisplayName("fetchSkin calls correct endpoint with undashed UUID")
    void fetchSkinCallsCorrectEndpoint() throws IOException, HttpException {
        String value = encodeTextures("http://textures.minecraft.net/texture/abc", null, null);

        try (MockedStatic<HttpUtil> http = mockStatic(HttpUtil.class)) {
            http.when(() -> HttpUtil.get(anyString())).thenReturn(profileJson(value));

            SkinUtil.fetchSkin(account);

            http.verify(() -> HttpUtil.get(
                    "https://sessionserver.mojang.com/session/minecraft/profile/"
                            + UUID_UNDASHED + "?unsigned=false"
            ));
        }
    }

    @Test
    @DisplayName("fetchSkin returns null when properties are missing")
    void fetchSkinNullPropertiesReturnsNull() throws IOException, HttpException {
        String json = "{\"id\":\"" + UUID_UNDASHED + "\",\"name\":\"Notch\"}";

        try (MockedStatic<HttpUtil> http = mockStatic(HttpUtil.class)) {
            http.when(() -> HttpUtil.get(anyString())).thenReturn(json);

            assertNull(SkinUtil.fetchSkin(account));
        }
    }

    @Test
    @DisplayName("fetchSkin returns null when no textures property exists")
    void fetchSkinNoTexturesPropertyReturnsNull() throws IOException, HttpException {
        String json = """
            {
              "id": "%s",
              "name": "Notch",
              "properties": [
                {"name": "other", "value": "irrelevant"}
              ]
            }
            """.formatted(UUID_UNDASHED);

        try (MockedStatic<HttpUtil> http = mockStatic(HttpUtil.class)) {
            http.when(() -> HttpUtil.get(anyString())).thenReturn(json);

            assertNull(SkinUtil.fetchSkin(account));
        }
    }

    @Test
    @DisplayName("fetchSkin returns null when SKIN url is missing")
    void fetchSkinMissingSkinUrlReturnsNull() throws IOException, HttpException {
        String decoded = "{\"textures\":{\"SKIN\":{}}}";
        String value = Base64.getEncoder()
                .encodeToString(decoded.getBytes(StandardCharsets.UTF_8));

        try (MockedStatic<HttpUtil> http = mockStatic(HttpUtil.class)) {
            http.when(() -> HttpUtil.get(anyString())).thenReturn(profileJson(value));

            assertNull(SkinUtil.fetchSkin(account));
        }
    }

    @Test
    @DisplayName("fetchSkin returns null when textures object is missing")
    void fetchSkinMissingTexturesObjectReturnsNull() throws IOException, HttpException {
        String decoded = "{\"other\":\"value\"}";
        String value = Base64.getEncoder()
                .encodeToString(decoded.getBytes(StandardCharsets.UTF_8));

        try (MockedStatic<HttpUtil> http = mockStatic(HttpUtil.class)) {
            http.when(() -> HttpUtil.get(anyString())).thenReturn(profileJson(value));

            assertNull(SkinUtil.fetchSkin(account));
        }
    }

    @Test
    @DisplayName("fetchSkin propagates IOException")
    void fetchSkinPropagatesIOException() {
        try (MockedStatic<HttpUtil> http = mockStatic(HttpUtil.class)) {
            http.when(() -> HttpUtil.get(anyString()))
                    .thenThrow(new IOException("network down"));

            assertThrows(IOException.class, () -> SkinUtil.fetchSkin(account));
        }
    }

    @Test
    @DisplayName("fetchSkin propagates HttpException")
    void fetchSkinPropagatesHttpException() {
        HttpException rateLimited = new HttpException.Builder()
                .status(429)
                .statusMessage("Too Many Requests")
                .transientFailure(true)
                .build();

        try (MockedStatic<HttpUtil> http = mockStatic(HttpUtil.class)) {
            http.when(() -> HttpUtil.get(anyString())).thenThrow(rateLimited);

            assertThrows(HttpException.class, () -> SkinUtil.fetchSkin(account));
        }
    }

    @Test
    @DisplayName("getDefaultModel(account) returns SkinModel.getDefault(UUID)")
    void getDefaultModelAccount() {
        SkinModel expected = SkinModel.getDefault(UUID_DASHED);
        assertEquals(expected, SkinUtil.getDefaultModel(account));
    }

    @Test
    @DisplayName("getDefaultModel(UUID) returns SkinModel.getDefault(UUID)")
    void getDefaultModelUuid() {
        assertEquals(SkinModel.getDefault(UUID_DASHED), SkinUtil.getDefaultModel(UUID_DASHED));
    }
}