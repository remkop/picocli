package picocli.codegen.annotation.processing.internal;

import picocli.CommandLine.ITypeConverter;
import picocli.codegen.annotation.processing.ITypeMetaData;
import picocli.codegen.util.Assert;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * Implementation of the {@link ITypeConverter} interface that provides metadata on the
 * {@code @Command(typeConverter = xxx.class)} annotation.
 *
 * @since 4.0
 */
public class TypeConverterMetaData implements ITypeConverter, ITypeMetaData {

    private final TypeMirror typeMirror;

    public TypeConverterMetaData(TypeMirror typeMirror) {
        this.typeMirror = Assert.notNull(typeMirror, "typeMirror");
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
