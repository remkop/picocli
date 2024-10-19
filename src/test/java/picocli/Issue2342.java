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
        new CommandLine(co)
            .setAllowOptionsAsOptionParameters(true)
            .parseArgs(args);
        List<String> expected = new ArrayList<String>(Arrays.asList(args));
        expected.remove(0);
        assertEquals(expected, co.compilerArguments);
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
            .setAllowOptionsAsOptionParameters(true)
            .setTrimQuotes(true)
            .parseArgs(args);
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
        new CommandLine(co)
            .setAllowOptionsAsOptionParameters(true)
            .parseArgs(args);
        List<String> expected = new ArrayList<String>(Arrays.asList(args));
        expected.remove(0);
        assertEquals(expected, co.compilerArguments);
    }
}
