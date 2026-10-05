package com.dervarex.minified.docs;

import com.dervarex.minified.docs.Site.ModuleDoc;
import com.dervarex.minified.docs.Site.Page;
import com.dervarex.minified.docs.Site.SearchEntry;
import com.dervarex.minified.docs.Site.TocEntry;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * One page per event, generated from the event types plus optional prose in {@code content/events/<Event>.md}
 */
final class EventPages {

    static final String SECTION = "events";
    static final String EVENT_INTERFACE = "com.dervarex.minified.events.Event";
    static final String EVENTS_MODULE = "minified-events";

    /**
     * @param group sidebar group, the module label or for the events module itself the sub package
     * @param posts places in the library sources that create the event
     */
    record EventDoc(TypeElement type, String group, String path, ModuleDoc module, List<Post> posts) {
    }

    record Post(String owner, Path file, long line) {
    }

    /**
     * A value the event carries, a record component or a getter
     */
    private record Field(String name, TypeMirror type, String accessor, String description) {
    }

    private final Site site;
    private final List<EventDoc> events = new ArrayList<>();
    private final Map<TypeElement, EventDoc> byType = new HashMap<>();

    EventPages(Site site) {
        this.site = site;
    }

    List<EventDoc> events() {
        return events;
    }

    EventDoc eventFor(TypeElement type) {
        return byType.get(type);
    }

    /**
     * Finds an event by the last path segment, which is the lower case simple name
     */
    EventDoc byLowerName(String name) {
        return events.stream().filter(e -> e.type().getSimpleName().toString().toLowerCase(Locale.ROOT).equals(name))
                .findFirst().orElse(null);
    }


    void collect() throws IOException {
        TypeElement eventInterface = site.elements.getTypeElement(EVENT_INTERFACE);
        if (eventInterface == null) {
            return;
        }
        TypeMirror erasedEvent = site.types.erasure(eventInterface.asType());
        for (TypeElement type : site.typePaths.keySet()) {
            if (type.equals(eventInterface) || type.getModifiers().contains(Modifier.ABSTRACT) || type.getKind().isInterface()) {
                continue;
            }
            if (!site.types.isAssignable(site.types.erasure(type.asType()), erasedEvent)) {
                continue;
            }
            ModuleDoc module = site.typePackages.get(type).module();
            String group = module.name().equals(EVENTS_MODULE) ? subPackageLabel(type) : module.label();
            String path = "events/" + Html.slug(group) + "/" + type.getSimpleName().toString().toLowerCase(Locale.ROOT) + "/";
            EventDoc doc = new EventDoc(type, group, path, module, new ArrayList<>());
            events.add(doc);
            byType.put(type, doc);
        }
        events.sort(Comparator.comparing((EventDoc e) -> !e.module().name().equals(EVENTS_MODULE))
                .thenComparing(EventDoc::group)
                .thenComparing(e -> e.type().getSimpleName().toString()));
        findPosts();
    }

    private String subPackageLabel(TypeElement type) {
        String pkg = site.elements.getPackageOf(type).getQualifiedName().toString();
        String last = pkg.substring(pkg.lastIndexOf('.') + 1);
        return Character.toUpperCase(last.charAt(0)) + last.substring(1);
    }

    private void findPosts() throws IOException {
        if (events.isEmpty()) {
            return;
        }
        Map<String, EventDoc> bySimpleName = new HashMap<>();
        events.forEach(e -> bySimpleName.put(e.type().getSimpleName().toString(), e));
        Pattern creation = Pattern.compile("new\\s+(" + String.join("|", bySimpleName.keySet()) + ")\\s*[(<]");
        for (Config.ModuleSource module : site.config.modules()) {
            List<Path> files;
            try (Stream<Path> walk = Files.walk(module.sourceDir())) {
                files = walk.filter(p -> p.toString().endsWith(".java")).sorted().toList();
            }
            for (Path file : files) {
                String text = Files.readString(file);
                Matcher m = creation.matcher(text);
                while (m.find()) {
                    long line = text.substring(0, m.start()).chars().filter(c -> c == '\n').count() + 1;
                    String owner = file.getFileName().toString().replaceFirst("\\.java$", "");
                    bySimpleName.get(m.group(1)).posts().add(new Post(owner, file.toAbsolutePath().normalize(), line));
                }
            }
        }
    }

    // ------------------------------------------------------------------ pages

    void build() throws IOException {
        for (EventDoc event : events) {
            site.addPage(page(event));
        }
    }

    /**
     * Grouped list of all events, used by the {@code ::: events} directive
     */
    String overview(String from) {
        Map<String, List<EventDoc>> groups = new LinkedHashMap<>();
        events.forEach(e -> groups.computeIfAbsent(e.group(), k -> new ArrayList<>()).add(e));
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, List<EventDoc>> group : groups.entrySet()) {
            b.append("<div class=\"type-definition\"><div class=\"type-definition-header\"><h3>").append(Html.esc(group.getKey()))
                    .append("</h3><span class=\"type-badge\">").append(group.getValue().get(0).module().name()).append("</span></div><div class=\"enum-rows\">");
            for (EventDoc event : group.getValue()) {
                String summary = site.comments.summary(site.comments.find(event.type()), from);
                if (summary.isEmpty()) {
                    summary = Html.esc(contentDescription(event));
                }
                b.append("<div class=\"enum-row\"><a href=\"").append(Html.rel(from, event.path())).append("\"><code>")
                        .append(event.type().getSimpleName()).append("</code></a><span>").append(summary).append("</span></div>");
            }
            b.append("</div></div>");
        }
        return b.toString();
    }

    private Path contentFile(EventDoc event) {
        return site.config.content().resolve("events").resolve(event.type().getSimpleName() + ".md");
    }

    private String contentDescription(EventDoc event) {
        Path file = contentFile(event);
        if (!Files.exists(file)) {
            return "";
        }
        try {
            return Markdown.frontMatter(Files.readString(file)).getOrDefault("description", "");
        } catch (IOException e) {
            return "";
        }
    }

    private Page page(EventDoc event) throws IOException {
        String from = event.path();
        TypeElement type = event.type();
        String name = type.getSimpleName().toString();
        DocComments.Doc doc = site.comments.find(type);
        StringBuilder b = new StringBuilder();
        List<TocEntry> toc = new ArrayList<>();

        String eyebrow = "<a href=\"" + Html.rel(from, "events/introduction/") + "\">Events</a><span class=\"api-crumb-sep\">/</span>"
                + Html.esc(event.group()) + "<span class=\"api-crumb-sep\">/</span><a href=\"" + Html.rel(from, event.module().path())
                + "\">" + event.module().name() + "</a>";
        b.append("<div class=\"api-header\"><div class=\"api-badges\">").append(Layout.badge("event", "kind"))
                .append("<a class=\"api-link\" href=\"").append(site.typeUrl(type, from)).append("\">API reference</a>");
        String source = site.sourceUrl(type);
        if (source != null) {
            b.append("<a class=\"api-source\" href=\"").append(source).append("\" rel=\"external\">Source</a>");
        }
        b.append("</div></div>");

        String body = site.comments.body(doc, from);
        String description = Html.stripTags(site.comments.summary(doc, from));
        Markdown.Result extra = null;
        if (Files.exists(contentFile(event))) {
            String markdown = Files.readString(contentFile(event));
            extra = site.guides.render(markdown, from, contentFile(event));
            if (description.isEmpty()) {
                description = Markdown.frontMatter(markdown).getOrDefault("description", "");
            }
        }
        if (!body.isEmpty()) {
            b.append("<div class=\"api-description\">").append(body.startsWith("<") ? body : "<p>" + body + "</p>").append("</div>");
        } else if (!description.isEmpty()) {
            b.append("<div class=\"api-description\"><p>").append(Html.esc(description)).append("</p></div>");
        }

        List<Field> fields = fields(type, doc, from);
        if (!fields.isEmpty()) {
            b.append("<h2 id=\"fields\">Fields</h2>");
            toc.add(new TocEntry(2, "fields", "Fields"));
            b.append("<div class=\"fields-details\"><div class=\"fields-grid\">");
            ApiPages api = new ApiPages(site);
            for (Field field : fields) {
                TypeElement enumType = documentedEnum(field.type());
                String badge = enumType == null ? api.typeBadge(field.type(), from)
                        : "<a class=\"type-link\" href=\"#type-" + enumType.getSimpleName() + "\">" + Html.esc(Site.nestedName(enumType)) + "</a>";
                b.append("<div class=\"field-card\"><div class=\"field-header\"><code>").append(Html.esc(field.name()))
                        .append("</code>").append(badge).append("</div><p>")
                        .append(field.description().isEmpty() ? "<span class=\"api-muted\">No description.</span>" : ApiPages.capitalize(field.description()))
                        .append("</p><span class=\"field-meta\">Accessor: <code>").append(Html.esc(field.accessor())).append("</code></span></div>");
            }
            b.append("</div></div>");
        }

        List<TypeElement> enums = fields.stream().map(f -> documentedEnum(f.type())).filter(java.util.Objects::nonNull).distinct().toList();
        if (!enums.isEmpty()) {
            b.append("<h2 id=\"types\">Types</h2>");
            toc.add(new TocEntry(2, "types", "Types"));
            ApiPages api = new ApiPages(site);
            for (TypeElement enumType : enums) {
                b.append(api.enumBlock(enumType, Site.nestedName(enumType), "type-" + enumType.getSimpleName(), from));
            }
        }

        if (extra != null) {
            b.append(extra.html());
            toc.addAll(extra.toc());
        }

        b.append("<h2 id=\"listening\">Listening</h2>");
        toc.add(new TocEntry(2, "listening", "Listening"));
        b.append("<p>Subscribe on the <code>EventBus</code> the event is posted to, see <a href=\"")
                .append(Html.rel(from, "events/introduction/")).append("\">Events</a> for which bus that is.</p>");
        b.append(Layout.tabs(List.of(
                new String[]{"Java", snippet(javaSnippet(name, fields), "java")},
                new String[]{"Kotlin", snippet(kotlinSnippet(name, fields), "kotlin")})));

        if (!event.posts().isEmpty()) {
            b.append("<h2 id=\"posted-by\">Posted by</h2>");
            toc.add(new TocEntry(2, "posted-by", "Posted by"));
            b.append("<ul class=\"api-used-by\">");
            for (Post post : event.posts()) {
                TypeElement ownerType = site.findType(post.owner());
                String label = post.owner() + ".java:" + post.line();
                b.append("<li>");
                if (ownerType != null && site.typePaths.containsKey(ownerType)) {
                    b.append("<a href=\"").append(site.typeUrl(ownerType, from)).append("\"><code>").append(post.owner()).append("</code></a> ");
                } else {
                    b.append("<code>").append(post.owner()).append("</code> ");
                }
                if (!site.config.repo().isEmpty()) {
                    b.append("<a class=\"api-source\" href=\"").append(site.sourceUrl(post.file(), post.line()))
                            .append("\" rel=\"external\">").append(Html.esc(label)).append("</a>");
                }
                b.append("</li>");
            }
            b.append("</ul>");
        }

        b.append("<p class=\"member-note\">API reference: <a href=\"").append(site.typeUrl(type, from)).append("\"><code>")
                .append(site.elements.getPackageOf(type).getQualifiedName()).append('.').append(name).append("</code></a></p>");

        site.search.add(new SearchEntry(name, "event", from, event.group() + " event"));
        return new Page(from, name, description, b.toString(), toc, SECTION, false, null, eyebrow);
    }

    private List<Field> fields(TypeElement type, DocComments.Doc doc, String from) {
        List<Field> fields = new ArrayList<>();
        if (type.getKind() == ElementKind.RECORD) {
            for (RecordComponentElement component : type.getRecordComponents()) {
                String name = component.getSimpleName().toString();
                fields.add(new Field(name, component.asType(), name + "()", site.comments.param(doc, name, from)));
            }
            return fields;
        }
        for (ExecutableElement method : ElementFilter.methodsIn(type.getEnclosedElements())) {
            if (site.env.isIncluded(method) && method.getParameters().isEmpty() && !method.getModifiers().contains(Modifier.STATIC)
                    && method.getReturnType().getKind() != TypeKind.VOID) {
                String name = method.getSimpleName().toString();
                fields.add(new Field(propertyName(name), method.getReturnType(), name + "()",
                        site.comments.summary(site.comments.find(method), from)));
            }
        }
        for (Lombok.Generated generated : Lombok.members(type)) {
            if (generated.kind() == Lombok.Kind.GETTER && !generated.field().getModifiers().contains(Modifier.STATIC)) {
                fields.add(new Field(generated.field().getSimpleName().toString(), generated.field().asType(),
                        generated.name() + "()", site.comments.summary(site.comments.find(generated.field()), from)));
            }
        }
        return fields;
    }

    private TypeElement documentedEnum(TypeMirror type) {
        TypeElement element = site.asTypeElement(type);
        return element != null && element.getKind() == ElementKind.ENUM && site.typePaths.containsKey(element) ? element : null;
    }

    private static String propertyName(String getter) {
        for (String prefix : List.of("get", "is")) {
            if (getter.length() > prefix.length() && getter.startsWith(prefix) && Character.isUpperCase(getter.charAt(prefix.length()))) {
                String rest = getter.substring(prefix.length());
                return Character.toLowerCase(rest.charAt(0)) + rest.substring(1);
            }
        }
        return getter;
    }

    private String javaSnippet(String event, List<Field> fields) {
        StringBuilder sb = new StringBuilder("eventBus.subscribe(").append(event).append(".class, event -> {\n");
        if (fields.isEmpty()) {
            sb.append("    // react to the event\n");
        }
        for (Field field : fields) {
            sb.append("    ").append(site.signatures.plain(field.type())).append(' ').append(field.name())
                    .append(" = event.").append(field.accessor()).append(";\n");
        }
        return sb.append("});").toString();
    }

    private String kotlinSnippet(String event, List<Field> fields) {
        StringBuilder sb = new StringBuilder("eventBus.subscribe(").append(event).append("::class.java) { event ->\n");
        if (fields.isEmpty()) {
            sb.append("    // react to the event\n");
        }
        for (Field field : fields) {
            boolean getter = !field.accessor().equals(field.name() + "()");
            sb.append("    val ").append(field.name()).append(" = event.")
                    .append(getter ? field.name() : field.accessor()).append('\n');
        }
        return sb.append('}').toString();
    }

    private static String snippet(String code, String lang) {
        return Layout.codeFrame(Highlighter.highlight(code, lang), lang, null);
    }
}
