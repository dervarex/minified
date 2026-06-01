package com.dervarex.minified.launch;

import com.dervarex.minified.launch.download.ClientDownloader;
import com.dervarex.minified.utils.exceptions.HttpException;

import java.io.IOException;
import java.nio.file.Path;

public class DownloaderTest {
    public static void main(String[] args) throws HttpException, IOException {
//        AssetDownloader assetDownloader = new AssetDownloader(10);
//        assetDownloader.downloadAssets("1.21.11", Path.of("/home/dervarex/Development/tmp/assets/"));
//        LibraryDownloader libraryDownloader = new LibraryDownloader(10);
//        libraryDownloader.downloadLibraries("1.21.11", "/home/dervarex/Development/tmp/libs/");
//
        ClientDownloader clientDownloader = new ClientDownloader();
        clientDownloader.downloadClient("1.21.11", Path.of("/home/dervarex/Development/tmp/jar/client.jar"));
    }
}
