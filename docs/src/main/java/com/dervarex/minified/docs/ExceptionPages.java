package com.dervarex.minified.docs;

import com.dervarex.minified.docs.EventPages.Field;
import com.dervarex.minified.docs.EventPages.Post;
import com.dervarex.minified.docs.Site.ModuleDoc;
import com.dervarex.minified.docs.Site.Page;
import com.dervarex.minified.docs.Site.SearchEntry;
import com.dervarex.minified.docs.Site.TocEntry;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ThrowsTree;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * All exception types of the library here
 * {@code content/exceptions/<Exception>.md}
 */
final class ExceptionPages {

    static final String SECTION = "exceptions";
    private static final Set<String> OBJECT_METHODS = Set.of("toString", "hashCode");
    private static final int SNIPPET_FIELDS = 3;

    /**
     * @param group     sidebar group, the module label
     * @param checked   whether callers have to catch or declare it
     * @param thrownBy  documented methods and constructors that declare it with {@code throws} or {@code @throws}
     * @param thrownAt  places in the library sources that create it
     */
    record ExceptionDoc(TypeElement type, String group, String path, ModuleDoc module, boolean checked,
                        Set<ExecutableElement> thrownBy, List<Post> thrownAt) {
    }

    private final Site site;
    private final List<ExceptionDoc> exceptions = new ArrayList<>();
    private final Map<TypeElement, ExceptionDoc> byType = new HashMap<>();

    ExceptionPages(Site site) {
        this.site = site;
    }

    List<ExceptionDoc> exceptions() {
        return exceptions;
    }

    ExceptionDoc exceptionFor(TypeElement type) {
        return byType.get(type);
    }

    ExceptionDoc byLowerName(String name) {
        return exceptions.stream().filter(e -> e.type().getSimpleName().toString().toLowerCase(Locale.ROOT).equals(name))
                .findFirst().orElse(null);
    }

    void collect() throws IOException {
        TypeElement throwable = site.elements.getTypeElement("java.lang.Throwable");
        TypeElement runtime = site.elements.getTypeElement("java.lang.RuntimeException");
        TypeElement error = site.elements.getTypeElement("java.lang.Error");
        for (TypeElement type : site.typePaths.keySet()) {
            if (type.getKind().isInterface() || !site.types.isSubtype(type.asType(), throwable.asType())) {
                continue;
            }
            boolean checked = !site.types.isSubtype(type.asType(), runtime.asType())
                    && !site.types.isSubtype(type.asType(), error.asType());
            ModuleDoc module = site.typePackages.get(type).module();
            String path = "exceptions/" + Html.slug(module.label()) + "/" + type.getSimpleName().toString().toLowerCase(Locale.ROOT) + "/";
            ExceptionDoc doc = new ExceptionDoc(type, module.label(), path, module, checked, new LinkedHashSet<>(), new ArrayList<>());
            exceptions.add(doc);
            byType.put(type, doc);
        }
        // same order as the modules in the API reference
        exceptions.sort(Comparator.comparing((ExceptionDoc e) -> site.modules.indexOf(e.module()))
                .thenComparing(e -> e.type().getSimpleName().toString()));
        findDeclarations();
        findThrowSites();
    }

    private void findDeclarations() {
        for (TypeElement owner : site.typePaths.keySet()) {
            List<ExecutableElement> executables = new ArrayList<>(ElementFilter.methodsIn(owner.getEnclosedElements()));
            executables.addAll(ElementFilter.constructorsIn(owner.getEnclosedElements()));
            for (ExecutableElement exec : executables) {
                if (!site.env.isIncluded(exec)) {
                    continue;
                }
                for (TypeMirror thrown : exec.getThrownTypes()) {
                    add(site.asTypeElement(thrown), exec);
                }
                DocComments.Doc doc = site.comments.find(exec);
                if (doc == null || doc.owner() != exec) {
                    continue;
                }
                for (DocTree tag : doc.tree().getBlockTags()) {
                    if (tag instanceof ThrowsTree t && site.comments.resolve(doc, t.getExceptionName()) instanceof TypeElement type) {
                        add(type, exec);
                    }
                }
            }
        }
    }

    private void add(TypeElement type, ExecutableElement exec) {
        ExceptionDoc doc = type == null ? null : byType.get(type);
        if (doc != null) {
            doc.thrownBy().add(exec);
        }
    }

    private void findThrowSites() throws IOException {
        if (exceptions.isEmpty()) {
            return;
        }
        Map<String, ExceptionDoc> bySimpleName = new HashMap<>();
        exceptions.forEach(e -> bySimpleName.put(e.type().getSimpleName().toString(), e));
        // also matches builders, new HttpException.Builder()...build() is thrown just the same
        Pattern creation = Pattern.compile("new\\s+(?:[\\w.]+\\.)?(" + String.join("|", bySimpleName.keySet()) + ")(?:\\.Builder)?\\s*[(<]");
        for (Config.ModuleSource module : site.config.modules()) {
            List<Path> files;
            try (Stream<Path> walk = Files.walk(module.sourceDir())) {
                files = walk.filter(p -> p.toString().endsWith(".java")).sorted().toList();
            }
            for (Path file : files) {
                Path normalized = file.toAbsolutePath().normalize();
                String text = Files.readString(file);
                Matcher m = creation.matcher(text);
                while (m.find()) {
                    ExceptionDoc exception = bySimpleName.get(m.group(1));
                    // the exception creating itself, like Builder.build(), is no throw site
                    if (normalized.equals(site.sourceFile(exception.type()))) {
                        continue;
                    }
                    long line = text.substring(0, m.start()).chars().filter(c -> c == '\n').count() + 1;
                    String owner = file.getFileName().toString().replaceFirst("\\.java$", "");
                    exception.thrownAt().add(new Post(owner, normalized, line));
                }
            }
        }
    }

    void build() throws IOException {
        for (ExceptionDoc exception : exceptions) {
            site.addPage(page(exception));
        }
    }

    /**
     * Grouped list of all exceptions
     */
    String overview(String from) {
        Map<String, List<ExceptionDoc>> groups = new LinkedHashMap<>();
        exceptions.forEach(e -> groups.computeIfAbsent(e.group(), k -> new ArrayList<>()).add(e));
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, List<ExceptionDoc>> group : groups.entrySet()) {
            List<ExceptionDoc> members = group.getValue();
            b.append("<div class=\"type-definition\"><div class=\"type-definition-header\"><h3>").append(Html.esc(group.getKey()))
                    .append("</h3><span class=\"type-badge\">").append(members.get(0).module().name()).append("</span></div><div class=\"enum-rows\">");
            for (ExceptionDoc root : members) {
                ExceptionDoc parent = documentedSuperclass(root);
                if (parent == null || !members.contains(parent)) {
                    overviewRow(b, root, members, 0, from);
                }
            }
            b.append("</div></div>");
        }
        return b.toString();
    }

    private void overviewRow(StringBuilder b, ExceptionDoc exception, List<ExceptionDoc> members, int depth, String from) {
        String summary = site.comments.summary(site.comments.find(exception.type()), from);
        b.append("<div class=\"enum-row").append(depth > 0 ? " exception-depth-" + Math.min(depth, 3) : "").append("\"><a href=\"")
                .append(Html.rel(from, exception.path())).append("\"><code>").append(exception.type().getSimpleName())
                .append("</code></a><span>");
        if (summary.isEmpty()) {
            TypeMirror superclass = exception.type().getSuperclass();
            summary = "Extends <code>" + Html.esc(site.signatures.plain(superclass)) + "</code>.";
        }
        b.append(summary).append("</span></div>");
        for (ExceptionDoc child : members) {
            if (documentedSuperclass(child) == exception) {
                overviewRow(b, child, members, depth + 1, from);
            }
        }
    }

    private ExceptionDoc documentedSuperclass(ExceptionDoc exception) {
        TypeElement superclass = site.asTypeElement(exception.type().getSuperclass());
        return superclass == null ? null : byType.get(superclass);
    }

    private Path contentFile(ExceptionDoc exception) {
        return site.config.content().resolve("exceptions").resolve(exception.type().getSimpleName() + ".md");
    }

    private Page page(ExceptionDoc exception) throws IOException {
        String from = exception.path();
        TypeElement type = exception.type();
        String name = type.getSimpleName().toString();
        DocComments.Doc doc = site.comments.find(type);
        StringBuilder b = new StringBuilder();
        List<TocEntry> toc = new ArrayList<>();

        String eyebrow = "<a href=\"" + Html.rel(from, "exceptions/introduction/") + "\">Exceptions</a><span class=\"api-crumb-sep\">/</span>"
                + "<a href=\"" + Html.rel(from, exception.module().path()) + "\">" + exception.module().name() + "</a>";
        b.append("<div class=\"api-header\"><div class=\"api-badges\">")
                .append(Layout.badge(exception.checked() ? "checked" : "unchecked", "kind",
                        exception.checked() ? "Has to be caught or declared with throws" : "Extends RuntimeException, catching it is optional"));
        if (!site.typePackages.get(type).exported()) {
            b.append(Layout.badge("internal", "internal", "The package isn't exported, you can't catch this type by name"));
        }
        b.append("<a class=\"api-link\" href=\"").append(site.typeUrl(type, from)).append("\">API reference</a>");
        String source = site.sourceUrl(type);
        if (source != null) {
            b.append("<a class=\"api-source\" href=\"").append(source).append("\" rel=\"external\">Source</a>");
        }
        b.append("</div></div>");

        String body = site.comments.body(doc, from);
        String description = Html.stripTags(site.comments.summary(doc, from));
        Markdown.Result extra = null;
        if (Files.exists(contentFile(exception))) {
            String markdown = Files.readString(contentFile(exception));
            extra = site.guides.render(markdown, from, contentFile(exception));
            if (description.isEmpty()) {
                description = Markdown.frontMatter(markdown).getOrDefault("description", "");
            }
        }
        if (!body.isEmpty()) {
            b.append("<div class=\"api-description\">").append(body.startsWith("<") ? body : "<p>" + body + "</p>").append("</div>");
        } else if (!description.isEmpty()) {
            b.append("<div class=\"api-description\"><p>").append(Html.esc(description)).append("</p></div>");
        }

        hierarchy(b, toc, exception, from);

        List<Field> fields = fields(type, from);
        if (!fields.isEmpty()) {
            b.append("<h2 id=\"fields\">Fields</h2>");
            toc.add(new TocEntry(2, "fields", "Fields"));
            b.append("<div class=\"fields-details\"><div class=\"fields-grid\">");
            ApiPages api = new ApiPages(site);
            for (Field field : fields) {
                b.append("<div class=\"field-card\"><div class=\"field-header\"><code>").append(Html.esc(field.name()))
                        .append("</code>").append(api.typeBadge(field.type(), from)).append("</div><p>")
                        .append(field.description().isEmpty() ? "<span class=\"api-muted\">No description.</span>" : ApiPages.capitalize(field.description()))
                        .append("</p><span class=\"field-meta\">Accessor: <code>").append(Html.esc(field.accessor())).append("</code></span></div>");
            }
            b.append("</div></div>");
        }

        if (extra != null) {
            b.append(extra.html());
            toc.addAll(extra.toc());
        }

        b.append("<h2 id=\"catching\">Catching</h2>");
        toc.add(new TocEntry(2, "catching", "Catching"));
        b.append(Layout.tabs(List.of(
                new String[]{"Java", snippet(javaSnippet(name, fields), "java")},
                new String[]{"Kotlin", snippet(kotlinSnippet(name, fields), "kotlin")})));

        if (!exception.thrownBy().isEmpty()) {
            b.append("<h2 id=\"thrown-by\">Thrown by</h2>");
            toc.add(new TocEntry(2, "thrown-by", "Thrown by"));
            b.append("<p class=\"api-muted\">Methods that declare it with <code>throws</code> or document it with <code>@throws</code>.</p><ul class=\"api-used-by\">");
            exception.thrownBy().stream()
                    .sorted(Comparator.comparing((ExecutableElement m) -> Site.nestedName((TypeElement) m.getEnclosingElement()))
                            .thenComparing(m -> m.getSimpleName().toString()))
                    .forEach(m -> b.append("<li><a href=\"").append(site.elementUrl(m, from)).append("\"><code>")
                            .append(memberLabel(m)).append("</code></a></li>"));
            b.append("</ul>");
        }

        if (!exception.thrownAt().isEmpty()) {
            b.append("<h2 id=\"created-in\">Created in</h2>");
            toc.add(new TocEntry(2, "created-in", "Created in"));
            b.append("<ul class=\"api-used-by\">");
            for (Post post : exception.thrownAt()) {
                TypeElement ownerType = site.findType(post.owner());
                b.append("<li>");
                if (ownerType != null && site.typePaths.containsKey(ownerType)) {
                    b.append("<a href=\"").append(site.typeUrl(ownerType, from)).append("\"><code>").append(post.owner()).append("</code></a> ");
                } else {
                    b.append("<code>").append(post.owner()).append("</code> ");
                }
                if (!site.config.repo().isEmpty()) {
                    b.append("<a class=\"api-source\" href=\"").append(site.sourceUrl(post.file(), post.line()))
                            .append("\" rel=\"external\">").append(Html.esc(post.owner() + ".java:" + post.line())).append("</a>");
                }
                b.append("</li>");
            }
            b.append("</ul>");
        }

        b.append("<p class=\"member-note\">API reference: <a href=\"").append(site.typeUrl(type, from)).append("\"><code>")
                .append(site.elements.getPackageOf(type).getQualifiedName()).append('.').append(Site.nestedName(type)).append("</code></a></p>");

        String kind = (exception.checked() ? "checked" : "unchecked") + " exception";
        site.search.add(new SearchEntry(name, "exception", from, exception.module().name() + " " + kind));
        return new Page(from, name, description, b.toString(), toc, SECTION, false, null, eyebrow);
    }

    private void hierarchy(StringBuilder b, List<TocEntry> toc, ExceptionDoc exception, String from) {
        List<TypeMirror> chain = new ArrayList<>();
        TypeMirror current = exception.type().getSuperclass();
        while (site.asTypeElement(current) != null && !site.asTypeElement(current).getQualifiedName().contentEquals("java.lang.Object")) {
            chain.add(0, current);
            current = site.asTypeElement(current).getSuperclass();
        }
        b.append("<h2 id=\"hierarchy\">Hierarchy</h2>");
        toc.add(new TocEntry(2, "hierarchy", "Hierarchy"));
        b.append("<dl class=\"api-relations\"><dt>Extends</dt><dd>");
        List<String> links = new ArrayList<>();
        for (TypeMirror t : chain) {
            TypeElement element = site.asTypeElement(t);
            ExceptionDoc documented = byType.get(element);
            String href = documented != null ? Html.rel(from, documented.path()) : site.typeUrl(element, from);
            String label = Html.esc(Site.nestedName(element));
            links.add(href == null ? "<code>" + label + "</code>" : "<a class=\"tref\" href=\"" + href + "\">" + label + "</a>");
        }
        b.append(String.join(" → ", links)).append(" → <code>").append(Html.esc(Site.nestedName(exception.type()))).append("</code></dd>");
        List<ExceptionDoc> subclasses = exceptions.stream().filter(e -> documentedSuperclass(e) == exception).toList();
        if (!subclasses.isEmpty()) {
            b.append("<dt>Subclasses</dt><dd>");
            List<String> subs = new ArrayList<>();
            for (ExceptionDoc sub : subclasses) {
                subs.add("<a class=\"tref\" href=\"" + Html.rel(from, sub.path()) + "\">" + Html.esc(Site.nestedName(sub.type())) + "</a>");
            }
            b.append(String.join(", ", subs)).append("</dd>");
        }
        b.append("</dl>");
    }

    /**
     * Getters of the exception and of its documented superclasses
     */
    private List<Field> fields(TypeElement type, String from) {
        List<Field> fields = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        TypeElement current = type;
        while (current != null && site.typePaths.containsKey(current)) {
            for (Field field : site.events.fields(current, site.comments.find(current), from)) {
                String method = field.accessor().replace("()", "");
                if (!OBJECT_METHODS.contains(method) && method.matches("(get|is)[A-Z].*") && seen.add(field.accessor())) {
                    fields.add(field);
                }
            }
            current = site.asTypeElement(current.getSuperclass());
        }
        return fields;
    }

    private String memberLabel(ExecutableElement m) {
        String owner = Site.nestedName((TypeElement) m.getEnclosingElement());
        String name = m.getSimpleName().contentEquals("<init>") ? "new " + owner : owner + "." + m.getSimpleName();
        List<String> params = m.getParameters().stream()
                .map(p -> site.signatures.plain(site.types.erasure(p.asType())))
                .map(t -> t.substring(t.lastIndexOf('.') + 1))
                .toList();
        return Html.esc(name + "(" + String.join(", ", params) + ")");
    }

    private String javaSnippet(String exception, List<Field> fields) {
        StringBuilder sb = new StringBuilder("try {\n    // call into Minified\n} catch (").append(exception).append(" e) {\n");
        if (fields.isEmpty()) {
            sb.append("    System.err.println(e.getMessage());\n");
        }
        for (Field field : fields.stream().limit(SNIPPET_FIELDS).toList()) {
            sb.append("    ").append(site.signatures.plain(field.type())).append(' ').append(field.name())
                    .append(" = e.").append(field.accessor()).append(";\n");
        }
        if (fields.size() > SNIPPET_FIELDS) {
            sb.append("    // ... see Fields for the rest\n");
        }
        return sb.append('}').toString();
    }

    private String kotlinSnippet(String exception, List<Field> fields) {
        StringBuilder sb = new StringBuilder("try {\n    // call into Minified\n} catch (e: ").append(exception).append(") {\n");
        if (fields.isEmpty()) {
            sb.append("    System.err.println(e.message)\n");
        }
        for (Field field : fields.stream().limit(SNIPPET_FIELDS).toList()) {
            boolean getter = !field.accessor().equals(field.name() + "()");
            sb.append("    val ").append(field.name()).append(" = e.").append(getter ? field.name() : field.accessor()).append('\n');
        }
        if (fields.size() > SNIPPET_FIELDS) {
            sb.append("    // ... see Fields for the rest\n");
        }
        return sb.append('}').toString();
    }

    private static String snippet(String code, String lang) {
        return Layout.codeFrame(Highlighter.highlight(code, lang), lang, null);
    }
}
