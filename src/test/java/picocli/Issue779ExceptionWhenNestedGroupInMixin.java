package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import static org.junit.Assert.*;

/**
 * DuplicateOptionAnnotationsException when a nested group is defined inside a mixin.
 * https://github.com/remkop/picocli/issues/779
 */
public class Issue779ExceptionWhenNestedGroupInMixin {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Command(name = "test-with-mixin-and-nested-group",
            description = "Picocli section test with mixin and nested group.",
            abbreviateSynopsis = true,
            sortOptions = false)
    public static class TestCommandWithMixin {
        @Mixin
        TestConfig testConfig = new TestConfig();

        private static class TestConfig {
            @ArgGroup(validate = false, heading = "A1%n")
            private final A1 a1 = new A1();

            private static class A1 {
                @ArgGroup(exclusive = true, heading = "NestedA1%n")
                private final NestedA1 nestedA1 = new NestedA1();

                private static class NestedA1 {
                    @Option(names = {"-ta1", "--test-a1"})
                    private String testA1;
                    @Option(names = {"-ta2", "--test-a2"})
                    private String testA2;
                }
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
                "Usage: test-with-mixin-and-nested-group [OPTIONS]%n" +
                "Picocli section test with mixin and nested group.%n" +
                "NestedA1%n" +
                "      -ta1, --test-a1=<testA1>%n" +
                "%n" +
                "      -ta2, --test-a2=<testA2>%n" +
                "%n" +
                "A1%n" +
                "A2%n" +
                "      -tb1, --test-b1=<testB1>%n" +
                "%n" +
                "      -tb2, --test-b2=<testB2>%n" +
                "%n");
        String actual = new CommandLine(new TestCommandWithMixin()).getUsageMessage(CommandLine.Help.Ansi.OFF);

        assertEquals(expected, actual);
    }
}
