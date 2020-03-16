package picocli;

import org.junit.Ignore;
import org.junit.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.UnmatchedArgumentException;
import picocli.test.Execution;
import picocli.test.Supplier;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static org.junit.Assert.*;

public class UnmatchedArgumentExceptionTest {

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
                "Did you mean: checkout or help or branch?%n");
        assertEquals(expected, baos.toString());
    }
    @Test
    @SuppressWarnings("deprecation")
    public void testUnmatchedArgumentSuggestsSubcommands2() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Demo.mainCommand().parseWithHandler(((CommandLine.IParseResultHandler)null), new PrintStream(baos), new String[]{"me"});
        String expected = format("" +
                "Unmatched argument at index 0: 'me'%n" +
                "Did you mean: merge?%n");
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

}
