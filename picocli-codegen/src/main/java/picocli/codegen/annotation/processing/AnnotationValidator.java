package picocli.codegen.annotation.processing;

import picocli.CommandLine;
import picocli.codegen.util.Assert;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;

public class AnnotationValidator {

    @SuppressWarnings("unchecked")
    private static final List<Class<? extends Annotation>> ALL = Collections.unmodifiableList(
            Arrays.asList(
                    CommandLine.Command.class,
                    CommandLine.Option.class,
                    CommandLine.Parameters.class,
                    CommandLine.Mixin.class,
                    CommandLine.ParentCommand.class,
                    CommandLine.Spec.class,
                    CommandLine.Unmatched.class,
                    CommandLine.ArgGroup.class
            ));
    private ProcessingEnvironment processingEnv;

    public AnnotationValidator(ProcessingEnvironment processingEnv) {
        this.processingEnv = Assert.notNull(processingEnv, "processingEnv");
    }

    public void validateAnnotations(RoundEnvironment roundEnv) {
        validateNoAnnotationsOnInterfaceField(roundEnv);
        validateInvalidCombinations(roundEnv);

        // TODO
        //validateSpecFieldTypeIsCommandSpec(roundEnv);
        //validateOptionOrParametersIsNotFinalPrimitiveOrFinalString(roundEnv);
        //validateUnmatchedFieldTypeIsStringArrayOrListOfString(roundEnv);
    }

    private void validateNoAnnotationsOnInterfaceField(RoundEnvironment roundEnv) {
        for (Class<? extends Annotation> cls : ALL) {
            validateNoAnnotationsOnInterfaceField(roundEnv.getElementsAnnotatedWith(cls));
        }
    }

    private void validateNoAnnotationsOnInterfaceField(Set<? extends Element> all) {
        for (Element element : all) {
            if (element.getKind() == ElementKind.FIELD &&
                    element.getEnclosingElement().getKind() == ElementKind.INTERFACE) {
                AnnotationMirror annotationMirror = getPicocliAnnotationMirror(element);
                error(element, annotationMirror, "Invalid picocli annotation on interface field %s.%s",
                        element.getEnclosingElement().toString(), element.getSimpleName());
            }
        }
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
