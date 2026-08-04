package com.dervarex.minified.launch.launch.modding.forge.installer;

import com.dervarex.minified.launch.events.loader.InstallForgeEvent;
import com.dervarex.minified.launch.exceptions.loader.forge.ForgeInstallerReportFailureException;
import com.dervarex.minified.launch.exceptions.loader.forge.ForgePreparationException;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.LaunchContext;
import com.dervarex.minified.launch.launch.modding.forge.api.ForgeInstallerFetcher;
import com.dervarex.minified.utils.download.DownloadHelper;

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
    private static void prepare(LaunchContext context) {
        context.getEventBus().post(new InstallForgeEvent(InstallForgeEvent.Stage.PREPARING, context.getLaunchConfiguration().getLoader().mcVersion(), context.getLaunchConfiguration().getLoader().loaderVersion()));
        Path gameDir = context.getLaunchConfiguration().getJarFile().getParent();
        try {
            Files.createDirectories(gameDir);
            Files.createDirectories(gameDir.resolve("versions"));
            Files.createDirectories(gameDir.resolve("libraries"));
            Path launcherProfiles = gameDir.resolve("launcher_profiles.json");

            if (!Files.exists(launcherProfiles)) {
                context.getEventBus().post(new InstallForgeEvent(InstallForgeEvent.Stage.WRITING_PROFILE, context.getLaunchConfiguration().getLoader().mcVersion(), context.getLaunchConfiguration().getLoader().loaderVersion()));
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
            throw new ForgePreparationException(e);
        }

    }

    private static void downloadInstaller(LaunchContext context) {
        String url = ForgeInstallerFetcher.getInstallerLink(
                context.getLaunchConfiguration().getLoader().loaderVersion()
        );
        context.getEventBus().post(new InstallForgeEvent(InstallForgeEvent.Stage.DOWNLOADING_INSTALLER, context.getLaunchConfiguration().getLoader().mcVersion(), context.getLaunchConfiguration().getLoader().loaderVersion()));


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
            throw new RuntimeException(e); // no custom exception here
        }

        Path installerPath = context.getLaunchConfiguration()
                .getJarFile()
                .getParent()
                .resolve("forge-installer.jar");

        DownloadHelper.download(
                url,
                installerPath,
                sha1
        );
    }

    private static void install(URLClassLoader urlClassLoader, File target, File installerFile, Consumer<String> logState, LaunchContext context)
            throws ReflectiveOperationException {
        context.getEventBus().post(new InstallForgeEvent(InstallForgeEvent.Stage.RUNNING_INSTALLER, context.getLaunchConfiguration().getLoader().mcVersion(), context.getLaunchConfiguration().getLoader().loaderVersion()));


        Class<?> utilClass = urlClassLoader.loadClass("net.minecraftforge.installer.json.Util");
        Method loadInstallProfile = utilClass.getMethod("loadInstallProfile");
        Object profile = loadInstallProfile.invoke(null);

        Class<?> clientInstallClass = urlClassLoader.loadClass("net.minecraftforge.installer.actions.ClientInstall");
        Class<?> callbackInterface = urlClassLoader.loadClass("net.minecraftforge.installer.actions.ProgressCallback");

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
            throw new ForgeInstallerReportFailureException("Forge installer reported failure");
        }
    }

    public void install(/*String versionOrMinecraftVersion, */LaunchContext context) {
        LaunchConfiguration config = context.getLaunchConfiguration();
        prepare(context);
        downloadInstaller(context);
        try {
            install(
                    new URLClassLoader(new java.net.URL[]{
                            Path.of(config.getJarFile().getParent().toAbsolutePath().toString(), "forge-installer.jar").toUri().toURL()
                    }),
                    config.getJarFile().getParent().toFile(),
                    Path.of(config.getJarFile().getParent().toAbsolutePath().toString(),"forge-installer.jar").toFile(),
                    System.out::println, // todo replace with logger
                    context
            );
        } catch (ReflectiveOperationException | MalformedURLException e) {
            context.getEventBus().post(new InstallForgeEvent(InstallForgeEvent.Stage.FAILED, context.getLaunchConfiguration().getLoader().mcVersion(), context.getLaunchConfiguration().getLoader().loaderVersion()));
            throw new RuntimeException(e); // no custom exception
        }
        context.getEventBus().post(new InstallForgeEvent(InstallForgeEvent.Stage.FINISHED, context.getLaunchConfiguration().getLoader().mcVersion(), context.getLaunchConfiguration().getLoader().loaderVersion()));
    }
}
