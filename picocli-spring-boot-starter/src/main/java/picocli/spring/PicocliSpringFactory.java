package picocli.spring;

import java.util.logging.Logger;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;

import java.util.Objects;

/**
 * PicocliSpringFactory is a {@link picocli.CommandLine.IFactory} implementation that
 * looks up classes in a given {@code org.springframework.context.ApplicationContext}.
 * This allows picocli subcommands (and other objects that are instantiated as needed)
 * to have {@code javax.inject} annotations that will be populated
 * by Spring's dependency injection framework.
 *
 * @author Thibaud Leprêtre
 * @since 4.0.0
 */
public class PicocliSpringFactory implements CommandLine.IFactory {
    private static final Logger logger = Logger.getLogger(PicocliSpringFactory.class.getName());

    private final ApplicationContext applicationContext;
    private final CommandLine.IFactory fallbackFactory;

    /**
     * Constructs a PicocliSpringFactory with the specified Application context,
     * and picocli's {@link CommandLine#defaultFactory() default factory} as the fallback factory
     * for classes not found in the application context.
     * @param applicationContext the application context to look up classes in; must be non-{@code null}
     */
    public PicocliSpringFactory(ApplicationContext applicationContext) {
        this(applicationContext, CommandLine.defaultFactory());
    }

    /**
     * Constructs a PicocliSpringFactory with the specified Application context,
     * and the specified fallback factory
     * for classes not found in the application context.
     * @param applicationContext the application context to look up classes in; must be non-{@code null}
     * @param fallbackFactory the factory used to instantiate classes that are
     *                        not found in the specified application context; must be non-{@code null}
     * @since 4.7.0
     */
    public PicocliSpringFactory(ApplicationContext applicationContext, CommandLine.IFactory fallbackFactory) {
        this.applicationContext = Objects.requireNonNull(applicationContext, "applicationContext");
        this.fallbackFactory = Objects.requireNonNull(fallbackFactory, "fallbackFactory");
    }

    @Override
    public <K> K create(Class<K> clazz) throws Exception {
        try {
            return getBeanOrCreate(clazz);
        } catch (Exception e) {
            logger.warning(String.format(
                "Unable to get bean of class %s, using fallback factory %s (%s)",
                clazz, fallbackFactory.getClass().getName(), e.toString()));
            return fallbackFactory.create(clazz);
        }
    }

    private <K> K getBeanOrCreate(Class<K> clazz) {
        try {
            return applicationContext.getBean(clazz);
        } catch (Exception e) {
            return applicationContext.getAutowireCapableBeanFactory().createBean(clazz);
        }
    }
}
