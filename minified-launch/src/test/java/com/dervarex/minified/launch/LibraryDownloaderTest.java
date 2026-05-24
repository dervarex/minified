package com.dervarex.minified.launch;

import com.dervarex.minified.launch.libraries.LibraryDownloader;

public class LibraryDownloaderTest {
    public static void main(String[] args) {
        LibraryDownloader downloader = new LibraryDownloader(10);
        downloader.downloadLibraries("1.21.11", "/home/dervarex/Development/tmp/libs/");
    }
}
