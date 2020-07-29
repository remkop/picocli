package picocli.codegen.annotation.processing;

import picocli.codegen.util.Assert;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the {@link Iterable} interface that provides metadata on the
 * {@code @Command(completionCandidates = xxx.class)} annotation.
 *
 * @since 4.0
 */
class CompletionCandidatesMetaData implements Iterable<String>, ITypeMetaData {

    private final TypeMirror typeMirror;

    public CompletionCandidatesMetaData(TypeMirror typeMirror) {
        this.typeMirror = Assert.notNull(typeMirror, "typeMirror");
    }

    /**
     * Returns the completion candidates from the annotations present on the specified element.
     * @param element the method or field annotated with {@code @Option} or {@code @Parameters}
     * @return the completion candidates or {@code null} if not found
     */
    public static Iterable<String> extract(Element element) {
        List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
        for (AnnotationMirror mirror : annotationMirrors) {
            DeclaredType annotationType = mirror.getAnnotationType();
            if (TypeUtil.isOption(annotationType) || TypeUtil.isParameter(annotationType)) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = mirror.getElementValues();
                for (ExecutableElement attribute : elementValues.keySet()) {
                    if ("completionCandidates".equals(attribute.getSimpleName().toString())) {
                        AnnotationValue typeMirror = elementValues.get(attribute);
                        return new CompletionCandidatesMetaData((TypeMirror) typeMirror.getValue());
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns {@code true} if the command did not have a {@code completionCandidates} annotation attribute.
     * @return {@code true} if the command did not have a {@code completionCandidates} annotation attribute.
     */
    public boolean isDefault() {
        return false;
    }

    /**
     * Returns the TypeMirror that this TypeConverterMetaData was constructed with.
     * @return the TypeMirror of the {@code @Command(completionCandidates = xxx.class)} annotation.
     */
    public TypeMirror getTypeMirror() {
        return typeMirror;
    }

    public TypeElement getTypeElement() {
        return (TypeElement) ((DeclaredType) typeMirror).asElement();
    }

    /** Always returns {@code null}. */
    @Override
    public Iterator<String> iterator() {
        throw new UnsupportedOperationException("Cannot instantiate " + typeMirror + " at compile time.");
    }

    /**
     * Returns a string representation of this object, for debugging purposes.
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return String.format("%s(%s)", getClass().getSimpleName(), isDefault() ? "default" : typeMirror);
    }

}
