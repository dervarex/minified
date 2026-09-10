package com.dervarex.minified.utils.shortcut.writer;

import com.dervarex.minified.utils.shortcut.Shortcut;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class WindowsShortcutWriter implements ShortcutWriter {

    @Override
    public Path write(Shortcut s) throws IOException {
        Path target = s.execPath().toAbsolutePath();
        Path workDir = s.directory() != null
                ? s.directory().toAbsolutePath()
                : target.getParent();

        StringBuilder ps = new StringBuilder();
        ps.append("$ws = New-Object -ComObject WScript.Shell; ");
        ps.append("$sc = $ws.CreateShortcut(")
                .append(ShortcutSupport.psQuote(s.shortcutPath().toAbsolutePath().toString()))
                .append("); ");
        ps.append("$sc.TargetPath = ")
                .append(ShortcutSupport.psQuote(target.toString())).append("; ");
        ps.append("$sc.WorkingDirectory = ")
                .append(ShortcutSupport.psQuote(workDir.toString())).append("; ");

        if (s.arguments() != null && !s.arguments().isBlank()) {
            ps.append("$sc.Arguments = ")
                    .append(ShortcutSupport.psQuote(s.arguments())).append("; ");
        }
        if (s.icon() != null) {
            ps.append("$sc.IconLocation = ")
                    .append(ShortcutSupport.psQuote(s.icon().toAbsolutePath().toString()))
                    .append("; ");
        }
        if (s.comment() != null) {
            ps.append("$sc.Description = ")
                    .append(ShortcutSupport.psQuote(s.comment())).append("; ");
        }
        ps.append("$sc.Save();");

        Files.createDirectories(s.shortcutPath().getParent());

        ProcessBuilder pb = new ProcessBuilder(
                "powershell", "-NoProfile", "-NonInteractive", "-Command", ps.toString()
        );
        pb.redirectErrorStream(true);

        Process process = pb.start();
        try {
            if (process.waitFor() != 0) {
                throw new IOException("PowerShell shortcut creation failed");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while creating shortcut", e);
        }
        return s.shortcutPath();
    }
}