package picocli.examples.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;

import static picocli.CommandLine.Spec.Target.MIXEE;

/**
 * This is a mixin that adds a {@code --verbose} option to a command.
 * This class will configure Log4j2, using the specified verbosity:
 * <ul>
 *   <li>{@code -vvv} : TRACE level is enabled</li>
 *   <li>{@code -vv} : DEBUG level is enabled</li>
 *   <li>{@code -v} : INFO level is enabled</li>
 *   <li>(not specified) : WARN level is enabled</li>
 * </ul>
 * <p>
 *   To add the {@code --verbose} option to a command, simply declare a {@code @Mixin}-annotated field with type {@code LoggingMixin}
 *   (if your command is a class), or a {@code @Mixin}-annotated method parameter of type {@code LoggingMixin} if your command
 *   is a {@code @Command}-annotated method.
 * </p>
 * <p>
 *   This mixin can be used on multiple commands, on any level in the command hierarchy.
 * </p>
 * <h3>Important:</h3>
 * <p>
 *   <ul>
 *       <li>The top-level command must implement the {@link IOwner} interface.</li>
 *       <li>Set an execution strategy that calls {@link #configureLoggers} before executing any command.
 *       This can be accomplished with {@code new CommandLine(...).setExecutionStrategy(LoggingMixin::executionStrategy)).execute(args)}.</li>
 *   </ul>
 * </p>
 */
public class LoggingMixin {
    private @Spec(MIXEE) CommandSpec spec; // spec of the command where the @Mixin is used

    private boolean[] verbosity = new boolean[0];

    /**
     * Sets the specified verbosity on the LoggingMixin of the top-level command.
     * @param verbosity the new verbosity value
     */
    @Option(names = {"-v", "--verbose"},
            description = {
                    "Specify multiple -v options to increase verbosity.",
                    "For example, `-v -v -v` or `-vvv`"}
    )
    public void setVerbose(boolean[] verbosity) {

        // Each subcommand that mixes in the LoggingMixin has its own instance of this class,
        // so there may be many LoggingMixin instances.
        // We want to store the verbosity state in a single, central place, so
        // we find the top-level command (which _must_ implement LoggingMixin.IOwner),
        // and store the verbosity level on that top-level command's LoggingMixin.
        //
        // In the main method, `LoggingMixin::executionStrategy` should be set as the execution strategy:
        // that will take the verbosity level that we stored in the top-level command's LoggingMixin
        // to configure Log4j2 before executing the command that the user specified.

        IOwner owner = spec.root().commandLine().getCommand();
        owner.getLoggingMixin().verbosity = verbosity;
    }

    /**
     * Returns the verbosity from the LoggingMixin of the top-level command.
     * @return the verbosity value
     */
    public boolean[] getVerbosity() {
        IOwner owner = spec.root().commandLine().getCommand();
        return owner.getLoggingMixin().verbosity;
    }

    /**
     * Configures Log4j2 based on the verbosity level of the top-level command's LoggingMixin,
     * before invoking the default execution strategy ({@link CommandLine.RunLast RunLast}) and returning the result.
     * <p>
     *   Example usage:
     * </p>
     * <pre>
     * public void main(String... args) {
     *     new CommandLine(new MyApp())
     *             .setExecutionStrategy(LoggingMixin::executionStrategy))
     *             .execute(args);
     * }
     * </pre>
     *
     * @param parseResult represents the result of parsing the command line
     * @return the exit code of executing the most specific subcommand
     */
    public static int executionStrategy(ParseResult parseResult) {
        IOwner owner = parseResult.commandSpec().root().commandLine().getCommand();
        owner.getLoggingMixin().configureLoggers();

        return new CommandLine.RunLast().execute(parseResult);
    }

    /**
     * Configures Log4j2, using the specified verbosity:
     * <ul>
     *   <li>{@code -vvv} : enable TRACE level</li>
     *   <li>{@code -vv} : enable DEBUG level</li>
     *   <li>{@code -v} : enable INFO level</li>
     *   <li>(not specified) : enable WARN level</li>
     * </ul>
     */
    public void configureLoggers() {
        configureAppender(LoggerContext.getContext(false), calcLogLevel());
    }

    private Level calcLogLevel() {
        switch (getVerbosity().length) {
            case 0:  return Level.WARN;
            case 1:  return Level.INFO;
            case 2:  return Level.DEBUG;
            default: return Level.TRACE;
        }
    }

    private void configureAppender(LoggerContext loggerContext, Level level) {
        final LoggerConfig rootConfig = loggerContext.getConfiguration().getRootLogger();
        for (Appender appender : rootConfig.getAppenders().values()) {
            if (appender instanceof ConsoleAppender) {
                rootConfig.removeAppender(appender.getName());
                rootConfig.addAppender(appender, level, null);
            }
        }
        if (rootConfig.getLevel().isMoreSpecificThan(level)) {
            rootConfig.setLevel(level);
        }
        loggerContext.updateLoggers();
    }

    // usually you would just have a log4j2.xml config file...
    // here we do a quick and dirty programmatic setup
    // IMPORTANT: The below MUST be called BEFORE any call to LogManager.getLogger() is made.
    public static LoggerContext initializeLog4j() {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setStatusLevel(Level.ERROR); // show internal log4j2 errors
        builder.setConfigurationName("QuickAndDirtySetup");
        AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE")
                .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
        appenderBuilder.add(builder.newLayout("PatternLayout")
                .addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable"));
        builder.add(appenderBuilder);
        //builder.add(builder.newLogger("org.apache.logging.log4j", Level.DEBUG)
        //        .add(builder.newAppenderRef("Stdout")).addAttribute("additivity", false));
        builder.add(builder.newRootLogger(Level.ERROR).add(builder.newAppenderRef("Stdout").addAttribute("level", Level.WARN)));
        return Configurator.initialize(builder.build());
    }

    /**
     * The top-level command must implement this interface.
     * <p>
     * If the {@code --verbose} option is specified on any subcommand,
     * {@code LoggingMixin} calls {@link #getLoggingMixin()} on the top-level command
     * and stores the specified verbosity in that {@code LoggingMixin} instance.
     * </p>
     */
    public interface IOwner {
        LoggingMixin getLoggingMixin();
    }
}
