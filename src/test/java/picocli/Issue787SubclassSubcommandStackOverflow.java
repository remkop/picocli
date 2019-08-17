package picocli;

import org.junit.Ignore;
import org.junit.Test;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

public class Issue787SubclassSubcommandStackOverflow {

    @Command(mixinStandardHelpOptions = true)
    static abstract class AbstractCommand implements Callable<Integer> {
        public Integer call() throws Exception {
            System.out.printf("Greetings from %s%n", getClass().getName());
            return 0;
        }
    }

    @Command(subcommands = { Sub1ExtendConcreteCmd.class, Sub2ExtendAbstractCmd.class },
            name = "ami-search", aliases = "search")
    static class ConcreteCommand extends AbstractCommand {
    }

    // will overflow (A)
    @Command(name = "ami-words1", aliases = "words1")
    static class Sub1ExtendConcreteCmd extends ConcreteCommand {
    }

    // no overflow (B)
    @Command(name = "ami-words2", aliases = "words2")
    static class Sub2ExtendAbstractCmd extends AbstractCommand {
    }

    // overflows
    @Ignore
    @Test
    public void testAMISearchSubCommands() {
        new CommandLine(new ConcreteCommand()).execute("ami-words1");
    }

    // overflows
    @Ignore
    @Test
    public void testAMIWords1Commands() {
        new CommandLine(new Sub1ExtendConcreteCmd()).execute();
    }

    // does not overflow
    @Test
    public void testAMIWords2Commands() {
        new CommandLine(new Sub2ExtendAbstractCmd()).execute();
    }

}
