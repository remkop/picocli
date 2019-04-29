package picocli;

import org.junit.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.UnmatchedArgumentException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
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
                "Unknown option: -fi%n" +
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
        assertTrue(actual, actual.startsWith("Unknown option: -x"));
        assertTrue(actual, actual.contains("Usage:"));
        assertFalse(actual, actual.contains("Possible solutions:"));
    }
    @Test
    @SuppressWarnings("deprecation")
    public void testUnmatchedArgumentSuggestsSubcommands() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Demo.mainCommand().parseWithHandler(((CommandLine.IParseResultHandler)null), new PrintStream(baos), new String[]{"chekcout"});
        String expected = format("" +
                "Unmatched argument: chekcout%n" +
                "Did you mean: checkout or help or branch?%n");
        assertEquals(expected, baos.toString());
    }
    @Test
    @SuppressWarnings("deprecation")
    public void testUnmatchedArgumentSuggestsSubcommands2() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Demo.mainCommand().parseWithHandler(((CommandLine.IParseResultHandler)null), new PrintStream(baos), new String[]{"me"});
        String expected = format("" +
                "Unmatched argument: me%n" +
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
}
