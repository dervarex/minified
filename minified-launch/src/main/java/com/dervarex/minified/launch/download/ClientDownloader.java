package com.dervarex.minified.launch.download;

import com.dervarex.minified.launch.utils.DownloadHelper;
import com.dervarex.minified.launch.version.VersionMetadataProvider;
import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.exceptions.NoConnectionException;
import com.dervarex.minified.utils.network.NetworkUtil;

import java.io.IOException;
import java.nio.file.Path;

@SuppressWarnings("unused")
public class ClientDownloader {

    /**
     * @param version the game version
     * @param path    the full path to the jar including the file name, for example /home/dervarex/client.jar
     */
    public void downloadClient(String version, Path path)
            throws HttpException, IOException {

        try {
            NetworkUtil.ensureOnline("download client jar");
        } catch (NoConnectionException nce) {
            System.err.println("You do not have a working internet connection!");
            return;
        }

        path.getParent().toFile().mkdirs();

        String url = VersionMetadataProvider.getVersionJsonUrl(version);
        if (url == null) {
            System.out.println("ClientDownloader: Cannot find version!");
            return;
        }
        String clientUrl = VersionMetadataProvider.getClientUrl(version);
        String sha1 = VersionMetadataProvider.getClientSha1(version);

        DownloadHelper.download(clientUrl, path, sha1);
    }
}