package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import static org.junit.Assert.*;


public class Issue776ArgGroupsIgnoredInMixinTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Command(name = "test",
            description = "Picocli test.",
            abbreviateSynopsis = true,
            sortOptions = false,
            usageHelpAutoWidth = true)
    static class TestCommand {

        @Mixin
        TestConfig testConfig = new TestConfig();

        private static class TestConfig {
            @ArgGroup(validate = false, heading = "A1%n")
            private final A1 a1 = new A1();

            private static class A1 {
                @Option(names = {"-ta1", "--test-a1"})
                private String testA1;
                @Option(names = {"-ta2", "--test-a2"})
                private String testA2;
            }

            @ArgGroup(validate = false, heading = "A2%n")
            private final A2 a2 = new A2();

            private static class A2 {
                @Option(names = {"-tb1", "--test-b1"})
                private String testB1;
                @Option(names = {"-tb2", "--test-b2"})
                private String testB2;
            }
        }
    }

    @Test
    public void testUsage() {
        String expected = String.format("" +
                "Usage: test [OPTIONS]%n" +
                "Picocli test.%n" +
                "A1%n" +
                "      -ta1, --test-a1=<testA1>%n" +
                "%n" +
                "      -ta2, --test-a2=<testA2>%n" +
                "%n" +
                "A2%n" +
                "      -tb1, --test-b1=<testB1>%n" +
                "%n" +
                "      -tb2, --test-b2=<testB2>%n" +
                "%n");
        String actual = new CommandLine(new TestCommand()).getUsageMessage(CommandLine.Help.Ansi.OFF);

        assertEquals(expected, actual);
    }
}
