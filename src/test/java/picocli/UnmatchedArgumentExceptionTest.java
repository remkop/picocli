package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.UnmatchedArgumentException;
import picocli.test.Execution;
import picocli.test.Supplier;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static java.lang.String.format;
import static org.junit.Assert.*;

public class UnmatchedArgumentExceptionTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Command
    static class Example {
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorRejectsNullList() {
        new UnmatchedArgumentException(new CommandLine(new Example()), (List<String>) null);
    }

    @Test
    public void testConstructorNonNullList() {
        List<String> args = Arrays.asList("a", "b", "c");
        UnmatchedArgumentException ex = new UnmatchedArgumentException(new CommandLine(new Example()), args);
        assertEquals(args, ex.getUnmatched());
        assertNotSame(args, ex.getUnmatched());
    }
    @Test
    @SuppressWarnings("deprecation")
    public void testUnmatchedArgumentSuggestsOptions() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine cmd = new CommandLine(new Demo.GitCommit());
        cmd.parseWithHandler(((CommandLine.IParseResultHandler)null), new PrintStream(baos), new String[]{"-fi"});
        String expected = format("" +
                "Unknown option: '-fi'%n" +
                "Possible solutions: --fixup, --file%n");
        assertEquals(expected, baos.toString());
    }
    @Test
    @SuppressWarnings("deprecation")
    public void testUnmatchedArgumentDoesNotSuggestOptionsIfNoMatch() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine cmd = new CommandLine(new Demo.GitCommit());
        cmd.parseWithHandler(((CommandLine.IParseResultHandler)null), new PrintStream(baos), new String[]{"-x"});
        String actual = baos.toString();
        assertTrue(actual, actual.startsWith("Unknown option: '-x'"));
        assertTrue(actual, actual.contains("Usage:"));
        assertFalse(actual, actual.contains("Possible solutions:"));
    }
    @Test
    @SuppressWarnings("deprecation")
    public void testUnmatchedArgumentSuggestsSubcommands() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Demo.mainCommand().parseWithHandler(((CommandLine.IParseResultHandler)null), new PrintStream(baos), new String[]{"chekcout"});
        String expected = format("" +
                "Unmatched argument at index 0: 'chekcout'%n" +
                "Did you mean: git checkout or git help or git branch?%n");
        assertEquals(expected, baos.toString());
    }
    @Test
    @SuppressWarnings("deprecation")
    public void testUnmatchedArgumentSuggestsSubcommands2() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Demo.mainCommand().parseWithHandler(((CommandLine.IParseResultHandler)null), new PrintStream(baos), new String[]{"me"});
        String expected = format("" +
                "Unmatched argument at index 0: 'me'%n" +
                "Did you mean: git merge?%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testPrintSuggestionsPrintStream() {
        CommandLine cmd = new CommandLine(new Demo.GitCommit());
        UnmatchedArgumentException ex = new UnmatchedArgumentException(cmd, Arrays.asList("-fi"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        UnmatchedArgumentException.printSuggestions(ex, new PrintStream(baos));
        String expected = format("" +
                "Possible solutions: --fixup, --file%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testPrintSuggestionsPrintStreamAutoFlushes() {
        CommandLine cmd = new CommandLine(new Demo.GitCommit());
        UnmatchedArgumentException ex = new UnmatchedArgumentException(cmd, Arrays.asList("-fi"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        UnmatchedArgumentException.printSuggestions(ex, new PrintStream(new BufferedOutputStream(baos)));
        String expected = format("" +
                "Possible solutions: --fixup, --file%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testPrintSuggestionsPrintWriter() {
        CommandLine cmd = new CommandLine(new Demo.GitCommit());
        UnmatchedArgumentException ex = new UnmatchedArgumentException(cmd, Arrays.asList("-fi"));
        StringWriter sw = new StringWriter();
        UnmatchedArgumentException.printSuggestions(ex, new PrintWriter(sw));
        String expected = format("" +
                "Possible solutions: --fixup, --file%n");
        assertEquals(expected, sw.toString());
    }

    @Test
    public void testPrintSuggestionsPrintWriterAutoFlushes() {
        CommandLine cmd = new CommandLine(new Demo.GitCommit());
        UnmatchedArgumentException ex = new UnmatchedArgumentException(cmd, Arrays.asList("-fi"));
        StringWriter sw = new StringWriter();
        UnmatchedArgumentException.printSuggestions(ex, new PrintWriter(new BufferedWriter(sw)));
        String expected = format("" +
                "Possible solutions: --fixup, --file%n");
        assertEquals(expected, sw.toString());
    }

    @Test
    public void testUnmatchedListWithNullElements() {
        CommandLine cmd = new CommandLine(new Demo.GitCommit());
        UnmatchedArgumentException ex = new UnmatchedArgumentException(cmd, Arrays.asList("-fi", null));
        StringWriter sw = new StringWriter();
        UnmatchedArgumentException.printSuggestions(ex, new PrintWriter(sw));
        String expected = format("" +
                "Possible solutions: --fixup, --file%n");
        assertEquals(expected, sw.toString());
    }

    //https://github.com/remkop/picocli/issues/887
    @Test
    public void testHiddenOptionsNotSuggested() {
        class MyApp {
            @Option(names = "--aaa", hidden = true) int a;
            @Option(names = "--apples", hidden = false) int apples;
            @Option(names = "--bbb", hidden = false) int b;
        }
        CommandLine cmd = new CommandLine(new MyApp());
        UnmatchedArgumentException ex = new UnmatchedArgumentException(cmd, Arrays.asList("--a", null));
        StringWriter sw = new StringWriter();
        UnmatchedArgumentException.printSuggestions(ex, new PrintWriter(sw));
        String expected = format("" +
                "Possible solutions: --apples%n");
        assertEquals(expected, sw.toString());
    }

    //https://github.com/remkop/picocli/issues/887
    @Test
    public void testHiddenCommandsNotSuggested() {

        @Command(name="Completion", subcommands = { picocli.AutoComplete.GenerateCompletion.class } )
        class CompletionSubcommandDemo implements Runnable {
            public void run() { }
        }
        Supplier<CommandLine> supplier = new Supplier<CommandLine>() {
            public CommandLine get() {
                CommandLine cmd = new CommandLine(new CompletionSubcommandDemo());
                CommandLine gen = cmd.getSubcommands().get("generate-completion");
                gen.getCommandSpec().usageMessage().hidden(true);
                return cmd;
            }
        };

        Execution execution = Execution.builder(supplier).execute("ge");
        execution.assertSystemErr("" +
                "Unmatched argument at index 0: 'ge'%n" +
                "Usage: Completion [COMMAND]%n");
    }


    @Test
    public void testUnmatchedExceptionStringConstructor() {
        UnmatchedArgumentException ex = new UnmatchedArgumentException(new CommandLine(CommandLine.Model.CommandSpec.create()), "aa");
        assertNotNull(ex.getUnmatched());
        assertTrue(ex.getUnmatched().isEmpty());
        assertTrue(ex.getSuggestions().isEmpty());
    }

    @Test
    public void testUnmatchedExceptionListConstructor() {
        UnmatchedArgumentException ex = new UnmatchedArgumentException(new CommandLine(CommandLine.Model.CommandSpec.create()), new ArrayList<String>());
        assertNotNull(ex.getUnmatched());
        assertTrue(ex.getUnmatched().isEmpty());
        assertTrue(ex.getSuggestions().isEmpty());

        ex = new UnmatchedArgumentException(new CommandLine(CommandLine.Model.CommandSpec.create()), Arrays.asList("a", "b"));
        assertEquals(Arrays.asList("a", "b"), ex.getUnmatched());
    }

    @Test
    public void testUnmatchedExceptionStackConstructor() {
        UnmatchedArgumentException ex = new UnmatchedArgumentException(new CommandLine(CommandLine.Model.CommandSpec.create()), new Stack<String>());
        assertNotNull(ex.getUnmatched());
        assertTrue(ex.getUnmatched().isEmpty());
        assertTrue(ex.getSuggestions().isEmpty());

        Stack<String> stack = new Stack<String>();
        stack.push("x");
        stack.push("y");
        stack.push("z");
        ex = new UnmatchedArgumentException(new CommandLine(CommandLine.Model.CommandSpec.create()), stack);
        assertEquals(Arrays.asList("z", "y", "x"), ex.getUnmatched());
    }

    @Test
    public void testUnmatchedExceptionIsUnknownOption() {
        CommandLine cmd = new CommandLine(CommandLine.Model.CommandSpec.create());

        assertFalse("unmatch list is null", new UnmatchedArgumentException(cmd, "").isUnknownOption());
        assertFalse("unmatch list is empty", new UnmatchedArgumentException(cmd, new ArrayList<String>()).isUnknownOption());

        List<String> likeAnOption = Arrays.asList("-x");
        assertTrue("first unmatched resembles option", new UnmatchedArgumentException(cmd, likeAnOption).isUnknownOption());

        List<String> unlikeOption = Arrays.asList("xxx");
        assertFalse("first unmatched doesn't resembles option", new UnmatchedArgumentException(cmd, unlikeOption).isUnknownOption());
    }
}
