package picocli;

import org.junit.Ignore;
import org.junit.Test;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static org.junit.Assert.*;

public class Issue2342 {
    static class CompileOptions {
        @Option(names = { "--compiler-arguments" }, split = " ",
            preprocessor = MyParamProcessor.class,
            description = "Compiler arguments to use to compile generated sources.")
        private List<String> compilerArguments;

        @Option(names = "--target", description = "Test for presence in compiler arguments")
        private String target;

    }

    static class MyParamProcessor implements CommandLine.IParameterPreprocessor {

        public boolean preprocess(Stack<String> args, CommandSpec commandSpec, ArgSpec argSpec, Map<String, Object> info) {
            List<String> copy = new ArrayList<String>();
            while (!args.isEmpty()) {
                copy.add(args.pop());
            }
            if (copy.isEmpty()) {
                return false; // let picocli generate an exception, --compiler-arguments needs some parameters
            }
            if (copy.contains("--target")) {
                // push last "--target" option and its parameter value back onto the stack
                String value = null;
                do {
                    value = copy.remove(copy.size() - 1);
                    args.push(value);
                } while (!"--target".equals(value));
            }
            // trim quotes if requested
            if (commandSpec.parser().trimQuotes()) {
                for (int i = 0; i < copy.size(); i++) {
                    String value = copy.get(i);
                    if (value.startsWith("\"") && value.endsWith("\"")
                    || value.startsWith("'") && value.endsWith("'")) {
                        value = value.substring(1, value.length() - 1);
                        copy.set(i, value);
                    }
                }
            }

            argSpec.setValue(copy);
            return true; // tell picocli that processing for --compiler-arguments is done
        }
    }

    @Ignore("I don't understand how this could be specified on the command line: " +
        "quoted values, but still somehow the shell would pass 3 separate String values " +
        "( \"--parameters,  --target and 21\" ) to the Java application?")
    @Test
    public void testArgsWithCompilerArgumentsSingleQuoteOnValue() {
        CompileOptions co = new CompileOptions();
        String[] args = new String[] {
            "--compiler-arguments",
            "'--parameters",
            "--target",
            "21'",
            "--target",
            "my-file.jar"
        };
        new CommandLine(co).parseArgs(args);

        // expect compilerArguments: "--parameters", "--target", "21"
        // expect target: "my-file.jar"
        String[] expected = new String[] {
            "--parameters", "--target", "21",
        };
        assertArrayEquals(expected, co.compilerArguments.toArray());
        assertEquals("my-file.jar", co.target);
    }

    @Ignore("I don't understand how this could be specified on the command line: " +
        "quoted values, but still somehow the shell would pass 3 separate String values " +
        "( \"--parameters,  --target and 21\" ) to the Java application?")
    @Test
    public void testArgsWithCompilerArgumentsDoubleQuoteOnValue() {
        CompileOptions co = new CompileOptions();
        String[] args = new String[] {
            "--compiler-arguments",
            "\"--parameters",
            "--target",
            "21\"",
            "--target",
            "my-file.jar"
        };
        new CommandLine(co).parseArgs(args);
        // expect compilerArguments: "--parameters", "--target", "21"
        // expect target: "my-file.jar"
        String[] expected = new String[] {
            "--parameters", "--target", "21",
        };
        assertArrayEquals(expected, co.compilerArguments.toArray());
        assertEquals("my-file.jar", co.target);
    }

    @Ignore("You really need --compiler-arguments to be included in the quoted value, and still be recognized as an option?")
    @Test
    public void testArgsWithCompilerArgumentsSingleQuote() {
        CompileOptions co = new CompileOptions();
        String[] args = new String[] {
            "'--compiler-arguments",
            "--parameters",
            "--target",
            "21'",
            "--target",
            "my-file.jar"
        };
        new CommandLine(co).parseArgs(args);
        // expect compilerArguments: "--parameters", "--target", "21"
        // expect target: "my-file.jar"
        String[] expected = new String[] {
            "--parameters", "--target", "21",
        };
        assertArrayEquals(expected, co.compilerArguments.toArray());
        assertEquals("my-file.jar", co.target);
    }

    @Ignore("You really need --compiler-arguments to be included in the quoted value, and still be recognized as an option?")
    @Test
    public void testArgsWithCompilerArgumentsDoubleQuote() {
        CompileOptions co = new CompileOptions();
        String[] args = new String[] {
            "\"--compiler-arguments",
            "--parameters",
            "--target",
            "21\"",
            "--target",
            "my-file.jar"
        };
        new CommandLine(co).parseArgs(args);
        // expect compilerArguments: "--parameters", "--target", "21"
        // expect target: "my-file.jar"
        String[] expected = new String[] {
            "--parameters", "--target", "21",
        };
        assertArrayEquals(expected, co.compilerArguments.toArray());
        assertEquals("my-file.jar", co.target);
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
            "'--a-param with space'", "--parameters", "'--release 21'", "--nowarn"
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
            "--a-param with space", "--parameters", "--release 21", "--nowarn");
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
