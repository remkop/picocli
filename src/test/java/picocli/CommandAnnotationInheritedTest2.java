package picocli;

import org.junit.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

public class CommandAnnotationInheritedTest2 {

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
