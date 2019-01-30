package picocli.codegen.annotation.processing;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * Abstraction over annotation attributes that take a class (or array of classes) as their value.
 * For example:
 * {@code @Command(defaultValueProvider = xxx.class)}
 *
 * @since 4.0
 */
public interface ITypeMetaData {

    /**
     * Returns {@code true} if the annotated element did not have the annotation attribute.
     * @return {@code true} if the value is the default value.
     */
    boolean isDefault();

    /**
     * Returns the TypeMirror of the value.
     * @return the TypeMirror of the {@code @Command(defaultValueProvider = xxx.class)} annotation.
     */
    TypeMirror getTypeMirror();

    /**
     * Returns the {@link TypeElement} of the {@link #getTypeMirror() type mirror}.
     * @return the type mirror as a TypeElement
     */
    TypeElement getTypeElement();

}
