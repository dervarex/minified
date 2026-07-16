package com.dervarex.minified.launch.launch.modding.forge.installer;

import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.modding.forge.api.ForgeInstallerFetcher;
import com.dervarex.minified.launch.launch.modding.forge.api.ForgeVersionFetcher;
import com.dervarex.minified.launch.utils.DownloadHelper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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

public class ForgeInstallerInjector {
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

    private static void downloadInstaller(
            LaunchConfiguration configurator,
            String loaderVersion
    ) {
        String url = ForgeInstallerFetcher.getInstallerLink(
                loaderVersion
        );

        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        String sha1 = "";

        try {
            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(url + ".sha1"))
                            .timeout(Duration.ofSeconds(10))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                System.out.println("Link: " + url + sha1);
                throw new IOException(
                        "Failed to fetch SHA1: HTTP " + response.statusCode()
                );
            }

            sha1 = response.body().trim();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        Path installerPath = configurator
                .getJarFile()
                .getParent()
                .resolve("forge-installer.jar");

        DownloadHelper.download(
                url,
                installerPath,
                sha1
        );
    }

    private static void install(URLClassLoader loader, File target, File installerFile, Consumer<String> logState)
            throws ReflectiveOperationException {

        Class<?> utilClass = loader.loadClass("net.minecraftforge.installer.json.Util");
        Method loadInstallProfile = utilClass.getMethod("loadInstallProfile");
        Object profile = loadInstallProfile.invoke(null);

        Class<?> clientInstallClass = loader.loadClass("net.minecraftforge.installer.actions.ClientInstall");
        Class<?> callbackInterface = loader.loadClass("net.minecraftforge.installer.actions.ProgressCallback");

        Object monitor = Proxy.newProxyInstance(
                callbackInterface.getClassLoader(),
                new Class<?>[]{callbackInterface},
                (proxy, method, args) -> {
                    if ("message".equals(method.getName()) && args != null && args.length > 0 && args[0] != null) {
                        logState.accept(String.valueOf(args[0]));
                    }
                    return null;
                }
        );

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

        Method run = clientInstallClass.getMethod("run", File.class, File.class);
        Object result = run.invoke(clientInstall, target, installerFile);

        if (result instanceof Boolean ok && !ok) {
            throw new IllegalStateException("Forge installer reported failure");
        }
    }

    public void install(LaunchConfiguration config, String version) {
        prepare(config);
        ForgeVersionFetcher forgeVersionFetcher = new ForgeVersionFetcher();
        downloadInstaller(config, forgeVersionFetcher.getLatest(version));
        try {
            install(
                    new URLClassLoader(new java.net.URL[]{
                            Path.of(config.getJarFile().getParent().toAbsolutePath().toString(), "forge-installer.jar").toUri().toURL()
                    }),
                    config.getJarFile().getParent().toFile(),
                    Path.of(config.getJarFile().getParent().toAbsolutePath().toString(),"forge-installer.jar").toFile(),
                    System.out::println
            );
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
