package com.dervarex.minified.docs;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Command line configuration of a generator run
 *
 * @param out      directory the site is written to
 * @param content  directory holding the hand-written guides and the sidebar
 * @param root     repository root, used to make source paths relative
 * @param version  library version the site documents
 * @param repo     repository url, used for source links
 * @param ref      git ref source links point at
 * @param classpath external dependencies the documented sources compile against
 * @param modules  documented modules
 * @param strict   whether warnings fail the build
 */
record Config(Path out, Path content, Path root, String version, String repo, String ref,
              String classpath, List<ModuleSource> modules, boolean strict) {

    /**
     * One source directory of a library module
     *
     * @param name        gradle project name, e.g. {@code minified-launch}
     * @param sourceDir   main java source directory
     * @param description gradle project description, may be empty
     */
    record ModuleSource(String name, Path sourceDir, String description) {
    }

    static Config parse(String[] args) {
        Path out = null, content = null, root = null;
        String version = "dev", repo = "", ref = "master", classpath = "";
        boolean strict = false;
        List<ModuleSource> modules = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--out" -> out = Path.of(args[++i]);
                case "--content" -> content = Path.of(args[++i]);
                case "--root" -> root = Path.of(args[++i]);
                case "--version" -> version = args[++i];
                case "--repo" -> repo = args[++i];
                case "--ref" -> ref = args[++i];
                case "--classpath" -> classpath = args[++i];
                case "--strict" -> strict = true;
                case "--module" -> {
                    String[] parts = args[++i].split("\\|", 3);
                    modules.add(new ModuleSource(parts[0], Path.of(parts[1]), parts.length > 2 ? parts[2] : ""));
                }
                default -> throw new IllegalArgumentException("Unknown argument " + args[i]);
            }
        }
        if (out == null || content == null || root == null || modules.isEmpty()) {
            throw new IllegalArgumentException("--out, --content, --root and at least one --module are required");
        }
        return new Config(out, content, root, version, repo, ref, classpath, List.copyOf(modules), strict);
    }
}
