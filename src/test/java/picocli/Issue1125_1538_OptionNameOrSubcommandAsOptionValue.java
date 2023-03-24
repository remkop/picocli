package picocli;

import java.io.File;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Callable;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.IParameterPreprocessor;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

import static org.junit.Assert.*;

public class Issue1125_1538_OptionNameOrSubcommandAsOptionValue {

    @Rule
    public SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

    @Command(name = "mycommand")
    static class MyCommand implements Callable<Integer> {

        @Option(names = "-output", preprocessor = MyPreprocessor.class)
        File output = new File(".");

        static class MyPreprocessor implements IParameterPreprocessor {
            public boolean preprocess(Stack<String> args, CommandSpec commandSpec, ArgSpec argSpec, Map<String, Object> info) {
                if (args.isEmpty()) {
                    throw new ParameterException(commandSpec.commandLine(), "Error: option '-output' requires a parameter");
                }
                String arg = args.pop();
                //System.err.printf("Setting %s to '%s'%n", argSpec, arg);
                argSpec.setValue(new File(arg));
                return true;
            }
        }

        @Option(names = "-option")
        File option = new File(".");

        @Option(names = "-x") String x;
        @Option(names = "-y") String y;

        public Integer call() {
            return 11;
        }

        @Command(name = "mySubcommand")
        public int mySubcommand() {
            return 13;
        }
    }

    @Test
    public void testSubcommandAsOptionNameWithCustomPreprocessor() {
        MyCommand obj = new MyCommand();
        CommandLine cmdLine = new CommandLine(obj);
        int exitCode = cmdLine.execute("-output", "abc", "mySubcommand");
        assertEquals(13, exitCode);
        assertEquals("abc", obj.output.getName());

        exitCode = cmdLine.execute("-output=mySubcommand", "mySubcommand");
        assertEquals(13, exitCode);
        assertEquals("mySubcommand", obj.output.getName());

        exitCode = cmdLine.execute("-output", "mySubcommand", "mySubcommand");
        assertEquals(13, exitCode);
        assertEquals("mySubcommand", obj.output.getName());
    }

    @Test
    public void testSubcommandAsOptionNameMaybeEnabled() {
        MyCommand obj = new MyCommand();
        CommandLine cmdLine = new CommandLine(obj).setAllowSubcommandsAsOptionParameters(true);
        int exitCode = cmdLine.execute("-option", "abc", "mySubcommand");
        assertEquals(13, exitCode);
        assertEquals("abc", obj.option.getName());

        exitCode = cmdLine.execute("-option=mySubcommand", "mySubcommand");
        assertEquals(13, exitCode);
        assertEquals("mySubcommand", obj.option.getName());

        exitCode = cmdLine.execute("-option", "mySubcommand", "mySubcommand");
        assertEquals(13, exitCode);
        assertEquals("mySubcommand", obj.option.getName());
    }

    @Test
    public void testSubcommandAsOptionNameDefault() {
        MyCommand obj = new MyCommand();
        CommandLine cmdLine = new CommandLine(obj);
        int exitCode = cmdLine.execute("-option", "abc", "mySubcommand");
        assertEquals(13, exitCode);
        assertEquals("abc", obj.option.getName());

        try {
            cmdLine.parseArgs("-option=mySubcommand", "mySubcommand");
            fail("expected exception");
        } catch (CommandLine.MissingParameterException ex) {
            assertEquals("Expected parameter for option '-option' but found 'mySubcommand'", ex.getMessage());
        }
    }

    @Test
    public void testAmbiguousOptionsDefault() {
        //-x -y=123
        MyCommand obj = new MyCommand();
        CommandLine cmdLine = new CommandLine(obj);
        // Clear the globally cached jansiInstalled value that might
        // have been set in a previous test to force the
        // Ansi#isJansiConsoleInstalled method to recalculate
        // the cached value.
        Ansi.jansiInstalled = null;
        int exitCode = cmdLine.execute("-x", "-y=123");
        assertEquals(2, exitCode);
        String expected = String.format("" +
            "Expected parameter for option '-x' but found '-y=123'%n" +
            "Usage: mycommand [-option=<option>] [-output=<output>] [-x=<x>] [-y=<y>]%n" +
            "                 [COMMAND]%n" +
            "      -option=<option>%n" +
            "      -output=<output>%n" +
            "  -x=<x>%n" +
            "  -y=<y>%n" +
            "Commands:%n" +
            "  mySubcommand%n");
        assertEquals(expected, systemErrRule.getLog());
    }

    @Test
    public void testAmbiguousOptionsWithOptionsAsOptionParametersEnabled() {
        //-x -y=123
        MyCommand obj = new MyCommand();
        CommandLine cmdLine = new CommandLine(obj).setAllowOptionsAsOptionParameters(true);
        int exitCode = cmdLine.execute("-x", "-y=123");
        assertEquals(11, exitCode);
        assertEquals("-y=123", obj.x);
        assertNull(obj.y);
    }
}
