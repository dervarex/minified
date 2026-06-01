package com.dervarex.minified.launch;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class LaunchConfigurator {

    private int downloadThreads = 5;
    private int resolutionWidth = 1920;
    private int resolutionHeight = 1080;
    private boolean customResolution = false;
    private int minRam;
    private int maxRam;

    private String launcherName = "Launcher";
    private String launcherVersion = "1.0.0";

    private boolean demoUser = false;
    private final List<String> extraJvmArgs = new ArrayList<>();

    private LaunchConfigurator() {
    }

    public static class Builder {

        private final LaunchConfigurator config = new LaunchConfigurator();

        public Builder downloadThreads(int threads) {
            config.downloadThreads = threads;
            return this;
        }

        public Builder resolution(int width, int height) {
            config.resolutionWidth = width;
            config.resolutionHeight = height;
            config.customResolution = true;
            return this;
        }

        public Builder launcherName(String name) {
            config.launcherName = name;
            return this;
        }

        public Builder launcherVersion(String version) {
            config.launcherVersion = version;
            return this;
        }

        public Builder isDemoUser(boolean demo) {
            config.demoUser = demo;
            return this;
        }
        public Builder minRam(int minRam) {
            config.minRam = minRam;
            return this;
        }
        public Builder maxRam(int maxRam) {
            config.maxRam = maxRam;
            return this;
        }

        public Builder extraJvmArg(String arg) {
            config.extraJvmArgs.add(arg);
            return this;
        }

        public Builder extraJvmArgs(List<String> args) {
            config.extraJvmArgs.addAll(args);
            return this;
        }

        public LaunchConfigurator build() {
            return config;
        }
    }
}