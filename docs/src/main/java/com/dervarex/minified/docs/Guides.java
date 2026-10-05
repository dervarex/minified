package com.dervarex.minified.docs;

import com.dervarex.minified.docs.Site.Page;
import com.dervarex.minified.docs.Site.SearchEntry;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Hand-written pages from {@code docs/content}. Inline code that names a documented type or member links to
 * the API reference, references to members that no longer exist are reported as warnings
 */
final class Guides {

    /**
     * @param path      page path, derived from the file path like the former Starlight slugs
     * @param hideTitle  whether the page has no visible title, set with an empty {@code title}
     * @param showToc    whether the "On this page" list is shown, off with {@code tableOfContents: false}
     * @param stylesheet additional stylesheet from the theme directory, or null
     */
    record Guide(Path file, String path, String title, String description, String markdown, boolean hideTitle,
                 boolean showToc, String stylesheet) {
    }

    private static final Pattern CODE_REF = Pattern.compile(
            "^(?:new\\s+)?([A-Z]\\w*(?:\\.[A-Z]\\w*)*)(?:(?:\\.|#|::)([a-z_$][\\w$]*))?(?:<[^>]*>)?(?:\\(.*\\))?;?$");
    private static final String OLD_BASE = "/minified-docs";

    private final Site site;
    private final List<Guide> guides = new ArrayList<>();
    private Set<String> knownPaths;

    Guides(Site site) {
        this.site = site;
    }

    void load() throws IOException {
        Path content = site.config.content();
        List<Path> files;
        try (Stream<Path> walk = Files.walk(content)) {
            files = walk.filter(p -> p.toString().endsWith(".md")).sorted().toList();
        }
        for (Path file : files) {
            Path relative = content.relativize(file);
            if (relative.startsWith("modules") || isEventContent(file)) {
                continue;
            }
            String text = Files.readString(file);
            Map<String, String> meta = Markdown.frontMatter(text);
            String slug = relative.toString().replace('\\', '/').replaceFirst("\\.md$", "").toLowerCase(Locale.ROOT);
            String path = slug.equals("index") ? "" : slug + "/";
            String title = meta.getOrDefault("title", "").replace("​", "").trim();
            String stylesheet = meta.getOrDefault("stylesheet", path.isEmpty() ? "home.css" : null);
            guides.add(new Guide(file, path, title.isEmpty() ? "Minified" : title, meta.getOrDefault("description", ""),
                    Markdown.stripFrontMatter(text), title.isEmpty(), !"false".equals(meta.get("tableOfContents")), stylesheet));
        }
    }

    private boolean isEventContent(Path file) {
        Path events = site.config.content().resolve("events");
        String base = file.getFileName().toString().replaceFirst("\\.md$", "");
        return file.getParent().equals(events) && site.events.byLowerName(base.toLowerCase(Locale.ROOT)) != null;
    }

    void build() {
        for (Guide guide : guides) {
            Markdown.Result result = render(guide.markdown(), guide.path(), guide.file());
            String section = guide.path().isEmpty() ? "" : guide.path().substring(0, guide.path().indexOf('/'));
            site.addPage(new Page(guide.path(), guide.title(), guide.description(), result.html(),
                    guide.showToc() ? result.toc() : List.of(), section, guide.hideTitle(), guide.stylesheet()));
            if (!guide.hideTitle()) {
                site.search.add(new SearchEntry(guide.title(), "guide", guide.path(), guide.description()));
                for (Site.TocEntry entry : result.toc()) {
                    if (entry.level() == 2) {
                        site.search.add(new SearchEntry(entry.text(), "section", guide.path() + "#" + entry.id(), guide.title()));
                    }
                }
            }
        }
    }

    Markdown.Result render(String markdown, String from, Path file) {
        String where = site.config.root().relativize(file.toAbsolutePath().normalize()).toString();
        String text = Markdown.stripFrontMatter(markdown).replace("{{version}}", site.config.version());
        return new Markdown(text, new Markdown.Context() {
            @Override
            public String link(String href) {
                return resolveLink(href, from, where);
            }

            @Override
            public String codeLink(String code) {
                return resolveCode(code, from, where);
            }

            @Override
            public String directive(String name, String argument) {
                return switch (name) {
                    case "events" -> site.events.overview(from);
                    default -> {
                        site.warnings.add(where, "unknown directive {{" + name + "}}");
                        yield null;
                    }
                };
            }
        }).render();
    }

    // ------------------------------------------------------------------ links

    private Set<String> knownPaths() {
        if (knownPaths == null) {
            knownPaths = new HashSet<>();
            guides.forEach(g -> knownPaths.add(g.path()));
            site.events.events().forEach(e -> knownPaths.add(e.path()));
            knownPaths.addAll(site.typePaths.values());
            site.packages.values().forEach(p -> knownPaths.add(p.path()));
            site.modules.forEach(m -> knownPaths.add(m.path()));
            knownPaths.add("api/");
        }
        return knownPaths;
    }

    private String resolveLink(String href, String from, String where) {
        if (href.isEmpty() || href.startsWith("#") || href.matches("^[a-z]+:.*")) {
            return href;
        }
        String fragment = "";
        int hash = href.indexOf('#');
        String target = href;
        if (hash >= 0) {
            fragment = href.substring(hash);
            target = href.substring(0, hash);
        }
        List<String> candidates = new ArrayList<>();
        if (target.startsWith("/")) {
            String path = target.startsWith(OLD_BASE + "/") ? target.substring(OLD_BASE.length()) : target;
            candidates.add(normalize(path.substring(1)));
        } else {
            // relative links were resolved against the page itself or its parent, try both
            candidates.add(normalize(from + target));
            candidates.add(normalize(parent(from) + target));
        }
        for (String candidate : candidates) {
            if (knownPaths().contains(candidate)) {
                return Html.rel(from, candidate) + fragment;
            }
        }
        if (target.startsWith("assets/") || target.startsWith("/assets/")) {
            return Html.rel(from, target.replaceFirst("^/", "")) + fragment;
        }
        String last = candidates.get(0).replaceAll("/$", "");
        last = last.substring(last.lastIndexOf('/') + 1);
        EventPages.EventDoc event = site.events.byLowerName(last);
        if (event != null) {
            return Html.rel(from, event.path()) + fragment;
        }
        site.warnings.add(where, "broken link " + href);
        return href;
    }

    private static String parent(String path) {
        String trimmed = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int slash = trimmed.lastIndexOf('/');
        return slash < 0 ? "" : trimmed.substring(0, slash + 1);
    }

    private static String normalize(String path) {
        Deque deque = new Deque();
        for (String part : path.split("/")) {
            if (part.isEmpty() || part.equals(".")) {
                continue;
            }
            if (part.equals("..")) {
                deque.pop();
            } else {
                deque.push(part.toLowerCase(Locale.ROOT));
            }
        }
        return deque.join();
    }

    private static final class Deque {
        private final List<String> parts = new ArrayList<>();

        void push(String part) {
            parts.add(part);
        }

        void pop() {
            if (!parts.isEmpty()) {
                parts.remove(parts.size() - 1);
            }
        }

        String join() {
            return parts.isEmpty() ? "" : String.join("/", parts) + "/";
        }
    }

    // ------------------------------------------------------------------ code references

    private String resolveCode(String code, String from, String where) {
        Matcher m = CODE_REF.matcher(code.trim());
        if (!m.matches()) {
            return null;
        }
        String[] parts = m.group(1).split("\\.");
        TypeElement type = null;
        int used = 0;
        for (int n = parts.length; n > 0; n--) {
            TypeElement candidate = site.findType(String.join(".", java.util.Arrays.copyOf(parts, n)));
            if (candidate != null) {
                type = candidate;
                used = n;
                break;
            }
        }
        if (type == null) {
            return null;
        }
        String member = m.group(2);
        if (used < parts.length) {
            // an upper case segment that is no nested type, e.g. an enum constant
            member = parts[used];
        }
        if (member == null) {
            return site.typeUrl(type, from);
        }
        String url = memberUrl(type, member, from);
        if ("".equals(url)) {
            // exists but is not part of the documented api, e.g. a private helper the prose explains
            return null;
        }
        if (url == null) {
            site.warnings.add(where, "`" + code + "` references " + Site.nestedName(type) + "." + member
                    + " which does not exist (anymore)");
            return site.typeUrl(type, from);
        }
        return url;
    }

    /**
     * Url of a member of a type, declared or inherited or generated by Lombok. Empty when the member exists but
     * is not documented, like a private method, null when there is no such member
     */
    private String memberUrl(TypeElement type, String name, String from) {
        boolean undocumented = false;
        for (Element e : site.elements.getAllMembers(type)) {
            if (!e.getSimpleName().contentEquals(name) || !(e.getKind() == ElementKind.METHOD
                    || e.getKind() == ElementKind.FIELD || e.getKind() == ElementKind.ENUM_CONSTANT)) {
                continue;
            }
            TypeElement owner = (TypeElement) e.getEnclosingElement();
            if (!site.typePaths.containsKey(owner)) {
                return site.typeUrl(type, from);
            }
            if (!site.env.isIncluded(e)) {
                // private, e.g. a field behind a Lombok getter, which is handled below
                undocumented = true;
                continue;
            }
            boolean recordAccessor = owner.getKind() == ElementKind.RECORD && e instanceof ExecutableElement exec
                    && exec.getParameters().isEmpty()
                    && owner.getRecordComponents().stream().anyMatch(c -> c.getSimpleName().contentEquals(name));
            if (recordAccessor) {
                return site.typeUrl(owner, from) + "#" + name;
            }
            return site.elementUrl(e, from);
        }
        for (TypeElement current = type; current != null; current = superclass(current)) {
            for (Lombok.Generated generated : Lombok.members(current)) {
                boolean forField = generated.kind() == Lombok.Kind.GETTER && generated.field().getSimpleName().contentEquals(name);
                if (generated.name().equals(name) || forField) {
                    String anchor = generated.kind() == Lombok.Kind.SETTER
                            ? Signatures.anchor(generated.name(), List.of(site.signatures.erasedName(generated.field().asType())))
                            : Signatures.anchor(generated.name(), List.of());
                    return site.typeUrl(current, from) + "#" + anchor;
                }
            }
        }
        return undocumented ? "" : null;
    }

    private TypeElement superclass(TypeElement type) {
        TypeElement sup = site.asTypeElement(type.getSuperclass());
        return sup != null && site.typePaths.containsKey(sup) ? sup : null;
    }
}
