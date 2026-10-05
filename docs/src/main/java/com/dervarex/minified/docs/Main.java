package com.dervarex.minified.docs;

import javax.tools.DocumentationTool;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Runs the javadoc tool over all library modules with {@link MinifiedDoclet} as the doclet
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        Config config = Config.parse(args);

        List<Path> sources = new ArrayList<>();
        for (Config.ModuleSource module : config.modules()) {
            try (Stream<Path> files = Files.walk(module.sourceDir())) {
                // the modules are documented together on the classpath, so module descriptors are left out
                files.filter(p -> p.toString().endsWith(".java"))
                        .filter(p -> !p.getFileName().toString().equals("module-info.java"))
                        .forEach(sources::add);
            }
        }

        DocumentationTool tool = ToolProvider.getSystemDocumentationTool();
        if (tool == null) {
            throw new IllegalStateException("No javadoc tool available, run the generator with a JDK");
        }

        MinifiedDoclet.config = config;
        try (StandardJavaFileManager fileManager = tool.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8)) {
            Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromPaths(sources);
            List<String> options = List.of(
                    "-classpath", config.classpath(),
                    "-encoding", "UTF-8",
                    "-quiet"
            );
            boolean ok = tool.getTask(null, fileManager, null, MinifiedDoclet.class, options, units).call();
            if (!ok) {
                System.exit(1);
            }
        }
    }
}
