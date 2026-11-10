package com.dervarex.minified.launch.profile;

import com.dervarex.minified.launch.exceptions.download.DownloadException;
import com.dervarex.minified.launch.exceptions.download.HttpDownloadException;
import com.dervarex.minified.launch.exceptions.profile.FailedToInstallProfileFileException;
import com.dervarex.minified.utils.sha.Hasher;
import org.apiguardian.api.API;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Puts the {@link ProfileFile}s of a profile into its game directory
 */
@API(status = API.Status.EXPERIMENTAL)
public class ProfileInstaller {
    private static final Map<String, String> ALGORITHMS = Map.of(
            "sha1", "SHA-1",
            "sha256", "SHA-256",
            "sha512", "SHA-512"
    );

    private static final String USER_AGENT = "dervarex/minified (https://github.com/dervarex/minified)";

    /**
     * Downloads the files of a profile into {@link Profile#gameDirectory()}
     *
     * @param profile the profile
     * @return the files that are missing and have no download URL, they have to be copied over manually
     * @throws FailedToInstallProfileFileException if a file could not be downloaded or its hash doesn't match
     */
    public static List<ProfileFile> install(Profile profile) {
        // todo: maybe include the missing files? but then we'd need a zip instead of a json...
        return install(profile.files(), profile.gameDirectory());
    }

    /**
     * Downloads files into a game directory
     *
     * @param files the files to install, the ones the client doesn't support are skipped
     * @param gameDirectory the directory the paths of the files are relative to
     * @return the files that are missing and have no download URL, they have to be copied over manually
     * @throws FailedToInstallProfileFileException if a file could not be downloaded or its hash doesn't match
     */
    public static List<ProfileFile> install(List<ProfileFile> files, Path gameDirectory) {
        Path root = gameDirectory.toAbsolutePath().normalize();
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        List<ProfileFile> missing = new ArrayList<>();
        for (ProfileFile file : files) {
            if (file.client() == SideRequirement.UNSUPPORTED) {
                continue;
            }
            if (!installFile(file, root, client)) {
                missing.add(file);
            }
        }
        return missing;
    }

    /**
     * {@return where the file is installed to, with the {@code .disabled} suffix if it's turned off}
     *
     * @param file the file
     * @param gameDirectory the directory the path of the file is relative to
     * @throws IllegalArgumentException if the path points outside of the game directory
     */
    public static Path resolve(ProfileFile file, Path gameDirectory) {
        Path root = gameDirectory.toAbsolutePath().normalize();
        Path target = root.resolve(file.path() + (file.enabled() ? "" : ProfileFile.DISABLED_SUFFIX)).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException(file.path() + " points outside of " + gameDirectory);
        }
        return target;
    }

    /**
     * @return false if the file is missing and has no download URL
     */
    private static boolean installFile(ProfileFile file, Path root, HttpClient client) {
        Path target = resolve(file, root);
        Path toggled = resolve(file.toBuilder().enabled(!file.enabled()).build(), root);

        try {
            if (Files.exists(target) && matches(file, target)) {
                return true;
            }
            // the file was turned on or off since it was installed
            if (Files.exists(toggled) && matches(file, toggled)) {
                Files.move(toggled, target, StandardCopyOption.REPLACE_EXISTING);
                return true;
            }
        } catch (IOException e) {
            throw new FailedToInstallProfileFileException("Failed to check " + target, file, e);
        }

        if (file.downloads().isEmpty()) {
            return false;
        }

        Exception lastFailure = null;
        // the URLs are mirrors, the first one that works will be used
        for (String url : file.downloads()) {
            try {
                download(url, file, target, client);
                return true;
            } catch (IOException | DownloadException | IllegalArgumentException e) {
                lastFailure = e;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new FailedToInstallProfileFileException("Interrupted while downloading " + file.path(), file, e);
            }
        }
        throw new FailedToInstallProfileFileException("Failed to download " + file.path(), file, lastFailure);
    }

    private static void download(String url, ProfileFile file, Path target, HttpClient client)
            throws IOException, InterruptedException {
        Files.createDirectories(target.getParent());
        Path temp = target.resolveSibling(target.getFileName() + ".download");

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("User-Agent", USER_AGENT)
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                response.body().close();
                throw new HttpDownloadException("HTTP " + response.statusCode() + " for " + url, response.statusCode());
            }

            try (InputStream in = response.body()) {
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            }

            if (!matches(file, temp)) {
                throw new DownloadException("Hash or size of " + file.path() + " from " + url + " doesn't match");
            }

            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    /**
     * Checks every hash we know the algorithm of and the size. A file without either is taken as it is
     */
    private static boolean matches(ProfileFile file, Path path) throws IOException {
        if (file.size() >= 0 && Files.size(path) != file.size()) {
            return false;
        }

        for (Map.Entry<String, String> hash : file.hashes().entrySet()) {
            String algorithm = ALGORITHMS.get(hash.getKey());
            if (algorithm != null && !hash.getValue().equalsIgnoreCase(hash(path, algorithm))) {
                return false;
            }
        }
        return true;
    }

    private static String hash(Path path, String algorithm) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(algorithm + " is not supported by this JVM", e);
        }

        try (InputStream in = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return Hasher.bytesToHex(digest.digest());
    }
}
