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

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.ParseResult;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import static org.junit.Assert.*;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.Parameters;
import static picocli.CommandLine.Range;

public class ModelParseResultTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

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

        assertSame(result.commandSpec().findOption("-t"), result.tentativeMatch.get(0));
        assertSame(result.commandSpec().findOption("-i"), result.tentativeMatch.get(1));
        assertSame(result.originalArgs().get(2), result.tentativeMatch.get(2));
        assertSame(result.commandSpec().positionalParameters().get(0), result.tentativeMatch.get(3));
        assertSame(result.commandSpec().positionalParameters().get(0), result.tentativeMatch.get(4));

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

        List<PositionalParamSpec> positionals = result.commandSpec().positionalParameters();
        assertSame(positionals.get(0), result.tentativeMatch.get(0));
        assertSame(positionals.get(0), result.tentativeMatch.get(1));
        assertSame(positionals.get(1), result.tentativeMatch.get(2));
        assertSame(positionals.get(1), result.tentativeMatch.get(3));
        assertSame(positionals.get(1), result.tentativeMatch.get(4));

        assertTrue(result.unmatched().isEmpty());
        assertFalse(result.hasSubcommand());
        assertFalse(result.isUsageHelpRequested());
        assertFalse(result.isVersionHelpRequested());

        assertEquals(Collections.emptyList(), result.matchedOptions());
        assertEquals(3, result.matchedPositionalsSet().size());
        assertEquals(new LinkedHashSet<PositionalParamSpec>(positionals), result.matchedPositionalsSet());

        assertEquals(5 + 2 + 4, result.matchedPositionals().size());
        assertEquals(Range.valueOf("0..1"), result.matchedPositionals().get(0).index());
        assertEquals(Range.valueOf("0..*"), result.matchedPositionals().get(1).index());
        assertEquals(Range.valueOf("0..1"), result.matchedPositionals().get(2).index());
        assertEquals(Range.valueOf("0..*"), result.matchedPositionals().get(3).index());
        assertEquals(Range.valueOf("1..*"), result.matchedPositionals().get(4).index());
        assertEquals(Range.valueOf("0..*"), result.matchedPositionals().get(5).index());
        assertEquals(Range.valueOf("1..*"), result.matchedPositionals().get(6).index());
        assertEquals(Range.valueOf("0..*"), result.matchedPositionals().get(7).index());
        assertEquals(Range.valueOf("1..*"), result.matchedPositionals().get(8).index());
        assertEquals(Range.valueOf("0..*"), result.matchedPositionals().get(9).index());
        assertEquals(Range.valueOf("1..*"), result.matchedPositionals().get(10).index());

        assertArrayEquals(args, (String[]) result.matchedPositionals().get(1).getValue());
        assertArrayEquals(new String[]{"a", "b"}, (String[]) result.matchedPositionals().get(0).getValue());
        assertArrayEquals(new String[]{"b", "c", "d", "e"}, (String[]) result.matchedPositionals().get(4).getValue());

        for (int i = 0; i < args.length; i++) {
            assertTrue(result.hasMatchedPositional(i));
            assertEquals(args[i], result.matchedPositional(i).stringValues().get(i));
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

        assertSame(parseResult.commandSpec().findOption("-x"), parseResult.tentativeMatch.get(0));
        assertSame(parseResult.originalArgs().get(1), parseResult.tentativeMatch.get(1));
        assertSame(parseResult.subcommand().commandSpec(), parseResult.tentativeMatch.get(2));
        assertSame(parseResult.subcommand().commandSpec().positionalParameters().get(0), parseResult.tentativeMatch.get(3));
        assertSame(parseResult.subcommand().commandSpec().positionalParameters().get(0), parseResult.tentativeMatch.get(4));
        assertSame(parseResult.subcommand().commandSpec().positionalParameters().get(0), parseResult.tentativeMatch.get(5));

        assertTrue(parseResult.hasMatchedOption("-x"));
        assertEquals("xval", parseResult.matchedOption("-x").stringValues().get(0));
        assertEquals("xval", parseResult.matchedOptionValue("-x", "xval"));
        assertFalse(parseResult.hasMatchedPositional(0));

        assertTrue(parseResult.hasSubcommand());
        ParseResult subResult = parseResult.subcommand();
        assertEquals(Arrays.asList("-x", "xval", "sub", "1", "2", "3"), subResult.originalArgs()); // TODO should subresult.originalArgs include the args consumed by the parent?

        assertTrue(subResult.hasMatchedPositional(0));
        assertTrue(subResult.hasMatchedPositional(1));
        assertTrue(subResult.hasMatchedPositional(2));
        assertFalse(subResult.hasMatchedPositional(3));
        assertEquals("1", subResult.matchedPositional(0).stringValues().get(0));
        assertEquals("2", subResult.matchedPositional(1).stringValues().get(1));
        assertEquals("3", subResult.matchedPositional(2).stringValues().get(2));
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
        ParseResult result = cmd.parseArgs("-x", "xval", "0", "1");

        assertSame(result.commandSpec().findOption("-x"), result.tentativeMatch.get(0));
        assertSame(result.originalArgs().get(1), result.tentativeMatch.get(1));
        assertSame(result.commandSpec().positionalParameters().get(0), result.tentativeMatch.get(2));
        assertSame(result.commandSpec().positionalParameters().get(1), result.tentativeMatch.get(3));

        List<PositionalParamSpec> all = cmd.getCommandSpec().positionalParameters();
        assertTrue(result.hasMatchedPositional(all.get(0)));
        assertTrue(result.hasMatchedPositional(all.get(1)));
        assertFalse(result.hasMatchedPositional(all.get(2)));
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
        assertNull(parseResult.matchedPositional(2));
    }

    @Test
    public void testMatchedPositionalsByIndex() {
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

        List<PositionalParamSpec> foundFirst = parseResult.matchedPositionals(0);
        assertEquals(1, foundFirst.size());
        assertSame(all.get(0), foundFirst.get(0));
        assertSame(parseResult.matchedPositional(0), foundFirst.get(0));

        List<PositionalParamSpec> foundSecond = parseResult.matchedPositionals(1);
        assertEquals(1, foundSecond.size());
        assertSame(all.get(1), foundSecond.get(0));
        assertSame(parseResult.matchedPositional(1), foundSecond.get(0));

        List<PositionalParamSpec> foundThird = parseResult.matchedPositionals(2);
        assertEquals(0, foundThird.size());
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

        assertEquals("0", parseResult.matchedPositional(0).stringValues().get(0));
        assertEquals("1", parseResult.matchedPositional(1).stringValues().get(0));

        assertNull(parseResult.matchedPositional(2));
        assertNull(parseResult.matchedPositional(3));
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

        assertEquals(Integer.valueOf(0), parseResult.matchedPositionalValue(0, Integer.valueOf(123)));
        assertEquals(Integer.valueOf(1), parseResult.matchedPositionalValue(1, Integer.valueOf(456)));

        assertEquals(Integer.valueOf(123), parseResult.matchedPositionalValue(2, Integer.valueOf(123)));
        assertEquals(Integer.valueOf(456), parseResult.matchedPositionalValue(3, Integer.valueOf(456)));
    }

    @Test
    public void testMatchedPositionalValue() {
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
    }

    @Test
    public void testIsUsageHelpRequested_initiallyFalse() {
        @Command(mixinStandardHelpOptions = true)
        class App {
            @Option(names = "-x") String x;
        }
        CommandLine cmd = new CommandLine(new App());
        assertFalse(cmd.isUsageHelpRequested());
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
    public void testIsVersionHelpRequested_initiallyFalse() {
        @Command(mixinStandardHelpOptions = true)
        class App {
            @Option(names = "-x") String x;
        }
        CommandLine cmd = new CommandLine(new App());
        assertFalse(cmd.isVersionHelpRequested());
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
    public void testGetParseResult_initiallyNull() {
        @Command(mixinStandardHelpOptions = true)
        class App {
            @Option(names = "-x") String x;
        }
        CommandLine cmd = new CommandLine(new App());
        assertNull(cmd.getParseResult());
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
        assertEquals("true", pr.matchedOption("verbose").stringValues().get(0));
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
        ParseResult result = cmd.parseArgs("-x", "value1", "-x", "value2");
        assertTrue(result.hasMatchedOption("x"));
        assertTrue(result.hasMatchedOption("-x"));
        assertTrue(result.hasMatchedOption("XX"));
        assertTrue(result.hasMatchedOption("++XX"));
        assertTrue(result.hasMatchedOption("XXX"));
        assertTrue(result.hasMatchedOption("/XXX"));

        assertFalse(result.hasMatchedOption("y"));
        assertFalse(result.hasMatchedOption("-y"));

        assertSame(result.commandSpec().findOption("-x"), result.tentativeMatch.get(0));
        assertSame(result.originalArgs().get(1), result.tentativeMatch.get(1));
        assertSame(result.commandSpec().findOption("-x"), result.tentativeMatch.get(2));
        assertSame(result.originalArgs().get(3), result.tentativeMatch.get(3));
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
        assertEquals("value1", parseResult.matchedOption("x").stringValues().get(0));
        assertEquals("value1", parseResult.matchedOption("-x").stringValues().get(0));
        assertEquals("value1", parseResult.matchedOption("XX").stringValues().get(0));
        assertEquals("value1", parseResult.matchedOption("++XX").stringValues().get(0));
        assertEquals("value1", parseResult.matchedOption("XXX").stringValues().get(0));
        assertEquals("value1", parseResult.matchedOption("/XXX").stringValues().get(0));

        assertEquals(null, parseResult.matchedOption("y"));
        assertEquals(null, parseResult.matchedOption("-y"));
    }

    @Test
    public void testOptionValue() {
        class App {
            @Option(names = {"-x", "++XX", "/XXX"}) int[] x;
            @Option(names = "-y") double y;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult result = cmd.parseArgs("-x", "123", "-x", "456", "-y", "3.14");
        int[] expected = {123, 456};
        assertArrayEquals(expected, result.matchedOptionValue("x", expected));
        assertArrayEquals(expected, result.matchedOptionValue("-x", expected));
        assertArrayEquals(expected, result.matchedOptionValue("XX", expected));
        assertArrayEquals(expected, result.matchedOptionValue("++XX", expected));
        assertArrayEquals(expected, result.matchedOptionValue("XXX", expected));
        assertArrayEquals(expected, result.matchedOptionValue("/XXX", expected));

        assertEquals(Double.valueOf(3.14), result.matchedOptionValue("y", 3.14));
        assertEquals(Double.valueOf(3.14), result.matchedOptionValue("-y", 3.14));

        assertSame(result.commandSpec().findOption("-x"), result.tentativeMatch.get(0));
        assertSame(result.originalArgs().get(1), result.tentativeMatch.get(1));
        assertSame(result.commandSpec().findOption("-x"), result.tentativeMatch.get(2));
        assertSame(result.originalArgs().get(3), result.tentativeMatch.get(3));
        assertSame(result.commandSpec().findOption("-y"), result.tentativeMatch.get(4));
        assertSame(result.originalArgs().get(5), result.tentativeMatch.get(5));
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

        assertEquals("val", parseResult.matchedOption('-').stringValues().get(0));
        assertEquals("val", parseResult.matchedOption("-").stringValues().get(0));
        assertEquals("val", parseResult.matchedOptionValue('-', "val"));
        assertEquals("val", parseResult.matchedOptionValue("-", "val"));

        assertNull("empty string should not match", parseResult.matchedOption(""));
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

        assertEquals("file1", pr.matchedOption('f').stringValues().get(0));
        assertEquals("file1", pr.matchedOption("-f").stringValues().get(0));
        assertEquals("file1", pr.matchedOption("--file").stringValues().get(0));

        List<String> expected = Arrays.asList("file1", "file2");
        assertEquals(expected, pr.matchedOption('f').stringValues());
        assertEquals(expected, pr.matchedOption("file").stringValues());

        // for examples in Programmatic API wiki page
        assert expected.equals(pr.matchedOption('f').stringValues());
        assert expected.equals(pr.matchedOption("file").stringValues());

        assertSame(pr.commandSpec().findOption("-V"), pr.tentativeMatch.get(0));
        assertSame(pr.commandSpec().findOption("-f"), pr.tentativeMatch.get(1));
        assertSame(pr.originalArgs().get(2), pr.tentativeMatch.get(2));
        assertSame(pr.commandSpec().findOption("-f"), pr.tentativeMatch.get(3));
        assertEquals(4, pr.tentativeMatch.size());
    }

    @Test
    public void testClusteredAndAttached() {
        class App {
            @Option(names = "-x") boolean extract;
            @Option(names = "-v") boolean verbose;
            @Option(names = "-f") File file;
            @Option(names = "-o") File outputFile;
            @Option(names = "-i") File inputFile;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult pr = cmd.parseArgs("-xvfFILE", "-oOUT", "-iOUT");

        assertSame(pr.commandSpec().findOption("-f"), pr.tentativeMatch.get(0));
        assertSame(pr.commandSpec().findOption("-o"), pr.tentativeMatch.get(1));
        assertSame(pr.commandSpec().findOption("-i"), pr.tentativeMatch.get(2));
        assertEquals(3, pr.tentativeMatch.size());
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

    @Test
    public void testBuilderAddUnmatched() {
        ParseResult.Builder builder = ParseResult.builder(CommandSpec.create());
        builder.addUnmatched("abc");
        ParseResult parseResult = builder.build();
        assertEquals(Arrays.asList("abc"), parseResult.unmatched());
    }

    @Test
    public void testBuilderAddUnmatchedStack() {
        Stack<String> stack = new Stack<String>();
        stack.push("a");
        stack.push("b");
        stack.push("c");

        ParseResult.Builder builder = ParseResult.builder(CommandSpec.create());
        builder.addUnmatched(stack);
        ParseResult parseResult = builder.build();
        assertEquals(Arrays.asList("c", "b", "a"), parseResult.unmatched());
    }

    @Test
    public void testParseResult_matchedOptionsSet() {
        @Command(mixinStandardHelpOptions = true) class A {}
        ParseResult pr = new CommandLine(new A()).parseArgs("--help", "--version");
        Set<OptionSpec> matched = pr.matchedOptionsSet();
        assertEquals(2, matched.size());
        assertTrue(matched.contains(pr.commandSpec().findOption("--help")));
        assertTrue(matched.contains(pr.commandSpec().findOption("--version")));
    }
}
