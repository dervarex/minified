package com.dervarex.minified;

import com.dervarex.minified.launch.launch.LaunchConfigurator;
import com.dervarex.minified.launch.launch.Launcher;

public class LauncherRunner {
    public void run(RunConfig config) {
        LaunchConfigurator launchConfigurator = new LaunchConfigurator.Builder()
                .assetsDirectory(config.tmpDir.resolve("assets"))
                .librariesDirectory(config.tmpDir.resolve("libraries"))
                .downloadThreads(10)
                .loader(config.loader)
                .maxRam(6044)
                .minRam(2048)
                .jarFile(config.tmpDir.resolve("jarfiles").resolve(config.version + ".jar"))
                .build();
        Launcher.launchMinecraft(config.version, null, launchConfigurator);
    }
}
