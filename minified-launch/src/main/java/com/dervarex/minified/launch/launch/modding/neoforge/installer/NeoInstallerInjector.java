package com.dervarex.minified.launch.launch.modding.neoforge.installer;

import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.modding.neoforge.api.NeoInstallerFetcher;
import com.dervarex.minified.launch.launch.modding.neoforge.api.NeoVersionFetcher;
import com.dervarex.minified.launch.utils.DownloadHelper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLClassLoader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.Consumer;

public class NeoInstallerInjector {
    private static final String INSTALLER_FILE_NAME = "neoforge-installer.jar";

    private static void prepare(LaunchConfiguration configurator) {
        Path gameDir = configurator.getJarFile().getParent();
        try {
            Files.createDirectories(gameDir);
            Files.createDirectories(gameDir.resolve("versions"));
            Files.createDirectories(gameDir.resolve("libraries"));
            Path launcherProfiles = gameDir.resolve("launcher_profiles.json");

            if (!Files.exists(launcherProfiles)) {
                Files.writeString(
                        launcherProfiles,
                        """
                                {
                                  "profiles": {}
                                }
                                """
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path downloadInstaller(
            LaunchConfiguration configurator,
            String versionOrMinecraftVersion
    ) {
        NeoVersionFetcher versionFetcher = new NeoVersionFetcher();
        String neoForgeVersion = versionFetcher.resolveLoaderVersion(versionOrMinecraftVersion);
        String url = NeoInstallerFetcher.getInstallerLink(neoForgeVersion);

        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        String sha1;

        try {
            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(url + ".sha1"))
                            .timeout(Duration.ofSeconds(10))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new IOException("Failed to fetch SHA1: HTTP " + response.statusCode());
            }

            sha1 = response.body().trim();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        Path installerPath = configurator
                .getJarFile()
                .getParent()
                .resolve(INSTALLER_FILE_NAME);

        DownloadHelper.download(url, installerPath, sha1);
        return installerPath;
    }

    private static void install(URLClassLoader loader, File target, File installerFile, Consumer<String> logState)
            throws ReflectiveOperationException {

        Class<?> utilClass = loader.loadClass("net.minecraftforge.installer.json.Util");
        Method loadInstallProfile = utilClass.getMethod("loadInstallProfile");
        Object profile = loadInstallProfile.invoke(null);

        Class<?> clientInstallClass = loader.loadClass("net.minecraftforge.installer.actions.ClientInstall");
        Class<?> callbackInterface = loader.loadClass("net.minecraftforge.installer.actions.ProgressCallback");

        Object monitor =
                callbackInterface
                        .getDeclaredField("TO_STD_OUT")
                        .get(null);

        Constructor<?> ctor = null;
        for (Constructor<?> c : clientInstallClass.getConstructors()) {
            Class<?>[] params = c.getParameterTypes();
            if (params.length == 2
                    && params[0].isAssignableFrom(profile.getClass())
                    && params[1].isAssignableFrom(callbackInterface)) {
                ctor = c;
                break;
            }
        }

        if (ctor == null) {
            throw new NoSuchMethodException("No matching ClientInstall constructor found");
        }

        Object clientInstall = ctor.newInstance(profile, monitor);

        Method run = clientInstallClass.getMethod(
                "run",
                File.class,
                java.util.function.Predicate.class,
                File.class
        );

        java.util.function.Predicate<String> optionals =
                value -> true;

        Object result = run.invoke(
                clientInstall,
                target,
                optionals,
                installerFile
        );

        if (result instanceof Boolean ok && !ok) {
            throw new IllegalStateException("NeoForge installer reported failure");
        }
    }

    public void install(LaunchConfiguration config, String versionOrMinecraftVersion) {
        prepare(config);
        Path installerPath = downloadInstaller(config, versionOrMinecraftVersion);
        try (URLClassLoader loader = new URLClassLoader(new java.net.URL[]{installerPath.toUri().toURL()})) {
            install(
                    loader,
                    config.getJarFile().getParent().toFile(),
                    installerPath.toFile(),
                    System.out::println
            );
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
