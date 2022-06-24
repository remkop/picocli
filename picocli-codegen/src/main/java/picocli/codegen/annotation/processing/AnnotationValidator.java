package picocli.codegen.annotation.processing;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.codegen.util.Assert;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

class AnnotationValidator {

    @SuppressWarnings("unchecked")
    private static final List<Class<? extends Annotation>> ALL = Collections.unmodifiableList(
            Arrays.asList(
                    CommandLine.Command.class,
                    Option.class,
                    Parameters.class,
                    CommandLine.Mixin.class,
                    CommandLine.ParentCommand.class,
                    CommandLine.Spec.class,
                    CommandLine.Unmatched.class,
                    CommandLine.ArgGroup.class
            ));
    private final ProcessingEnvironment processingEnv;
    private final TypeMirror stringType;
    private final Types typeUtils;

    public AnnotationValidator(ProcessingEnvironment processingEnv) {
        this.processingEnv = Assert.notNull(processingEnv, "processingEnv");
        this.stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();
        this.typeUtils = processingEnv.getTypeUtils();
    }

    public void validateAnnotations(RoundEnvironment roundEnv) {
        validateNoAnnotationsOnInterfaceField(roundEnv);
        validateInvalidCombinations(roundEnv);

        Set<? extends Element> optionElements = roundEnv.getElementsAnnotatedWith(Option.class);
        validateOptions(optionElements);
        validatePositionalParameters(roundEnv.getElementsAnnotatedWith(Parameters.class));

        // TODO
        //validateSpecFieldTypeIsCommandSpec(roundEnv);
        //validateOptionOrParametersIsNotFinalPrimitiveOrFinalString(roundEnv);
        //validateUnmatchedFieldTypeIsStringArrayOrListOfString(roundEnv);
    }

    @SuppressWarnings("deprecation")
    private void validateOptions(Set<? extends Element> optionElements) {
        final Map<Element, Integer> usageHelpOptions = new HashMap<Element, Integer>();
        final Map<Element, Integer> versionHelpOptions = new HashMap<Element, Integer>();
        for (Element element : optionElements) {
            element.accept(new SimpleElementVisitor6<Void, Option>() {
                @Override
                public Void visitVariable(VariableElement e, Option option) {
                    checkOption(e, e.asType(), option);
                    return null;
                }

                @Override
                public Void visitExecutable(ExecutableElement e, Option option) {
                    List<? extends TypeMirror> parameterTypes = ((ExecutableType) e.asType()).getParameterTypes();
                    if (parameterTypes.isEmpty() && e.getReturnType().getKind() == TypeKind.VOID) {
                        error(e, null, "Only getter or setter methods can be annotated with @Option, but %s is neither.",
                                e.getSimpleName());
                        return null;
                    }
                    boolean isGetter = parameterTypes.isEmpty() && e.getReturnType().getKind() != TypeKind.VOID;
                    TypeMirror type = isGetter ? e.getReturnType() : parameterTypes.get(0);
                    checkOption(e, type, option);
                    return null;
                }

                private void checkOption(Element e, TypeMirror type, Option option) {
                    if (option.negatable()) {
                        checkBooleanOptionType(e, type, "%s must be a boolean: only boolean options can be negatable.");
                    }
                    if (option.usageHelp()) {
                        increment(usageHelpOptions, e.getEnclosingElement());
                        checkBooleanOptionType(e, type, "%s must be a boolean: a command can have max one usageHelp boolean flag that triggers display of the usage help message.");
                    }
                    if (option.versionHelp()) {
                        increment(versionHelpOptions, e.getEnclosingElement());
                        checkBooleanOptionType(e, type, "%s must be a boolean: a command can have max one versionHelp boolean flag that triggers display of the version information.");
                    }
                    if (option.usageHelp() && option.versionHelp()) {
                        error(e, null, "An option can be usageHelp or versionHelp, but %s is both.", e.getSimpleName());
                    }
                    if (option.split().length() > 0 && !new CompileTimeTypeInfo(type).isMultiValue()) {
                        error(e, null, "%s has a split regex but is a single-value type", e.getSimpleName());
                    }
                    maybeValidateFinalFields(e, type, "@Option");
                }
            }, element.getAnnotation(Option.class));
        }

        assertOneEntry(usageHelpOptions, "An command can only have one usageHelp option, but %s has %s.");
        assertOneEntry(versionHelpOptions, "An command can only have one versionHelp option, but %s has %s.");
    }

    private void assertOneEntry(Map<Element, Integer> usageHelpOptions, String msg) {
        for (Map.Entry<Element, Integer> entry : usageHelpOptions.entrySet()) {
            if (entry.getValue() > 1) {
                error(entry.getKey(), null, msg, entry.getKey().getSimpleName(), entry.getValue());
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void validatePositionalParameters(Set<? extends Element> positionalElements) {
        for (Element element : positionalElements) {
            element.accept(new SimpleElementVisitor6<Void, Parameters>() {
                @Override
                public Void visitVariable(VariableElement e, Parameters option) {
                    checkOption(e, e.asType(), option);
                    return null;
                }

                @Override
                public Void visitExecutable(ExecutableElement e, Parameters positinal) {
                    List<? extends TypeMirror> parameterTypes = ((ExecutableType) e.asType()).getParameterTypes();
                    if (parameterTypes.isEmpty() && e.getReturnType().getKind() == TypeKind.VOID) {
                        error(e, null, "Only getter or setter methods can be annotated with @Parameters, but %s is neither.",
                                e.getSimpleName());
                        return null;
                    }
                    boolean isGetter = parameterTypes.isEmpty() && e.getReturnType().getKind() != TypeKind.VOID;
                    TypeMirror type = isGetter ? e.getReturnType() : parameterTypes.get(0);
                    checkOption(e, type, positinal);
                    return null;
                }

                private void checkOption(Element e, TypeMirror type, Parameters positional) {
                    if (positional.split().length() > 0 && !new CompileTimeTypeInfo(type).isMultiValue()) {
                        error(e, null, "%s has a split regex but is a single-value type", e.getSimpleName());
                    }
                    maybeValidateFinalFields(e, type, "@Parameters");
                }
            }, element.getAnnotation(Parameters.class));
        }
    }

    private <T> void increment(Map<T, Integer> map, T key) {
        Integer existing = map.get(key);
        if (existing == null) {
            map.put(key, 1);
        } else {
            map.put(key, existing + 1);
        }
    }

    private Void checkBooleanOptionType(Element e, TypeMirror type, String msg) {
        if (!CompileTimeTypeInfo.isBooleanType(type)) {
            error(e, null, msg, e.getSimpleName());
        }
        return null;
    }

    private void validateNoAnnotationsOnInterfaceField(RoundEnvironment roundEnv) {
        for (Class<? extends Annotation> cls : ALL) {
            validateNoAnnotationsOnInterfaceField(roundEnv.getElementsAnnotatedWith(cls));
        }
    }

    private void validateNoAnnotationsOnInterfaceField(Set<? extends Element> all) {
        for (Element element : all) {
            if (isInterfaceField(element)) {
                AnnotationMirror annotationMirror = getPicocliAnnotationMirror(element);
                error(element, annotationMirror, "Invalid picocli annotation on interface field %s.%s",
                    element.getEnclosingElement().toString(), element.getSimpleName());
            }
        }
    }

    private boolean isInterfaceField(Element element) {
        return element.getKind() == ElementKind.FIELD && element.getEnclosingElement().getKind() == ElementKind.INTERFACE;
    }

    private void validateInvalidCombinations(RoundEnvironment roundEnv) {
        for (int i = 0; i < ALL.size(); i++) {
            for (int j = i + 1; j < ALL.size(); j++) {
                validateInvalidCombination(roundEnv, ALL.get(i), ALL.get(j));
            }
        }
    }

    private <T1 extends Annotation, T2 extends Annotation> void validateInvalidCombination(
        RoundEnvironment roundEnv, Class<T1> c1, Class<T2> c2) {
        for (Element element : roundEnv.getElementsAnnotatedWith(c1)) {
            if (element.getAnnotation(c2) != null) {
                AnnotationMirror annotationMirror = getPicocliAnnotationMirror(element);
                error(element, annotationMirror, "%s cannot have both @%s and @%s annotations",
                    element, c1.getCanonicalName(), c2.getCanonicalName());
            }
        }
    }

    private void maybeValidateFinalFields(Element e, TypeMirror type, String annotation) {
        ElementKind elementKind = e.getKind();
        boolean isFinal = e.getModifiers().contains(Modifier.FINAL);
        // We have an existing validator for fields in an interface. Ignore those elements here.
        boolean shouldValidateFinal = elementKind.isField() && !isInterfaceField(e);
        if (!isFinal || !shouldValidateFinal) {
            return;
        }
        boolean isConstantValueField = false;
        for (VariableElement variableElement : ElementFilter.fieldsIn(Collections.singletonList(e))) {
             isConstantValueField = isConstantValueField || variableElement.getConstantValue() != null;
        }
        if ((type.getKind().isPrimitive() || typeUtils.isAssignable(type, stringType)) && isConstantValueField) {
            error(e, null, "Constant (final) primitive and String fields like %s cannot be used as %s: compile-time constant inlining may hide new values written to it.", e.getSimpleName(), annotation);
        }
    }

    private AnnotationMirror getPicocliAnnotationMirror(Element element) {
        AnnotationMirror annotationMirror = null;
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (mirror.getAnnotationType().toString().startsWith("picocli")) {
                annotationMirror = mirror;
            }
        }
        return annotationMirror;
    }

    void error(Element e, AnnotationMirror mirror, String msg, Object... args) {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                format(msg, args),
                e, mirror);
    }
}
