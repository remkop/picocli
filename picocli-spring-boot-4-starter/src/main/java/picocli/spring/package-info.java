/**
 * Provides classes and interfaces for integrating picocli with Spring Boot.
 * <p>
 * Picocli instantiates objects at initialization time, using a {@link picocli.CommandLine.IFactory}.
 * This package provides a factory implementation that looks up objects in Spring's ApplicationContext.
 * When picocli uses this factory to instantiate subcommands, type converters, and other
 * picocli components, this factory will use Spring's ApplicationContext
 * to instantiate classes that are annotated with
 * {@code @org.springframework.stereotype.Component}.
 * This ensures that {@code @javax.inject.Inject} or {@code @Autowired}-annotated
 * beans in those picocli components are injected by Spring.
 * </p>
 */
package picocli.spring;
