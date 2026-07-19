package com.dervarex.minified.launch;

import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.Launcher;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;

import java.nio.file.Path;

public class VersionSupportTest {
    public static Path tempDir = Path.of("/home/dervarex/Development/tmp/nf/");
    public static void main(String[] args) {
        LaunchConfiguration launchConfiguration = new LaunchConfiguration.Builder()
                .downloadThreads(10)
                .launcherName("MinifiedLauncher")
                .launcherVersion("1.0.0")
                .assetsDirectory(tempDir.resolve("assets"))
                .librariesDirectory(tempDir.resolve("jar/libraries"))
                .jarFile(tempDir.resolve("jar/client.jar"))
                .isDemoUser(false)
                .loader(new VanillaLoader("1.17"))
                .build();

        Launcher.launchMinecraft(
                null,
                launchConfiguration
        );
    }
}