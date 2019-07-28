package picocli.shell.jline2.example;

import java.util.Arrays;
import java.util.List;

import picocli.CommandLine;
import picocli.CommandLine.IFactory;

/**
 * <p>Can serve for {@link #create(Class)} from a list of given instances or
 * delegates to a {@link CommandLine#defaultFactory()} if no objects for class
 * available.
 * <p>Usually this would be done with 
 * <a href="https://picocli.info/#_dependency_injection">dependency injection</a>.
 * @since 4.0.1
 * @see <a href="https://picocli.info/#_dependency_injection">https://picocli.info/#_dependency_injection</a>
 */
public class CustomFactory implements IFactory {

    private final IFactory factory = CommandLine.defaultFactory();
    private final List<Object> instances;

    public CustomFactory(Object... instances) {
        this.instances = Arrays.asList(instances);
    }

    public <K> K create(Class<K> cls) throws Exception {
        for(Object obj : instances) {
            if(cls.isAssignableFrom(obj.getClass())) {
                return cls.cast(obj);
            }
        }
        return factory.create(cls);
    }
}
