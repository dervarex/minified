package com.dervarex.minified.docs;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Build-time syntax highlighting for the languages the docs use. Token classes map onto the Night Owl
 * palette the previous Starlight site used, see {@code .tok-*} in style.css
 */
final class Highlighter {

    private static final Set<String> JAVA = Set.of(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void",
            "volatile", "while", "var", "record", "sealed", "permits", "yield");
    private static final Set<String> KOTLIN = Set.of(
            "as", "break", "class", "continue", "do", "else", "for", "fun", "if", "in", "interface", "is",
            "object", "package", "return", "super", "this", "throw", "try", "typealias", "val", "var",
            "when", "while", "by", "catch", "constructor", "finally", "import", "init", "where", "abstract",
            "companion", "data", "enum", "inner", "internal", "lateinit", "open", "override", "private",
            "protected", "public", "sealed", "suspend", "vararg", "it");
    private static final Set<String> GROOVY;
    private static final Set<String> LITERALS = Set.of("true", "false", "null");
    private static final String OPERATORS = "=+-*/%<>!&|?:^~";
    private static final String PUNCTUATION = "(){}[];,";

    static {
        Set<String> groovy = new HashSet<>(JAVA);
        groovy.addAll(Set.of("def", "as", "in", "trait"));
        GROOVY = Set.copyOf(groovy);
    }

    private Highlighter() {
    }

    static boolean supports(String lang) {
        return keywords(lang) != null;
    }

    static String highlight(String code, String lang) {
        Set<String> keywords = keywords(lang);
        if (keywords == null) {
            return Html.esc(code);
        }
        return new Lexer(code, keywords).run();
    }

    private static Set<String> keywords(String lang) {
        return switch (lang == null ? "" : lang.toLowerCase(Locale.ROOT)) {
            case "java" -> JAVA;
            case "kotlin", "kt", "kts" -> KOTLIN;
            case "groovy", "gradle" -> GROOVY;
            default -> null;
        };
    }

    private static final class Lexer {
        private final String src;
        private final Set<String> keywords;
        private final StringBuilder out = new StringBuilder();
        private int pos;
        private String previousWord = "";

        Lexer(String src, Set<String> keywords) {
            this.src = src;
            this.keywords = keywords;
        }

        String run() {
            while (pos < src.length()) {
                char c = src.charAt(pos);
                if (src.startsWith("//", pos)) {
                    emit("c", untilLineEnd());
                } else if (src.startsWith("/*", pos)) {
                    int end = src.indexOf("*/", pos + 2);
                    emit("c", take(end < 0 ? src.length() : end + 2));
                } else if (src.startsWith("\"\"\"", pos)) {
                    int end = src.indexOf("\"\"\"", pos + 3);
                    emit("s", take(end < 0 ? src.length() : end + 3));
                } else if (c == '"' || c == '\'') {
                    emit("s", quoted(c));
                } else if (Character.isDigit(c)) {
                    emit("n", number());
                } else if (c == '@' && pos + 1 < src.length() && Character.isJavaIdentifierStart(src.charAt(pos + 1))) {
                    int start = pos++;
                    while (pos < src.length() && (Character.isJavaIdentifierPart(src.charAt(pos)) || src.charAt(pos) == '.')) {
                        pos++;
                    }
                    emit("v", src.substring(start, pos));
                } else if (Character.isJavaIdentifierStart(c)) {
                    word();
                } else if (OPERATORS.indexOf(c) >= 0) {
                    int start = pos;
                    while (pos < src.length() && OPERATORS.indexOf(src.charAt(pos)) >= 0) {
                        pos++;
                    }
                    emit("k", src.substring(start, pos));
                } else if (PUNCTUATION.indexOf(c) >= 0) {
                    emit("p", String.valueOf(c));
                    pos++;
                } else {
                    out.append(Html.esc(String.valueOf(c)));
                    pos++;
                }
            }
            return out.toString();
        }

        private void word() {
            int start = pos;
            while (pos < src.length() && Character.isJavaIdentifierPart(src.charAt(pos))) {
                pos++;
            }
            String word = src.substring(start, pos);
            char next = nextSignificant();
            String cls;
            if (LITERALS.contains(word)) {
                cls = "l";
            } else if (keywords.contains(word)) {
                cls = "k";
            } else if (next == '(') {
                cls = "f";
            } else if (previousWord.equals("new")) {
                cls = null;
            } else if (next == '.' || (next == '=' && !src.startsWith("==", skipSpaces()))) {
                cls = "v";
            } else if (Character.isUpperCase(word.charAt(0)) && (Character.isJavaIdentifierStart(next) || next == '<')) {
                cls = "k";
            } else {
                cls = null;
            }
            previousWord = word;
            if (cls == null) {
                out.append(Html.esc(word));
            } else {
                emit(cls, word);
            }
        }

        private int skipSpaces() {
            int i = pos;
            while (i < src.length() && (src.charAt(i) == ' ' || src.charAt(i) == '\t')) {
                i++;
            }
            return i;
        }

        private char nextSignificant() {
            int i = skipSpaces();
            return i < src.length() ? src.charAt(i) : '\0';
        }

        private String quoted(char quote) {
            int start = pos++;
            while (pos < src.length()) {
                char c = src.charAt(pos);
                if (c == '\\') {
                    pos += 2;
                    continue;
                }
                pos++;
                if (c == quote || c == '\n') {
                    break;
                }
            }
            pos = Math.min(pos, src.length());
            return src.substring(start, pos);
        }

        private String number() {
            int start = pos;
            while (pos < src.length() && (Character.isLetterOrDigit(src.charAt(pos)) || src.charAt(pos) == '_'
                    || (src.charAt(pos) == '.' && pos + 1 < src.length() && Character.isDigit(src.charAt(pos + 1))))) {
                pos++;
            }
            return src.substring(start, pos);
        }

        private String untilLineEnd() {
            int end = src.indexOf('\n', pos);
            return take(end < 0 ? src.length() : end);
        }

        private String take(int end) {
            String text = src.substring(pos, end);
            pos = end;
            return text;
        }

        private void emit(String cls, String text) {
            if (!cls.equals("p") && !cls.equals("c")) {
                previousWord = cls.equals("k") ? text : "";
            }
            out.append("<span class=\"tok-").append(cls).append("\">").append(Html.esc(text)).append("</span>");
        }
    }
}
