/*
   Copyright 2017 Remko Popma

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package picocli;

import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.ParseResult;

import static org.junit.Assert.*;
import static picocli.CommandLine.*;

public class CommandLineParseResultTest {
    @Test
    public void testCommandSpec_IsCommandLineCommandSpec() {
        class App {
            @Parameters String[] positional;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult result = cmd.parseArgs("a", "b");
        assertSame(cmd.getCommandSpec(), result.commandSpec());
    }
    @Test
    public void testBasicUsage() {
        class App {
            @Option(names = {"-t", "-ttt"}) boolean boolVal;
            @Option(names = {"-i", "-int"}) int intVal;
            @Parameters String[] positional;
        }
        ParseResult result = new CommandLine(new App()).parseArgs("-t", "-i", "1", "a", "b");
        assertEquals(Arrays.asList("-t", "-i", "1", "a", "b"), result.originalArgs());

        assertTrue(result.unmatched().isEmpty());
        assertFalse(result.hasSubcommand());
        assertFalse(result.isUsageHelpRequested());
        assertFalse(result.isVersionHelpRequested());

        assertTrue(result.hasMatchedOption("-ttt"));
        assertTrue(result.hasMatchedOption("-t"));
        assertTrue(result.hasMatchedOption("-i"));
        assertTrue(result.hasMatchedOption("-int"));
        assertFalse(result.hasMatchedOption("-unknown"));

        assertTrue(result.hasMatchedPositional(0));
        assertTrue(result.hasMatchedPositional(1));
    }
    @Test
    public void testMultipleOverlappingPositionals() {
        class App {
            @Parameters String[] all;
            @Parameters(index = "0..1") String[] zeroOne;
            @Parameters(index = "1..*") String[] oneAndUp;
        }
        String[] args = {"a", "b", "c", "d", "e"};
        ParseResult result = new CommandLine(new App()).parseArgs(args);
        assertEquals(Arrays.asList(args), result.originalArgs());

        assertTrue(result.unmatched().isEmpty());
        assertFalse(result.hasSubcommand());
        assertFalse(result.isUsageHelpRequested());
        assertFalse(result.isVersionHelpRequested());

        assertEquals(Collections.emptyList(), result.matchedOptions());
        assertEquals(3, result.matchedPositionals().size());
        assertEquals(Range.valueOf("0..1"), result.matchedPositionals().get(0).index());
        assertEquals(Range.valueOf("0..*"), result.matchedPositionals().get(1).index());
        assertEquals(Range.valueOf("1..*"), result.matchedPositionals().get(2).index());

        assertArrayEquals(args, (String[]) result.matchedPositionals().get(1).getValue());
        assertArrayEquals(new String[]{"a", "b"}, (String[]) result.matchedPositionals().get(0).getValue());
        assertArrayEquals(new String[]{"b", "c", "d", "e"}, (String[]) result.matchedPositionals().get(2).getValue());

        for (int i = 0; i < args.length; i++) {
            assertTrue(result.hasMatchedPositional(i));
            assertEquals(args[i], result.rawPositionalValue(i));
        }
        assertFalse(result.hasMatchedPositional(args.length));
    }

    @Test
    public void testOriginalArgsForSubcommands() {
        class App {
            @Option(names = "-x") String x;
        }
        class Sub {
            @Parameters String[] all;
        }
        CommandLine cmd = new CommandLine(new App());
        cmd.addSubcommand("sub", new Sub());
        ParseResult parseResult = cmd.parseArgs("-x", "xval", "sub", "1", "2", "3");
        assertEquals(Arrays.asList("-x", "xval", "sub", "1", "2", "3"), parseResult.originalArgs());

        assertTrue(parseResult.hasMatchedOption("-x"));
        assertEquals("xval", parseResult.rawOptionValue("-x"));
        assertEquals("xval", parseResult.matchedOptionValue("-x", "xval"));
        assertFalse(parseResult.hasMatchedPositional(0));

        assertTrue(parseResult.hasSubcommand());
        ParseResult subResult = parseResult.subcommand();
        assertEquals(Arrays.asList("-x", "xval", "sub", "1", "2", "3"), subResult.originalArgs()); // TODO should subresult.originalArgs include the args consumed by the parent?

        assertTrue(subResult.hasMatchedPositional(0));
        assertTrue(subResult.hasMatchedPositional(1));
        assertTrue(subResult.hasMatchedPositional(2));
        assertFalse(subResult.hasMatchedPositional(3));
        assertEquals("1", subResult.rawPositionalValue(0));
        assertEquals("2", subResult.rawPositionalValue(1));
        assertEquals("3", subResult.rawPositionalValue(2));
    }

    @Test
    public void testHasMatchedPositionalByPositionalSpec() {
        class App {
            @Option(names = "-x") String x;
            @Parameters(index = "0", arity = "0..1") int index0 = -1;
            @Parameters(index = "1", arity = "0..1") int index1 = -1;
            @Parameters(index = "2", arity = "0..1") int index2 = -1;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x", "xval", "0", "1");

        List<PositionalParamSpec> all = cmd.getCommandSpec().positionalParameters();
        assertTrue(parseResult.hasMatchedPositional(all.get(0)));
        assertTrue(parseResult.hasMatchedPositional(all.get(1)));
        assertFalse(parseResult.hasMatchedPositional(all.get(2)));
    }

    @Test
    public void testMatchedPositionals_ReturnsOnlyMatchedPositionals() {
        class App {
            @Option(names = "-x") String x;
            @Parameters(index = "0", arity = "0..1") int index0 = -1;
            @Parameters(index = "1", arity = "0..1") int index1 = -1;
            @Parameters(index = "2", arity = "0..1") int index2 = -1;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x", "xval", "0", "1");

        List<PositionalParamSpec> all = cmd.getCommandSpec().positionalParameters();
        assertEquals(3, all.size());

        List<PositionalParamSpec> found = parseResult.matchedPositionals();
        assertEquals(2, found.size());
        assertSame(all.get(0), found.get(0));
        assertSame(all.get(1), found.get(1));

        assertSame(parseResult.matchedPositional(0), found.get(0));
        assertSame(parseResult.matchedPositional(1), found.get(1));
    }

    @Test
    public void testMatchedPositional_ReturnsNullForNonMatchedPosition() {
        class App {
            @Parameters(index = "0", arity = "0..1") int index0 = -1;
            @Parameters(index = "1", arity = "0..1") int index1 = -1;
            @Parameters(index = "2", arity = "0..1") int index2 = -1;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("0", "1");

        assertNotNull(parseResult.matchedPositional(0));
        assertNotNull(parseResult.matchedPositional(1));

        assertNull(parseResult.matchedPositional(2));
        assertNull(parseResult.matchedPositional(3));
    }

    @Test
    public void testRawPositionalValue_ReturnsNullForNonMatchedPosition() {
        class App {
            @Parameters(index = "0", arity = "0..1") int index0 = -1;
            @Parameters(index = "1", arity = "0..1") int index1 = -1;
            @Parameters(index = "2", arity = "0..1") int index2 = -1;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("0", "1");

        assertEquals("0", parseResult.rawPositionalValue(0));
        assertEquals("1", parseResult.rawPositionalValue(1));

        assertNull(parseResult.rawPositionalValue(2));
        assertNull(parseResult.rawPositionalValue(3));
    }

    @Test
    public void testRawPositionalValueWithDefault_ReturnsDefaultForNonMatchedPosition() {
        class App {
            @Parameters(index = "0", arity = "0..1") int index0 = -1;
            @Parameters(index = "1", arity = "0..1") int index1 = -1;
            @Parameters(index = "2", arity = "0..1") int index2 = -1;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("0", "1");

        assertEquals("0", parseResult.rawPositionalValue(0, "abc"));
        assertEquals("1", parseResult.rawPositionalValue(1, "def"));

        assertEquals("ghi", parseResult.rawPositionalValue(2, "ghi"));
        assertEquals("xyz", parseResult.rawPositionalValue(3, "xyz"));
    }

    @Test
    public void testPositionalValue() {
        class App {
            @Parameters(index = "0", arity = "0..1") int index0 = -1;
            @Parameters(index = "1", arity = "0..1") int index1 = -1;
            @Parameters(index = "2", arity = "0..1") int index2 = -1;
        }
        App app = new App();
        CommandLine cmd = new CommandLine(app);
        ParseResult parseResult = cmd.parseArgs("0", "1");
        assertEquals( 0, app.index0);
        assertEquals( 1, app.index1);
        assertEquals(-1, app.index2);

        List<PositionalParamSpec> found = parseResult.matchedPositionals();
        assertEquals(2, found.size());
        assertEquals(Integer.valueOf(0), parseResult.matchedPositionalValue(0, 0));
        assertEquals(Integer.valueOf(1), parseResult.matchedPositionalValue(1, 1));
        assertNull(parseResult.matchedPositionalValue(2, null));

        List<PositionalParamSpec> all = cmd.getCommandSpec().positionalParameters();
        assertEquals(3, all.size());
        assertEquals(Integer.valueOf(0), parseResult.matchedPositionalValue(all.get(0), 0));
        assertEquals(Integer.valueOf(1), parseResult.matchedPositionalValue(all.get(1), 1));
        assertEquals(Integer.valueOf(-1), parseResult.matchedPositionalValue(all.get(2), -1));
        assertNull(parseResult.matchedPositionalValue(null, null));
    }

    @Test
    public void testIsUsageHelpRequested() {
        @Command(mixinStandardHelpOptions = true)
        class App {
            @Option(names = "-x") String x;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-h");
        assertTrue(parseResult.isUsageHelpRequested());
        assertFalse(parseResult.isVersionHelpRequested());

        assertSame(cmd.getCommandSpec().optionsMap().get("-h"), parseResult.matchedOption('h'));

        assertTrue(parseResult.unmatched().isEmpty());
        assertTrue(parseResult.matchedPositionals().isEmpty());
    }

    @Test
    public void testIsVersionHelpRequested() {
        @Command(mixinStandardHelpOptions = true)
        class App {
            @Option(names = "-x") String x;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("--version");
        assertFalse(parseResult.isUsageHelpRequested());
        assertTrue(parseResult.isVersionHelpRequested());

        assertSame(cmd.getCommandSpec().optionsMap().get("--version"), parseResult.matchedOption('V'));
    }

    @Test
    public void testMatchedOptions_ReturnsOnlyMatchedOptions() {
        class App {
            @Option(names = "-a", arity = "0..1") String a;
            @Option(names = "-b", arity = "0..1") String b;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-a");

        List<OptionSpec> options = parseResult.matchedOptions();
        assertEquals(1, options.size());

        Map<String, OptionSpec> optionsMap = cmd.getCommandSpec().optionsMap();
        assertTrue(parseResult.hasMatchedOption(optionsMap.get("-a")));
        assertFalse(parseResult.hasMatchedOption(optionsMap.get("-b")));
    }

    @Test
    public void testMatchedOption_ReturnsOnlyMatchedOptions() {
        class App {
            @Option(names = "-a", arity = "0..1") String a;
            @Option(names = "-b", arity = "0..1") String b;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-a");

        assertNotNull(parseResult.matchedOption('a'));
        assertNotNull(parseResult.matchedOption("a"));
        assertNotNull(parseResult.matchedOption("-a"));

        assertNull(parseResult.matchedOption('b'));
        assertNull(parseResult.matchedOption("b"));
        assertNull(parseResult.matchedOption("-b"));
    }
    @Test
    public void testRawOptionValueForBooleanOptions_ReturnsStringTrue() {
        CommandSpec spec = CommandSpec.create();
        spec.addOption(OptionSpec.builder("-V", "--verbose").build());
        CommandLine commandLine = new CommandLine(spec);

        ParseResult pr = commandLine.parseArgs("--verbose");

        assertTrue(pr.hasMatchedOption("--verbose")); // as specified on command line
        assertTrue(pr.hasMatchedOption('V'));     // single-character alias works too
        assertTrue(pr.hasMatchedOption("verbose"));   // command name without hyphens

        assertTrue(pr.matchedOptionValue("verbose", Boolean.FALSE));
        assertEquals("true", pr.rawOptionValue("verbose"));
    }

    @Test
    public void testHasMatchedOptionByOptionSpec() {
        class App {
            @Option(names = "-x", arity = "0..1") String x;
            @Option(names = "-y", arity = "0..1") String y;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x");

        Map<String, OptionSpec> optionsMap = cmd.getCommandSpec().optionsMap();
        assertTrue(parseResult.hasMatchedOption(optionsMap.get("-x")));
        assertFalse(parseResult.hasMatchedOption(optionsMap.get("-y")));
    }

    @Test
    public void testHasMatchedOptionByShortName() {
        class App {
            @Option(names = "-x") String[] x;
            @Option(names = "-y") String y;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x", "value1", "-x", "value2");
        assertTrue(parseResult.hasMatchedOption('x'));
        assertFalse(parseResult.hasMatchedOption('y'));
    }

    @Test
    public void testHasMatchedOptionByName_VariousPrefixes() {
        class App {
            @Option(names = {"-x", "++XX", "/XXX"}) String[] x;
            @Option(names = "-y") String y;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x", "value1", "-x", "value2");
        assertTrue(parseResult.hasMatchedOption("x"));
        assertTrue(parseResult.hasMatchedOption("-x"));
        assertTrue(parseResult.hasMatchedOption("XX"));
        assertTrue(parseResult.hasMatchedOption("++XX"));
        assertTrue(parseResult.hasMatchedOption("XXX"));
        assertTrue(parseResult.hasMatchedOption("/XXX"));

        assertFalse(parseResult.hasMatchedOption("y"));
        assertFalse(parseResult.hasMatchedOption("-y"));
    }

    @Test
    public void testMatchedOption_returnsOnlyMatchedOptions() {
        class App {
            @Option(names = {"-x", "++XX", "/XXX"}) String[] x;
            @Option(names = "-y") String y;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x", "value1", "-x", "value2");
        OptionSpec x = cmd.getCommandSpec().posixOptionsMap().get('x');
        assertSame(x, parseResult.matchedOption('x'));
        assertSame(x, parseResult.matchedOption("x"));
        assertSame(x, parseResult.matchedOption("-x"));
        assertSame(x, parseResult.matchedOption("XX"));
        assertSame(x, parseResult.matchedOption("++XX"));
        assertSame(x, parseResult.matchedOption("XXX"));
        assertSame(x, parseResult.matchedOption("/XXX"));
    }

    @Test
    public void testMatchedOption_returnsNullForNonMatchedOption() {
        class App {
            @Option(names = {"-x", "++XX", "/XXX"}) String[] x;
            @Option(names = "-y") String y;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x", "value1", "-x", "value2");
        assertNull(parseResult.matchedOption('y'));
        assertNull(parseResult.matchedOption("y"));
    }

    @Test
    public void testRawOptionValue() {
        class App {
            @Option(names = {"-x", "++XX", "/XXX"}) String[] x;
            @Option(names = "-y") String y;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x", "value1", "-x", "value2");
        assertEquals("value1", parseResult.rawOptionValue("x"));
        assertEquals("value1", parseResult.rawOptionValue("-x"));
        assertEquals("value1", parseResult.rawOptionValue("XX"));
        assertEquals("value1", parseResult.rawOptionValue("++XX"));
        assertEquals("value1", parseResult.rawOptionValue("XXX"));
        assertEquals("value1", parseResult.rawOptionValue("/XXX"));

        assertEquals(null, parseResult.rawOptionValue("y"));
        assertEquals(null, parseResult.rawOptionValue("-y"));
    }

    @Test
    public void testRawOptionValue_ByOptionSpec() {
        class App {
            @Option(names = {"-x", "++XX", "/XXX"}) String[] x;
            @Option(names = "-y") String y;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x", "value1", "-x", "value2");

        OptionSpec x = cmd.getCommandSpec().optionsMap().get("-x");
        OptionSpec y = cmd.getCommandSpec().optionsMap().get("-y");

        assertEquals("value1", parseResult.rawOptionValue(x));

        assertEquals(null, parseResult.rawOptionValue(y));
        assertEquals(null, parseResult.rawOptionValue((OptionSpec) null));
    }

    @Test
    public void testRawOptionValue_ByShortName() {
        class App {
            @Option(names = {"-x", "++XX", "/XXX"}) String[] x;
            @Option(names = "-y") String y;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x", "value1", "-x", "value2");
        assertEquals("value1", parseResult.rawOptionValue('x'));
        assertEquals(null, parseResult.rawOptionValue('y'));
    }

    @Test
    public void testRawOptionValueWithDefault() {
        class App {
            @Option(names = {"-x", "++XX", "/XXX"}) String[] x;
            @Option(names = "-y") String y;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x", "value1", "-x", "value2");

        String defaultValue = "DEFAULTVAL";
        assertEquals(defaultValue, parseResult.rawOptionValue("y", defaultValue));
        assertEquals(defaultValue, parseResult.rawOptionValue("-y", defaultValue));

        assertEquals(defaultValue, parseResult.rawOptionValue("z", defaultValue));
        assertEquals(defaultValue, parseResult.rawOptionValue("--non-existing", defaultValue));
    }

    @Test
    public void testRawOptionValueWithDefault_ByShortName() {
        class App {
            @Option(names = {"-x", "++XX", "/XXX"}) String[] x;
            @Option(names = "-y") String y;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x", "value1", "-x", "value2");

        String defaultValue = "DEFAULTVAL";
        assertEquals(defaultValue, parseResult.rawOptionValue('y', defaultValue));

        assertEquals(defaultValue, parseResult.rawOptionValue('z', defaultValue)); // non-existing option
        assertEquals(defaultValue, parseResult.rawOptionValue('%', defaultValue)); // non-existing option
    }

    @Test
    public void testRawOptionValueWithDefault_returnsDefaultForNonMatched() {
        class App {
            @Option(names = "-y") String y;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-y", "value1");

        String defaultValue = "DEFAULTVAL";
        assertEquals(defaultValue, parseResult.rawOptionValue("Z", defaultValue));
        assertEquals(defaultValue, parseResult.rawOptionValue("--non-existing", defaultValue));
    }

    @Test
    public void testRawOptionValues() {
        class App {
            @Option(names = {"-x", "++XX", "/XXX"}) int[] x;
            @Option(names = "-y") String y;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x", "123", "-x", "456");
        List<String> expected = Arrays.asList("123", "456");
        assertEquals(expected, parseResult.rawOptionValues("x"));
        assertEquals(expected, parseResult.rawOptionValues("-x"));
        assertEquals(expected, parseResult.rawOptionValues("XX"));
        assertEquals(expected, parseResult.rawOptionValues("++XX"));
        assertEquals(expected, parseResult.rawOptionValues("XXX"));
        assertEquals(expected, parseResult.rawOptionValues("/XXX"));

        assertEquals(Collections.emptyList(), parseResult.rawOptionValues("y"));
        assertEquals(Collections.emptyList(), parseResult.rawOptionValues("-y"));
    }

    @Test
    public void testRawOptionValues_ByShortName() {
        class App {
            @Option(names = {"-x", "++XX", "/XXX"}) int[] x;
            @Option(names = "-y") String y;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x", "123", "-x", "456");
        List<String> expected = Arrays.asList("123", "456");
        assertEquals(expected, parseResult.rawOptionValues('x'));

        assertEquals(Collections.emptyList(), parseResult.rawOptionValues('y'));
        assertEquals(Collections.emptyList(), parseResult.rawOptionValues('%')); // non-existing option
    }

    @Test
    public void testOptionValue() {
        class App {
            @Option(names = {"-x", "++XX", "/XXX"}) int[] x;
            @Option(names = "-y") double y;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x", "123", "-x", "456", "-y", "3.14");
        int[] expected = {123, 456};
        assertArrayEquals(expected, parseResult.matchedOptionValue("x", expected));
        assertArrayEquals(expected, parseResult.matchedOptionValue("-x", expected));
        assertArrayEquals(expected, parseResult.matchedOptionValue("XX", expected));
        assertArrayEquals(expected, parseResult.matchedOptionValue("++XX", expected));
        assertArrayEquals(expected, parseResult.matchedOptionValue("XXX", expected));
        assertArrayEquals(expected, parseResult.matchedOptionValue("/XXX", expected));

        assertEquals(Double.valueOf(3.14), parseResult.matchedOptionValue("y", 3.14));
        assertEquals(Double.valueOf(3.14), parseResult.matchedOptionValue("-y", 3.14));
    }

    @Test(expected = ClassCastException.class)
    public void testOptionValueWrongType() {
        class App {
            @Option(names = "-x") int[] x;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x", "123", "-x", "456");
        long[] wrongType = {123L, 456L};
        assertArrayEquals(wrongType, parseResult.matchedOptionValue("x", wrongType));
    }

    @Test
    public void testOptionValue_ByShortName() {
        class App {
            @Option(names = {"-x", "++XX", "/XXX"}) int[] x;
            @Option(names = "-y") double y;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x", "123", "-x", "456", "-y", "3.14");
        int[] expected = {123, 456};
        assertArrayEquals(expected,parseResult.matchedOptionValue('x', expected));
        assertEquals(Double.valueOf(3.14), parseResult.matchedOptionValue('y', 3.14));
        assertNull(parseResult.matchedOptionValue('%', null)); // non-existing option
    }

    @Test
    public void testOptionValue_NullIfNotMatched() {
        class App {
            @Option(names = "-y") String y = "initial";
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs();

        assertNull(parseResult.matchedOptionValue("y", null));
        assertNull(parseResult.matchedOptionValue("-y", null));
    }

    @Test
    public void testOptionWithNonJavaIdentifierName() {
        class App {
            @Option(names = "-") String dash;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-", "val");

        assertEquals("val", parseResult.rawOptionValue('-'));
        assertEquals("val", parseResult.rawOptionValue("-"));
        assertEquals("val", parseResult.matchedOptionValue('-', "val"));
        assertEquals("val", parseResult.matchedOptionValue("-", "val"));

        assertNull("empty string should not match", parseResult.rawOptionValue(""));
        assertNull("empty string should not match", parseResult.matchedOptionValue("", null));
    }

    @Test
    public void testRawOptionValueReturnsFirstValue() {
        CommandSpec spec = CommandSpec.create();
        spec.addOption(OptionSpec.builder("-V", "--verbose").build());
        spec.addOption(OptionSpec.builder("-f", "--file")
                .paramLabel("FILES")
                .type(List.class)
                .auxiliaryTypes(File.class) // List<File>
                .description("The files to process").build());
        CommandLine commandLine = new CommandLine(spec);

        String[] args = { "--verbose", "-f", "file1", "--file=file2" };
        ParseResult pr = commandLine.parseArgs(args);

        assertEquals(Arrays.asList(args), pr.originalArgs());

        assertEquals("file1", pr.rawOptionValue('f'));
        assertEquals("file1", pr.rawOptionValue("-f"));
        assertEquals("file1", pr.rawOptionValue("--file"));

        // for examples in Programmatic API wiki page
        assert "file1".equals(pr.rawOptionValue('f'));
        assert "file1".equals(pr.rawOptionValue("-f"));
        assert "file1".equals(pr.rawOptionValue("--file"));

        List<String> expected = Arrays.asList("file1", "file2");
        assertEquals(expected, pr.rawOptionValues('f'));
        assertEquals(expected, pr.rawOptionValues("file"));

        // for examples in Programmatic API wiki page
        assert expected.equals(pr.rawOptionValues('f'));
        assert expected.equals(pr.rawOptionValues("file"));
    }

    @Test
    public void testOptionValueReturnsAllValuesConvertedToType() {
        CommandSpec spec = CommandSpec.create();
        spec.addOption(OptionSpec.builder("-V", "--verbose").build());
        spec.addOption(OptionSpec.builder("-f", "--file")
                .paramLabel("FILES")
                .type(List.class)
                .auxiliaryTypes(File.class) // List<File>
                .description("The files to process").build());
        CommandLine commandLine = new CommandLine(spec);

        String[] args = { "--verbose", "-f", "file1", "--file=file2" };
        ParseResult pr = commandLine.parseArgs(args);

        List<File> expected = Arrays.asList(new File("file1"), new File("file2"));
        assertEquals(expected, pr.matchedOptionValue('f', Collections.<File>emptyList()));
        assertEquals(expected, pr.matchedOptionValue("--file", Collections.<File>emptyList()));

        // for examples in Programmatic API wiki page
        assert expected.equals(pr.matchedOptionValue('f', Collections.<File>emptyList()));
        assert expected.equals(pr.matchedOptionValue("--file", Collections.<File>emptyList()));
    }
}
