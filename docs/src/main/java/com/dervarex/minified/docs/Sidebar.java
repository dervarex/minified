package com.dervarex.minified.docs;

import com.dervarex.minified.docs.Site.ModuleDoc;
import com.dervarex.minified.docs.Site.PackageDoc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Left navigation, read from {@code content/sidebar.txt} with the generated Events and API sections inserted
 * where {@code @events} and {@code @api} are written
 */
final class Sidebar {

    sealed interface Item permits Link, Group, PackageLink {
    }

    record Link(String label, String path) implements Item {
    }

    record Group(String label, List<Item> items) implements Item {
    }

    /**
     * A package, expanded into its types while one of its pages is shown
     */
    record PackageLink(String label, PackageDoc pkg) implements Item {
    }

    private final List<Item> items;

    private Sidebar(List<Item> items) {
        this.items = items;
    }

    List<Item> items() {
        return items;
    }

    static Sidebar load(Site site) throws IOException {
        Path file = site.config.content().resolve("sidebar.txt");
        List<Item> root = new ArrayList<>();
        Deque<Map.Entry<Integer, List<Item>>> stack = new ArrayDeque<>();
        stack.push(Map.entry(-1, root));
        for (String raw : Files.readAllLines(file)) {
            if (raw.isBlank() || raw.trim().startsWith("#")) {
                continue;
            }
            int indent = raw.length() - raw.stripLeading().length();
            String line = raw.trim();
            while (stack.peek().getKey() >= indent) {
                stack.pop();
            }
            List<Item> parent = stack.peek().getValue();
            if (line.equals("@events")) {
                parent.add(events(site));
            } else if (line.equals("@api")) {
                parent.add(api(site));
            } else if (line.contains("->")) {
                String[] parts = line.split("->", 2);
                String path = normalize(parts[1].trim());
                if (!site.pageTitles.containsKey(path)) {
                    site.warnings.add("docs/content/sidebar.txt", "no page at " + parts[1].trim());
                }
                parent.add(new Link(parts[0].trim(), path));
            } else {
                Group group = new Group(line, new ArrayList<>());
                parent.add(group);
                stack.push(Map.entry(indent, group.items()));
            }
        }
        return new Sidebar(root);
    }

    private static String normalize(String path) {
        String p = path.replaceAll("^/+", "");
        return p.isEmpty() || p.endsWith("/") ? p : p + "/";
    }

    private static Group events(Site site) {
        List<Item> items = new ArrayList<>();
        if (site.pageTitles.containsKey("events/introduction/")) {
            items.add(new Link("Introduction", "events/introduction/"));
        }
        Map<String, List<Item>> groups = new LinkedHashMap<>();
        for (EventPages.EventDoc event : site.events.events()) {
            groups.computeIfAbsent(event.group(), k -> new ArrayList<>())
                    .add(new Link(event.type().getSimpleName().toString(), event.path()));
        }
        groups.forEach((label, links) -> items.add(new Group(label, links)));
        return new Group("Events", items);
    }

    private static Group api(Site site) {
        List<Item> items = new ArrayList<>();
        items.add(new Link("Overview", "api/"));
        for (ModuleDoc module : site.modules) {
            if (module.packages().isEmpty()) {
                continue;
            }
            List<Item> moduleItems = new ArrayList<>();
            moduleItems.add(new Link("Overview", module.path()));
            String prefix = commonPrefix(site);
            for (PackageDoc pkg : module.packages()) {
                String label = pkg.name().length() > prefix.length() ? pkg.name().substring(prefix.length()) : pkg.name();
                moduleItems.add(new PackageLink(label.isEmpty() ? pkg.name() : label, pkg));
            }
            items.add(new Group(module.name(), moduleItems));
        }
        return new Group("API Reference", items);
    }

    /**
     * Package prefix shared by all documented packages, e.g. {@code com.dervarex.minified.}
     */
    private static String commonPrefix(Site site) {
        String prefix = null;
        for (String name : site.packages.keySet()) {
            if (prefix == null) {
                prefix = name;
            }
            while (!name.startsWith(prefix)) {
                prefix = prefix.substring(0, prefix.length() - 1);
            }
        }
        if (prefix == null) {
            return "";
        }
        int dot = prefix.lastIndexOf('.');
        return dot < 0 ? "" : prefix.substring(0, dot + 1);
    }
}
