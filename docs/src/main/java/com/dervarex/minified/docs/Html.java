package com.dervarex.minified.docs;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Small HTML helpers shared by all page renderers
 */
final class Html {

    private static final Pattern TAG = Pattern.compile("<[^>]*>");
    private static final Pattern ENTITY = Pattern.compile("&(#x?[0-9a-fA-F]+|[a-zA-Z]+);");
    private static final Map<String, String> NAMED_ENTITIES = Map.of(
            "lt", "<", "gt", ">", "amp", "&", "quot", "\"", "apos", "'", "nbsp", " ", "middot", "·");

    private Html() {
    }

    static String esc(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length() + 16);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    static String unescape(String html) {
        Matcher m = ENTITY.matcher(html);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String name = m.group(1);
            String replacement;
            if (name.startsWith("#x") || name.startsWith("#X")) {
                replacement = Character.toString(Integer.parseInt(name.substring(2), 16));
            } else if (name.startsWith("#")) {
                replacement = Character.toString(Integer.parseInt(name.substring(1)));
            } else {
                replacement = NAMED_ENTITIES.getOrDefault(name, m.group());
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    static String stripTags(String html) {
        return unescape(TAG.matcher(html).replaceAll(""));
    }

    static String slug(String text) {
        String plain = Normalizer.normalize(stripTags(text), Normalizer.Form.NFKD)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("[\\s-]+", "-");
        return plain.isEmpty() ? "section" : plain;
    }

    /**
     * Relative url from the page at {@code from} to the page at {@code to}. Page paths are directory
     * paths without leading slash, e.g. {@code guides/getting-started/}, the home page is {@code ""}
     */
    static String rel(String from, String to) {
        int depth = 0;
        for (int i = 0; i < from.length(); i++) {
            if (from.charAt(i) == '/') {
                depth++;
            }
        }
        String url = "../".repeat(depth) + to;
        return url.isEmpty() ? "./" : url;
    }

    /**
     * Collapses the common leading indentation of a block of text and trims blank edge lines
     */
    static String dedent(String text) {
        String[] lines = text.replace("\t", "    ").split("\n", -1);
        int start = 0, end = lines.length;
        while (start < end && lines[start].isBlank()) {
            start++;
        }
        while (end > start && lines[end - 1].isBlank()) {
            end--;
        }
        int indent = Integer.MAX_VALUE;
        for (int i = start; i < end; i++) {
            if (!lines[i].isBlank()) {
                indent = Math.min(indent, lines[i].length() - lines[i].stripLeading().length());
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) {
            if (i > start) {
                sb.append('\n');
            }
            sb.append(lines[i].isBlank() ? "" : lines[i].substring(indent).stripTrailing());
        }
        return sb.toString();
    }
}
