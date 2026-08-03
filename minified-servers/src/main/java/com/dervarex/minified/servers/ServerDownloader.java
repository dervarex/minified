package com.dervarex.minified.servers;

import com.dervarex.minified.launch.launch.LaunchContext;
import com.dervarex.minified.launch.utils.DownloadHelper;
import com.dervarex.minified.launch.version.VersionMetadataProvider;
import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.exceptions.NoConnectionException;
import com.dervarex.minified.utils.network.NetworkUtil;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

@SuppressWarnings("unused")
public class ServerDownloader {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    /**
     *
     * @param version the game version
     * @param path    the full path to the jar including the file name, for example /home/dervarex/server.jar
     * @param context the launch context
     */
    public void downloadServer(String version, Path path, @Nullable LaunchContext context) throws HttpException, IOException {
        try {
            if(context != null) {
                context.getEventBus().post(new CheckConnectionEvent());
            }
            NetworkUtil.ensureOnline("download server jar");
        } catch (NoConnectionException nce) {
            System.err.println("You do not have a working internet connection!");
            if(context != null) {
                context.getEventBus().post(new OfflineEvent());
            }
            return;
        }
        path.getParent().toFile().mkdirs();
        String url = VersionMetadataProvider.getVersionJsonUrl(version);
        if (url == null) {
            System.out.println("ServerDownloader: Cannot find version!");
            return;
        }

        HttpRequest request = DownloadHelper.prepareClientRequest(url, "server");

        try {
            HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(path));
            System.out.println("Downloaded server jar to: " + response.body().toAbsolutePath());
        } catch (IOException | InterruptedException e) {
            System.err.println("Error while download: " + e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
