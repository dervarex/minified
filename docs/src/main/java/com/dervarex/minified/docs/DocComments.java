package com.dervarex.minified.docs;

import com.sun.source.doctree.AttributeTree;
import com.sun.source.doctree.BlockTagTree;
import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.EntityTree;
import com.sun.source.doctree.ErroneousTree;
import com.sun.source.doctree.IndexTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.doctree.SinceTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.SummaryTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.doctree.ThrowsTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.doctree.UnknownInlineTagTree;
import com.sun.source.doctree.ValueTree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.TreePath;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Renders javadoc comments to HTML, resolving links against the site
 */
final class DocComments {

    /**
     * A doc comment together with the element it is attached to
     *
     * @param tree          the parsed comment
     * @param owner         element the comment is written on, used to resolve references
     * @param inheritedFrom the overridden method the comment was copied from, or null
     */
    record Doc(DocCommentTree tree, Element owner, ExecutableElement inheritedFrom) {
    }

    /**
     * One rendered block tag like {@code @param} or {@code @throws}
     */
    record Tag(String name, String html) {
    }

    private final Site site;

    DocComments(Site site) {
        this.site = site;
    }

    /**
     * The comment of an element, falling back to the comment of the method it overrides
     */
    Doc find(Element e) {
        if (e == null) {
            return null;
        }
        DocCommentTree tree = site.trees.getDocCommentTree(e);
        if (tree != null) {
            return new Doc(tree, e, null);
        }
        if (e instanceof ExecutableElement method && method.getKind() == ElementKind.METHOD) {
            ExecutableElement overridden = overriddenWithDoc(method);
            if (overridden != null) {
                return new Doc(site.trees.getDocCommentTree(overridden), overridden, overridden);
            }
        }
        return null;
    }

    ExecutableElement overridden(ExecutableElement method) {
        return overridden(method, false);
    }

    private ExecutableElement overriddenWithDoc(ExecutableElement method) {
        return overridden(method, true);
    }

    private ExecutableElement overridden(ExecutableElement method, boolean requireDoc) {
        TypeElement owner = (TypeElement) method.getEnclosingElement();
        Deque<TypeMirror> queue = new ArrayDeque<>(site.types.directSupertypes(owner.asType()));
        Set<String> seen = new HashSet<>();
        while (!queue.isEmpty()) {
            TypeElement type = site.asTypeElement(queue.poll());
            if (type == null || !seen.add(type.getQualifiedName().toString())) {
                continue;
            }
            for (ExecutableElement candidate : ElementFilter.methodsIn(type.getEnclosedElements())) {
                if (site.elements.overrides(method, candidate, owner)
                        && (!requireDoc || site.trees.getDocCommentTree(candidate) != null)) {
                    return candidate;
                }
            }
            queue.addAll(site.types.directSupertypes(type.asType()));
        }
        return null;
    }

    // ------------------------------------------------------------------ parts of a comment

    String body(Doc doc, String from) {
        return doc == null ? "" : render(doc, doc.tree().getFullBody(), from);
    }

    String summary(Doc doc, String from) {
        if (doc == null) {
            return "";
        }
        String html = render(doc, doc.tree().getFirstSentence(), from);
        // a summary should stay inline, drop block level markup
        return html.replaceAll("(?is)<(/?)(p|pre|ul|ol|li|div|h\\d|blockquote|table)[^>]*>", " ").trim();
    }

    String param(Doc doc, String name, String from) {
        if (doc == null) {
            return "";
        }
        for (DocTree tag : doc.tree().getBlockTags()) {
            if (tag instanceof ParamTree param && !param.isTypeParameter()
                    && param.getName().getName().contentEquals(name)) {
                return render(doc, param.getDescription(), from);
            }
        }
        return "";
    }

    String typeParam(Doc doc, String name, String from) {
        if (doc == null) {
            return "";
        }
        for (DocTree tag : doc.tree().getBlockTags()) {
            if (tag instanceof ParamTree param && param.isTypeParameter()
                    && param.getName().getName().contentEquals(name)) {
                return render(doc, param.getDescription(), from);
            }
        }
        return "";
    }

    String returns(Doc doc, String from) {
        if (doc == null) {
            return "";
        }
        for (DocTree tag : doc.tree().getBlockTags()) {
            if (tag instanceof ReturnTree ret) {
                return render(doc, ret.getDescription(), from);
            }
        }
        // {@return ...} written inline in the body
        for (DocTree tree : doc.tree().getFullBody()) {
            if (tree instanceof ReturnTree ret) {
                return render(doc, ret.getDescription(), from);
            }
        }
        return "";
    }

    /**
     * Rendered {@code @throws} tags as pairs of linked exception type and description
     */
    List<String[]> throwsTags(Doc doc, String from) {
        List<String[]> result = new ArrayList<>();
        if (doc == null) {
            return result;
        }
        for (DocTree tag : doc.tree().getBlockTags()) {
            if (tag instanceof ThrowsTree t) {
                result.add(new String[]{reference(doc, t.getExceptionName(), List.of(), from, true),
                        render(doc, t.getDescription(), from)});
            }
        }
        return result;
    }

    /**
     * The remaining block tags: see, since, deprecated, author and notes like {@code @apiNote}
     */
    List<Tag> otherTags(Doc doc, String from) {
        List<Tag> result = new ArrayList<>();
        if (doc == null) {
            return result;
        }
        for (DocTree tag : doc.tree().getBlockTags()) {
            switch (tag) {
                case SeeTree see -> {
                    List<? extends DocTree> ref = see.getReference();
                    if (ref.size() == 1 && ref.get(0) instanceof ReferenceTree r) {
                        result.add(new Tag("See also", reference(doc, r, List.of(), from, true)));
                    } else {
                        result.add(new Tag("See also", render(doc, ref, from)));
                    }
                }
                case SinceTree since -> result.add(new Tag("Since", render(doc, since.getBody(), from)));
                case DeprecatedTree deprecated -> result.add(new Tag("Deprecated", render(doc, deprecated.getBody(), from)));
                case UnknownBlockTagTree unknown -> result.add(new Tag(noteLabel(unknown.getTagName()), render(doc, unknown.getContent(), from)));
                default -> {
                }
            }
        }
        return result;
    }

    String deprecation(Doc doc, String from) {
        if (doc == null) {
            return "";
        }
        for (DocTree tag : doc.tree().getBlockTags()) {
            if (tag instanceof DeprecatedTree deprecated) {
                return render(doc, deprecated.getBody(), from);
            }
        }
        return "";
    }

    private static String noteLabel(String tag) {
        return switch (tag) {
            case "apiNote" -> "API note";
            case "implNote" -> "Implementation note";
            case "implSpec" -> "Implementation requirements";
            default -> tag;
        };
    }

    // ------------------------------------------------------------------ rendering

    String render(Doc doc, List<? extends DocTree> trees, String from) {
        Renderer renderer = new Renderer(doc, from);
        renderer.all(trees);
        return renderer.finish();
    }

    private final class Renderer {
        private final Doc doc;
        private final String from;
        private final StringBuilder out = new StringBuilder();
        private StringBuilder pre;

        Renderer(Doc doc, String from) {
            this.doc = doc;
            this.from = from;
        }

        void all(List<? extends DocTree> trees) {
            trees.forEach(this::one);
        }

        String finish() {
            if (pre != null) {
                out.append(codeBlock(pre.toString()));
            }
            return out.toString().trim();
        }

        private void one(DocTree tree) {
            switch (tree) {
                case ErroneousTree erroneous -> out.append(Html.esc(erroneous.getBody()));
                case TextTree text -> {
                    if (pre != null) {
                        pre.append(Html.unescape(text.getBody()));
                    } else {
                        out.append(text.getBody());
                    }
                }
                case EntityTree entity -> {
                    if (pre != null) {
                        pre.append(Html.unescape("&" + entity.getName() + ";"));
                    } else {
                        out.append('&').append(entity.getName()).append(';');
                    }
                }
                case StartElementTree start -> {
                    if (start.getName().contentEquals("pre")) {
                        pre = new StringBuilder();
                        return;
                    }
                    out.append('<').append(start.getName());
                    for (DocTree attribute : start.getAttributes()) {
                        if (attribute instanceof AttributeTree a) {
                            out.append(' ').append(a.getName());
                            if (a.getValueKind() != AttributeTree.ValueKind.EMPTY) {
                                out.append("=\"").append(Html.esc(Html.unescape(render(doc, a.getValue(), from)))).append('"');
                            }
                        }
                    }
                    out.append(start.isSelfClosing() ? "/>" : ">");
                }
                case EndElementTree end -> {
                    if (end.getName().contentEquals("pre") && pre != null) {
                        out.append(codeBlock(pre.toString()));
                        pre = null;
                    } else {
                        out.append("</").append(end.getName()).append('>');
                    }
                }
                case LiteralTree literal -> {
                    String body = literal.getBody().getBody();
                    if (pre != null) {
                        pre.append(body);
                    } else if (literal.getKind() == DocTree.Kind.CODE) {
                        out.append("<code>").append(Html.esc(body)).append("</code>");
                    } else {
                        out.append(Html.esc(body));
                    }
                }
                case LinkTree link -> out.append(reference(doc, link.getReference(), link.getLabel(), from,
                        link.getKind() == DocTree.Kind.LINK));
                case ValueTree value -> out.append(value(value));
                case SummaryTree summary -> all(summary.getSummary());
                case IndexTree index -> all(List.of(index.getSearchTerm()));
                case ReturnTree ret -> {
                    out.append("Returns ");
                    all(ret.getDescription());
                    out.append('.');
                }
                case UnknownInlineTagTree unknown -> all(unknown.getContent());
                default -> {
                    if (tree.getKind() == DocTree.Kind.INHERIT_DOC) {
                        inheritDoc();
                    } else if (!(tree instanceof BlockTagTree)) {
                        out.append(Html.esc(tree.toString()));
                    }
                }
            }
        }

        private void inheritDoc() {
            if (doc.owner() instanceof ExecutableElement method) {
                ExecutableElement overridden = overriddenWithDoc(method);
                if (overridden != null) {
                    Doc parent = new Doc(site.trees.getDocCommentTree(overridden), overridden, overridden);
                    out.append(render(parent, parent.tree().getFullBody(), from));
                }
            }
        }

        private String value(ValueTree value) {
            if (value.getReference() == null) {
                return doc.owner() instanceof VariableElement v && v.getConstantValue() != null
                        ? Signatures.constantHtml(v.getConstantValue()) : "";
            }
            Element target = resolve(doc, value.getReference());
            if (target instanceof VariableElement v && v.getConstantValue() != null) {
                return "<code>" + Signatures.constantHtml(v.getConstantValue()) + "</code>";
            }
            return "<code>" + Html.esc(value.getReference().getSignature()) + "</code>";
        }
    }

    private String codeBlock(String code) {
        return Layout.codeFrame(Highlighter.highlight(Html.dedent(code), "java"), "java", null);
    }

    // ------------------------------------------------------------------ references

    Element resolve(Doc doc, ReferenceTree reference) {
        TreePath path = site.trees.getPath(doc.owner());
        if (path == null) {
            return null;
        }
        DocTreePath docPath = DocTreePath.getPath(path, doc.tree(), reference);
        return docPath == null ? null : site.trees.getElement(docPath);
    }

    private String reference(Doc doc, ReferenceTree reference, List<? extends DocTree> label, String from, boolean code) {
        Element target = resolve(doc, reference);
        String text = label.isEmpty() ? defaultLabel(reference.getSignature(), target, doc.owner()) : null;
        String labelHtml = text != null ? Html.esc(text) : render(doc, label, from);
        if (code && text != null) {
            labelHtml = "<code>" + labelHtml + "</code>";
        }
        if (target == null) {
            if (!reference.getSignature().isBlank()) {
                site.warnings.add(where(doc), "unresolved link {@link " + reference.getSignature() + "}");
            }
            return labelHtml;
        }
        String url = site.elementUrl(target, from);
        if (url == null) {
            return labelHtml;
        }
        return "<a href=\"" + url + "\"" + (url.startsWith("http") ? " rel=\"external\"" : "") + ">" + labelHtml + "</a>";
    }

    private String defaultLabel(String signature, Element target, Element context) {
        String sig = signature.replaceAll("[\\w.]*\\.([A-Z]\\w*)", "$1").replace(" ", "");
        int hash = sig.indexOf('#');
        if (hash < 0) {
            return target instanceof TypeElement type ? Site.nestedName(type) : sig;
        }
        String owner = sig.substring(0, hash);
        String member = sig.substring(hash + 1).replace(",", ", ");
        return owner.isEmpty() ? member : owner + "." + member;
    }

    String where(Doc doc) {
        Element owner = doc.owner();
        TypeElement type = site.enclosingType(owner);
        String typeName = type == null ? owner.toString() : type.getQualifiedName().toString();
        return owner instanceof TypeElement ? typeName : typeName + "#" + owner.getSimpleName();
    }
}
