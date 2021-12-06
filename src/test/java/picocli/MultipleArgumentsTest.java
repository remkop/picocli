package picocli;

import org.junit.Test;
import picocli.CommandLine.Command;

import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Multiple Arguments Testing based on https://picocli.info/#_multiple_values
 * for clarification based on analysis by @StaffanArvidsson
 * <p>
 * These tests demonstrate the correct picocli implementation for
 * multiple argument with split, flags, and type conversion
 * @author madfoal
 */
public class MultipleArgumentsTest {

    // Used as string literal for the argument flag
    static final String FLAG_STRING = "-t";

    // Used as string literal for the good test
    static final String GOOD_STRING = "good test";

    // Used as string literal for the bad test
    static final String BAD_STRING = "Bad Test";

    @Command(name = "Issue-1383")
    /**
     * Tests issue 1383 https://github.com/remkop/picocli/issues/1383
     * Based on documentation for https://picocli.info/#_repeated_options
     * <p>
     * Allows for multiple java class arguments within an array to be used with
     * a converter class
     * @author madfoal
     */
     static class Issue1383{
        @CommandLine.Option(names = FLAG_STRING,split=",", converter = MyType.MyConverter.class)
        MyType[] args;
        static class MyType {
            String lowCaseStr;
            private MyType(final String txt) {
                lowCaseStr = txt;
            }
            public static class MyConverter implements CommandLine.ITypeConverter<Issue1383.MyType> {
                @Override
                public Issue1383.MyType convert(final String arg) throws Exception {
                    if (!arg.toLowerCase(Locale.ROOT).equals(arg)) {
                        throw new CommandLine.TypeConversionException("Type Conversion Failure\nArgument: \"" + arg + "\" must be lower case.\n");
                    }
                    return new Issue1383.MyType(arg);
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
     * @author madfoal
     */
    @Test
    public void testOneIssue1383RepeatedOptions() {
        final String[] args = {FLAG_STRING, GOOD_STRING, FLAG_STRING, BAD_STRING};

        final Issue1383 obj = new Issue1383();
        new CommandLine(obj).execute(args);
        assertEquals("Multiple Arguments with Type Converter Failure",GOOD_STRING,obj.args[0].lowCaseStr);
    }

    /**
     * Tests issue 1383 https://github.com/remkop/picocli/issues/1383
     * Based on documentation for https://picocli.info/#_repeated_options
     * <p>
     * Allows for multiple java class arguments within an array to be used with
     * a converter class
     * @author madfoal
     */
    @Test
    public void testOneIssue1383SplitRegex() {
        final String[] args = {FLAG_STRING, GOOD_STRING+","+BAD_STRING};
        final Issue1383 obj = new Issue1383();
        new CommandLine(obj).execute(args);
        assertNotEquals("Multiple Arguments with Type Converter Failure",null,obj);
    }
}
