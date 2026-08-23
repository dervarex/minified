package com.dervarex.minified.launch.exceptions.libraries;

import com.dervarex.minified.launch.launch.modding.Loader;
import lombok.Getter;

@Getter
public class FailedToLoadLibrariesException extends RuntimeException {
    private final Loader loader;

    public FailedToLoadLibrariesException(String message, Loader loader) {
        super(message);
        this.loader = loader;
    }

    public FailedToLoadLibrariesException(String message, Loader loader, Throwable cause) {
        super(message, cause);
        this.loader = loader;
    }
}
