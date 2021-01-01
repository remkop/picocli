package picocli.codegen.annotation.processing;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IDefaultValueProvider;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.codegen.util.Assert;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

/**
 * Implementation of the {@link IDefaultValueProvider} interface that provides metadata on the
 * {@code @Command(defaultValueProvider = xxx.class)} annotation.
 *
 * @since 4.0
 */
class DefaultValueProviderMetaData implements IDefaultValueProvider, ITypeMetaData {

    private final TypeMirror typeMirror;

    public DefaultValueProviderMetaData() {
        this.typeMirror = null;
    }
    public DefaultValueProviderMetaData(TypeMirror typeMirror) {
        this.typeMirror = Assert.notNull(typeMirror, "typeMirror");
    }

    /**
     * Sets the specified {@code CommandSpec}'s
     * {@linkplain CommandSpec#defaultValueProvider(CommandLine.IDefaultValueProvider) default value provider}
     * to a {@code DefaultValueProviderMetaData} instance if the annotation attribute was present on the
     * specified {@code Command} annotation.
     *
     * @param result the command spec to initialize
     * @param cmd the {@code @Command} annotation to inspect
     */
    public static void initDefaultValueProvider(CommandSpec result, Command cmd) {
        try {
            // this is a hack to get access to the TypeMirror of the default provider class
            cmd.defaultValueProvider();
        } catch (MirroredTypeException ex) {
            DefaultValueProviderMetaData provider = new DefaultValueProviderMetaData(ex.getTypeMirror());
            if (!provider.isDefault()) {
                result.defaultValueProvider(provider);
            }
        }
    }

    /**
     * Returns {@code true} if the command did not have a {@code defaultValueProvider} annotation attribute.
     * @return {@code true} if the command did not have a {@code defaultValueProvider} annotation attribute.
     */
    public boolean isDefault() {
        return typeMirror == null || "picocli.CommandLine.NoDefaultProvider".equals(getTypeElement().getQualifiedName().toString());
    }

    /**
     * Returns the TypeMirror that this DefaultValueProviderMetaData was constructed with.
     * @return the TypeMirror of the {@code @Command(defaultValueProvider = xxx.class)} annotation.
     */
    public TypeMirror getTypeMirror() {
        return typeMirror;
    }

    public TypeElement getTypeElement() {
        return (TypeElement) ((DeclaredType) typeMirror).asElement();
    }

    /** Always returns {@code null}. */
    @Override
    public String defaultValue(ArgSpec argSpec) { return null; }

    /**
     * Returns a string representation of this object, for debugging purposes.
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return String.format("%s(%s)", getClass().getSimpleName(), isDefault() ? "default" : typeMirror);
    }
}
