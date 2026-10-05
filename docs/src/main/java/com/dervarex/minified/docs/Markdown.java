package com.dervarex.minified.docs;

import com.dervarex.minified.docs.Site.TocEntry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown renderer for the guides. Supports the CommonMark subset the guides use plus what the former
 * Starlight site offered: raw HTML blocks, {@code :::note} asides and {@code <Tabs>/<TabItem>}
 */
final class Markdown {

    interface Context {
        /**
         * Rewrites a link target, returns it unchanged when it cannot be resolved
         */
        String link(String href);

        /**
         * Url an inline code span should link to, or null
         */
        String codeLink(String code);

        /**
         * Html for a block directive like {@code {{events}}}, or null when unknown
         */
        String directive(String name, String argument);
    }

    record Result(String html, List<TocEntry> toc) {
    }

    private static final Pattern FENCE = Pattern.compile("^(\\s*)(`{3,}|~{3,})\\s*([^\\s`]*)(.*)$");
    private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.*?)\\s*#*\\s*$");
    private static final Pattern HEADING_ID = Pattern.compile("\\s*\\{#([\\w-]+)}$");
    private static final Pattern HR = Pattern.compile("^\\s*([-*_])(\\s*\\1){2,}\\s*$");
    private static final Pattern LIST_ITEM = Pattern.compile("^(\\s*)([-*+]|\\d{1,9}[.)])(\\s+|$)(.*)$");
    private static final Pattern ASIDE = Pattern.compile("^\\s*:::(note|tip|caution|danger)(?:\\[(.*)])?\\s*$");
    private static final Pattern DIRECTIVE = Pattern.compile("^\\s*\\{\\{\\s*(\\w+)\\s*([^}]*)}}\\s*$");
    private static final Pattern HTML_LINE = Pattern.compile("^\\s*</?([A-Za-z][\\w-]*)(?=[\\s>/]|$)");
    private static final Pattern TAB_ITEM = Pattern.compile("^\\s*<TabItem\\s+label=[\"']([^\"']*)[\"'][^>]*>\\s*$");
    private static final Pattern ATTR_URL = Pattern.compile("\\b(href|src)=\"([^\"]*)\"");
    private static final Pattern TAG = Pattern.compile("^(</?[A-Za-z][\\w-]*(?:\\s+[^<>]*?)?/?>|<!--.*?-->)", Pattern.DOTALL);
    private static final Pattern AUTOLINK = Pattern.compile("^<(https?://[^>\\s]+)>");
    private static final Pattern LINK = Pattern.compile("(!?)\\[((?:[^\\[\\]]|\\[[^\\]]*])*)]\\(\\s*([^)\\s]+)(?:\\s+\"([^\"]*)\")?\\s*\\)");
    private static final Pattern ENTITY = Pattern.compile("&(#x?[0-9a-fA-F]+|[a-zA-Z]+);");
    private static final Set<String> BLOCK_TAGS = Set.of("div", "details", "summary", "section", "p", "table", "thead",
            "tbody", "tr", "td", "th", "ul", "ol", "li", "pre", "figure", "figcaption", "aside", "nav", "header", "footer",
            "h1", "h2", "h3", "h4", "h5", "h6", "hr", "blockquote", "dl", "dt", "dd", "img", "br", "Tabs", "TabItem", "iframe",
            "video", "picture", "style", "script");

    private final Context context;
    private final List<String> lines;
    private final List<TocEntry> toc;
    private final Set<String> ids;
    private final boolean topLevel;
    private final StringBuilder out = new StringBuilder();
    private int i;
    private int htmlDepth;

    Markdown(String text, Context context) {
        this(List.of(text.replace("\r\n", "\n").split("\n", -1)), context, new ArrayList<>(), new HashSet<>(), true);
    }

    private Markdown(List<String> lines, Context context, List<TocEntry> toc, Set<String> ids, boolean topLevel) {
        this.lines = lines;
        this.context = context;
        this.toc = toc;
        this.ids = ids;
        this.topLevel = topLevel;
    }

    Result render() {
        blocks();
        return new Result(out.toString(), toc);
    }

    private String nested(List<String> body) {
        Markdown md = new Markdown(body, context, toc, ids, false);
        md.blocks();
        return md.out.toString();
    }

    // ------------------------------------------------------------------ front matter

    static Map<String, String> frontMatter(String text) {
        Map<String, String> values = new LinkedHashMap<>();
        String normalized = text.replace("\r\n", "\n");
        if (!normalized.startsWith("---\n")) {
            return values;
        }
        int end = normalized.indexOf("\n---", 4);
        if (end < 0) {
            return values;
        }
        for (String line : normalized.substring(4, end).split("\n")) {
            int colon = line.indexOf(':');
            if (colon > 0 && !line.startsWith(" ")) {
                String value = line.substring(colon + 1).trim();
                if (value.length() >= 2 && (value.startsWith("\"") && value.endsWith("\"") || value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                values.put(line.substring(0, colon).trim(), value);
            }
        }
        return values;
    }

    static String stripFrontMatter(String text) {
        String normalized = text.replace("\r\n", "\n");
        if (!normalized.startsWith("---\n")) {
            return normalized;
        }
        int end = normalized.indexOf("\n---", 4);
        if (end < 0) {
            return normalized;
        }
        int lineEnd = normalized.indexOf('\n', end + 4);
        return lineEnd < 0 ? "" : normalized.substring(lineEnd + 1);
    }

    // ------------------------------------------------------------------ blocks

    private void blocks() {
        while (i < lines.size()) {
            String line = lines.get(i);
            if (line.isBlank()) {
                i++;
                continue;
            }
            Matcher m;
            if ((m = FENCE.matcher(line)).matches()) {
                fence(m);
            } else if ((m = ASIDE.matcher(line)).matches()) {
                aside(m.group(1), m.group(2));
            } else if ((m = DIRECTIVE.matcher(line)).matches()) {
                String html = context.directive(m.group(1), m.group(2).trim());
                out.append(html != null ? html : "<p>" + inline(line.trim()) + "</p>");
                i++;
            } else if ((m = HEADING.matcher(line)).matches() && !line.startsWith("    ")) {
                heading(m.group(1).length(), m.group(2));
                i++;
            } else if (HR.matcher(line).matches()) {
                out.append("<hr>");
                i++;
            } else if (line.trim().startsWith("|") && i + 1 < lines.size() && lines.get(i + 1).trim().matches("^\\|?\\s*:?-{2,}:?\\s*(\\|\\s*:?-{2,}:?\\s*)*\\|?$")) {
                table();
            } else if (line.trim().startsWith(">")) {
                blockquote();
            } else if (line.trim().equals("<Tabs>")) {
                tabs();
            } else if (isHtmlLine(line)) {
                htmlLine(line);
                i++;
            } else if ((m = LIST_ITEM.matcher(line)).matches()) {
                list(m.group(1).length(), isOrdered(m.group(2)));
            } else {
                paragraph();
            }
        }
    }

    private boolean isHtmlLine(String line) {
        Matcher m = HTML_LINE.matcher(line);
        if (!m.find()) {
            return false;
        }
        return BLOCK_TAGS.contains(m.group(1)) || htmlDepth > 0;
    }

    private void htmlLine(String line) {
        String html = rewriteHtml(line);
        Matcher open = Pattern.compile("<(div|details|section|figure|aside|table|ul|ol|dl|blockquote)\\b[^>]*?(/?)>").matcher(html);
        while (open.find()) {
            if (open.group(2).isEmpty()) {
                htmlDepth++;
            }
        }
        Matcher close = Pattern.compile("</(div|details|section|figure|aside|table|ul|ol|dl|blockquote)>").matcher(html);
        while (close.find()) {
            htmlDepth = Math.max(0, htmlDepth - 1);
        }
        out.append(html).append('\n');
    }

    private String rewriteHtml(String html) {
        String rewritten = html.replace("className=", "class=");
        Matcher m = ATTR_URL.matcher(rewritten);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(m.group(1) + "=\"" + context.link(m.group(2)) + "\""));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private void fence(Matcher open) {
        int indent = open.group(1).length();
        String marker = open.group(2);
        String lang = open.group(3);
        String meta = open.group(4);
        Matcher titleMatcher = Pattern.compile("title=[\"']([^\"']*)[\"']").matcher(meta);
        String title = titleMatcher.find() ? titleMatcher.group(1) : null;
        i++;
        StringBuilder code = new StringBuilder();
        while (i < lines.size()) {
            String line = lines.get(i);
            String trimmed = line.trim();
            if (trimmed.startsWith(marker.substring(0, 1).repeat(marker.length())) && trimmed.replace(marker.substring(0, 1), "").isEmpty()) {
                i++;
                break;
            }
            int strip = 0;
            while (strip < indent && strip < line.length() && line.charAt(strip) == ' ') {
                strip++;
            }
            code.append(line.substring(strip)).append('\n');
            i++;
        }
        String text = code.toString().replaceAll("\n+$", "");
        out.append(Layout.codeFrame(Highlighter.highlight(text, lang), lang, title));
    }

    private void aside(String type, String title) {
        i++;
        List<String> body = new ArrayList<>();
        boolean inFence = false;
        while (i < lines.size()) {
            String line = lines.get(i);
            if (FENCE.matcher(line).matches()) {
                inFence = !inFence;
            }
            if (!inFence && line.trim().equals(":::")) {
                i++;
                break;
            }
            body.add(line);
            i++;
        }
        String label = title != null && !title.isBlank() ? inline(title) : switch (type) {
            case "tip" -> "Tip";
            case "caution" -> "Caution";
            case "danger" -> "Danger";
            default -> "Note";
        };
        out.append(Layout.aside(type, label, nested(body)));
    }

    private void heading(int level, String text) {
        String id = null;
        Matcher custom = HEADING_ID.matcher(text);
        if (custom.find()) {
            id = custom.group(1);
            text = text.substring(0, custom.start());
        }
        String html = inline(text);
        if (id == null) {
            id = Html.slug(html);
        }
        String unique = id;
        for (int n = 1; !ids.add(unique); n++) {
            unique = id + "-" + n;
        }
        if (topLevel && (level == 2 || level == 3)) {
            toc.add(new TocEntry(level, unique, Html.stripTags(html)));
        }
        out.append("<h").append(level).append(" id=\"").append(unique).append("\">").append(html).append("</h").append(level).append('>');
    }

    private void table() {
        List<String> header = cells(lines.get(i));
        List<String> separators = cells(lines.get(i + 1));
        List<String> aligns = new ArrayList<>();
        for (String s : separators) {
            String t = s.trim();
            aligns.add(t.startsWith(":") && t.endsWith(":") ? "center" : t.endsWith(":") ? "right" : t.startsWith(":") ? "left" : null);
        }
        i += 2;
        out.append("<table><thead><tr>");
        for (int c = 0; c < header.size(); c++) {
            out.append(cell("th", header.get(c), c < aligns.size() ? aligns.get(c) : null));
        }
        out.append("</tr></thead><tbody>");
        while (i < lines.size() && lines.get(i).trim().startsWith("|")) {
            List<String> row = cells(lines.get(i));
            out.append("<tr>");
            for (int c = 0; c < header.size(); c++) {
                out.append(cell("td", c < row.size() ? row.get(c) : "", c < aligns.size() ? aligns.get(c) : null));
            }
            out.append("</tr>");
            i++;
        }
        out.append("</tbody></table>");
    }

    private String cell(String tag, String content, String align) {
        return "<" + tag + (align == null ? "" : " align=\"" + align + "\"") + ">" + inline(content.trim()) + "</" + tag + ">";
    }

    private static List<String> cells(String row) {
        String trimmed = row.trim();
        if (trimmed.startsWith("|")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.endsWith("|") && !trimmed.endsWith("\\|")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        List<String> cells = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inCode = false;
        for (int c = 0; c < trimmed.length(); c++) {
            char ch = trimmed.charAt(c);
            if (ch == '`') {
                inCode = !inCode;
            }
            if (ch == '\\' && c + 1 < trimmed.length() && trimmed.charAt(c + 1) == '|') {
                current.append('|');
                c++;
            } else if (ch == '|' && !inCode) {
                cells.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        cells.add(current.toString());
        return cells;
    }

    private void blockquote() {
        List<String> body = new ArrayList<>();
        while (i < lines.size() && lines.get(i).trim().startsWith(">")) {
            String stripped = lines.get(i).trim().substring(1);
            body.add(stripped.startsWith(" ") ? stripped.substring(1) : stripped);
            i++;
        }
        out.append("<blockquote>").append(nested(body)).append("</blockquote>");
    }

    private void tabs() {
        i++;
        List<String[]> tabs = new ArrayList<>();
        String label = null;
        List<String> body = new ArrayList<>();
        int depth = 0;
        boolean inFence = false;
        while (i < lines.size()) {
            String line = lines.get(i);
            String trimmed = line.trim();
            if (FENCE.matcher(line).matches()) {
                inFence = !inFence;
            }
            if (!inFence) {
                if (trimmed.equals("<Tabs>")) {
                    depth++;
                } else if (trimmed.equals("</Tabs>")) {
                    if (depth == 0) {
                        i++;
                        break;
                    }
                    depth--;
                }
                Matcher item = TAB_ITEM.matcher(line);
                if (depth == 0 && item.matches()) {
                    label = item.group(1);
                    body = new ArrayList<>();
                    i++;
                    continue;
                }
                if (depth == 0 && trimmed.equals("</TabItem>")) {
                    tabs.add(new String[]{Html.esc(label), nested(Html.dedent(String.join("\n", body)).lines().toList())});
                    label = null;
                    i++;
                    continue;
                }
            }
            if (label != null) {
                body.add(line);
            }
            i++;
        }
        out.append(Layout.tabs(tabs));
    }

    private static boolean isOrdered(String marker) {
        return Character.isDigit(marker.charAt(0));
    }

    private void list(int indent, boolean ordered) {
        Matcher first = LIST_ITEM.matcher(lines.get(i));
        first.matches();
        int start = ordered ? Integer.parseInt(first.group(2).replaceAll("\\D", "")) : 1;
        List<List<String>> items = new ArrayList<>();
        boolean loose = false;
        while (i < lines.size()) {
            Matcher m = LIST_ITEM.matcher(lines.get(i));
            if (!m.matches() || m.group(1).length() != indent || isOrdered(m.group(2)) != ordered) {
                break;
            }
            int contentIndent = indent + m.group(2).length() + Math.max(1, m.group(3).length());
            List<String> item = new ArrayList<>();
            item.add(m.group(4));
            i++;
            while (i < lines.size()) {
                String line = lines.get(i);
                if (line.isBlank()) {
                    int next = i + 1;
                    while (next < lines.size() && lines.get(next).isBlank()) {
                        next++;
                    }
                    if (next < lines.size() && leadingSpaces(lines.get(next)) >= contentIndent) {
                        loose = true;
                        item.add("");
                        i++;
                        continue;
                    }
                    break;
                }
                int lead = leadingSpaces(line);
                Matcher sibling = LIST_ITEM.matcher(line);
                if (lead < contentIndent && (sibling.matches() || HEADING.matcher(line).matches() || FENCE.matcher(line).matches()
                        || HR.matcher(line).matches() || isHtmlLine(line))) {
                    break;
                }
                item.add(line.substring(Math.min(lead, contentIndent)));
                i++;
            }
            items.add(item);
            if (i < lines.size() && lines.get(i).isBlank()) {
                int next = i;
                while (next < lines.size() && lines.get(next).isBlank()) {
                    next++;
                }
                Matcher m2 = next < lines.size() ? LIST_ITEM.matcher(lines.get(next)) : null;
                if (m2 != null && m2.matches() && m2.group(1).length() == indent && isOrdered(m2.group(2)) == ordered) {
                    loose = true;
                    i = next;
                }
            }
        }
        out.append(ordered ? (start != 1 ? "<ol start=\"" + start + "\">" : "<ol>") : "<ul>");
        for (List<String> item : items) {
            String html = nested(item);
            if (!loose && html.startsWith("<p>")) {
                int end = html.indexOf("</p>");
                html = html.substring(3, end) + html.substring(end + 4);
            }
            out.append("<li>").append(html).append("</li>");
        }
        out.append(ordered ? "</ol>" : "</ul>");
    }

    private static int leadingSpaces(String line) {
        int n = 0;
        while (n < line.length() && line.charAt(n) == ' ') {
            n++;
        }
        return n;
    }

    private void paragraph() {
        StringBuilder text = new StringBuilder();
        while (i < lines.size()) {
            String line = lines.get(i);
            if (line.isBlank() || (text.length() > 0 && startsBlock(line))) {
                break;
            }
            if (text.length() > 0) {
                text.append('\n');
            }
            text.append(line.trim());
            i++;
        }
        String content = text.toString();
        boolean hardBreaks = content.contains("  \n");
        String html = inline(content);
        if (hardBreaks) {
            html = html.replace("  \n", "<br>\n");
        }
        out.append("<p>").append(html).append("</p>");
    }

    private boolean startsBlock(String line) {
        return FENCE.matcher(line).matches() || HEADING.matcher(line).matches() || HR.matcher(line).matches()
                || ASIDE.matcher(line).matches() || line.trim().startsWith(">") || line.trim().equals("<Tabs>")
                || isHtmlLine(line) || LIST_ITEM.matcher(line).matches();
    }

    // ------------------------------------------------------------------ inline

    String inline(String text) {
        List<String> slots = new ArrayList<>();
        StringBuilder t = new StringBuilder();
        int p = 0;
        while (p < text.length()) {
            char c = text.charAt(p);
            if (c == '\\' && p + 1 < text.length() && "\\`*_{}[]()#+-.!<>|~\"".indexOf(text.charAt(p + 1)) >= 0) {
                t.append(slot(slots, Html.esc(String.valueOf(text.charAt(p + 1)))));
                p += 2;
            } else if (c == '`') {
                int run = 0;
                while (p + run < text.length() && text.charAt(p + run) == '`') {
                    run++;
                }
                String fence = "`".repeat(run);
                int close = text.indexOf(fence, p + run);
                while (close >= 0 && close + run < text.length() && text.charAt(close + run) == '`') {
                    close = text.indexOf(fence, close + run + 1);
                }
                if (close < 0) {
                    t.append(fence);
                    p += run;
                    continue;
                }
                String code = text.substring(p + run, close).replace('\n', ' ');
                if (code.length() > 2 && code.startsWith(" ") && code.endsWith(" ")) {
                    code = code.substring(1, code.length() - 1);
                }
                t.append(slot(slots, codeSpan(code)));
                p = close + run;
            } else if (c == '<') {
                String rest = text.substring(p);
                Matcher auto = AUTOLINK.matcher(rest);
                Matcher tag = TAG.matcher(rest);
                if (auto.find()) {
                    t.append(slot(slots, "<a href=\"" + Html.esc(auto.group(1)) + "\" rel=\"external\">" + Html.esc(auto.group(1)) + "</a>"));
                    p += auto.end();
                } else if (tag.find()) {
                    t.append(slot(slots, tag.group().startsWith("<!--") ? "" : rewriteHtml(tag.group())));
                    p += tag.end();
                } else {
                    t.append(c);
                    p++;
                }
            } else {
                t.append(c);
                p++;
            }
        }

        Matcher link = LINK.matcher(t.toString());
        StringBuilder linked = new StringBuilder();
        while (link.find()) {
            String href = Html.esc(context.link(link.group(3)));
            String html;
            if (!link.group(1).isEmpty()) {
                html = "<img src=\"" + href + "\" alt=\"" + Html.esc(restore(link.group(2), slots).replaceAll("<[^>]*>", "")) + "\">";
            } else {
                boolean external = href.startsWith("http");
                html = "<a href=\"" + href + "\"" + (external ? " rel=\"external\"" : "")
                        + (link.group(4) != null ? " title=\"" + Html.esc(link.group(4)) + "\"" : "") + ">"
                        + emphasis(escapeText(link.group(2))) + "</a>";
            }
            link.appendReplacement(linked, Matcher.quoteReplacement(slot(slots, html)));
        }
        link.appendTail(linked);

        return restore(emphasis(escapeText(linked.toString())), slots);
    }

    private String codeSpan(String code) {
        String html = "<code>" + Html.esc(code) + "</code>";
        String url = context.codeLink(code);
        return url == null ? html : "<a class=\"code-ref\" href=\"" + Html.esc(url) + "\">" + html + "</a>";
    }

    private static String slot(List<String> slots, String html) {
        slots.add(html);
        return "\u0000" + (slots.size() - 1) + "\u0000";
    }

    private static String restore(String text, List<String> slots) {
        String result = text;
        for (int round = 0; round < 5 && result.indexOf('\u0000') >= 0; round++) {
            Matcher m = Pattern.compile("\u0000(\\d+)\u0000").matcher(result);
            StringBuilder sb = new StringBuilder();
            while (m.find()) {
                m.appendReplacement(sb, Matcher.quoteReplacement(slots.get(Integer.parseInt(m.group(1)))));
            }
            m.appendTail(sb);
            result = sb.toString();
        }
        return result;
    }

    private static String escapeText(String text) {
        StringBuilder sb = new StringBuilder();
        Matcher entity = ENTITY.matcher(text);
        int last = 0;
        while (entity.find()) {
            sb.append(Html.esc(text.substring(last, entity.start()))).append(entity.group());
            last = entity.end();
        }
        sb.append(Html.esc(text.substring(last)));
        return sb.toString();
    }

    private static String emphasis(String html) {
        return html
                .replaceAll("\\*\\*(?=\\S)(.+?)(?<=\\S)\\*\\*", "<strong>$1</strong>")
                .replaceAll("(?<![\\w])__(?=\\S)(.+?)(?<=\\S)__(?![\\w])", "<strong>$1</strong>")
                .replaceAll("(?<![\\w*])\\*(?=[^\\s*])(.+?)(?<=[^\\s*])\\*(?![\\w*])", "<em>$1</em>")
                .replaceAll("(?<![\\w])_(?=[^\\s_])(.+?)(?<=[^\\s_])_(?![\\w])", "<em>$1</em>")
                .replaceAll("~~(?=\\S)(.+?)(?<=\\S)~~", "<del>$1</del>");
    }
}
