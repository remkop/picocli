package picocli.examples.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "app", subcommands = LoggingSub.class)
class MyApp implements Runnable {
    private static Logger logger = LogManager.getLogger(MyApp.class);

    @Spec CommandSpec spec;

    @Option(names = {"-v", "--verbose"},
            description = {
                    "Specify multiple -v options to increase verbosity.",
                    "For example, `-v -v -v` or `-vvv`"})
    boolean[] verbosity = new boolean[0];

    private void configureLoggers() {
        if (verbosity.length >= 3) {
            Configurator.setRootLevel(Level.TRACE);
        } else if (verbosity.length == 2) {
            Configurator.setRootLevel(Level.DEBUG);
        } else if (verbosity.length == 1) {
            Configurator.setRootLevel(Level.INFO);
        } else {
            Configurator.setRootLevel(Level.WARN);
        }
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
        // usually you would just have a log4j2.xml config file...
        // here we do a quick and dirty programmatic setup
        configureLog4j();

        CommandLine.IExecutionStrategy loggerSetup = parseResult -> {
            // during command line parsing, a new log level may have been set with -v;
            // reflect this setting first
            MyApp app = parseResult.commandSpec().commandLine().getCommand();
            app.configureLoggers();

            // then invoke the default execution strategy to run the specified command
            return new CommandLine.RunLast().execute(parseResult);
        };
        int exitCode = new CommandLine(new MyApp())
                .setExecutionStrategy(loggerSetup)
                .execute(args);
        System.exit(exitCode);
    }

    private static void configureLog4j() {
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
        builder.add(builder.newRootLogger(Level.WARN).add(builder.newAppenderRef("Stdout")));
        /*LoggerContext loggerContext = */Configurator.initialize(builder.build());
    }
}
