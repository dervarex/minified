package com.dervarex.minified.utils.shortcut.writer;

import com.dervarex.minified.utils.shortcut.Shortcut;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
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
        // read it, or a chatty powershell fills the pipe and waits forever
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        try {
            if (process.waitFor() != 0) {
                throw new IOException("PowerShell shortcut creation failed: " + output);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            InterruptedIOException interrupted = new InterruptedIOException("Interrupted while creating shortcut");
            interrupted.initCause(e);
            throw interrupted;
        }
        return s.shortcutPath();
    }
}