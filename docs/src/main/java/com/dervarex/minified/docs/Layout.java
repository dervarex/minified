package com.dervarex.minified.docs;

import com.dervarex.minified.docs.Site.Page;
import com.dervarex.minified.docs.Site.TocEntry;

import java.util.List;

/**
 * Page shell modelled on the Starlight layout of the former site: header, left navigation, content and the
 * "On this page" table of contents
 */
final class Layout {

    private static final String GITHUB = "https://github.com/dervarex/minified";
    private static final String DISCORD = "https://discord.gg/aG6NgQBENr";

    private static final String ICON_SEARCH = "<svg aria-hidden=\"true\" width=\"16\" height=\"16\" viewBox=\"0 0 24 24\" fill=\"currentColor\"><path d=\"M21.71 20.29 18 16.61A9 9 0 1 0 16.61 18l3.68 3.68a1 1 0 0 0 1.42 0 1 1 0 0 0 0-1.39ZM11 18a7 7 0 1 1 7-7 7 7 0 0 1-7 7Z\"/></svg>";
    private static final String ICON_MENU = "<svg aria-hidden=\"true\" width=\"16\" height=\"16\" viewBox=\"0 0 24 24\" fill=\"currentColor\"><path d=\"M3 8h18a1 1 0 1 0 0-2H3a1 1 0 0 0 0 2Zm18 8H3a1 1 0 0 0 0 2h18a1 1 0 0 0 0-2Zm0-5H3a1 1 0 0 0 0 2h18a1 1 0 0 0 0-2Z\"/></svg>";
    private static final String ICON_CARET = "<svg aria-hidden=\"true\" class=\"caret\" width=\"16\" height=\"16\" viewBox=\"0 0 24 24\" fill=\"currentColor\"><path d=\"m14.83 11.29-4.24-4.24a1 1 0 1 0-1.42 1.41L12.71 12l-3.54 3.54a1 1 0 0 0 0 1.41 1 1 0 0 0 .71.29 1 1 0 0 0 .71-.29l4.24-4.24a1.002 1.002 0 0 0 0-1.42Z\"/></svg>";
    private static final String ICON_GITHUB = "<svg aria-hidden=\"true\" width=\"16\" height=\"16\" viewBox=\"0 0 24 24\" fill=\"currentColor\"><path d=\"M12 .3a12 12 0 0 0-3.8 23.38c.6.12.83-.26.83-.57L9 21.07c-3.34.72-4.04-1.61-4.04-1.61-.55-1.39-1.34-1.76-1.34-1.76-1.08-.74.09-.73.09-.73 1.2.09 1.83 1.24 1.83 1.24 1.08 1.83 2.81 1.3 3.5 1 .1-.78.42-1.31.76-1.61-2.67-.3-5.47-1.33-5.47-5.93 0-1.31.47-2.38 1.24-3.22-.14-.3-.54-1.52.1-3.18 0 0 1-.32 3.3 1.23a11.5 11.5 0 0 1 6 0c2.28-1.55 3.29-1.23 3.29-1.23.64 1.66.24 2.88.12 3.18a4.65 4.65 0 0 1 1.23 3.22c0 4.61-2.8 5.63-5.48 5.92.42.36.81 1.1.81 2.22l-.01 3.29c0 .31.2.69.82.57A12 12 0 0 0 12 .3Z\"/></svg>";
    private static final String ICON_DISCORD = "<svg aria-hidden=\"true\" width=\"16\" height=\"16\" viewBox=\"0 0 24 24\" fill=\"currentColor\"><path d=\"M20.32 4.37a19.8 19.8 0 0 0-4.89-1.52.07.07 0 0 0-.08.04c-.2.38-.44.87-.6 1.25a18.27 18.27 0 0 0-5.49 0 12.64 12.64 0 0 0-.62-1.25.08.08 0 0 0-.08-.04 19.74 19.74 0 0 0-4.88 1.52.07.07 0 0 0-.04.03C.53 9.05-.32 13.58.1 18.06c0 .02.01.04.03.06a19.9 19.9 0 0 0 5.99 3.03.08.08 0 0 0 .09-.03c.46-.63.87-1.3 1.22-1.99a.08.08 0 0 0-.04-.1 13.1 13.1 0 0 1-1.87-.9.08.08 0 0 1 0-.12l.36-.3a.07.07 0 0 1 .08 0 14.2 14.2 0 0 0 12.06 0 .07.07 0 0 1 .08 0l.37.3a.08.08 0 0 1 0 .12 12.3 12.3 0 0 1-1.88.9.08.08 0 0 0-.04.1c.36.7.77 1.36 1.22 1.99a.08.08 0 0 0 .09.03 19.84 19.84 0 0 0 6-3.03.08.08 0 0 0 .03-.05c.5-5.18-.84-9.67-3.55-13.66a.06.06 0 0 0-.03-.03ZM8.02 15.33c-1.18 0-2.16-1.08-2.16-2.42 0-1.33.96-2.42 2.16-2.42 1.21 0 2.18 1.1 2.16 2.42 0 1.34-.96 2.42-2.16 2.42Zm7.97 0c-1.18 0-2.15-1.08-2.15-2.42 0-1.33.95-2.42 2.15-2.42 1.21 0 2.18 1.1 2.16 2.42 0 1.34-.95 2.42-2.16 2.42Z\"/></svg>";
    private static final String ICON_THEME = "<svg aria-hidden=\"true\" class=\"select-icon\" width=\"16\" height=\"16\" viewBox=\"0 0 24 24\" fill=\"currentColor\"><path d=\"M21.64 13a1 1 0 0 0-1.05-.14 8.05 8.05 0 0 1-3.37.73 8.15 8.15 0 0 1-8.14-8.1 8.59 8.59 0 0 1 .25-2A1 1 0 0 0 8 2.36a10.14 10.14 0 1 0 14 11.69 1 1 0 0 0-.36-1.05Zm-9.5 6.69A8.14 8.14 0 0 1 7.08 5.22v.27a10.15 10.15 0 0 0 10.14 10.14 9.79 9.79 0 0 0 2.1-.22 8.11 8.11 0 0 1-7.18 4.32v-.04Z\"/></svg>";
    private static final String ICON_VERSION = "<svg aria-hidden=\"true\" class=\"select-icon\" width=\"16\" height=\"16\" viewBox=\"0 0 24 24\" fill=\"currentColor\"><path d=\"M21.41 11.58l-9-9A2 2 0 0 0 11 2H4a2 2 0 0 0-2 2v7a2 2 0 0 0 .59 1.42l9 9A2 2 0 0 0 13 22a2 2 0 0 0 1.41-.59l7-7A2 2 0 0 0 22 13a2 2 0 0 0-.59-1.42ZM13 20l-9-9V4h7l9 9-7 7ZM6.5 5A1.5 1.5 0 1 0 8 6.5 1.5 1.5 0 0 0 6.5 5Z\"/></svg>";
    private static final String ICON_CHEVRON = "<svg aria-hidden=\"true\" class=\"select-caret\" width=\"16\" height=\"16\" viewBox=\"0 0 24 24\" fill=\"currentColor\"><path d=\"M17 9.17a1 1 0 0 0-1.41 0L12 12.71 8.46 9.17a1 1 0 1 0-1.41 1.42l4.24 4.24a1.002 1.002 0 0 0 1.42 0L17 10.59a1.002 1.002 0 0 0 0-1.42Z\"/></svg>";

    private final Site site;
    private final Sidebar sidebar;

    Layout(Site site, Sidebar sidebar) {
        this.site = site;
        this.sidebar = sidebar;
    }

    String render(Page page) {
        String root = Html.rel(page.path(), "");
        boolean hasToc = page.toc().size() > 0;
        String title = page.hideTitle() ? "Minified" : page.title() + " | Minified";
        StringBuilder b = new StringBuilder(32_000);
        b.append("<!doctype html>\n<html lang=\"en\" dir=\"ltr\" data-theme=\"dark\"").append(hasToc ? " data-has-toc" : "").append(">\n<head>\n")
                .append("<meta charset=\"utf-8\">\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n")
                .append("<title>").append(Html.esc(title)).append("</title>\n");
        if (!page.description().isEmpty()) {
            b.append("<meta name=\"description\" content=\"").append(Html.esc(page.description())).append("\">\n");
        }
        b.append("<meta name=\"generator\" content=\"minified docs\">\n")
                .append("<link rel=\"icon\" href=\"").append(root).append("favicon.svg\" type=\"image/svg+xml\">\n")
                .append("<link rel=\"stylesheet\" href=\"").append(root).append("assets/style.css\">\n")
                .append("<link rel=\"stylesheet\" href=\"").append(root).append("assets/components.css\">\n");
        if (page.extraCss() != null) {
            b.append("<link rel=\"stylesheet\" href=\"").append(root).append("assets/").append(page.extraCss()).append("\">\n");
        }
        // applied before the first paint so the page never flashes in the wrong theme
        b.append("<script>(function(){try{var t=localStorage.getItem('starlight-theme');")
                .append("if(t!=='light'&&t!=='dark'){t=matchMedia('(prefers-color-scheme: light)').matches?'light':'dark';}")
                .append("document.documentElement.dataset.theme=t;}catch(e){}})();</script>\n")
                .append("<script src=\"").append(root).append("assets/search-index.js\" defer></script>\n")
                .append("<script src=\"").append(root).append("assets/site.js\" defer></script>\n")
                .append("</head>\n<body data-root=\"").append(root).append("\" data-path=\"").append(Html.esc(page.path()))
                .append("\" data-version=\"").append(Html.esc(site.config.version())).append("\">\n")
                .append("<a class=\"skip-link\" href=\"#_top\">Skip to content</a>\n");

        header(b, root);

        b.append("<nav class=\"sidebar\" aria-label=\"Main\"><div class=\"sidebar-content\">");
        b.append("<ul class=\"top-level\">");
        for (Sidebar.Item item : sidebar.items()) {
            item(b, item, page.path(), true);
        }
        b.append("</ul></div></nav>\n");

        b.append("<div class=\"main-frame\"><div class=\"main-columns\">");
        if (hasToc) {
            b.append("<aside class=\"right-sidebar\" aria-label=\"On this page\"><div class=\"right-sidebar-panel\"><h2 id=\"starlight__on-this-page\">On this page</h2><ul class=\"toc\">")
                    .append("<li><a href=\"#_top\" class=\"toc-level-2\">Overview</a></li>");
            for (TocEntry entry : page.toc()) {
                b.append("<li><a href=\"#").append(Html.esc(entry.id())).append("\" class=\"toc-level-").append(entry.level()).append("\">")
                        .append(Html.esc(entry.text())).append("</a></li>");
            }
            b.append("</ul></div></aside>");
        }
        b.append("<main data-pagefind-body lang=\"en\" dir=\"ltr\">");
        b.append("<div class=\"version-banner\" hidden></div>");
        if (!page.hideTitle()) {
            b.append("<div class=\"content-panel title-panel\"><div class=\"sl-container\">");
            if (page.eyebrow() != null) {
                b.append("<p class=\"title-eyebrow\">").append(page.eyebrow()).append("</p>");
            }
            b.append("<h1 id=\"_top\"").append(page.title().length() > 28 ? " class=\"long-title\"" : "").append('>')
                    .append(breakable(page.title())).append("</h1></div></div>");
        } else {
            b.append("<span id=\"_top\"></span>");
        }
        boolean reference = page.section().equals(ApiPages.SECTION) || page.section().equals(EventPages.SECTION) && page.eyebrow() != null;
        b.append("<div class=\"content-panel\"><div class=\"sl-container\"><div class=\"sl-markdown-content")
                .append(reference ? " reference-page" : "").append("\">")
                .append(page.body())
                .append("</div>")
                .append("<footer class=\"footer\"><p>Minified is not affiliated with, endorsed by, or sponsored by Mojang Studios or Microsoft. Minecraft is a trademark of Microsoft.</p></footer>")
                .append("</div></div></main></div></div>\n");

        b.append("<dialog class=\"search-dialog\" aria-label=\"Search\"><div class=\"search-box\">")
                .append("<div class=\"search-input-row\">").append(ICON_SEARCH)
                .append("<input type=\"search\" class=\"search-input\" placeholder=\"Search the docs\" autocomplete=\"off\" spellcheck=\"false\" aria-label=\"Search\">")
                .append("<button type=\"button\" class=\"search-cancel\" data-close-search>Cancel</button></div>")
                .append("<ul class=\"search-results\" role=\"listbox\"></ul>")
                .append("<p class=\"search-hint\"><kbd>↑</kbd><kbd>↓</kbd> to navigate · <kbd>Enter</kbd> to open · <kbd>Esc</kbd> to close</p>")
                .append("</div></dialog>\n");
        b.append("</body>\n</html>\n");
        return b.toString();
    }

    private void header(StringBuilder b, String root) {
        b.append("<header class=\"header\"><div class=\"header-inner\">")
                .append("<div class=\"title-wrapper\"><a href=\"").append(root).append("\" class=\"site-title\"><span>Minified</span></a></div>")
                .append("<div class=\"search-wrapper\"><button type=\"button\" class=\"search-button\" data-open-search aria-label=\"Search\">")
                .append(ICON_SEARCH).append("<span class=\"search-label\">Search</span><kbd class=\"search-kbd\"><kbd>Ctrl</kbd><kbd>K</kbd></kbd></button></div>")
                .append("<div class=\"right-group\">")
                .append("<label class=\"select-wrapper version-select\" title=\"Documentation version\">").append(ICON_VERSION)
                .append("<span class=\"sr-only\">Version</span><select data-version-select><option value=\"")
                .append(Html.esc(site.config.version())).append("\" selected>v").append(Html.esc(site.config.version()))
                .append("</option></select>").append(ICON_CHEVRON).append("</label>")
                .append("<div class=\"social-icons\">")
                .append("<a href=\"").append(GITHUB).append("\" rel=\"me\" class=\"social-link\">").append("<span class=\"sr-only\">GitHub</span>").append(ICON_GITHUB).append("</a>")
                .append("<a href=\"").append(DISCORD).append("\" rel=\"me\" class=\"social-link\">").append("<span class=\"sr-only\">Discord</span>").append(ICON_DISCORD).append("</a>")
                .append("</div>")
                .append("<label class=\"select-wrapper theme-select\" title=\"Select theme\">").append(ICON_THEME)
                .append("<span class=\"sr-only\">Select theme</span><select data-theme-select>")
                .append("<option value=\"dark\">Dark</option><option value=\"light\">Light</option><option value=\"auto\">Auto</option>")
                .append("</select>").append(ICON_CHEVRON).append("</label>")
                .append("</div>")
                .append("<button type=\"button\" class=\"menu-button\" aria-label=\"Menu\" aria-expanded=\"false\" data-open-menu>").append(ICON_MENU).append("</button>")
                .append("</div></header>\n");
    }

    private void item(StringBuilder b, Sidebar.Item item, String current, boolean topLevel) {
        switch (item) {
            case Sidebar.Link link -> b.append("<li>").append(link(link.label(), link.path(), current)).append("</li>");
            case Sidebar.Group group -> {
                boolean open = contains(group, current);
                b.append("<li><details").append(open ? " open" : "").append("><summary><span class=\"group-label\"><span class=\"")
                        .append(topLevel ? "large" : "").append("\">").append(Html.esc(group.label())).append("</span></span>")
                        .append(ICON_CARET).append("</summary><ul>");
                for (Sidebar.Item child : group.items()) {
                    item(b, child, current, false);
                }
                b.append("</ul></details></li>");
            }
            case Sidebar.PackageLink pkg -> {
                if (!current.startsWith(pkg.pkg().path())) {
                    b.append("<li>").append(link(pkg.label(), pkg.pkg().path(), current)).append("</li>");
                    return;
                }
                b.append("<li><details open><summary><span class=\"group-label\"><span>").append(breakable(pkg.label()))
                        .append("</span></span>").append(ICON_CARET).append("</summary><ul>")
                        .append("<li>").append(link("Overview", pkg.pkg().path(), current)).append("</li>");
                for (var type : pkg.pkg().types()) {
                    b.append("<li>").append(link(Site.nestedName(type), site.typePaths.get(type), current)).append("</li>");
                }
                b.append("</ul></details></li>");
            }
        }
    }

    /**
     * Escaped dotted name that may only wrap after its dots
     */
    private static String breakable(String name) {
        return Html.esc(name).replace(".", ".<wbr>");
    }

    private String link(String label, String path, String current) {
        boolean active = path.equals(current);
        return "<a href=\"" + Html.rel(current, path) + "\"" + (active ? " aria-current=\"page\"" : "") + "><span>"
                + breakable(label) + "</span></a>";
    }

    private boolean contains(Sidebar.Item item, String current) {
        return switch (item) {
            case Sidebar.Link link -> link.path().equals(current);
            case Sidebar.PackageLink pkg -> current.startsWith(pkg.pkg().path());
            case Sidebar.Group group -> group.items().stream().anyMatch(child -> contains(child, current));
        };
    }

    // ------------------------------------------------------------------ components shared by the page renderers

    static String codeFrame(String highlighted, String lang, String title) {
        boolean terminal = lang != null && List.of("sh", "bash", "shell", "console", "powershell").contains(lang);
        StringBuilder b = new StringBuilder("<div class=\"code-frame");
        if (title != null || terminal) {
            b.append(terminal ? " is-terminal" : " has-title");
        }
        b.append("\">");
        if (title != null || terminal) {
            b.append("<div class=\"code-header\"><span class=\"code-title\">").append(title == null ? "" : Html.esc(title)).append("</span></div>");
        }
        b.append("<pre").append(lang == null || lang.isEmpty() ? "" : " data-lang=\"" + Html.esc(lang) + "\"").append("><code>")
                .append(highlighted).append("</code></pre>")
                .append("<button type=\"button\" class=\"copy-button\" aria-label=\"Copy to clipboard\" title=\"Copy to clipboard\"><span class=\"copy-feedback\">Copied!</span></button>")
                .append("</div>");
        return b.toString();
    }

    /**
     * Tabs like Starlight's, {@code tabs} holds pairs of label html and panel html
     */
    static String tabs(List<String[]> tabs) {
        StringBuilder b = new StringBuilder("<div class=\"tabs\"><div class=\"tab-list\" role=\"tablist\">");
        for (int i = 0; i < tabs.size(); i++) {
            b.append("<button type=\"button\" role=\"tab\" class=\"tab\" aria-selected=\"").append(i == 0)
                    .append("\" data-label=\"").append(Html.esc(Html.stripTags(tabs.get(i)[0]))).append("\">")
                    .append(tabs.get(i)[0]).append("</button>");
        }
        b.append("</div>");
        for (int i = 0; i < tabs.size(); i++) {
            b.append("<div class=\"tab-panel\" role=\"tabpanel\"").append(i == 0 ? "" : " hidden").append('>')
                    .append(tabs.get(i)[1]).append("</div>");
        }
        return b.append("</div>").toString();
    }

    static String aside(String type, String title, String html) {
        return "<aside class=\"starlight-aside starlight-aside--" + type + "\" aria-label=\"" + Html.esc(Html.stripTags(title)) + "\">"
                + "<p class=\"starlight-aside__title\" aria-hidden=\"true\">" + asideIcon(type) + title + "</p>"
                + "<div class=\"starlight-aside__content\">" + html + "</div></aside>";
    }

    private static String asideIcon(String type) {
        String path = switch (type) {
            case "tip" -> "M1.43909 8.85483L1.44039 8.85354L4.96668 5.33815C5.30653 4.99386 5.7685 4.79662 6.2524 4.78972L6.26553 4.78963L12.9014 4.78962L13.8479 3.84308C16.9187 0.772319 20.0546 0.770617 21.4678 0.975145C21.8617 1.02914 22.2271 1.21053 22.5083 1.4917C22.7894 1.77284 22.9708 2.13821 23.0248 2.53199C23.2294 3.94517 23.2278 7.08119 20.1569 10.1521L19.2107 11.0983V17.7338L19.2106 17.7469C19.2037 18.2308 19.0067 18.6933 18.6624 19.0331L15.1456 22.5608C14.9095 22.7966 14.6137 22.964 14.29 23.0449C13.9663 23.1259 13.6267 23.1174 13.3074 23.0204C12.9881 22.9235 12.7011 22.7417 12.4771 22.4944C12.2533 22.2473 12.1006 21.9441 12.0355 21.6171L11.1783 17.3417L6.65869 12.822L4.34847 12.3589L2.38351 11.965C2.05664 11.8998 1.75272 11.747 1.50564 11.5232C1.25835 11.2992 1.07653 11.0122 0.979561 10.6929C0.882595 10.3736 0.874125 10.034 0.955057 9.7103C1.03599 9.38659 1.20328 9.09092 1.43909 8.85483Z";
            case "caution" -> "M12 7.5a1 1 0 0 0-1 1v4a1 1 0 0 0 2 0v-4a1 1 0 0 0-1-1Zm0 8a1 1 0 1 0 0 2 1 1 0 0 0 0-2Zm9.71-8.21-5-5A1 1 0 0 0 16 2H8a1 1 0 0 0-.71.29l-5 5A1 1 0 0 0 2 8v8a1 1 0 0 0 .29.71l5 5A1 1 0 0 0 8 22h8a1 1 0 0 0 .71-.29l5-5A1 1 0 0 0 22 16V8a1 1 0 0 0-.29-.71ZM20 15.59 15.59 20H8.41L4 15.59V8.41L8.41 4h7.18L20 8.41v7.18Z";
            case "danger" -> "M12 16a1 1 0 1 0 0 2 1 1 0 0 0 0-2Zm10.67 1.47-8.05-14a3 3 0 0 0-5.24 0l-8 14A3 3 0 0 0 3.94 22h16.12a3 3 0 0 0 2.61-4.53Zm-1.73 2a1 1 0 0 1-.88.51H3.94a1 1 0 0 1-.88-.51 1 1 0 0 1 0-1l8-14a1 1 0 0 1 1.78 0l8.05 14a1 1 0 0 1 .05 1.02v-.02ZM12 8a1 1 0 0 0-1 1v4a1 1 0 0 0 2 0V9a1 1 0 0 0-1-1Z";
            default -> "M12 11C11.7348 11 11.4804 11.1054 11.2929 11.2929C11.1054 11.4804 11 11.7348 11 12V16C11 16.2652 11.1054 16.5196 11.2929 16.7071C11.4804 16.8946 11.7348 17 12 17C12.2652 17 12.5196 16.8946 12.7071 16.7071C12.8946 16.5196 13 16.2652 13 16V12C13 11.7348 12.8946 11.4804 12.7071 11.2929C12.5196 11.1054 12.2652 11 12 11ZM12.38 7.08C12.1365 6.97998 11.8635 6.97998 11.62 7.08C11.4973 7.12759 11.3851 7.19896 11.29 7.29C11.2017 7.3872 11.1306 7.49882 11.08 7.62C11.024 7.73868 10.9966 7.86882 11 8C10.9992 8.13161 11.0245 8.26207 11.0742 8.38391C11.124 8.50574 11.1973 8.61656 11.29 8.71C11.3872 8.79833 11.4988 8.86936 11.62 8.92C11.7715 8.98224 11.936 9.00632 12.099 8.99011C12.2619 8.97391 12.4184 8.91792 12.5547 8.82707C12.691 8.73622 12.8029 8.61328 12.8805 8.46907C12.9582 8.32486 12.9992 8.16378 13 8C12.9963 7.73523 12.8927 7.48163 12.71 7.29C12.6149 7.19896 12.5028 7.12759 12.38 7.08ZM12 2C10.0222 2 8.08879 2.58649 6.4443 3.6853C4.79981 4.78412 3.51809 6.3459 2.76121 8.17317C2.00433 10.0004 1.8063 12.0111 2.19215 13.9509C2.578 15.8907 3.53041 17.6725 4.92894 19.0711C6.32746 20.4696 8.10929 21.422 10.0491 21.8079C11.9889 22.1937 13.9996 21.9957 15.8268 21.2388C17.6541 20.4819 19.2159 19.2002 20.3147 17.5557C21.4135 15.9112 22 13.9778 22 12C22 10.6868 21.7413 9.38642 21.2388 8.17317C20.7363 6.95991 19.9997 5.85752 19.0711 4.92893C18.1425 4.00035 17.0401 3.26375 15.8268 2.7612C14.6136 2.25866 13.3132 2 12 2ZM12 20C10.4178 20 8.87104 19.5308 7.55544 18.6518C6.23985 17.7727 5.21447 16.5233 4.60897 15.0615C4.00347 13.5997 3.84504 11.9911 4.15372 10.4393C4.4624 8.88743 5.22433 7.46197 6.34315 6.34315C7.46197 5.22433 8.88743 4.4624 10.4393 4.15372C11.9911 3.84504 13.5997 4.00346 15.0615 4.60896C16.5233 5.21447 17.7727 6.23984 18.6518 7.55544C19.5308 8.87103 20 10.4177 20 12C20 14.1217 19.1572 16.1566 17.6569 17.6569C16.1566 19.1571 14.1217 20 12 20Z";
        };
        return "<svg aria-hidden=\"true\" class=\"starlight-aside__icon\" width=\"16\" height=\"16\" viewBox=\"0 0 24 24\" fill=\"currentColor\"><path d=\"" + path + "\"/></svg>";
    }

    static String badge(String text, String cls) {
        return badge(text, cls, null);
    }

    static String badge(String text, String cls, String title) {
        return "<span class=\"api-badge api-badge-" + cls + "\"" + (title == null ? "" : " title=\"" + Html.esc(title) + "\"") + ">"
                + Html.esc(text) + "</span>";
    }
}
