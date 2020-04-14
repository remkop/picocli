package picocli.examples.logging_mixin_simple;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.ParseResult;

@Command(name = "app", subcommands = Sub.class)
public class MyApp implements Runnable {
    private static Logger logger = LogManager.getLogger(MyApp.class);

    @Mixin LoggingMixin loggingMixin;

    @Override
    public void run() {
        logger.trace("Starting... (trace) from app");
        logger.debug("Starting... (debug) from app");
        logger.info ("Starting... (info)  from app");
        logger.warn ("Starting... (warn)  from app");
    }

    private Level calcLogLevel() {
        switch (loggingMixin.verbosity.length) {
            case 0:  return Level.WARN;
            case 1:  return Level.INFO;
            case 2:  return Level.DEBUG;
            default: return Level.TRACE;
        }
    }

    // A reference to this method can be used as a custom execution strategy
    // that first configures Log4j based on the specified verbosity level,
    // and then delegates to the default execution strategy.
    private int executionStrategy(ParseResult parseResult) {
        Configurator.setRootLevel(calcLogLevel()); // configure log4j
        return new CommandLine.RunLast().execute(parseResult); // default execution strategy
    }

    public static void main(String[] args) {
        MyApp app = new MyApp();
        new CommandLine(app)
                .setExecutionStrategy(app::executionStrategy)
                .execute(args);
    }
}
