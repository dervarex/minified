package com.dervarex.minified.launch.download;

import com.dervarex.minified.launch.utils.DownloadHelper;
import com.dervarex.minified.launch.version.VersionMetadataProvider;
import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.exceptions.NoConnectionException;
import com.dervarex.minified.utils.network.NetworkUtil;

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
     * @param path the full path to the jar including the file name, for example /home/dervarex/server.jar
     * @return true if the download worked correctly, false if there was an error
     */
    public boolean downloadServer(String version, Path path) throws HttpException, IOException {
        try {
            NetworkUtil.ensureOnline("download server jar");
        } catch (NoConnectionException nce) {
            System.err.println("You do not have a working internet connection!");
            return false;
        }
        path.getParent().toFile().mkdirs();
        String url = VersionMetadataProvider.getVersionJsonUrl(version);
        if (url == null) {
            System.out.println("ServerDownloader: Cannot find version!");
            return false;
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
        return true;
    }

}
