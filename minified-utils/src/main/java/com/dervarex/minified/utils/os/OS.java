package com.dervarex.minified.utils.os;

import org.apiguardian.api.API;

/**
 * Represents an Operating System.
 * <p>
 * Use {@link getCurrentOS} for getting the Operating System of the User.
 */
@API(status = API.Status.STABLE)
public enum OS {
    WINDOWS("windows"),
    LINUX("linux"),
    MACOS("osx"),
    UNKNOWN("unknown");

    private final String name;

    OS(String name) {
        this.name = name;
    }

    /**
     * {@return the operating system's name}
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the operating system's name, same as {@link getName()}
     * @return the operating system's name
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * {@return the current os of the user, where this project is running}
     */
    public static OS getCurrentOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac") || os.contains("darwin")) {
            return OS.MACOS;
        } else if (os.contains("win")) {
            return OS.WINDOWS;
        } else if (os.contains("nix") || os.contains("nux")) {
            return OS.LINUX;
        }
        return OS.UNKNOWN;
    }
}
