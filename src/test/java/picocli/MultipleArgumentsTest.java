package picocli;

import org.junit.Test;
import picocli.CommandLine.Command;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Multiple Arguments Testing based on https://picocli.info/#_multiple_values
 * for clarification based on analysis by @StaffanArvidsson
 * <p>
 * These tests demonstrate the correct picocli implementation for
 * multiple argument with split, flags, and type conversion
 * @author @madfoal
 */
public class MultipleArgumentsTest {

/** default */static final String FLAG_STRING = "-t";  // String literal for the argument flag
/** default */static final String GOOD_STRING = "good test"; // String literal for the good test
/** default */static final String BAD_STRING = "Bad Test"; // String literal for the bad test

    @Command(name = "Issue-1383")
    /**
     * Tests issue 1383 https://github.com/remkop/picocli/issues/1383
     * Based on documentation for https://picocli.info/#_repeated_options
     * <p>
     * Allows for multiple java class arguments within an array to be used with
     * a converter class
     * @author @madfoal
     */
     static class Issue1383 implements Runnable{
        @CommandLine.Option(names = FLAG_STRING,split=",", converter = TestType.TestConverter.class)
        private TestType[] args;

        public void setArgs(final TestType... args){
            this.args = args.clone();
        }

        public TestType[] getArgs() {
            return this.args.clone();
        }

        static class TestType {
            private String str; // only data field for this test type

            private TestType(final String txt) {
                str = txt;
            }

            public void setStr(final String str) {
                this.str = str;
            }

            public String getStr() {
                return this.str;
            }

            /**
             * Test Conversion for issue 1383.  Overloads convert.
             * @author madfoal
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

        @Override
        public void run() {
            // business logic
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
        assertEquals("Multiple Arguments with Type Converter Failure"
                ,GOOD_STRING
                ,obj.getArgs()[0].getStr());
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

    /**
     * Tests issue 1383 https://github.com/remkop/picocli/issues/1383
     * Based on documentation for https://picocli.info/#_repeated_options
     * <p>
     * Functionality testing for public method getArgs
     * @author madfoal
     */
    @Test
    public void testThreeGetArgs() {
        final String[] args = {FLAG_STRING, GOOD_STRING};
        final Issue1383 obj = new Issue1383();
        new CommandLine(obj).execute(args);
        assertNotEquals("Multiple Arguments with Type Converter Failure"
                ,new Issue1383.TestType(GOOD_STRING)
                ,obj.getArgs()[0]);
    }


}
