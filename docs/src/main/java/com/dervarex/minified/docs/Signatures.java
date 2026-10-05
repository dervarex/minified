package com.dervarex.minified.docs;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Renders types and declarations as linked, highlighted HTML
 */
final class Signatures {

    private static final Set<Modifier> SHOWN = EnumSet.of(Modifier.PUBLIC, Modifier.PROTECTED, Modifier.ABSTRACT,
            Modifier.DEFAULT, Modifier.STATIC, Modifier.SEALED, Modifier.NON_SEALED, Modifier.FINAL);

    private final Site site;

    Signatures(Site site) {
        this.site = site;
    }

    // ------------------------------------------------------------------ types

    String type(TypeMirror t, String from) {
        return switch (t.getKind()) {
            case DECLARED -> declared((DeclaredType) t, from);
            case ARRAY -> type(((ArrayType) t).getComponentType(), from) + "[]";
            case TYPEVAR -> "<span class=\"tvar\">" + Html.esc(((TypeVariable) t).asElement().getSimpleName().toString()) + "</span>";
            case WILDCARD -> {
                WildcardType w = (WildcardType) t;
                if (w.getExtendsBound() != null) {
                    yield "? <span class=\"tok-k\">extends</span> " + type(w.getExtendsBound(), from);
                }
                if (w.getSuperBound() != null) {
                    yield "? <span class=\"tok-k\">super</span> " + type(w.getSuperBound(), from);
                }
                yield "?";
            }
            case INTERSECTION -> ((IntersectionType) t).getBounds().stream().map(b -> type(b, from)).collect(Collectors.joining(" &amp; "));
            case UNION -> ((UnionType) t).getAlternatives().stream().map(b -> type(b, from)).collect(Collectors.joining(" | "));
            case ERROR -> "<span class=\"tref\">" + Html.esc(stripAnnotations(t.toString())) + "</span>";
            default -> "<span class=\"tok-k\">" + Html.esc(t.getKind() == TypeKind.VOID ? "void" : stripAnnotations(t.toString())) + "</span>";
        };
    }

    private String declared(DeclaredType t, String from) {
        TypeElement element = (TypeElement) t.asElement();
        String name = Html.esc(element.getSimpleName().toString());
        String title = Html.esc(element.getQualifiedName().toString());
        String url = site.typeUrl(element, from);
        StringBuilder sb = new StringBuilder();
        if (url != null) {
            boolean external = !site.typePaths.containsKey(element);
            sb.append("<a class=\"tref\" href=\"").append(url).append("\" title=\"").append(title).append('"')
                    .append(external ? " rel=\"external\"" : "").append('>').append(name).append("</a>");
        } else {
            sb.append("<span class=\"tref\" title=\"").append(title).append("\">").append(name).append("</span>");
        }
        if (!t.getTypeArguments().isEmpty()) {
            sb.append("&lt;").append(t.getTypeArguments().stream().map(a -> type(a, from)).collect(Collectors.joining(", "))).append("&gt;");
        }
        return sb.toString();
    }

    /**
     * Plain text form with simple names, e.g. {@code List<String>}
     */
    String plain(TypeMirror t) {
        return switch (t.getKind()) {
            case DECLARED -> {
                DeclaredType d = (DeclaredType) t;
                String name = d.asElement().getSimpleName().toString();
                yield d.getTypeArguments().isEmpty() ? name
                        : name + "<" + d.getTypeArguments().stream().map(this::plain).collect(Collectors.joining(", ")) + ">";
            }
            case ARRAY -> plain(((ArrayType) t).getComponentType()) + "[]";
            case TYPEVAR -> ((TypeVariable) t).asElement().getSimpleName().toString();
            case WILDCARD -> {
                WildcardType w = (WildcardType) t;
                yield w.getExtendsBound() != null ? "? extends " + plain(w.getExtendsBound())
                        : w.getSuperBound() != null ? "? super " + plain(w.getSuperBound()) : "?";
            }
            default -> stripAnnotations(t.toString());
        };
    }

    private static String stripAnnotations(String s) {
        return s.replaceAll("@[\\w.]+(\\([^)]*\\))?\\s*", "").replaceAll("\\b[a-z][\\w]*\\.(?=[A-Za-z])", "");
    }

    private String erasure(TypeMirror t) {
        TypeMirror erased = site.types.erasure(t);
        if (erased.getKind() == TypeKind.ARRAY) {
            return erasure(((ArrayType) erased).getComponentType()) + "[]";
        }
        if (erased.getKind() == TypeKind.DECLARED) {
            return ((DeclaredType) erased).asElement().getSimpleName().toString();
        }
        return stripAnnotations(erased.toString());
    }

    // ------------------------------------------------------------------ anchors

    String anchor(Element e) {
        if (e instanceof ExecutableElement exec) {
            String name = exec.getKind() == ElementKind.CONSTRUCTOR
                    ? exec.getEnclosingElement().getSimpleName().toString()
                    : exec.getSimpleName().toString();
            return name + "(" + exec.getParameters().stream().map(p -> erasure(p.asType())).collect(Collectors.joining(",")) + ")";
        }
        return e.getSimpleName().toString();
    }

    static String anchor(String name, List<String> erasedParams) {
        return name + "(" + String.join(",", erasedParams) + ")";
    }

    String erasedName(TypeMirror t) {
        return erasure(t);
    }

    // ------------------------------------------------------------------ declarations

    String modifiers(Element e) {
        boolean inInterface = e.getEnclosingElement() != null && e.getEnclosingElement().getKind().isInterface();
        List<String> shown = new ArrayList<>();
        for (Modifier m : e.getModifiers()) {
            if (!SHOWN.contains(m)) {
                continue;
            }
            if (inInterface && !(e instanceof TypeElement) && (m == Modifier.PUBLIC || m == Modifier.ABSTRACT)) {
                continue;
            }
            if (inInterface && e instanceof VariableElement && (m == Modifier.STATIC || m == Modifier.FINAL)) {
                continue;
            }
            if (e instanceof TypeElement type && (type.getKind() == ElementKind.ENUM || type.getKind() == ElementKind.RECORD)
                    && (m == Modifier.FINAL || (m == Modifier.STATIC && type.getNestingKind().isNested()))) {
                continue;
            }
            if (e instanceof TypeElement type && type.getKind().isInterface() && (m == Modifier.ABSTRACT || m == Modifier.STATIC)) {
                continue;
            }
            shown.add(m.toString());
        }
        return shown.isEmpty() ? "" : "<span class=\"tok-k\">" + String.join(" ", shown) + "</span> ";
    }

    String typeParameters(List<? extends TypeParameterElement> params, String from) {
        if (params.isEmpty()) {
            return "";
        }
        List<String> rendered = new ArrayList<>();
        for (TypeParameterElement p : params) {
            StringBuilder sb = new StringBuilder("<span class=\"tvar\">").append(p.getSimpleName()).append("</span>");
            List<? extends TypeMirror> bounds = p.getBounds();
            if (!(bounds.size() == 1 && bounds.get(0).toString().equals("java.lang.Object"))) {
                sb.append(" <span class=\"tok-k\">extends</span> ")
                        .append(bounds.stream().map(b -> type(b, from)).collect(Collectors.joining(" &amp; ")));
            }
            rendered.add(sb.toString());
        }
        return "&lt;" + String.join(", ", rendered) + "&gt;";
    }

    String executable(ExecutableElement m, String from) {
        StringBuilder sb = new StringBuilder(modifiers(m));
        String typeParams = typeParameters(m.getTypeParameters(), from);
        if (!typeParams.isEmpty()) {
            sb.append(typeParams).append(' ');
        }
        String name;
        if (m.getKind() == ElementKind.CONSTRUCTOR) {
            name = m.getEnclosingElement().getSimpleName().toString();
        } else {
            sb.append(type(m.getReturnType(), from)).append(' ');
            name = m.getSimpleName().toString();
        }
        sb.append("<span class=\"tok-f\">").append(Html.esc(name)).append("</span>");
        List<String> params = new ArrayList<>();
        List<? extends VariableElement> parameters = m.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            VariableElement p = parameters.get(i);
            String type;
            if (m.isVarArgs() && i == parameters.size() - 1 && p.asType() instanceof ArrayType array) {
                type = type(array.getComponentType(), from) + "...";
            } else {
                type = type(p.asType(), from);
            }
            params.add(type + " <span class=\"pname\">" + Html.esc(p.getSimpleName().toString()) + "</span>");
        }
        appendParams(sb, params);
        if (!m.getThrownTypes().isEmpty()) {
            sb.append(" <span class=\"tok-k\">throws</span> ")
                    .append(m.getThrownTypes().stream().map(t -> type(t, from)).collect(Collectors.joining(", ")));
        }
        return sb.toString();
    }

    static void appendParams(StringBuilder sb, List<String> params) {
        int length = params.stream().mapToInt(p -> Html.stripTags(p).length()).sum();
        if (params.size() > 2 && length > 60) {
            sb.append("(\n    ").append(String.join(",\n    ", params)).append("\n)");
        } else {
            sb.append('(').append(String.join(", ", params)).append(')');
        }
    }

    String field(VariableElement f, String from) {
        StringBuilder sb = new StringBuilder(modifiers(f));
        sb.append(type(f.asType(), from)).append(' ')
                .append("<span class=\"tok-v\">").append(Html.esc(f.getSimpleName().toString())).append("</span>");
        Object constant = f.getConstantValue();
        if (constant != null) {
            sb.append(" <span class=\"tok-k\">=</span> ").append(constantHtml(constant));
        }
        return sb.toString();
    }

    static String constantHtml(Object constant) {
        if (constant instanceof String s) {
            return "<span class=\"tok-s\">\"" + Html.esc(s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")) + "\"</span>";
        }
        if (constant instanceof Character c) {
            return "<span class=\"tok-s\">'" + Html.esc(String.valueOf(c)) + "'</span>";
        }
        if (constant instanceof Boolean) {
            return "<span class=\"tok-l\">" + constant + "</span>";
        }
        String suffix = constant instanceof Long ? "L" : constant instanceof Float ? "f" : "";
        return "<span class=\"tok-n\">" + constant + suffix + "</span>";
    }

    String typeDeclaration(TypeElement t, String from) {
        StringBuilder sb = new StringBuilder();
        String status = apiStatus(t);
        if (status != null) {
            sb.append("<span class=\"tok-v\">@API</span><span class=\"tok-p\">(</span>status <span class=\"tok-k\">=</span> ")
                    .append(status).append("<span class=\"tok-p\">)</span>\n");
        }
        sb.append(modifiers(t)).append("<span class=\"tok-k\">").append(kindKeyword(t)).append("</span> ")
                .append("<span class=\"tok-t\">").append(Html.esc(t.getSimpleName().toString())).append("</span>")
                .append(typeParameters(t.getTypeParameters(), from));
        if (t.getKind() == ElementKind.RECORD) {
            List<String> components = t.getRecordComponents().stream()
                    .map(c -> type(c.asType(), from) + " <span class=\"pname\">" + c.getSimpleName() + "</span>")
                    .toList();
            appendParams(sb, components);
        }
        TypeMirror superclass = t.getSuperclass();
        if (t.getKind() == ElementKind.CLASS && superclass.getKind() == TypeKind.DECLARED
                && !superclass.toString().equals("java.lang.Object")) {
            sb.append("\n    <span class=\"tok-k\">extends</span> ").append(type(superclass, from));
        }
        List<? extends TypeMirror> interfaces = t.getInterfaces();
        if (!interfaces.isEmpty()) {
            sb.append("\n    <span class=\"tok-k\">").append(t.getKind().isInterface() ? "extends" : "implements").append("</span> ")
                    .append(interfaces.stream().map(i -> type(i, from)).collect(Collectors.joining(", ")));
        }
        if (!t.getPermittedSubclasses().isEmpty()) {
            sb.append("\n    <span class=\"tok-k\">permits</span> ")
                    .append(t.getPermittedSubclasses().stream().map(i -> type(i, from)).collect(Collectors.joining(", ")));
        }
        return sb.toString();
    }

    static String kindKeyword(TypeElement t) {
        return switch (t.getKind()) {
            case INTERFACE -> "interface";
            case ENUM -> "enum";
            case RECORD -> "record";
            case ANNOTATION_TYPE -> "@interface";
            default -> "class";
        };
    }

    static String kindLabel(TypeElement t, Site site) {
        if (t.getKind() == ElementKind.CLASS && site.types.isSubtype(t.asType(),
                site.elements.getTypeElement("java.lang.Throwable").asType())) {
            return "exception";
        }
        return switch (t.getKind()) {
            case INTERFACE -> "interface";
            case ENUM -> "enum";
            case RECORD -> "record";
            case ANNOTATION_TYPE -> "annotation";
            default -> t.getModifiers().contains(Modifier.ABSTRACT) ? "abstract class" : "class";
        };
    }

    // ------------------------------------------------------------------ annotations

    /**
     * Value of the apiguardian {@code @API(status = ...)} annotation, or null
     */
    String apiStatus(Element e) {
        for (AnnotationMirror annotation : e.getAnnotationMirrors()) {
            String name = ((TypeElement) annotation.getAnnotationType().asElement()).getQualifiedName().toString();
            if (!name.equals("org.apiguardian.api.API")) {
                continue;
            }
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotation.getElementValues().entrySet()) {
                if (entry.getKey().getSimpleName().contentEquals("status")) {
                    Object value = entry.getValue().getValue();
                    return value instanceof VariableElement v ? v.getSimpleName().toString() : String.valueOf(value);
                }
            }
        }
        return null;
    }

    static AnnotationMirror annotation(Element e, String simpleName) {
        for (AnnotationMirror annotation : e.getAnnotationMirrors()) {
            TypeElement type = (TypeElement) annotation.getAnnotationType().asElement();
            String qualified = type.getQualifiedName().toString();
            if (qualified.equals("lombok." + simpleName) || qualified.equals(simpleName)) {
                return annotation;
            }
        }
        return null;
    }

    static String annotationValue(AnnotationMirror annotation, String key, String fallback) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotation.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals(key)) {
                Object value = entry.getValue().getValue();
                return value instanceof VariableElement v ? v.getSimpleName().toString() : String.valueOf(value);
            }
        }
        return fallback;
    }
}
