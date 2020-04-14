package picocli.examples.logging_mixin_advanced;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

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
 *   Make sure that {@link LoggingMixin#configureLoggers} is called before executing any command.
 *   This can be accomplished with:
 * </p><pre>
 * public static void main(String... args) {
 *     new CommandLine(new MyApp())
 *             .setExecutionStrategy(LoggingMixin::executionStrategy))
 *             .execute(args);
 * }
 * </pre>
 */
@Command(name = "app", subcommands = LoggingSub.class)
class MyApp implements Runnable {
    static {
        LoggingMixin.initializeLog4j(); // programmatic initialization; must be done before calling LogManager.getLogger()
    }
    private static Logger logger = LogManager.getLogger(MyApp.class);

    @Mixin LoggingMixin loggingMixin;

    @Override
    public void run() {
        logger.trace("Starting... (trace) from app");
        logger.debug("Starting... (debug) from app");
        logger.info ("Starting... (info)  from app");
        logger.warn ("Starting... (warn)  from app");
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new MyApp())
                .setExecutionStrategy(LoggingMixin::executionStrategy)
                .execute(args);
        System.exit(exitCode);
    }
}
