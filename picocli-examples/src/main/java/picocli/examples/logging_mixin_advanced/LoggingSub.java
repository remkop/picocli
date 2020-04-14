package picocli.examples.logging_mixin_advanced;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "sub", subcommands = LoggingSubSub.class)
public class LoggingSub implements Runnable {
    private static Logger logger = LogManager.getLogger();

    @Mixin LoggingMixin loggingMixin;

    @Override
    public void run() {
        logger.trace("Hi (tracing)   from app sub");
        logger.debug("Hi (debugging) from app sub");
        logger.info ("Hi (info)      from app sub");
        logger.warn ("Hi (warning)   from app sub");
    }

    @Command(name = "subsubmethod")
    void commandMethodSub(@Mixin LoggingMixin loggingMixin) {
        logger.trace("Hi (tracing)   from app sub subsubmethod");
        logger.debug("Hi (debugging) from app sub subsubmethod");
        logger.info ("Hi (info)      from app sub subsubmethod");
        logger.warn ("Hi (warning)   from app sub subsubmethod");
    }
}
