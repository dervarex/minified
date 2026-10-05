package com.dervarex.minified.docs;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;
import jdk.javadoc.doclet.DocletEnvironment;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * The whole documentation site: indexes the documented sources, builds every page and writes them out
 */
final class Site {

    static final String JDK_API = "https://docs.oracle.com/en/java/javase/21/docs/api/";
    private static final Pattern EXPORTS = Pattern.compile("^\\s*exports\\s+([\\w.]+)\\s*;", Pattern.MULTILINE);

    record ModuleDoc(String name, String label, String description, Path sourceDir, Set<String> exported,
                     List<PackageDoc> packages) {
        String path() {
            return "api/" + name + "/";
        }
    }

    record PackageDoc(String name, ModuleDoc module, PackageElement element, List<TypeElement> types) {
        String path() {
            return "api/" + name + "/";
        }

        boolean exported() {
            return module.exported().contains(name);
        }
    }

    record TocEntry(int level, String id, String text) {
    }

    /**
     * @param path       directory path of the page, {@code ""} for the home page
     * @param section    sidebar section the page belongs to, used to expand the right sidebar group
     * @param hideTitle  whether the title panel is left out, like on the home page
     * @param extraCss   additional stylesheet in the theme directory, may be null
     * @param eyebrow    html shown above the title, like the module and package of a type, may be null
     */
    record Page(String path, String title, String description, String body, List<TocEntry> toc,
                String section, boolean hideTitle, String extraCss, String eyebrow) {

        Page(String path, String title, String description, String body, List<TocEntry> toc,
             String section, boolean hideTitle, String extraCss) {
            this(path, title, description, body, toc, section, hideTitle, extraCss, null);
        }
    }

    record SearchEntry(String title, String kind, String path, String detail) {
    }

    final DocletEnvironment env;
    final Config config;
    final Elements elements;
    final Types types;
    final DocTrees trees;
    final Warnings warnings;
    final DocComments comments;
    final Signatures signatures;

    final List<ModuleDoc> modules = new ArrayList<>();
    final Map<String, PackageDoc> packages = new TreeMap<>();
    final Map<TypeElement, String> typePaths = new LinkedHashMap<>();
    final Map<TypeElement, PackageDoc> typePackages = new HashMap<>();
    final Map<String, List<TypeElement>> typesByName = new HashMap<>();
    final Map<TypeElement, Set<TypeElement>> subtypes = new HashMap<>();
    final Map<TypeElement, Set<ExecutableElement>> usages = new HashMap<>();
    final List<Page> pages = new ArrayList<>();
    final List<SearchEntry> search = new ArrayList<>();
    final Map<String, String> pageTitles = new HashMap<>();

    EventPages events;
    Guides guides;

    Site(DocletEnvironment env, Config config) {
        this.env = env;
        this.config = config;
        this.elements = env.getElementUtils();
        this.types = env.getTypeUtils();
        this.trees = env.getDocTrees();
        this.warnings = new Warnings(config.strict());
        this.comments = new DocComments(this);
        this.signatures = new Signatures(this);
    }

    Warnings warnings() {
        return warnings;
    }

    void generate() throws IOException {
        indexTypes();
        events = new EventPages(this);
        events.collect();
        guides = new Guides(this);
        guides.load();

        new ApiPages(this).build();
        events.build();
        guides.build();

        Sidebar sidebar = Sidebar.load(this);
        Layout layout = new Layout(this, sidebar);
        for (Page page : pages) {
            Path file = config.out().resolve(page.path()).resolve("index.html");
            Files.createDirectories(file.getParent());
            Files.writeString(file, layout.render(page), StandardCharsets.UTF_8);
        }
        copyTheme();
        writeSearchIndex();
        Files.writeString(config.out().resolve("VERSION"), config.version() + "\n");
        warnings.print();
        System.out.println("docs: wrote " + pages.size() + " pages to " + config.out());
    }

    void addPage(Page page) {
        pages.add(page);
        pageTitles.put(page.path(), page.title());
    }

    // ------------------------------------------------------------------ indexing

    private void indexTypes() throws IOException {
        Map<Path, ModuleDoc> bySourceDir = new LinkedHashMap<>();
        for (Config.ModuleSource source : config.modules()) {
            Path descriptor = source.sourceDir().resolve("module-info.java");
            Set<String> exported = new LinkedHashSet<>();
            if (Files.exists(descriptor)) {
                Matcher m = EXPORTS.matcher(Files.readString(descriptor));
                while (m.find()) {
                    exported.add(m.group(1));
                }
            }
            ModuleDoc module = new ModuleDoc(source.name(), moduleLabel(source.name()), source.description(),
                    source.sourceDir().toAbsolutePath().normalize(), exported, new ArrayList<>());
            modules.add(module);
            bySourceDir.put(module.sourceDir(), module);
        }

        for (TypeElement type : ElementFilter.typesIn(env.getIncludedElements())) {
            ModuleDoc module = moduleOf(type, bySourceDir);
            if (module == null) {
                continue;
            }
            PackageElement pkg = elements.getPackageOf(type);
            String pkgName = pkg.getQualifiedName().toString();
            PackageDoc doc = packages.computeIfAbsent(pkgName, n -> {
                PackageDoc created = new PackageDoc(n, module, pkg, new ArrayList<>());
                module.packages().add(created);
                return created;
            });
            doc.types().add(type);
            typePackages.put(type, doc);
            typePaths.put(type, doc.path() + nestedName(type) + "/");
            typesByName.computeIfAbsent(type.getSimpleName().toString(), k -> new ArrayList<>()).add(type);
            if (type.getNestingKind().isNested()) {
                typesByName.computeIfAbsent(nestedName(type), k -> new ArrayList<>()).add(type);
            }
        }
        for (PackageDoc pkg : packages.values()) {
            pkg.types().sort(Comparator.comparing(Site::nestedName));
        }
        for (ModuleDoc module : modules) {
            module.packages().sort(Comparator.comparing(PackageDoc::name));
        }

        for (TypeElement type : typePaths.keySet()) {
            for (TypeMirror sup : types.directSupertypes(type.asType())) {
                TypeElement supType = asTypeElement(sup);
                if (supType != null && typePaths.containsKey(supType)) {
                    subtypes.computeIfAbsent(supType, k -> new LinkedHashSet<>()).add(type);
                }
            }
            for (ExecutableElement exec : ElementFilter.methodsIn(type.getEnclosedElements())) {
                if (!env.isIncluded(exec)) {
                    continue;
                }
                List<TypeMirror> mentioned = new ArrayList<>();
                mentioned.add(exec.getReturnType());
                exec.getParameters().forEach(p -> mentioned.add(p.asType()));
                for (TypeMirror t : mentioned) {
                    collectDeclared(t, used -> {
                        if (!used.equals(type) && typePaths.containsKey(used)) {
                            usages.computeIfAbsent(used, k -> new LinkedHashSet<>()).add(exec);
                        }
                    });
                }
            }
        }
    }

    private void collectDeclared(TypeMirror t, java.util.function.Consumer<TypeElement> sink) {
        switch (t.getKind()) {
            case DECLARED -> {
                sink.accept((TypeElement) ((DeclaredType) t).asElement());
                ((DeclaredType) t).getTypeArguments().forEach(a -> collectDeclared(a, sink));
            }
            case ARRAY -> collectDeclared(((javax.lang.model.type.ArrayType) t).getComponentType(), sink);
            case WILDCARD -> {
                var w = (javax.lang.model.type.WildcardType) t;
                if (w.getExtendsBound() != null) {
                    collectDeclared(w.getExtendsBound(), sink);
                }
                if (w.getSuperBound() != null) {
                    collectDeclared(w.getSuperBound(), sink);
                }
            }
            default -> {
            }
        }
    }

    private ModuleDoc moduleOf(TypeElement type, Map<Path, ModuleDoc> bySourceDir) {
        Path source = sourceFile(type);
        if (source == null) {
            return null;
        }
        for (Map.Entry<Path, ModuleDoc> entry : bySourceDir.entrySet()) {
            if (source.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    static String moduleLabel(String name) {
        String shortName = name.startsWith("minified-") ? name.substring("minified-".length()) : name;
        return shortName.isEmpty() ? name : Character.toUpperCase(shortName.charAt(0)) + shortName.substring(1);
    }

    static String nestedName(TypeElement type) {
        StringBuilder sb = new StringBuilder(type.getSimpleName());
        Element enclosing = type.getEnclosingElement();
        while (enclosing instanceof TypeElement outer) {
            sb.insert(0, outer.getSimpleName() + ".");
            enclosing = outer.getEnclosingElement();
        }
        return sb.toString();
    }

    TypeElement asTypeElement(TypeMirror t) {
        return t.getKind() == TypeKind.DECLARED ? (TypeElement) ((DeclaredType) t).asElement() : null;
    }

    TypeElement enclosingType(Element e) {
        Element current = e;
        while (current != null && !(current instanceof TypeElement)) {
            current = current.getEnclosingElement();
        }
        return (TypeElement) current;
    }

    // ------------------------------------------------------------------ source positions

    Path sourceFile(Element e) {
        TreePath path = trees.getPath(e);
        if (path == null) {
            return null;
        }
        return Path.of(path.getCompilationUnit().getSourceFile().toUri()).toAbsolutePath().normalize();
    }

    long line(Element e) {
        TreePath path = trees.getPath(e);
        if (path == null) {
            return -1;
        }
        CompilationUnitTree unit = path.getCompilationUnit();
        long pos = trees.getSourcePositions().getStartPosition(unit, path.getLeaf());
        return pos < 0 ? -1 : unit.getLineMap().getLineNumber(pos);
    }

    String sourceUrl(Element e) {
        Path file = sourceFile(e);
        if (file == null || config.repo().isEmpty()) {
            return null;
        }
        return sourceUrl(file, line(e));
    }

    String sourceUrl(Path file, long line) {
        String relative = config.root().toAbsolutePath().normalize().relativize(file).toString().replace('\\', '/');
        return config.repo() + "/blob/" + config.ref() + "/" + relative + (line > 0 ? "#L" + line : "");
    }

    // ------------------------------------------------------------------ links

    /**
     * Url of a documented type relative to {@code from}, a JDK api link for JDK types, null otherwise
     */
    String typeUrl(TypeElement type, String from) {
        String path = typePaths.get(type);
        if (path != null) {
            return Html.rel(from, path);
        }
        ModuleElement module = elements.getModuleOf(type);
        if (module != null && !module.isUnnamed()) {
            String name = module.getQualifiedName().toString();
            if (name.startsWith("java.") || name.startsWith("jdk.")) {
                String pkg = elements.getPackageOf(type).getQualifiedName().toString();
                return JDK_API + name + "/" + pkg.replace('.', '/') + "/" + nestedName(type) + ".html";
            }
        }
        return null;
    }

    /**
     * Url of any documented element relative to {@code from}, or null when it has no page
     */
    String elementUrl(Element e, String from) {
        if (e instanceof TypeElement type) {
            return typeUrl(type, from);
        }
        if (e instanceof PackageElement pkg) {
            PackageDoc doc = packages.get(pkg.getQualifiedName().toString());
            return doc == null ? null : Html.rel(from, doc.path());
        }
        if (e instanceof ExecutableElement || e instanceof VariableElement) {
            TypeElement owner = enclosingType(e);
            if (owner == null) {
                return null;
            }
            String url = typeUrl(owner, from);
            if (url == null) {
                return null;
            }
            return typePaths.containsKey(owner) ? url + "#" + signatures.anchor(e) : url;
        }
        if (e.getKind() == ElementKind.RECORD_COMPONENT) {
            String url = typeUrl((TypeElement) e.getEnclosingElement(), from);
            return url == null ? null : url + "#" + e.getSimpleName();
        }
        return null;
    }

    /**
     * Finds a documented type by simple or nested name, e.g. {@code LaunchConfiguration.Builder}
     */
    TypeElement findType(String name) {
        List<TypeElement> found = typesByName.get(name);
        if (found == null) {
            TypeElement byFqn = elements.getTypeElement(name);
            return byFqn != null && typePaths.containsKey(byFqn) ? byFqn : null;
        }
        return found.size() == 1 ? found.get(0) : found.stream()
                .filter(t -> typePackages.get(t).exported())
                .findFirst().orElse(found.get(0));
    }

    // ------------------------------------------------------------------ output

    private void copyTheme() throws IOException {
        Path theme = config.content().getParent().resolve("theme");
        Path target = config.out().resolve("assets");
        Files.createDirectories(target);
        try (Stream<Path> files = Files.list(theme)) {
            for (Path file : files.toList()) {
                if (file.getFileName().toString().equals("favicon.svg")) {
                    Files.copy(file, config.out().resolve("favicon.svg"), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.copy(file, target.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private void writeSearchIndex() throws IOException {
        StringBuilder sb = new StringBuilder("window.MINIFIED_SEARCH=[\n");
        for (SearchEntry entry : search) {
            sb.append('[').append(json(entry.title())).append(',').append(json(entry.kind())).append(',')
                    .append(json(entry.path())).append(',').append(json(entry.detail())).append("],\n");
        }
        sb.append("];\n");
        Files.writeString(config.out().resolve("assets/search-index.js"), sb.toString(), StandardCharsets.UTF_8);
    }

    static String json(String s) {
        if (s == null) {
            return "\"\"";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '<' -> sb.append("\\u003c");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format(Locale.ROOT, "\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }

}
