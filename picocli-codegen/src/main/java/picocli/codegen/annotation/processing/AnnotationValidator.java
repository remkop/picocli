package picocli.codegen.annotation.processing;

import picocli.CommandLine;
import picocli.codegen.util.Assert;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.lang.String.format;

public class AnnotationValidator {

    private static final Set<Class<? extends Annotation>> ALL = Collections.unmodifiableSet(
            new LinkedHashSet<Class<? extends Annotation>>(Arrays.asList(
                    CommandLine.Command.class,
                    CommandLine.Option.class,
                    CommandLine.Parameters.class,
                    CommandLine.Mixin.class,
                    CommandLine.ParentCommand.class,
                    CommandLine.Spec.class,
                    CommandLine.Unmatched.class,
                    CommandLine.ArgGroup.class
            ))
    );
    private ProcessingEnvironment processingEnv;

    public AnnotationValidator(ProcessingEnvironment processingEnv) {
        this.processingEnv = Assert.notNull(processingEnv, "processingEnv");
    }

    public void validateAnnotations(RoundEnvironment roundEnv) {
        validateNoAnnotationsOnInterfaceField(roundEnv);
        validateInvalidCombination(roundEnv, CommandLine.Mixin.class, CommandLine.Option.class);
        validateInvalidCombination(roundEnv, CommandLine.Mixin.class, CommandLine.Parameters.class);
        validateInvalidCombination(roundEnv, CommandLine.Mixin.class, CommandLine.Unmatched.class);
        validateInvalidCombination(roundEnv, CommandLine.Mixin.class, CommandLine.Spec.class);
        validateInvalidCombination(roundEnv, CommandLine.Unmatched.class, CommandLine.Option.class);
        validateInvalidCombination(roundEnv, CommandLine.Unmatched.class, CommandLine.Parameters.class);
        validateInvalidCombination(roundEnv, CommandLine.Spec.class, CommandLine.Option.class);
        validateInvalidCombination(roundEnv, CommandLine.Spec.class, CommandLine.Parameters.class);
        validateInvalidCombination(roundEnv, CommandLine.Spec.class, CommandLine.Unmatched.class);
        validateInvalidCombination(roundEnv, CommandLine.Option.class, CommandLine.Parameters.class);

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
                error(element, "Invalid picocli annotation on interface field %s.%s",
                        element.getEnclosingElement().toString(), element.getSimpleName());
            }
        }
    }

    private <T1 extends Annotation, T2 extends Annotation> void validateInvalidCombination(
            RoundEnvironment roundEnv, Class<T1> c1, Class<T2> c2) {
        for (Element element : roundEnv.getElementsAnnotatedWith(c1)) {
            if (element.getAnnotation(c2) != null) {
                error(element, "%s cannot have both @%s and @%s annotations",
                        element, c1.getCanonicalName(), c2.getCanonicalName());
            }
        }
    }

    void error(Element e, String msg, Object... args) {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                format(msg, args),
                e);
    }
}
