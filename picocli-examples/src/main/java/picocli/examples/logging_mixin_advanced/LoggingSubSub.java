package picocli.examples.logging_mixin_advanced;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "subsub")
public class LoggingSubSub implements Runnable {
    private static Logger logger = LogManager.getLogger();

    @Mixin LoggingMixin loggingMixin;

    @Override
    public void run() {
        logger.trace("Hi (tracing)   from app subsub");
        logger.debug("Hi (debugging) from app subsub");
        logger.info ("Hi (info)      from app subsub");
        logger.warn ("Hi (warning)   from app subsub");
    }

    @Command
    void subsubsubmethod(@Mixin LoggingMixin loggingMixin) {
        logger.trace("Hi (tracing)   from app subsub subsubsubmethod");
        logger.debug("Hi (debugging) from app subsub subsubsubmethod");
        logger.info ("Hi (info)      from app subsub subsubsubmethod");
        logger.warn ("Hi (warning)   from app subsub subsubsubmethod");
    }
}
