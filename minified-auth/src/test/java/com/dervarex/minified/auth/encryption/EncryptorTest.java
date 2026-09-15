package com.dervarex.minified.auth.encryption;

import com.dervarex.minified.events.EventBus;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Apparently underscores are more readable than a method named loadEncryptedSessionwithValidBase64ButGarbagethrowsException... someone really could've mentioned that beforehand.
class EncryptorTest {

    @TempDir
    Path tempDir;

    private Path keyFile;
    private Path sessionFile;
    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        keyFile = tempDir.resolve("master.key");
        sessionFile = tempDir.resolve("session.enc");
        eventBus = new EventBus();
    }

    @Test
    void loadOrCreateMasterKey_createsNewKeyWhenMissing() throws Exception {
        assertFalse(Files.exists(keyFile));

        SecretKey key = Encryptor.loadOrCreateMasterKey(keyFile, eventBus);

        assertNotNull(key);
        assertEquals("AES", key.getAlgorithm());
        assertEquals(32, key.getEncoded().length, "AES-256 key must be 32 bytes");
        assertTrue(Files.exists(keyFile), "key file must be created");
        assertArrayEquals(key.getEncoded(), Files.readAllBytes(keyFile),
                "written key bytes must match returned key");
    }

    @Test
    void loadOrCreateMasterKey_loadsExistingKey() throws Exception {
        byte[] existing = new byte[32];
        for (int i = 0; i < existing.length; i++) {
            existing[i] = (byte) i;
        }
        Files.write(keyFile, existing);

        SecretKey key = Encryptor.loadOrCreateMasterKey(keyFile, eventBus);

        assertNotNull(key);
        assertEquals("AES", key.getAlgorithm());
        assertArrayEquals(existing, key.getEncoded());
    }

    @Test
    void loadOrCreateMasterKey_isIdempotent() throws Exception {
        SecretKey first = Encryptor.loadOrCreateMasterKey(keyFile, eventBus);
        SecretKey second = Encryptor.loadOrCreateMasterKey(keyFile, eventBus);

        assertArrayEquals(first.getEncoded(), second.getEncoded(),
                "loading the same key file twice must yield the same key");
    }

    @Test
    void loadOrCreateMasterKey_doesntOverwriteExistingFile() throws Exception {
        byte[] existing = new byte[32];
        Files.write(keyFile, existing);

        Encryptor.loadOrCreateMasterKey(keyFile, eventBus);

        assertArrayEquals(existing, Files.readAllBytes(keyFile),
                "existing key file must not be overwritten");
    }

    @Test
    void saveEncryptedSession_writesBase64EncodedData() throws Exception {
        SecretKey key = Encryptor.loadOrCreateMasterKey(keyFile, eventBus);
        JsonObject session = new JsonObject();
        session.addProperty("token", "super-secret-value");

        Encryptor.saveEncryptedSession(session, key, sessionFile, eventBus);

        assertTrue(Files.exists(sessionFile));
        byte[] raw = Files.readAllBytes(sessionFile);
        byte[] decoded = Base64.getDecoder().decode(raw);

        assertTrue(decoded.length > 0, "decoded payload must not be empty");
        assertFalse(new String(decoded).contains("super-secret-value"),
                "plaintext must not be recoverable from raw decoded bytes");
    }

    @Test
    void saveEncryptedSession_createsFileWhenMissing() throws Exception {
        SecretKey key = Encryptor.loadOrCreateMasterKey(keyFile, eventBus);
        JsonObject session = new JsonObject();
        session.addProperty("k", "v");

        assertFalse(Files.exists(sessionFile));
        Encryptor.saveEncryptedSession(session, key, sessionFile, eventBus);
        assertTrue(Files.exists(sessionFile));
    }

    @Test
    void saveEncryptedSession_overwritesExistingFile() throws Exception {
        SecretKey key = Encryptor.loadOrCreateMasterKey(keyFile, eventBus);

        JsonObject first = new JsonObject();
        first.addProperty("v", "first");
        Encryptor.saveEncryptedSession(first, key, sessionFile, eventBus);

        JsonObject second = new JsonObject();
        second.addProperty("v", "second");
        Encryptor.saveEncryptedSession(second, key, sessionFile, eventBus);

        JsonObject loaded = Encryptor.loadEncryptedSession(sessionFile, key, eventBus);
        assertEquals("second", loaded.get("v").getAsString());
    }

    @Test
    void loadEncryptedSession_returnsNullWhenFileMissing() throws Exception {
        SecretKey key = Encryptor.loadOrCreateMasterKey(keyFile, eventBus);

        assertFalse(Files.exists(sessionFile));
        JsonObject loaded = Encryptor.loadEncryptedSession(sessionFile, key, eventBus);

        assertNull(loaded, "missing session file must produce null, not an exception");
    }

    // Roundtrips

    @Test
    void saveAndLoadEncryptedSession_roundtrip() throws Exception {
        SecretKey key = Encryptor.loadOrCreateMasterKey(keyFile, eventBus);

        JsonObject session = new JsonObject();
        session.addProperty("accessToken", "my-access-token");
        session.addProperty("uuid", "069a79f4-44e9-4726-a5be-fca90e38aaf5");
        session.addProperty("name", "Notch");

        Encryptor.saveEncryptedSession(session, key, sessionFile, eventBus);
        JsonObject loaded = Encryptor.loadEncryptedSession(sessionFile, key, eventBus);

        assertNotNull(loaded);
        assertEquals(session, loaded);
        assertEquals("my-access-token", loaded.get("accessToken").getAsString());
        assertEquals("069a79f4-44e9-4726-a5be-fca90e38aaf5", loaded.get("uuid").getAsString());
    }

    @Test
    void roundtrip_withKeyReloadedFromDisk() throws Exception {
        SecretKey key = Encryptor.loadOrCreateMasterKey(keyFile, eventBus);

        JsonObject session = new JsonObject();
        session.addProperty("x", "y");
        Encryptor.saveEncryptedSession(session, key, sessionFile, eventBus);

        // reload the key from disk
        SecretKey reloaded = Encryptor.loadOrCreateMasterKey(keyFile, eventBus);
        JsonObject loaded = Encryptor.loadEncryptedSession(sessionFile, reloaded, eventBus);

        assertEquals(session, loaded);
    }

    @Test
    void roundtrip_withNestedAndUnicodeContent() throws Exception {
        SecretKey key = Encryptor.loadOrCreateMasterKey(keyFile, eventBus);

        JsonObject session = new JsonObject();
        JsonObject nested = new JsonObject();
        nested.addProperty("a", 1);
        nested.addProperty("b", true);
        nested.addProperty("c", 3.14);
        session.add("nested", nested);
        session.addProperty("unicode", "héllo wörld 日本語 🎉"); // yes, this is intentional, it's to see if those characters work too
        session.addProperty("nullValue", (String) null);

        Encryptor.saveEncryptedSession(session, key, sessionFile, eventBus);
        JsonObject loaded = Encryptor.loadEncryptedSession(sessionFile, key, eventBus);

        assertEquals(session, loaded);
        assertEquals("héllo wörld 日本語 🎉", loaded.get("unicode").getAsString());
        assertEquals(1, loaded.getAsJsonObject("nested").get("a").getAsInt());
        assertTrue(loaded.getAsJsonObject("nested").get("b").getAsBoolean());
    }

    @Test
    void roundtrip_emptyJsonObject() throws Exception {
        SecretKey key = Encryptor.loadOrCreateMasterKey(keyFile, eventBus);

        JsonObject session = new JsonObject();
        Encryptor.saveEncryptedSession(session, key, sessionFile, eventBus);

        JsonObject loaded = Encryptor.loadEncryptedSession(sessionFile, key, eventBus);
        assertNotNull(loaded);
        assertEquals(0, loaded.size());
    }

    @Test
    void roundtrip_largePayload() throws Exception {
        SecretKey key = Encryptor.loadOrCreateMasterKey(keyFile, eventBus);

        JsonObject session = new JsonObject();
        StringBuilder big = new StringBuilder();
        for (int i = 0; i < 10_000; i++) {
            big.append("abcdefghij");
        }
        session.addProperty("blob", big.toString());

        Encryptor.saveEncryptedSession(session, key, sessionFile, eventBus);
        JsonObject loaded = Encryptor.loadEncryptedSession(sessionFile, key, eventBus);

        assertEquals(big.toString(), loaded.get("blob").getAsString());
    }

    // Should error

    @Test
    void loadEncryptedSession_withWrongKey_throwsException() throws Exception {
        SecretKey key = Encryptor.loadOrCreateMasterKey(keyFile, eventBus);
        JsonObject session = new JsonObject();
        session.addProperty("token", "value");
        Encryptor.saveEncryptedSession(session, key, sessionFile, eventBus);

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, new SecureRandom());
        SecretKey otherKey = keyGen.generateKey();

        assertThrows(Exception.class,
                () -> Encryptor.loadEncryptedSession(sessionFile, otherKey, eventBus));
    }

    @Test
    void loadEncryptedSession_withInvalidBase64_throwsException() throws Exception {
        SecretKey key = Encryptor.loadOrCreateMasterKey(keyFile, eventBus);
        Files.write(sessionFile, "!!! this is not valid base64 !!!".getBytes());

        assertThrows(IllegalArgumentException.class,
                () -> Encryptor.loadEncryptedSession(sessionFile, key, eventBus));
    }

    @Test
    void loadEncryptedSession_withValidBase64ButGarbage_throwsException() throws Exception {
        SecretKey key = Encryptor.loadOrCreateMasterKey(keyFile, eventBus);
        Files.write(sessionFile, Base64.getEncoder().encode("too short".getBytes()));

        assertThrows(Exception.class,
                () -> Encryptor.loadEncryptedSession(sessionFile, key, eventBus));
    }

    @Test
    void loadEncryptedSession_withTruncatedCiphertext_throwsException() throws Exception {
        SecretKey key = Encryptor.loadOrCreateMasterKey(keyFile, eventBus);

        JsonObject session = new JsonObject();
        session.addProperty("token", "value");
        Encryptor.saveEncryptedSession(session, key, sessionFile, eventBus);

        // cut the ciphertext in half
        byte[] raw = Files.readAllBytes(sessionFile);
        byte[] decoded = Base64.getDecoder().decode(raw);
        byte[] truncated = new byte[decoded.length / 2];
        System.arraycopy(decoded, 0, truncated, 0, truncated.length);
        Files.write(sessionFile, Base64.getEncoder().encode(truncated));

        assertThrows(Exception.class,
                () -> Encryptor.loadEncryptedSession(sessionFile, key, eventBus));
    }
}