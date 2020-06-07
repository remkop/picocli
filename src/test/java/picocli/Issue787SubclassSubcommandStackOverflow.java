package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

import static org.junit.Assert.*;

public class Issue787SubclassSubcommandStackOverflow {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

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
    @Test
    public void testAMISearchSubCommands() {
        try {
            new CommandLine(new ConcreteCommand()).execute("ami-words1");
            fail("Expected exception");
        } catch (CommandLine.InitializationException ex) {
            assertEquals("ami-search (picocli.Issue787SubclassSubcommandStackOverflow$ConcreteCommand) has a subcommand (picocli.Issue787SubclassSubcommandStackOverflow$Sub1ExtendConcreteCmd) that is a subclass of itself", ex.getMessage());
        }
    }

    // overflows
    @Test
    public void testAMIWords1Commands() {
        try {
            new CommandLine(new Sub1ExtendConcreteCmd()).execute();
            fail("Expected exception");
        } catch (CommandLine.InitializationException ex) {
            assertEquals("ami-search (picocli.Issue787SubclassSubcommandStackOverflow$ConcreteCommand) has a subcommand (picocli.Issue787SubclassSubcommandStackOverflow$Sub1ExtendConcreteCmd) that is a subclass of itself", ex.getMessage());
        }
    }

    // does not overflow
    @Test
    public void testAMIWords2Commands() {
        new CommandLine(new Sub2ExtendAbstractCmd()).execute();
    }

    @Command(name = "parent", subcommands = Sub.class)
    static class Parent { }

    @Command(name = "sub")
    static class Sub extends Parent {}

    @Test
    public void testSimple() {
        try {
            new CommandLine(new Parent());
            fail("Expected exception");
        } catch (CommandLine.InitializationException ok) {
            assertEquals("parent (picocli.Issue787SubclassSubcommandStackOverflow$Parent) has a subcommand (picocli.Issue787SubclassSubcommandStackOverflow$Sub) that is a subclass of itself", ok.getMessage());
        }
    }

    @Command(name = "self-ref", subcommands = Simplest.class)
    static class Simplest {}

    @Test
    public void testSimplest() {
        try {
            new CommandLine(new Simplest());
            fail("Expected exception");
        } catch (CommandLine.InitializationException ok) {
            assertEquals("self-ref (picocli.Issue787SubclassSubcommandStackOverflow$Simplest) cannot be a subcommand of itself", ok.getMessage());
        }
    }
}
