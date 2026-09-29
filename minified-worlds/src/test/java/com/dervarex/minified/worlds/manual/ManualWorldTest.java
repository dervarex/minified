package com.dervarex.minified.worlds.manual;

import com.dervarex.minified.worlds.save.WorldSave;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// ./gradlew minified-worlds:manualTest -Dworld.folder="path/to/world"
@Tag("manual")
class ManualWorldTest {

    @Test
    void testWorld() throws IOException {
        String folder = System.getProperty("world.folder");
        assumeTrue(folder != null && !folder.isBlank(), "Please provide a path using -Dworld.folder=/path/to/world");
        Path world = Path.of(folder).toAbsolutePath();
        assumeTrue(Files.isDirectory(world), "Folder does not exist: " + world);

        Report report = new Report();
        System.out.println("Testing world " + world);

        try (ScratchWorld scratch = ScratchWorld.copyOf(world)) {
            System.out.println("Everything that writes goes to " + scratch.root() + ", your world stays untouched");

            report.section("World");
            WorldSave save = report.load("load WorldSave", () -> new WorldSave(world));
            if (save != null) {
                new SaveChecks(report, world, scratch).run(save);
                new PlayerChecks(report, world, scratch).run(save.getPlayers());
            }
            new DimensionChecks(report, world, scratch).run();
        }

        report.printOverview();
        assertEquals(0, report.count(Report.Status.FAIL), "some world checks failed, the overview above knows which");
    }
}
