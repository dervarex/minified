package com.dervarex.minified.launch.exceptions.version;

import lombok.Getter;

import java.io.IOException;

/**
 * Gets thrown when Mojang's version manifest doesn't list the requested version, or the version has no client jar
 * to download
 */
@Getter
public class VersionNotFoundException extends IOException {
    /**
     * The Minecraft version that was asked for, for example {@code 1.21.11}
     */
    private final String version;

    public VersionNotFoundException(String message, String version) {
        super(message);
        this.version = version;
    }
}
