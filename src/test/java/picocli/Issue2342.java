package picocli;

import org.junit.Test;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class Issue2342 {
    static class CompileOptions {
        @Option(names = { "--compiler-arguments" }, split = " ", arity = "*",
            description = "Compiler arguments to use to compile generated sources.")
        private List<String> compilerArguments;
    }

    @Test
    public void testArgsWithSpaces() {
        CompileOptions co = new CompileOptions();
        String[] args = new String[] {
            "--compiler-arguments",
            "\"--a-param with space\"",
            "--parameters",
            "\"--release 21\"",
            "--nowarn"
        };
        new CommandLine(co).parseArgs(args);
        List<String> expected = new ArrayList<String>(Arrays.asList(args));
        expected.remove(0);

        // spaces are preserved, quotes are preserved
        System.out.println(co.compilerArguments);
        assertEquals(expected, co.compilerArguments);
    }

    @Test
    public void testSingleQuotesAreNotSupported() {
        CompileOptions co = new CompileOptions();
        String[] args = new String[] {
            "--compiler-arguments",
            "'--a-param with space'",
            "--parameters",
            "'--release 21'",
            "--nowarn"
        };
        new CommandLine(co).parseArgs(args);

        String[] expected = new String[] {
            "'--a-param", "with", "space'", "--parameters", "'--release", "21'", "--nowarn"
        };
        assertArrayEquals(expected, co.compilerArguments.toArray());
    }

    @Test
    public void testArgsWithSpacesQuotesTrimmed() {
        CompileOptions co = new CompileOptions();
        String[] args = new String[] {
            "--compiler-arguments",
            "\"--a-param with space\"",
            "--parameters",
            "\"--release 21\"",
            "--nowarn"
        };
        new CommandLine(co)
            .setTrimQuotes(true)
            .parseArgs(args);

        // note: .setTrimQuotes(true)
        // results in "--a-param with space" being treated as 3 separate values
        List<String> expected = Arrays.asList(
            "--a-param", "with", "space", "--parameters", "--release", "21", "--nowarn");
        assertEquals(expected, co.compilerArguments);
    }

    @Test
    public void testArgsSeparateReleaseFrom21() {
        CompileOptions co = new CompileOptions();
        String[] args = new String[] {
            "--compiler-arguments",
            "\"--a-param with space\"",
            "--parameters",
            "--release",
            "21",
            "--nowarn"
        };
        new CommandLine(co).parseArgs(args);
        List<String> expected = new ArrayList<String>(Arrays.asList(args));
        expected.remove(0);
        assertEquals(expected, co.compilerArguments);
    }

    @Test
    public void testArgsWithOptionLikeValues() {
        CompileOptions co = new CompileOptions();
        String[] args = new String[] {
            "--compiler-arguments",
            // need to set .setAllowOptionsAsOptionParameters(true)
            // if we want an option to consume other options as parameters
            "--compiler-arguments", // <-- value that looks like an option
            "\"--a-param with space\"",
            "--parameters=--parameters",
            "\"--release 21\"",
            "--nowarn"
        };
        new CommandLine(co)
            .setAllowOptionsAsOptionParameters(true)
            .parseArgs(args);
        List<String> expected = new ArrayList<String>(Arrays.asList(args));
        expected.remove(0);
        assertEquals(expected, co.compilerArguments);
    }
}
