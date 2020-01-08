package picocli.codegen.annotation.processing;

import picocli.CommandLine.ITypeConverter;
import picocli.codegen.util.Assert;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the {@link ITypeConverter} interface that provides metadata on the
 * {@code @Command(typeConverter = xxx.class)} annotation.
 *
 * @since 4.0
 */
class TypeConverterMetaData implements ITypeConverter, ITypeMetaData {

    private final TypeMirror typeMirror;

    public TypeConverterMetaData(TypeMirror typeMirror) {
        this.typeMirror = Assert.notNull(typeMirror, "typeMirror");
    }

    /**
     * Returns the type converters from the annotations present on the specified element.
     * @param element the method or field annotated with {@code @Option} or {@code @Parameters}
     * @return the type converters or an empty array if not found
     */
    @SuppressWarnings("unchecked")
    public static TypeConverterMetaData[] extract(Element element) {
        List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
        for (AnnotationMirror mirror : annotationMirrors) {
            DeclaredType annotationType = mirror.getAnnotationType();
            if (TypeUtil.isOption(annotationType) || TypeUtil.isParameter(annotationType)) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = mirror.getElementValues();
                for (ExecutableElement attribute : elementValues.keySet()) {
                    if ("converter".equals(attribute.getSimpleName().toString())) {
                        AnnotationValue list = elementValues.get(attribute);
                        List<AnnotationValue> typeMirrors = (List<AnnotationValue>) list.getValue();
                        List<TypeConverterMetaData> result = new ArrayList<TypeConverterMetaData>();
                        for (AnnotationValue annotationValue : typeMirrors) {
                            result.add(new TypeConverterMetaData((TypeMirror) annotationValue.getValue()));
                        }
                        return result.toArray(new TypeConverterMetaData[0]);
                    }
                }
            }
        }
        return new TypeConverterMetaData[0];
    }

    /**
     * Returns {@code true} if the command did not have a {@code typeConverter} annotation attribute.
     * @return {@code true} if the command did not have a {@code typeConverter} annotation attribute.
     */
    public boolean isDefault() {
        return false;
    }

    /**
     * Returns the TypeMirror that this TypeConverterMetaData was constructed with.
     * @return the TypeMirror of the {@code @Command(typeConverter = xxx.class)} annotation.
     */
    public TypeMirror getTypeMirror() {
        return typeMirror;
    }

    public TypeElement getTypeElement() {
        return (TypeElement) ((DeclaredType) typeMirror).asElement();
    }

    /** Always returns {@code null}. */
    @Override
    public Object convert(String value) { return null; }

    /**
     * Returns a string representation of this object, for debugging purposes.
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return String.format("%s(%s)", getClass().getSimpleName(), isDefault() ? "default" : typeMirror);
    }
}
