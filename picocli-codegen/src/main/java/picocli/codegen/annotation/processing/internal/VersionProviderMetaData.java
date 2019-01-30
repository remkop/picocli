package picocli.codegen.annotation.processing.internal;

import picocli.CommandLine.IVersionProvider;
import picocli.codegen.annotation.processing.ITypeMetaData;
import picocli.codegen.util.Assert;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * Implementation of the {@link IVersionProvider} interface that provides metadata on the
 * {@code @Command(versionProvider = xxx.class)} annotation.
 *
 * @since 4.0
 */
public class VersionProviderMetaData implements IVersionProvider, ITypeMetaData {

    private final TypeMirror typeMirror;

    public VersionProviderMetaData() {
        this.typeMirror = null;
    }
    public VersionProviderMetaData(TypeMirror typeMirror) {
        this.typeMirror = Assert.notNull(typeMirror, "typeMirror");
    }

    /**
     * Returns {@code true} if the command did not have a {@code versionProvider} annotation attribute.
     * @return {@code true} if the command did not have a {@code versionProvider} annotation attribute.
     */
    public boolean isDefault() {
        return typeMirror == null || "picocli.CommandLine.NoVersionProvider".equals(getTypeElement().getQualifiedName().toString());
    }

    /**
     * Returns the TypeMirror that this VersionProviderMetaData was constructed with.
     * @return the TypeMirror of the {@code @Command(versionProvider = xxx.class)} annotation.
     */
    public TypeMirror getTypeMirror() {
        return typeMirror;
    }

    public TypeElement getTypeElement() {
        return (TypeElement) ((DeclaredType) typeMirror).asElement();
    }

    /** Always returns an empty array. */
    @Override
    public String[] getVersion() { return new String[0]; }

    /**
     * Returns a string representation of this object, for debugging purposes.
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return String.format("%s(%s)", getClass().getSimpleName(), isDefault() ? "default" : typeMirror);
    }
}
