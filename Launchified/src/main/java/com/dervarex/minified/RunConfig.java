package com.dervarex.minified;

import com.dervarex.minified.launch.launch.modding.Loader;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RunConfig {
    public String version = "1.21.11";
    public Loader loader = Loader.Quilt;
    public Path tmpDir = Paths.get(
            System.getProperty("user.home"),
            ".launchified-tmp"
    );
}
