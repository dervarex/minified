package com.dervarex.minified.docs;

import com.dervarex.minified.docs.Site.ModuleDoc;
import com.dervarex.minified.docs.Site.PackageDoc;
import com.dervarex.minified.docs.Site.Page;
import com.dervarex.minified.docs.Site.SearchEntry;
import com.dervarex.minified.docs.Site.TocEntry;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * API reference: the reference index, one page per module, package and type
 */
final class ApiPages {

    static final String SECTION = "api";

    /**
     * A documented member, either a real element or one Lombok generates
     */
    private record Member(String name, String anchor, String signature, String summary, String body,
                          List<String[]> params, String[] returns, List<String[]> throwsList,
                          List<DocComments.Tag> tags, List<String> badges, String sourceUrl, String note,
                          String plainSignature, String kind, String typeText) {
    }

    private final Site site;
    private final DocComments comments;
    private final Signatures sig;

    ApiPages(Site site) {
        this.site = site;
        this.comments = site.comments;
        this.sig = site.signatures;
    }

    void build() throws IOException {
        site.addPage(indexPage());
        for (ModuleDoc module : site.modules) {
            if (module.packages().isEmpty()) {
                continue;
            }
            site.addPage(modulePage(module));
            for (PackageDoc pkg : module.packages()) {
                site.addPage(packagePage(pkg));
                for (TypeElement type : pkg.types()) {
                    site.addPage(typePage(type, pkg));
                }
            }
        }
    }

    // ------------------------------------------------------------------ index & module & package

    private Page indexPage() {
        String from = "api/";
        StringBuilder b = new StringBuilder();
        b.append("<p>Every public type of every Minified module, generated from the source of version <code>")
                .append(Html.esc(site.config.version())).append("</code>. ")
                .append("Lombok accessors are included, and each member links to its source.</p>");
        b.append("<div class=\"fields-grid\">");
        for (ModuleDoc module : site.modules) {
            if (module.packages().isEmpty()) {
                continue;
            }
            int typeCount = module.packages().stream().mapToInt(p -> p.types().size()).sum();
            b.append(card(Html.rel(from, module.path()), module.name(), null,
                    Html.esc(moduleDescription(module)),
                    module.packages().size() + " packages · " + typeCount + " types"));
        }
        b.append("</div>");
        site.search.add(new SearchEntry("API Reference", "page", from, "All modules"));
        return new Page(from, "API Reference", "API reference of all Minified modules", b.toString(),
                List.of(), SECTION, false, null);
    }

    private String moduleDescription(ModuleDoc module) {
        Path intro = site.config.content().resolve("modules").resolve(module.name() + ".md");
        if (Files.exists(intro)) {
            try {
                String description = Markdown.frontMatter(Files.readString(intro)).get("description");
                if (description != null) {
                    return description;
                }
            } catch (IOException ignored) {
                // fall back to the gradle description
            }
        }
        return module.description().isBlank() ? "The " + module.name() + " module." : module.description();
    }

    private Page modulePage(ModuleDoc module) throws IOException {
        String from = module.path();
        StringBuilder b = new StringBuilder();
        List<TocEntry> toc = new ArrayList<>();
        Path intro = site.config.content().resolve("modules").resolve(module.name() + ".md");
        if (Files.exists(intro)) {
            Markdown.Result md = site.guides.render(Files.readString(intro), from, intro);
            b.append(md.html());
            toc.addAll(md.toc());
        } else {
            b.append("<p>").append(Html.esc(moduleDescription(module))).append("</p>");
        }

        b.append("<h2 id=\"installation\">Installation</h2>");
        toc.add(new TocEntry(2, "installation", "Installation"));
        String coordinate = "com.github.dervarex.minified:" + module.name() + ":" + site.config.version();
        b.append(Layout.tabs(List.of(
                new String[]{"Gradle (Groovy)", Layout.codeFrame(Highlighter.highlight(
                        "implementation '" + coordinate + "'", "groovy"), "groovy", "build.gradle")},
                new String[]{"Gradle (Kotlin)", Layout.codeFrame(Highlighter.highlight(
                        "implementation(\"" + coordinate + "\")", "kotlin"), "kotlin", "build.gradle.kts")}
        )));

        section(b, toc, "packages", "Packages");
        b.append("<div class=\"fields-grid\">");
        for (PackageDoc pkg : module.packages()) {
            String summary = comments.summary(comments.find(pkg.element()), from);
            b.append(card(Html.rel(from, pkg.path()), pkg.name(), pkg.exported() ? null : "internal",
                    summary, pkg.types().size() + " types"));
        }
        b.append("</div>");

        List<EventPages.EventDoc> moduleEvents = site.events.events().stream()
                .filter(e -> e.module() == module).toList();
        if (!moduleEvents.isEmpty()) {
            b.append("<h2 id=\"events\">Events</h2>");
            toc.add(new TocEntry(2, "events", "Events"));
            b.append("<ul>");
            for (EventPages.EventDoc event : moduleEvents) {
                b.append("<li><a href=\"").append(Html.rel(from, event.path())).append("\"><code>")
                        .append(event.type().getSimpleName()).append("</code></a></li>");
            }
            b.append("</ul>");
        }

        site.search.add(new SearchEntry(module.name(), "module", from, moduleDescription(module)));
        return new Page(from, module.name(), moduleDescription(module), b.toString(), toc, SECTION, false, null,
                "<a href=\"" + Html.rel(from, "api/") + "\">API Reference</a>");
    }

    private Page packagePage(PackageDoc pkg) {
        String from = pkg.path();
        DocComments.Doc doc = comments.find(pkg.element());
        StringBuilder b = new StringBuilder();
        b.append("<div class=\"api-header\"><div class=\"api-badges\">")
                .append(Layout.badge("package", "kind"));
        String status = sig.apiStatus(pkg.element());
        if (status != null) {
            b.append(statusBadge(status));
        }
        if (!pkg.exported()) {
            b.append(Layout.badge("internal", "internal", "Not exported by the module, may change without notice"));
        }
        b.append("</div></div>");
        String body = comments.body(doc, from);
        if (!body.isEmpty()) {
            b.append("<div class=\"api-description\">").append(body).append("</div>");
        }

        List<TocEntry> toc = new ArrayList<>();
        Map<String, List<TypeElement>> byKind = new LinkedHashMap<>();
        for (String kind : List.of("interface", "class", "abstract class", "record", "enum", "exception", "annotation")) {
            byKind.put(kind, new ArrayList<>());
        }
        for (TypeElement type : pkg.types()) {
            byKind.get(Signatures.kindLabel(type, site)).add(type);
        }
        for (Map.Entry<String, List<TypeElement>> entry : byKind.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            String heading = plural(entry.getKey());
            section(b, toc, Html.slug(heading), heading);
            b.append("<div class=\"fields-grid\">");
            for (TypeElement type : entry.getValue()) {
                String typeStatus = sig.apiStatus(type);
                b.append(card(Html.rel(from, site.typePaths.get(type)), Site.nestedName(type),
                        site.elements.isDeprecated(type) ? "deprecated" : null,
                        comments.summary(comments.find(type), from),
                        typeStatus == null ? null : "API status: " + typeStatus.toLowerCase()));
            }
            b.append("</div>");
        }
        site.search.add(new SearchEntry(pkg.name(), "package", from, pkg.module().name()));
        String eyebrow = "<a href=\"" + Html.rel(from, pkg.module().path()) + "\">" + pkg.module().name() + "</a>";
        return new Page(from, pkg.name(), "Package " + pkg.name(), b.toString(), toc, SECTION, false, null,
                eyebrow);
    }

    private static String plural(String kind) {
        return switch (kind) {
            case "class" -> "Classes";
            case "abstract class" -> "Abstract classes";
            case "interface" -> "Interfaces";
            case "record" -> "Records";
            case "enum" -> "Enums";
            case "exception" -> "Exceptions";
            default -> "Annotations";
        };
    }

    static String card(String href, String name, String badge, String summary, String meta) {
        StringBuilder b = new StringBuilder("<a class=\"field-card-link\" href=\"").append(href).append("\">")
                .append("<div class=\"field-card\"><div class=\"field-header\"><code>").append(Html.esc(name)).append("</code>");
        if (badge != null) {
            b.append("<span class=\"type-badge\">").append(Html.esc(badge)).append("</span>");
        }
        b.append("</div>");
        b.append("<p>").append(summary == null || summary.isBlank() ? "<span class=\"api-muted\">No description.</span>" : summary).append("</p>");
        if (meta != null) {
            b.append("<span class=\"field-meta\">").append(Html.esc(meta)).append("</span>");
        }
        return b.append("</div></a>").toString();
    }

    // ------------------------------------------------------------------ type pages

    private Page typePage(TypeElement type, PackageDoc pkg) {
        String from = site.typePaths.get(type);
        DocComments.Doc doc = comments.find(type);
        String name = Site.nestedName(type);
        StringBuilder b = new StringBuilder();
        List<TocEntry> toc = new ArrayList<>();

        StringBuilder eyebrow = new StringBuilder()
                .append("<a href=\"").append(Html.rel(from, pkg.module().path())).append("\">").append(pkg.module().name()).append("</a>")
                .append("<span class=\"api-crumb-sep\">/</span>")
                .append("<a href=\"").append(Html.rel(from, pkg.path())).append("\">").append(pkg.name()).append("</a>");
        Element enclosing = type.getEnclosingElement();
        List<TypeElement> outers = new ArrayList<>();
        while (enclosing instanceof TypeElement outer) {
            outers.add(0, outer);
            enclosing = outer.getEnclosingElement();
        }
        for (TypeElement outer : outers) {
            eyebrow.append("<span class=\"api-crumb-sep\">/</span><a href=\"").append(site.typeUrl(outer, from)).append("\">")
                    .append(outer.getSimpleName()).append("</a>");
        }
        b.append("<div class=\"api-header\"><div class=\"api-badges\">").append(Layout.badge(Signatures.kindLabel(type, site), "kind"));
        String status = sig.apiStatus(type);
        if (status != null) {
            b.append(statusBadge(status));
        }
        if (site.elements.isDeprecated(type)) {
            b.append(Layout.badge("deprecated", "deprecated"));
        }
        if (!pkg.exported()) {
            b.append(Layout.badge("internal", "internal", "The package is not exported by the module, it may change without notice"));
        }
        if (Lombok.isLombokAnnotated(type)) {
            b.append(Layout.badge("lombok", "lombok", "Some members are generated by Lombok"));
        }
        String source = site.sourceUrl(type);
        if (source != null) {
            b.append("<a class=\"api-source\" href=\"").append(source).append("\" rel=\"external\">Source</a>");
        }
        b.append("</div></div>");

        String declaration = sig.typeDeclaration(type, from);
        b.append("<div class=\"api-declaration\">").append(Layout.codeFrame(declaration, "java", null)).append("</div>");

        String deprecation = comments.deprecation(doc, from);
        if (!deprecation.isEmpty()) {
            b.append(Layout.aside("caution", "Deprecated", deprecation));
        }
        String body = comments.body(doc, from);
        if (!body.isEmpty()) {
            b.append("<div class=\"api-description\">").append(body.startsWith("<") ? body : "<p>" + body + "</p>").append("</div>");
        }

        String relations = relations(type, doc, from);
        if (!relations.isEmpty()) {
            b.append(relations);
        }
        for (DocComments.Tag tag : comments.otherTags(doc, from)) {
            if (!tag.name().equals("Deprecated")) {
                b.append("<p class=\"api-tag\"><strong>").append(Html.esc(tag.name())).append(":</strong> ").append(tag.html()).append("</p>");
            }
        }

        // record components
        if (type.getKind() == ElementKind.RECORD && !type.getRecordComponents().isEmpty()) {
            section(b, toc, "components", "Record components");
            b.append(componentCards(type, doc, from));
        }

        // enum constants
        List<VariableElement> constants = ElementFilter.fieldsIn(type.getEnclosedElements()).stream()
                .filter(f -> f.getKind() == ElementKind.ENUM_CONSTANT).toList();
        if (!constants.isEmpty()) {
            section(b, toc, "constants", "Enum constants");
            b.append(enumBlock(type, null, null, from));
            for (VariableElement constant : constants) {
                site.search.add(new SearchEntry(name + "." + constant.getSimpleName(), "constant",
                        from + "#" + constant.getSimpleName(), pkg.name()));
            }
        }

        List<Member> fields = new ArrayList<>();
        List<Member> constructors = new ArrayList<>();
        List<Member> methods = new ArrayList<>();
        Set<String> componentNames = type.getRecordComponents().stream()
                .map(c -> c.getSimpleName().toString()).collect(Collectors.toSet());
        for (Element e : type.getEnclosedElements()) {
            if (!site.env.isIncluded(e)) {
                continue;
            }
            switch (e.getKind()) {
                case FIELD -> fields.add(field((VariableElement) e, from));
                case CONSTRUCTOR -> {
                    if (type.getKind() != ElementKind.ENUM) {
                        constructors.add(executable((ExecutableElement) e, from));
                    }
                }
                case METHOD -> {
                    ExecutableElement m = (ExecutableElement) e;
                    if (site.elements.getOrigin(m) == Elements.Origin.MANDATED) {
                        continue;
                    }
                    boolean implicitAccessor = componentNames.contains(m.getSimpleName().toString())
                            && m.getParameters().isEmpty() && site.trees.getDocCommentTree(m) == null;
                    if (!implicitAccessor && !isImplicitMethod(type, m)) {
                        methods.add(executable(m, from));
                    }
                }
                default -> {
                }
            }
        }
        for (Lombok.Generated generated : Lombok.members(type)) {
            Member member = lombok(type, generated, from);
            if (generated.kind() == Lombok.Kind.NO_ARGS_CONSTRUCTOR) {
                constructors.add(member);
            } else {
                methods.add(member);
            }
        }
        methods.sort(Comparator.comparing(Member::name));

        if (!fields.isEmpty()) {
            section(b, toc, "fields", "Fields");
            memberIndex(b, fields);
            fields.forEach(m -> member(b, toc, m));
        }
        if (!constructors.isEmpty()) {
            section(b, toc, "constructors", "Constructors");
            constructors.forEach(m -> member(b, toc, m));
        }
        if (!methods.isEmpty()) {
            section(b, toc, "methods", "Methods");
            memberIndex(b, methods);
            methods.forEach(m -> member(b, toc, m));
        }
        for (Member m : fields) {
            site.search.add(new SearchEntry(name + "." + m.name(), "field", from + "#" + m.anchor(), m.plainSignature()));
        }
        for (Member m : methods) {
            site.search.add(new SearchEntry(name + "." + m.name() + "()", "method", from + "#" + m.anchor(), m.plainSignature()));
        }

        inherited(type, b, toc, from);
        nested(type, b, toc, from);
        usedBy(type, b, toc, from);

        site.search.add(new SearchEntry(name, Signatures.kindLabel(type, site), from, pkg.name()));
        String description = Html.stripTags(comments.summary(doc, from));
        return new Page(from, name, description.isEmpty() ? Signatures.kindLabel(type, site) + " " + name : description,
                b.toString(), toc, SECTION, false, null, eyebrow.toString());
    }

    /**
     * Constants of an enum as rows, constants that read like a final success or failure state are coloured
     *
     * @param title header of the block, null for none
     * @param id    anchor of the block, null when the rows themselves carry the constant anchors
     */
    String enumBlock(TypeElement type, String title, String id, String from) {
        StringBuilder b = new StringBuilder("<div class=\"type-definition\"");
        if (id != null) {
            b.append(" id=\"").append(id).append('"');
        }
        b.append('>');
        if (title != null) {
            b.append("<div class=\"type-definition-header\"><h3>").append(Html.esc(title)).append("</h3>");
            if (id != null) {
                b.append("<a class=\"type-link\" href=\"").append(site.typeUrl(type, from)).append("\">API</a>");
            }
            b.append("</div>");
        }
        b.append("<div class=\"enum-rows\">");
        for (VariableElement constant : ElementFilter.fieldsIn(type.getEnclosedElements())) {
            if (constant.getKind() != ElementKind.ENUM_CONSTANT) {
                continue;
            }
            String constantName = constant.getSimpleName().toString();
            String status = constantName.matches("(?i)FINISHED|SUCCESS|SUCCEEDED|DONE|COMPLETED?") ? " status-success"
                    : constantName.matches("(?i)FAILED|FAILURE|ERROR|ABORTED") ? " status-error" : "";
            b.append("<div class=\"enum-row").append(status).append('"').append(id == null ? " id=\"" + constantName + "\"" : "")
                    .append("><code>").append(constantName).append("</code><span>")
                    .append(comments.summary(comments.find(constant), from)).append("</span></div>");
        }
        return b.append("</div></div>").toString();
    }

    /**
     * Members the compiler generates: equals, hashCode and toString of records, values and valueOf of enums
     */
    private boolean isImplicitMethod(TypeElement type, ExecutableElement m) {
        if (site.trees.getDocCommentTree(m) != null || site.trees.getTree(m) != null) {
            return false;
        }
        String name = m.getSimpleName().toString();
        int params = m.getParameters().size();
        if (type.getKind() == ElementKind.RECORD) {
            return (name.equals("equals") && params == 1) || ((name.equals("hashCode") || name.equals("toString")) && params == 0);
        }
        if (type.getKind() == ElementKind.ENUM) {
            return (name.equals("values") && params == 0) || (name.equals("valueOf") && params == 1);
        }
        return false;
    }

    private String statusBadge(String status) {
        String cls = switch (status) {
            case "STABLE" -> "stable";
            case "EXPERIMENTAL" -> "experimental";
            case "MAINTAINED" -> "maintained";
            case "DEPRECATED" -> "deprecated";
            default -> "internal";
        };
        return Layout.badge(status.toLowerCase(), cls, "@API(status = " + status + ")");
    }

    private static void section(StringBuilder b, List<TocEntry> toc, String id, String title) {
        b.append("<h2 id=\"").append(id).append("\">").append(title).append("</h2>");
        toc.add(new TocEntry(2, id, title));
    }

    private String relations(TypeElement type, DocComments.Doc doc, String from) {
        StringBuilder b = new StringBuilder();
        List<? extends TypeParameterElement> typeParams = type.getTypeParameters();
        if (!typeParams.isEmpty()) {
            b.append("<dt>Type parameters</dt><dd><ul class=\"param-list\">");
            for (TypeParameterElement p : typeParams) {
                String description = comments.typeParam(doc, p.getSimpleName().toString(), from);
                b.append("<li><code>").append(p.getSimpleName()).append("</code>")
                        .append(description.isEmpty() ? "" : " – " + description).append("</li>");
            }
            b.append("</ul></dd>");
        }
        List<String> chain = new ArrayList<>();
        TypeMirror superclass = type.getSuperclass();
        while (superclass.getKind() == TypeKind.DECLARED && !superclass.toString().equals("java.lang.Object")) {
            chain.add(sig.type(superclass, from));
            superclass = site.asTypeElement(superclass).getSuperclass();
        }
        if (!chain.isEmpty() && type.getKind() == ElementKind.CLASS) {
            b.append("<dt>Superclasses</dt><dd class=\"api-chain\">").append(String.join(" <span class=\"api-muted\">→</span> ", chain)).append("</dd>");
        }
        Set<String> interfaces = new LinkedHashSet<>();
        allInterfaces(type.asType(), interfaces, from);
        if (!interfaces.isEmpty()) {
            b.append("<dt>").append(type.getKind().isInterface() ? "Superinterfaces" : "Implemented interfaces").append("</dt><dd>")
                    .append(String.join(", ", interfaces)).append("</dd>");
        }
        Set<TypeElement> subs = site.subtypes.getOrDefault(type, Set.of());
        if (!subs.isEmpty()) {
            b.append("<dt>").append(type.getKind().isInterface() ? "Known implementations" : "Known subclasses").append("</dt><dd>")
                    .append(subs.stream().sorted(Comparator.comparing(Site::nestedName))
                            .map(s -> "<a class=\"tref\" href=\"" + site.typeUrl(s, from) + "\">" + Site.nestedName(s) + "</a>")
                            .collect(Collectors.joining(", ")))
                    .append("</dd>");
        }
        if (type.getEnclosingElement() instanceof TypeElement outer) {
            b.append("<dt>Enclosing type</dt><dd><a class=\"tref\" href=\"").append(site.typeUrl(outer, from)).append("\">")
                    .append(Site.nestedName(outer)).append("</a></dd>");
        }
        EventPages.EventDoc event = site.events.eventFor(type);
        if (event != null) {
            b.append("<dt>Event</dt><dd><a href=\"").append(Html.rel(from, event.path()))
                    .append("\">When it fires and how to listen to it</a></dd>");
        }
        return b.isEmpty() ? "" : "<dl class=\"api-relations\">" + b + "</dl>";
    }

    private void allInterfaces(TypeMirror type, Set<String> out, String from) {
        for (TypeMirror sup : site.types.directSupertypes(type)) {
            TypeElement element = site.asTypeElement(sup);
            if (element == null) {
                continue;
            }
            if (element.getKind().isInterface()) {
                out.add(sig.type(sup, from));
            }
            allInterfaces(sup, out, from);
        }
    }

    String componentCards(TypeElement record, DocComments.Doc doc, String from) {
        StringBuilder b = new StringBuilder("<div class=\"fields-grid\">");
        for (RecordComponentElement component : record.getRecordComponents()) {
            String componentName = component.getSimpleName().toString();
            String description = comments.param(doc, componentName, from);
            b.append("<div class=\"field-card\" id=\"").append(componentName).append("\"><div class=\"field-header\"><code>")
                    .append(componentName).append("</code>").append(typeBadge(component.asType(), from)).append("</div>")
                    .append("<p>").append(description.isEmpty() ? "<span class=\"api-muted\">No description.</span>" : capitalize(description)).append("</p>")
                    .append("<span class=\"field-meta\">Accessor: <code>").append(componentName).append("()</code></span></div>");
            site.search.add(new SearchEntry(Site.nestedName(record) + "." + componentName + "()", "component",
                    site.typePaths.get(record) + "#" + componentName, sig.plain(component.asType())));
        }
        return b.append("</div>").toString();
    }

    /**
     * Upper cases the first letter of a description, {@code @param} texts are usually written in lower case
     */
    static String capitalize(String html) {
        for (int i = 0; i < html.length(); i++) {
            char c = html.charAt(i);
            if (c == '<') {
                int end = html.indexOf('>', i);
                if (end < 0 || html.startsWith("<code", i)) {
                    return html;
                }
                i = end;
            } else if (!Character.isWhitespace(c)) {
                return Character.isLowerCase(c) ? html.substring(0, i) + Character.toUpperCase(c) + html.substring(i + 1) : html;
            }
        }
        return html;
    }

    /**
     * Type of a field card, a link when the type (or the type argument of a collection) is documented
     */
    String typeBadge(TypeMirror type, String from) {
        TypeElement element = site.asTypeElement(type);
        if (element != null && site.typePaths.containsKey(element)) {
            return "<a class=\"type-link\" href=\"" + site.typeUrl(element, from) + "\">" + Html.esc(sig.plain(type)) + "</a>";
        }
        return "<span class=\"type-badge\">" + Html.esc(sig.plain(type)) + "</span>";
    }

    // ------------------------------------------------------------------ members

    private Member field(VariableElement field, String from) {
        DocComments.Doc doc = comments.find(field);
        String signature = sig.field(field, from);
        List<String> badges = new ArrayList<>();
        addCommonBadges(field, badges);
        return new Member(field.getSimpleName().toString(), field.getSimpleName().toString(), signature,
                comments.summary(doc, from), comments.body(doc, from), List.of(), null, List.of(),
                comments.otherTags(doc, from), badges, site.sourceUrl(field), null, Html.stripTags(signature),
                field.getModifiers().contains(Modifier.FINAL) && field.getModifiers().contains(Modifier.STATIC) ? "constant" : "field",
                sig.plain(field.asType()));
    }

    private Member executable(ExecutableElement m, String from) {
        DocComments.Doc doc = comments.find(m);
        String signature = sig.executable(m, from);
        List<String[]> params = new ArrayList<>();
        for (VariableElement p : m.getParameters()) {
            params.add(new String[]{p.getSimpleName().toString(), sig.type(p.asType(), from),
                    comments.param(doc, p.getSimpleName().toString(), from)});
        }
        for (TypeParameterElement p : m.getTypeParameters()) {
            String description = comments.typeParam(doc, p.getSimpleName().toString(), from);
            if (!description.isEmpty()) {
                params.add(new String[]{"<" + p.getSimpleName() + ">", "", description});
            }
        }
        String[] returns = null;
        if (m.getKind() == ElementKind.METHOD && m.getReturnType().getKind() != TypeKind.VOID) {
            returns = new String[]{sig.type(m.getReturnType(), from), comments.returns(doc, from)};
        }
        List<String[]> throwsList = new ArrayList<>(comments.throwsTags(doc, from));
        for (TypeMirror thrown : m.getThrownTypes()) {
            String simple = sig.plain(thrown);
            boolean documented = throwsList.stream().anyMatch(t -> Html.stripTags(t[0]).endsWith(simple));
            if (!documented) {
                throwsList.add(new String[]{sig.type(thrown, from), ""});
            }
        }
        List<String> badges = new ArrayList<>();
        addCommonBadges(m, badges);
        String note = null;
        if (m.getKind() == ElementKind.METHOD) {
            ExecutableElement overridden = comments.overridden(m);
            if (overridden != null) {
                TypeElement owner = (TypeElement) overridden.getEnclosingElement();
                String url = site.elementUrl(overridden, from);
                String label = Site.nestedName(owner) + "." + overridden.getSimpleName() + "()";
                String link = url == null ? "<code>" + Html.esc(label) + "</code>"
                        : "<a href=\"" + url + "\"><code>" + Html.esc(label) + "</code></a>";
                note = (owner.getKind().isInterface() && !m.getEnclosingElement().getKind().isInterface()
                        ? "Implements " : "Overrides ") + link
                        + (doc != null && doc.inheritedFrom() != null ? ", description copied from there" : "");
            }
        }
        String name = m.getKind() == ElementKind.CONSTRUCTOR ? m.getEnclosingElement().getSimpleName().toString()
                : m.getSimpleName().toString();
        return new Member(name, sig.anchor(m), signature, comments.summary(doc, from), comments.body(doc, from),
                params, returns, throwsList, comments.otherTags(doc, from), badges, site.sourceUrl(m), note,
                Html.stripTags(signature), m.getKind() == ElementKind.CONSTRUCTOR ? "constructor" : "method",
                m.getKind() == ElementKind.CONSTRUCTOR ? "" : sig.plain(m.getReturnType()));
    }

    private void addCommonBadges(Element e, List<String> badges) {
        Set<Modifier> modifiers = e.getModifiers();
        if (modifiers.contains(Modifier.STATIC)) {
            badges.add(Layout.badge("static", "kind"));
        }
        if (modifiers.contains(Modifier.ABSTRACT) && !e.getEnclosingElement().getKind().isInterface()) {
            badges.add(Layout.badge("abstract", "kind"));
        }
        if (modifiers.contains(Modifier.DEFAULT)) {
            badges.add(Layout.badge("default", "kind"));
        }
        if (modifiers.contains(Modifier.PROTECTED)) {
            badges.add(Layout.badge("protected", "kind"));
        }
        String status = sig.apiStatus(e);
        if (status != null && !status.equals("STABLE")) {
            badges.add(statusBadge(status));
        }
        if (site.elements.isDeprecated(e)) {
            badges.add(Layout.badge("deprecated", "deprecated"));
        }
    }

    private Member lombok(TypeElement type, Lombok.Generated generated, String from) {
        VariableElement field = generated.field();
        String access = "<span class=\"tok-k\">" + generated.access() + "</span> ";
        String badge = Layout.badge("lombok", "lombok", "Generated by Lombok");
        if (generated.kind() == Lombok.Kind.NO_ARGS_CONSTRUCTOR) {
            String signature = access + "<span class=\"tok-f\">" + generated.name() + "</span>()";
            return new Member(generated.name(), Signatures.anchor(generated.name(), List.of()), signature,
                    "Creates an instance with default values.", "<p>Creates an instance with default values.</p>",
                    List.of(), null, List.of(), List.of(), List.of(badge), site.sourceUrl(type),
                    "Generated by Lombok's <code>@NoArgsConstructor</code>", Html.stripTags(signature), "constructor", "");
        }
        DocComments.Doc doc = comments.find(field);
        String fieldName = field.getSimpleName().toString();
        String fieldDoc = comments.body(doc, from);
        String fieldSummary = comments.summary(doc, from);
        String typeHtml = sig.type(field.asType(), from);
        boolean isStatic = field.getModifiers().contains(Modifier.STATIC);
        String staticKeyword = isStatic ? "<span class=\"tok-k\">static</span> " : "";
        List<String> badges = new ArrayList<>(List.of(badge));
        if (isStatic) {
            badges.add(0, Layout.badge("static", "kind"));
        }
        if (generated.kind() == Lombok.Kind.GETTER) {
            String signature = access + staticKeyword + typeHtml + " <span class=\"tok-f\">" + generated.name() + "</span>()";
            String summary = fieldSummary.isEmpty() ? "Returns the <code>" + fieldName + "</code> value." : fieldSummary;
            return new Member(generated.name(), Signatures.anchor(generated.name(), List.of()), signature, summary,
                    fieldDoc.isEmpty() ? "<p>" + summary + "</p>" : fieldDoc, List.of(),
                    new String[]{typeHtml, ""}, List.of(), List.of(), badges, site.sourceUrl(field),
                    "Generated by Lombok's <code>@Getter</code> for the field <code>" + fieldName + "</code>",
                    Html.stripTags(signature), "method", sig.plain(field.asType()));
        }
        String signature = access + staticKeyword + "<span class=\"tok-k\">void</span> <span class=\"tok-f\">" + generated.name()
                + "</span>(" + typeHtml + " <span class=\"pname\">" + fieldName + "</span>)";
        String summary = fieldSummary.isEmpty() ? "Sets the <code>" + fieldName + "</code> value." : fieldSummary;
        List<String[]> params = new ArrayList<>();
        params.add(new String[]{fieldName, typeHtml, ""});
        return new Member(generated.name(), Signatures.anchor(generated.name(), List.of(sig.erasedName(field.asType()))),
                signature, summary, fieldDoc.isEmpty() ? "<p>" + summary + "</p>" : fieldDoc, params, null, List.of(),
                List.of(), badges, site.sourceUrl(field),
                "Generated by Lombok's <code>@Setter</code> for the field <code>" + fieldName + "</code>",
                Html.stripTags(signature), "method", "void");
    }

    private void memberIndex(StringBuilder b, List<Member> members) {
        if (members.size() < 2) {
            return;
        }
        b.append("<div class=\"member-index\">");
        for (Member m : members) {
            String summary = Html.stripTags(m.summary());
            b.append("<a class=\"member-index-row\" href=\"#").append(Html.esc(m.anchor())).append("\">")
                    .append("<code class=\"mi-sig\">");
            if (!m.typeText().isEmpty() && !m.kind().equals("constructor")) {
                b.append("<span class=\"mi-type\">").append(Html.esc(m.typeText())).append("</span> ");
            }
            b.append("<span class=\"mi-name\">").append(Html.esc(m.name())).append("</span>")
                    .append("<span class=\"mi-params\">").append(Html.esc(params(m))).append("</span></code>")
                    .append("<span class=\"mi-summary\">").append(Html.esc(summary)).append("</span></a>");
        }
        b.append("</div>");
    }

    private static String shortSignature(Member m) {
        return m.name() + params(m);
    }

    /**
     * Parameter list of a member as written in its anchor, e.g. {@code (Class, EventListener)}, empty for fields
     */
    private static String params(Member m) {
        String anchor = m.anchor();
        int open = anchor.indexOf('(');
        return open < 0 ? "" : anchor.substring(open).replace(",", ", ");
    }

    private void member(StringBuilder b, List<TocEntry> toc, Member m) {
        String anchor = Html.esc(m.anchor());
        toc.add(new TocEntry(3, m.anchor(), shortSignature(m)));
        b.append("<section class=\"member\" id=\"").append(anchor).append("\">")
                .append("<div class=\"member-head\">")
                .append("<h3 class=\"member-name\"><a href=\"#").append(anchor).append("\">")
                .append(Html.esc(m.name())).append("<span class=\"member-params\">").append(Html.esc(params(m)))
                .append("</span></a></h3>")
                .append("<div class=\"member-badges\">").append(String.join("", m.badges()));
        if (m.sourceUrl() != null) {
            b.append("<a class=\"api-source\" href=\"").append(m.sourceUrl()).append("\" rel=\"external\">Source</a>");
        }
        b.append("</div></div>");
        b.append("<pre class=\"member-sig\"><code>").append(m.signature()).append("</code></pre>");

        StringBuilder content = new StringBuilder();
        if (!m.body().isEmpty()) {
            content.append("<div class=\"member-body\">").append(m.body()).append("</div>");
        }
        StringBuilder rows = new StringBuilder();
        if (!m.params().isEmpty()) {
            rows.append("<tr class=\"mt-head\"><th colspan=\"3\">Parameters</th></tr>");
            for (String[] p : m.params()) {
                rows.append("<tr><td><code class=\"mt-name\">").append(Html.esc(p[0])).append("</code></td><td>");
                if (!p[1].isEmpty()) {
                    rows.append("<code class=\"mt-type\">").append(p[1]).append("</code>");
                }
                rows.append("</td><td class=\"mt-desc\">").append(capitalize(p[2])).append("</td></tr>");
            }
        }
        if (m.returns() != null) {
            rows.append("<tr class=\"mt-head\"><th colspan=\"3\">Returns</th></tr><tr><td colspan=\"2\"><code class=\"mt-type\">")
                    .append(m.returns()[0]).append("</code></td><td class=\"mt-desc\">").append(capitalize(m.returns()[1])).append("</td></tr>");
        }
        if (!m.throwsList().isEmpty()) {
            rows.append("<tr class=\"mt-head\"><th colspan=\"3\">Throws</th></tr>");
            for (String[] t : m.throwsList()) {
                String type = t[0].replace("<code>", "").replace("</code>", "");
                rows.append("<tr><td colspan=\"2\"><code class=\"mt-type mt-throws\">").append(type)
                        .append("</code></td><td class=\"mt-desc\">").append(capitalize(t[1])).append("</td></tr>");
            }
        }
        for (DocComments.Tag tag : m.tags()) {
            rows.append("<tr class=\"mt-head\"><th colspan=\"3\">").append(Html.esc(tag.name()))
                    .append("</th></tr><tr><td colspan=\"3\" class=\"mt-desc\">").append(tag.html()).append("</td></tr>");
        }
        if (!rows.isEmpty()) {
            content.append("<table class=\"member-table\"><tbody>").append(rows).append("</tbody></table>");
        }
        if (m.note() != null) {
            content.append("<p class=\"member-note\">").append(m.note()).append("</p>");
        }
        if (!content.isEmpty()) {
            b.append("<div class=\"member-content\">").append(content).append("</div>");
        }
        b.append("</section>");
    }

    private void inherited(TypeElement type, StringBuilder b, List<TocEntry> toc, String from) {
        Set<Element> all = new HashSet<>(site.elements.getAllMembers(type));
        Map<TypeElement, List<ExecutableElement>> byOwner = new LinkedHashMap<>();
        Deque<TypeMirror> queue = new ArrayDeque<>(site.types.directSupertypes(type.asType()));
        Set<TypeElement> seen = new HashSet<>();
        while (!queue.isEmpty()) {
            TypeElement sup = site.asTypeElement(queue.poll());
            if (sup == null || !seen.add(sup)) {
                continue;
            }
            queue.addAll(site.types.directSupertypes(sup.asType()));
            if (!site.typePaths.containsKey(sup)) {
                continue;
            }
            List<ExecutableElement> methods = ElementFilter.methodsIn(sup.getEnclosedElements()).stream()
                    .filter(all::contains)
                    .filter(site.env::isIncluded)
                    .filter(m -> !(sup.getKind().isInterface() && m.getModifiers().contains(Modifier.STATIC)))
                    .sorted(Comparator.comparing(m -> m.getSimpleName().toString()))
                    .toList();
            if (!methods.isEmpty()) {
                byOwner.put(sup, methods);
            }
        }
        if (byOwner.isEmpty()) {
            return;
        }
        section(b, toc, "inherited", "Inherited methods");
        for (Map.Entry<TypeElement, List<ExecutableElement>> entry : byOwner.entrySet()) {
            b.append("<details class=\"fields-details api-inherited\"><summary class=\"fields-summary\"><span><strong>From ")
                    .append(Site.nestedName(entry.getKey())).append("</strong><small>").append(entry.getValue().size())
                    .append(entry.getValue().size() == 1 ? " method" : " methods")
                    .append("</small></span><span class=\"fields-chevron\" aria-hidden=\"true\">›</span></summary><p class=\"api-inherited-list\">")
                    .append(entry.getValue().stream()
                            .map(m -> "<a href=\"" + site.elementUrl(m, from) + "\"><code>" + m.getSimpleName() + "()</code></a>")
                            .collect(Collectors.joining(" ")))
                    .append("</p></details>");
        }
    }

    private void nested(TypeElement type, StringBuilder b, List<TocEntry> toc, String from) {
        List<TypeElement> nestedTypes = ElementFilter.typesIn(type.getEnclosedElements()).stream()
                .filter(site.typePaths::containsKey).toList();
        if (nestedTypes.isEmpty()) {
            return;
        }
        section(b, toc, "nested", "Nested types");
        b.append("<div class=\"fields-grid\">");
        for (TypeElement nestedType : nestedTypes) {
            b.append(card(site.typeUrl(nestedType, from), Site.nestedName(nestedType), Signatures.kindLabel(nestedType, site),
                    comments.summary(comments.find(nestedType), from), null));
        }
        b.append("</div>");
    }

    private void usedBy(TypeElement type, StringBuilder b, List<TocEntry> toc, String from) {
        Set<ExecutableElement> uses = site.usages.getOrDefault(type, Set.of());
        if (uses.isEmpty()) {
            return;
        }
        section(b, toc, "used-by", "Used by");
        b.append("<p class=\"api-muted\">Methods of other types that accept or return <code>")
                .append(Site.nestedName(type)).append("</code>.</p><ul class=\"api-used-by\">");
        uses.stream()
                .sorted(Comparator.comparing((ExecutableElement m) -> Site.nestedName((TypeElement) m.getEnclosingElement()))
                        .thenComparing(m -> m.getSimpleName().toString()))
                .limit(60)
                .forEach(m -> b.append("<li><a href=\"").append(site.elementUrl(m, from)).append("\"><code>")
                        .append(Site.nestedName((TypeElement) m.getEnclosingElement())).append('.')
                        .append(m.getSimpleName()).append("()</code></a></li>"));
        b.append("</ul>");
    }
}
