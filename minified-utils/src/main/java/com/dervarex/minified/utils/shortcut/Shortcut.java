package com.dervarex.minified.utils.shortcut;

import com.dervarex.minified.utils.os.OS;
import com.dervarex.minified.utils.shortcut.writer.ShortcutSupport;
import com.dervarex.minified.utils.shortcut.writer.ShortcutWriter;

import java.io.IOException;
import java.nio.file.Path;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public final class Shortcut {

    private final String name;
    private final Path shortcutPath;
    private final Path execPath;
    private final Path directory;
    private final Path icon;
    private final OS os;
    private final String arguments;
    private final boolean terminal;
    private final String comment;

    private Shortcut(Builder b) {
        this.name = b.name;
        this.shortcutPath = b.shortcutPath;
        this.execPath = b.execPath;
        this.directory = b.directory;
        this.icon = b.icon;
        this.os = b.os;
        this.arguments = b.arguments;
        this.terminal = b.terminal;
        this.comment = b.comment;
    }

    public Path write() throws IOException {
        return ShortcutWriter.forOS(os).write(this);
    }

    public String name() { return name; }
    public Path shortcutPath() { return shortcutPath; }
    public Path execPath() { return execPath; }
    public Path directory() { return directory; }
    public Path icon() { return icon; }
    public OS os() { return os; }
    public String arguments() { return arguments; }
    public boolean terminal() { return terminal; }
    public String comment() { return comment; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {

        private String name;
        private Path shortcutPath;
        private Path execPath;
        private Path directory;
        private Path icon;
        private OS os;
        private String arguments;
        private boolean terminal;
        private String comment;

        /**
         * Sets the name
         *
         * @param name the name
         * @return this builder
         */
        public Builder name(String name) { this.name = name; return this; }
        /**
         * Sets the shortcut path
         *
         * @param p the shortcut path
         * @return this builder
         */
        public Builder shortcutPath(Path p) { this.shortcutPath = p; return this; }
        /**
         * Sets the program the shortcut runs
         *
         * @param p the program the shortcut runs
         * @return this builder
         */
        public Builder execPath(Path p) { this.execPath = p; return this; }
        /**
         * Sets the directory
         *
         * @param p the directory
         * @return this builder
         */
        public Builder directory(Path p) { this.directory = p; return this; }
        /**
         * Sets the icon
         *
         * @param p the icon
         * @return this builder
         */
        public Builder icon(Path p) { this.icon = p; return this; }
        /**
         * Sets the os
         *
         * @param os the os
         * @return this builder
         */
        public Builder os(OS os) { this.os = os; return this; }
        /**
         * Sets the arguments
         *
         * @param a the arguments
         * @return this builder
         */
        public Builder arguments(String a) { this.arguments = a; return this; }
        public Builder terminal(boolean t) { this.terminal = t; return this; }
        /**
         * Sets the comment
         *
         * @param c the comment
         * @return this builder
         */
        public Builder comment(String c) { this.comment = c; return this; }

        public Shortcut build() {
            if (execPath == null) {
                throw new IllegalStateException("execPath is required");
            }
            if (os == null) {
                os = OS.getCurrentOS();
            }
            if (name == null || name.isBlank()) {
                name = ShortcutSupport.deriveName(execPath);
            }
            if (shortcutPath == null) {
                shortcutPath = ShortcutSupport.defaultShortcutPath(os, name);
            }
            return new Shortcut(this);
        }
    }
}
