package com.dervarex.minified.docs;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * The javadoc tool does not run annotation processors, so members Lombok generates are reconstructed here
 */
final class Lombok {

    enum Kind { GETTER, SETTER, NO_ARGS_CONSTRUCTOR }

    /**
     * A member Lombok generates
     *
     * @param field  the field the member is generated for, null for constructors
     * @param access {@code public} or {@code protected}
     */
    record Generated(Kind kind, String name, VariableElement field, String access) {
    }

    private Lombok() {
    }

    static List<Generated> members(TypeElement type) {
        List<Generated> result = new ArrayList<>();
        AnnotationMirror classGetter = Signatures.annotation(type, "Getter");
        AnnotationMirror classSetter = Signatures.annotation(type, "Setter");
        List<ExecutableElement> declared = ElementFilter.methodsIn(type.getEnclosedElements());

        for (VariableElement field : ElementFilter.fieldsIn(type.getEnclosedElements())) {
            if (field.getKind() != ElementKind.FIELD) {
                continue;
            }
            boolean isStatic = field.getModifiers().contains(Modifier.STATIC);
            String getterAccess = access(Signatures.annotation(field, "Getter"), isStatic ? null : classGetter);
            if (getterAccess != null) {
                String name = getterName(field);
                if (!declares(declared, name, 0)) {
                    result.add(new Generated(Kind.GETTER, name, field, getterAccess));
                }
            }
            if (!field.getModifiers().contains(Modifier.FINAL)) {
                String setterAccess = access(Signatures.annotation(field, "Setter"), isStatic ? null : classSetter);
                if (setterAccess != null) {
                    String name = "set" + capitalize(baseName(field));
                    if (!declares(declared, name, 1)) {
                        result.add(new Generated(Kind.SETTER, name, field, setterAccess));
                    }
                }
            }
        }

        AnnotationMirror noArgs = Signatures.annotation(type, "NoArgsConstructor");
        if (noArgs != null) {
            String access = level(Signatures.annotationValue(noArgs, "access", "PUBLIC"));
            boolean exists = ElementFilter.constructorsIn(type.getEnclosedElements()).stream()
                    .anyMatch(c -> c.getParameters().isEmpty());
            if (access != null && !exists) {
                result.add(new Generated(Kind.NO_ARGS_CONSTRUCTOR, type.getSimpleName().toString(), null, access));
            }
        }
        return result;
    }

    /**
     * Access of a generated accessor, the field annotation wins over the class annotation
     */
    private static String access(AnnotationMirror onField, AnnotationMirror onClass) {
        AnnotationMirror effective = onField != null ? onField : onClass;
        if (effective == null) {
            return null;
        }
        return level(Signatures.annotationValue(effective, "value", "PUBLIC"));
    }

    private static String level(String accessLevel) {
        return switch (accessLevel) {
            case "PUBLIC" -> "public";
            case "PROTECTED" -> "protected";
            default -> null; // NONE, PRIVATE, PACKAGE and MODULE are not part of the documented api
        };
    }

    private static boolean declares(List<ExecutableElement> methods, String name, int params) {
        return methods.stream().anyMatch(m -> m.getSimpleName().contentEquals(name) && m.getParameters().size() == params);
    }

    static String getterName(VariableElement field) {
        String name = field.getSimpleName().toString();
        if (field.asType().getKind() == TypeKind.BOOLEAN) {
            return isPrefixed(name) ? name : "is" + capitalize(name);
        }
        return "get" + capitalize(name);
    }

    private static String baseName(VariableElement field) {
        String name = field.getSimpleName().toString();
        return field.asType().getKind() == TypeKind.BOOLEAN && isPrefixed(name) ? name.substring(2) : name;
    }

    private static boolean isPrefixed(String name) {
        return name.length() > 2 && name.startsWith("is") && Character.isUpperCase(name.charAt(2));
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    static boolean isLombokAnnotated(Element e) {
        return e.getAnnotationMirrors().stream().anyMatch(a ->
                ((TypeElement) a.getAnnotationType().asElement()).getQualifiedName().toString().startsWith("lombok."));
    }
}
