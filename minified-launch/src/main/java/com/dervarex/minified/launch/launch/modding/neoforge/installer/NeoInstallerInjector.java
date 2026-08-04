package com.dervarex.minified.launch.launch.modding.neoforge.installer;

import com.dervarex.minified.launch.events.loader.InstallNeoforgeEvent;
import com.dervarex.minified.launch.exceptions.download.DownloadException;
import com.dervarex.minified.launch.exceptions.loader.neoforge.NeoforgeInstallerReportFailureException;
import com.dervarex.minified.launch.exceptions.loader.neoforge.NeoforgePreparationException;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.LaunchContext;
import com.dervarex.minified.launch.launch.modding.neoforge.api.NeoInstallerFetcher;
import com.dervarex.minified.launch.launch.modding.neoforge.api.NeoVersionFetcher;
import com.dervarex.minified.utils.download.DownloadHelper;

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

    private static void prepare(LaunchContext context) {
        context.getEventBus().post(new InstallNeoforgeEvent(InstallNeoforgeEvent.Stage.PREPARING, context.getLaunchConfiguration().getLoader().mcVersion(), context.getLaunchConfiguration().getLoader().loaderVersion()));
        Path gameDir = context.getLaunchConfiguration().getJarFile().getParent();
        try {
            Files.createDirectories(gameDir);
            Files.createDirectories(gameDir.resolve("versions"));
            Files.createDirectories(gameDir.resolve("libraries"));
            Path launcherProfiles = gameDir.resolve("launcher_profiles.json");

            if (!Files.exists(launcherProfiles)) {
                context.getEventBus().post(new InstallNeoforgeEvent(InstallNeoforgeEvent.Stage.WRITING_PROFILE, context.getLaunchConfiguration().getLoader().mcVersion(), context.getLaunchConfiguration().getLoader().loaderVersion()));
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
            throw new NeoforgePreparationException(e);
        }
    }

    private static Path downloadInstaller(
            LaunchContext context
    ) {
        context.getEventBus().post(new InstallNeoforgeEvent(InstallNeoforgeEvent.Stage.DOWNLOADING_INSTALLER, context.getLaunchConfiguration().getLoader().mcVersion(), context.getLaunchConfiguration().getLoader().loaderVersion()));
        NeoVersionFetcher versionFetcher = new NeoVersionFetcher();
        String neoForgeVersion = versionFetcher.resolveLoaderVersion(context.getLaunchConfiguration().getLoader().mcVersion());
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
            throw new DownloadException(e);
        }

        Path installerPath = context.getLaunchConfiguration()
                .getJarFile()
                .getParent()
                .resolve(INSTALLER_FILE_NAME);

        DownloadHelper.download(url, installerPath, sha1);
        return installerPath;
    }

    private static void install(URLClassLoader loader, File target, File installerFile, Consumer<String> logState, LaunchContext context)
            throws ReflectiveOperationException {
        context.getEventBus().post(new InstallNeoforgeEvent(InstallNeoforgeEvent.Stage.RUNNING_INSTALLER, context.getLaunchConfiguration().getLoader().mcVersion(), context.getLaunchConfiguration().getLoader().loaderVersion()));

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
            throw new NeoforgeInstallerReportFailureException("NeoForge installer reported failure");
        }
    }

    public void install(/*String versionOrMinecraftVersion, */LaunchContext context) {
        LaunchConfiguration config = context.getLaunchConfiguration();
        prepare(context);
        Path installerPath = downloadInstaller(context);
        try (URLClassLoader loader = new URLClassLoader(new java.net.URL[]{installerPath.toUri().toURL()})) {
            install(
                    loader,
                    config.getJarFile().getParent().toFile(),
                    installerPath.toFile(),
                    System.out::println,
                    context
            );
        } catch (ReflectiveOperationException | IOException e) {
            context.getEventBus().post(new InstallNeoforgeEvent(InstallNeoforgeEvent.Stage.FAILED, context.getLaunchConfiguration().getLoader().mcVersion(), context.getLaunchConfiguration().getLoader().loaderVersion()));
            throw new RuntimeException(e);
        }
        context.getEventBus().post(new InstallNeoforgeEvent(InstallNeoforgeEvent.Stage.FINISHED, context.getLaunchConfiguration().getLoader().mcVersion(), context.getLaunchConfiguration().getLoader().loaderVersion()));
    }
}
