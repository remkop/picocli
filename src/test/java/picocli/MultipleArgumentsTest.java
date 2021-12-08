package picocli;

import org.junit.Test;
import picocli.CommandLine.Command;
import java.util.Locale;
import static org.junit.Assert.assertEquals;

/**
 * Multiple Arguments Testing based on https://picocli.info/#_multiple_values
 * for clarification based on analysis from issue 1383
 * https://github.com/remkop/picocli/issues/1383
 * <p>
 * These tests demonstrate the correct picocli implementation for
 * multiple argument with split, flags, and type conversion
 * @author @madfoal, @lind6
 */
public class MultipleArgumentsTest {

/** default */static final String FLAG_STRING = "-t";  // String literal for the argument flag
/** default */static final String GOOD_STRING = "good test"; // String literal for the good test
/** default */static final String BAD_STRING = "Bad Test"; // String literal for the bad test
/** default */static final String errorMessage = "Invalid value for option '-t' (<args>): "
            +"Type Conversion Failure\n" +
            "Argument: \"Bad Test\" must be lower case.\n";
    @Command(name = "Issue-1383")
    /**
     * Tests issue 1383 https://github.com/remkop/picocli/issues/1383
     * Based on documentation for https://picocli.info/#_repeated_options
     * <p>
     * Allows for multiple java class arguments within an array to be used with
     * a converter class
     * @author @madfoal, @lind6
     */
     static class Issue1383{
        @CommandLine.Option(names = FLAG_STRING,split=",", converter = TestType.TestConverter.class)
        TestType[] args;

        static class TestType {
            String str; // only data field for this test type

            private TestType(final String txt) {
                str = txt;
            }

            /**
             * Test Conversion for issue 1383.  Overloads convert.
             * @author @madfoal, @lind6
             */
            public static class TestConverter implements
                    CommandLine.ITypeConverter<Issue1383.TestType> {

                @Override
                public Issue1383.TestType convert(final String arg) throws Exception {
                    if (!arg.toLowerCase(Locale.ROOT).equals(arg)) {
                        throw new CommandLine.TypeConversionException(
                                "Type Conversion Failure\nArgument: \""
                                + arg + "\" must be lower case.\n");
                    }
                    return new Issue1383.TestType(arg);
                }
            }
        }
    }

    /**
     * Tests issue 1383 https://github.com/remkop/picocli/issues/1383
     * Based on documentation for https://picocli.info/#_repeated_options
     * <p>
     * Allows for multiple java class arguments within an array to be used with
     * a converter class
     * @author @madfoal, @lind6
     */
    @Test
    public void testOneIssue1383RepeatedOptions() {
        final String[] args = {FLAG_STRING, GOOD_STRING, FLAG_STRING, BAD_STRING};

        final Issue1383 obj = new Issue1383();
        try {
            new CommandLine(obj).parseArgs(args);
        } catch ( CommandLine.PicocliException e) {
            assertEquals("Multiple Arguments with Type Converter Failure",errorMessage,e.getMessage());
        }
    }

    /**
     * Tests issue 1383 https://github.com/remkop/picocli/issues/1383
     * Based on documentation for https://picocli.info/#_repeated_options
     * <p>
     * Allows for multiple java class arguments within an array to be used with
     * a converter class
     * @author @madfoal, @lind6
     */
    @Test
    public void testOneIssue1383SplitRegex() {
        final String[] args = {FLAG_STRING, GOOD_STRING+","+BAD_STRING};
        final Issue1383 obj = new Issue1383();
        try {
            new CommandLine(obj).parseArgs(args);
        } catch ( CommandLine.PicocliException e) {
            assertEquals("Multiple Arguments with Type Converter Failure",errorMessage,e.getMessage());
        }
    }
}
