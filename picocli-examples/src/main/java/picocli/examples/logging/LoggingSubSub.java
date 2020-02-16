package picocli.examples.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "subsub")
public class LoggingSubSub implements Runnable {
    private static Logger logger = LogManager.getLogger();

    @Mixin
    Verbosity verbosity;

    @Override
    public void run() {
        logger.trace("Hi (tracing) from LoggingSubSub");
        logger.debug("Hi (debugging) from LoggingSubSub");
        logger.info("Hi (info) from LoggingSubSub");
        logger.warn("Hi (warning) from LoggingSubSub");
    }

    @Command
    void commandMethodSub(@Mixin Verbosity myVerbosity) {
        logger.trace("Hi (tracing) from subsub.commandMethodSub");
        logger.debug("Hi (debugging) from subsub.commandMethodSub");
        logger.info("Hi (info) from subsub.commandMethodSub");
        logger.warn("Hi (warning) from subsub.commandMethodSub");
    }
}
