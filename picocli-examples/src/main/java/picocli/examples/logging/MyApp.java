package picocli.examples.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/**
 * This class, together with {@link LoggingMixin}, demonstrates how a mixin can be used to
 * implement "global" options: options that exist on all (or almost all) commands and subcommands.
 * <p>
 *   The {@code LoggingMixin} defines the {@code --verbose} option, and knows how to configure
 *   Log4j2 to enable TRACE, DEBUG or INFO level based on the specified verbosity level.
 * </p>
 * <p>
 *   To add the {@code --verbose} option to a command, simply declare a {@code @Mixin}-annotated field with type {@code LoggingMixin}
 *   (if your command is a class), or a {@code @Mixin}-annotated method parameter of type {@code LoggingMixin} if your command
 *   is a {@code @Command}-annotated method. See {@link LoggingSub} and {@link LoggingSubSub} for examples.
 * </p>
 * <p>
 *   To use {@code LoggingMixin} in other applications than this example, make sure that:
 * </p>
 * <ul>
 *   <li>The top-level command implements the {@link LoggingMixin.IOwner} interface.</li>
 *   <li>The execution strategy calls {@link LoggingMixin#configureLoggers} before executing any command.
 *   An easy way to accomplish this is with this code:
 *   {@code new CommandLine(xxx).setExecutionStrategy(LoggingMixin::executionStrategy)).execute(args)}.</li>
 * </ul>
 */
@Command(name = "app", subcommands = LoggingSub.class)
class MyApp implements Runnable, LoggingMixin.IOwner {
    static {
        LoggingMixin.initializeLog4j(); // programmatic initialization; must be done before calling LogManager.getLogger()
    }
    private static Logger logger = LogManager.getLogger(MyApp.class);

    @Spec CommandSpec spec;

    @Mixin LoggingMixin loggingMixin;

    @Override
    public LoggingMixin getLoggingMixin() {
        return loggingMixin;
    }

    @Override
    public void run() {
        String synopsis = spec.commandLine().getHelp().synopsis(0).trim();
        logger.trace("Starting... (trace) from {}", synopsis);
        logger.debug("Starting... (debug) from {}", synopsis);
        logger.info("Starting... (info)  from {}", synopsis);
        logger.warn("Starting... (warn)  from {}", synopsis);
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new MyApp())
                .setExecutionStrategy(LoggingMixin::executionStrategy)
                .execute(args);
        System.exit(exitCode);
    }
}
