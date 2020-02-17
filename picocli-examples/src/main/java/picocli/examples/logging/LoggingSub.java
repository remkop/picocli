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
        String synopsis = verbosity.spec.commandLine().getHelp().synopsis(0).trim();

        logger.trace("Hi (tracing)   from {}", synopsis);
        logger.debug("Hi (debugging) from {}", synopsis);
        logger.info("Hi (info)      from {}", synopsis);
        logger.warn("Hi (warning)   from {}", synopsis);
    }

    @Command
    void commandMethodSub(@Mixin Verbosity myVerbosity) {
        String synopsis = myVerbosity.spec.commandLine().getHelp().synopsis(0).trim();

        logger.trace("Hi (tracing)   from {}", synopsis);
        logger.debug("Hi (debugging) from {}", synopsis);
        logger.info("Hi (info)      from {}", synopsis);
        logger.warn("Hi (warning)   from {}", synopsis);
    }
}
