package com.dervarex.minified.utils.sha;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hasher {
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    public static String sha1(Path path) throws IOException {
        MessageDigest digest;

        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 is not supported by this JVM", e);
        }

        try (InputStream in = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int read;

            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }

        return bytesToHex(digest.digest());
    }

    public String sha(Path path, int hashbits) throws Exception {

        MessageDigest digest = MessageDigest.getInstance("SHA-" + hashbits);

        try (InputStream in = Files.newInputStream(path)) {

            byte[] buffer = new byte[8192];

            int read;

            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return bytesToHex(digest.digest());
    }

    public static String bytesToHex(byte[] bytes) {

        char[] chars = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {

            int v = bytes[i] & 0xFF;

            chars[i * 2] = HEX[v >>> 4];
            chars[i * 2 + 1] = HEX[v & 0x0F];
        }

        return new String(chars);
    }
}
