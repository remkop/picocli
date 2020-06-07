package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

public class CommandAnnotationInheritedTest2 {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Command(subcommands = CommandLine.HelpCommand.class)
    static abstract class AbstractCommand {
    }

    static abstract class AbstractCommandSubclass extends AbstractCommand {
    }

    static class ConcreteCommand extends AbstractCommandSubclass {
    }

    @Test
    public void testNoInitializationException() {
        // if Command is @Inherited, the below line throws
        // picocli.CommandLine$InitializationException: Another subcommand named 'help' already exists for command '<main class>'
        new CommandLine(new ConcreteCommand());
    }
}
