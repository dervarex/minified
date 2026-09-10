package com.dervarex.minified.utils.shortcut.writer;

import com.dervarex.minified.utils.shortcut.Shortcut;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

final class LinuxShortcutWriter implements ShortcutWriter {

    @Override
    public Path write(Shortcut s) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("[Desktop Entry]\n");
        sb.append("Type=Application\n");
        sb.append("Name=").append(ShortcutSupport.escapeDesktop(s.name())).append('\n');

        if (s.comment() != null) {
            sb.append("Comment=").append(ShortcutSupport.escapeDesktop(s.comment())).append('\n');
        }

        sb.append("Exec=").append(buildExecLine(s)).append('\n');

        if (s.directory() != null) {
            sb.append("Path=").append(s.directory().toAbsolutePath()).append('\n');
        }
        if (s.icon() != null) {
            sb.append("Icon=").append(s.icon().toAbsolutePath()).append('\n');
        }

        sb.append("Terminal=").append(s.terminal()).append('\n');
        sb.append("Categories=Utility;\n");

        Path target = s.shortcutPath();
        Files.createDirectories(target.getParent());
        Files.writeString(target, sb.toString(), StandardCharsets.UTF_8);
        target.toFile().setExecutable(true, false);
        return target;
    }

    private String buildExecLine(Shortcut s) {
        StringBuilder sb = new StringBuilder();
        sb.append('"').append(s.execPath().toAbsolutePath()).append('"');
        if (s.arguments() != null && !s.arguments().isBlank()) {
            sb.append(' ').append(s.arguments());
        }
        return sb.toString();
    }
}