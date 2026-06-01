package com.dervarex.minified.launch;

import lombok.Getter;

@Getter
public class LaunchConfigurator {

    private int downloadThreads = 5;
    private int resolutionWidth = 1920;
    private int resolutionHeight = 1080;
    private boolean customResolution = false;

    private String launcherName = "Launcher";
    private String launcherVersion = "1.0.0";

    private boolean demoUser = false;

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

        public LaunchConfigurator build() {
            return config;
        }
    }
}