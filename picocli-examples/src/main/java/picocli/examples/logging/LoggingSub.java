package picocli.examples.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "sub", subcommands = LoggingSubSub.class)
public class LoggingSub implements Runnable {
    private static Logger logger = LogManager.getLogger();

    @Mixin Verbosity verbosity;

    @Override
    public void run() {
        logger.trace("Hi (tracing) from LoggingSub");
        logger.debug("Hi (debugging) from LoggingSub");
        logger.info("Hi (info) from LoggingSub");
        logger.warn("Hi (warning) from LoggingSub");
    }

    @Command
    void commandMethodSub(@Mixin Verbosity myVerbosity) {
        logger.trace("Hi (tracing) from commandMethodSub");
        logger.debug("Hi (debugging) from commandMethodSub");
        logger.info("Hi (info) from commandMethodSub");
        logger.warn("Hi (warning) from commandMethodSub");
    }
}
