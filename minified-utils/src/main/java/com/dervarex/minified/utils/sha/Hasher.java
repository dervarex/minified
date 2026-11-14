package com.dervarex.minified.utils.sha;

import com.dervarex.minified.utils.exceptions.HashFileException;
import com.dervarex.minified.utils.exceptions.InvalidHashBitsException;
import com.dervarex.minified.utils.exceptions.UnsupportedHashAlgorithmException;
import org.apiguardian.api.API;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hashing utilities
 */
@API(status=API.Status.STABLE)
public class Hasher {

    private Hasher() {}
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    /**
     * Generates the SHA-1 has of the file at the given path and returns it as a lowercase hexadecimal string.
     * <p>Since the file is streamed using an 8 KiB buffer, this should work for large files too without nuking the memory.
     * @param path file to compute the SHA-1 hash of, may not be null
     * @return the SHA-1 hash of the file contents, as a lowercase hexadecimal string (40 characters)
     * @throws HashFileException if the file can't be opened or read
     * @throws NullPointerException if {@code path} is {@code null}
     * @throws UnsupportedHashAlgorithmException if the JVM does not support the SHA-1 algorithm (should not happen on any normal JVM)
     */
    public static String sha1(Path path) throws HashFileException {
        return sha(path, 1);
    }

    /**
     * Generates the SHA hash of the file at the given path using the given sha algorithm
     * and returns it as a lowercase hexadecimal string.
     * <p>Since the file is streamed using an 8 KiB buffer, this should work for large files too without nuking the memory.
     *
     * <p><strong>Security note:</strong> SHA-1 ({@code hashbits == 1}) is considered
     * cryptographically broken and must not be used for collision-resistant purposes
     * such as digital signatures or certificate validation. Prefer {@code 256} or
     * stronger for security-sensitive use.
     *
     * @param path file to compute the SHA hash of, may not be null
     * @param hashbits the SHA variant to use, e.g. {@code 1} for SHA-1 or {@code 256} for SHA-256
     * @return the SHA hash of the file contents, as a lowercase hexadecimal string
     * @throws UnsupportedHashAlgorithmException if your JVM doesn't support that hash algorithm
     * @throws HashFileException if the file could not be opened or read
     * @throws NullPointerException if {@code path} is {@code null}
     */
    public static String sha(Path path, int hashbits) throws HashFileException {

        if (hashbits != 1 && (hashbits <= 0 || hashbits % 8 != 0)) {
            throw new InvalidHashBitsException(hashbits);
        }

        String algorithmName = (hashbits == 1) ? "SHA-1" : "SHA-" + hashbits;
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedHashAlgorithmException(algorithmName, e);
        }

        try (InputStream in = Files.newInputStream(path)) {

            byte[] buffer = new byte[8192];

            int read;

            try {
                while ((read = in.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            } catch (IOException e) {
                throw new HashFileException(path, HashFileException.Phase.READ, e);
            }
        } catch (IOException e) {
            throw new HashFileException(path, HashFileException.Phase.OPEN, e);
        }
        return bytesToHex(digest.digest());
    }

    /**
     * Encodes the given byte array as a lowercase hexadecimal string
     *
     * @param bytes the bytes to encode, may not be {@code null}
     * @return a lowercase hexadecimal string of length {@code 2 * bytes.length}
     * @throws NullPointerException if {@code bytes} is {@code null}
     */
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