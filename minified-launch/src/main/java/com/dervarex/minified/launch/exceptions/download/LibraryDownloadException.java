package com.dervarex.minified.launch.exceptions.download;

public class LibraryDownloadException extends DownloadException {
    public LibraryDownloadException(String message) {
        super(message);
    }
    public LibraryDownloadException() {
        super();
    }
    public LibraryDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
    public LibraryDownloadException(Throwable cause) {
        super(cause);
    }
}
