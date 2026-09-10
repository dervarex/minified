package com.dervarex.minified.utils.shortcut.writer;

import com.dervarex.minified.utils.shortcut.Shortcut;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

final class MacShortcutWriter implements ShortcutWriter {

    @Override
    public Path write(Shortcut s) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("#!/bin/bash\n");

        if (s.directory() != null) {
            sb.append("cd ").append(ShortcutSupport.shellQuote(
                    s.directory().toAbsolutePath().toString())).append('\n');
        }

        sb.append("exec ").append(ShortcutSupport.shellQuote(
                s.execPath().toAbsolutePath().toString()));

        if (s.arguments() != null && !s.arguments().isBlank()) {
            sb.append(' ').append(s.arguments());
        }
        sb.append('\n');

        Path target = s.shortcutPath();
        Files.createDirectories(target.getParent());
        Files.writeString(target, sb.toString(), StandardCharsets.UTF_8);
        target.toFile().setExecutable(true, false);
        return target;
    }
}