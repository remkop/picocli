package picocli.codegen.annotation.processing;

import picocli.CommandLine.Model.IGetter;
import picocli.CommandLine.Model.ISetter;

import javax.lang.model.element.Element;

/**
 * Implementation of the {@link IGetter} and {@link ISetter} interface that allows
 * custom {@code CommandSpec} annotation processors to inspect {@code ArgSpec} objects
 * to discover what program element was annotated with {@code @Option} or {@code @Parameters}.
 *
 * @since 4.0
 */
public class AnnotatedElementHolder implements IGetter, ISetter {

    private final Element element;

    /**
     * Constructs a new {@code AnnotatedElementHolder} with the specified element
     * @param element the program element annotated with {@code @Option} or {@code @Parameters}
     */
    public AnnotatedElementHolder(Element element) {
        this.element = element;
    }

    /**
     * Returns the program element annotated with {@code @Option} or {@code @Parameters}.
     * @return the program element for an {@code ArgSpec}.
     */
    public Element getElement() {
        return element;
    }

    /**
     * This implementation does nothing and always returns {@code null}.
     * @param <T> ignored
     * @return {@code null} always
     */
    @Override
    public <T> T get() {
        return null;
    }

    /**
     * This implementation does nothing.
     * @param value the new value of the option or positional parameter. Ignored.
     * @param <T> ignored
     * @return {@code null} always
     */
    @Override
    public <T> T set(T value) {
        return null;
    }

    /**
     * Returns a string representation of this binding, for debugging purposes.
     * @return a string representation of this binding
     */
    @Override
    public String toString() {
        return String.format("%s(%s %s in %s)", getClass().getSimpleName(), element.getKind(), element, element.getEnclosingElement());
    }
}
