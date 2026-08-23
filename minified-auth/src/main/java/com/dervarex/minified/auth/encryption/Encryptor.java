package com.dervarex.minified.auth.encryption;

import com.dervarex.minified.auth.events.encryption.CreateMasterKeyEvent;
import com.dervarex.minified.auth.events.encryption.LoadEncryptedSessionEvent;
import com.dervarex.minified.auth.events.encryption.SaveEncryptedSessionEvent;
import com.dervarex.minified.events.EventBus;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apiguardian.api.API;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;

@API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.auth.*"})
public class Encryptor {
    private static final Gson GSON = new Gson();

    /**
     * Loads the master key, if it already exists, or creates a new one and saves it to disk.
     *
     * @param keyFile the file to check for
     * @return the SecretKey that will be used to encrypt and decrypt the refreshtoken
     * @throws Exception when something goes wrong with file access or key generation
     */
    public static SecretKey loadOrCreateMasterKey(Path keyFile, EventBus eventBus) throws Exception {
        if (Files.exists(keyFile)) {
            byte[] rawKey = Files.readAllBytes(keyFile);
            System.out.println("Loaded existing master key");
            return new SecretKeySpec(rawKey, "AES");
        } else {
            System.out.println("Creating new master key");
            eventBus.post(new CreateMasterKeyEvent(keyFile));
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256, new SecureRandom());
            SecretKey key = keyGen.generateKey();
            Files.write(keyFile, key.getEncoded());
            return key;
        }
    }

    /**
     * Encrypts the session json and saves it to disk.
     * <p>
     * The session can be loaded again with {@link #loadEncryptedSession(Path, SecretKey, EventBus)}
     *
     * @param sessionJson the JSON object of the Session
     * @param masterKey   the SecretKey to encrypt and decrypt the session
     * @param sessionFile the File to save the Session to
     * @throws Exception when encrypting or writing to the file fails
     */
    public static void saveEncryptedSession(JsonObject sessionJson, SecretKey masterKey, Path sessionFile, EventBus eventBus) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, masterKey);
        byte[] encrypted = cipher.doFinal(sessionJson.toString().getBytes());
        Files.write(sessionFile, Base64.getEncoder().encode(encrypted));
        System.out.println("Session saved to " + sessionFile);
        eventBus.post(new SaveEncryptedSessionEvent(sessionFile));
    }

    /**
     * Loads the encrypted session from disk and returns the decrypted json object.
     *
     * @param sessionFile the file where it will load the session from
     * @param masterKey   the master key, used to decrypt the file
     * @return the decrypted session as a JsonObject, or null if no session file exists
     */
    public static JsonObject loadEncryptedSession(Path sessionFile, SecretKey masterKey, EventBus eventBus) throws Exception {
        if (!Files.exists(sessionFile)) {
            System.out.println("No saved session");
            return null;
        }
        byte[] encrypted = Base64.getDecoder().decode(Files.readAllBytes(sessionFile));
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, masterKey);
        String json = new String(cipher.doFinal(encrypted));
        System.out.println("Loaded saved session");
        eventBus.post(new LoadEncryptedSessionEvent(sessionFile));
        return GSON.fromJson(json, JsonObject.class);
    }
}
