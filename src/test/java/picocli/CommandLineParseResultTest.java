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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

        assertTrue(result.hasOption("-ttt"));
        assertTrue(result.hasOption("-t"));
        assertTrue(result.hasOption("-i"));
        assertTrue(result.hasOption("-int"));
        assertFalse(result.hasOption("-unknown"));

        assertTrue(result.hasPositional(0));
        assertTrue(result.hasPositional(1));
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

        assertEquals(Collections.emptyList(), result.options());
        assertEquals(3, result.positionalParams().size());
        assertEquals(Range.valueOf("0..1"), result.positionalParams().get(0).index());
        assertEquals(Range.valueOf("0..*"), result.positionalParams().get(1).index());
        assertEquals(Range.valueOf("1..*"), result.positionalParams().get(2).index());

        assertArrayEquals(args, (String[]) result.positionalParams().get(1).getValue());
        assertArrayEquals(new String[]{"a", "b"}, (String[]) result.positionalParams().get(0).getValue());
        assertArrayEquals(new String[]{"b", "c", "d", "e"}, (String[]) result.positionalParams().get(2).getValue());

        for (int i = 0; i < args.length; i++) {
            assertTrue(result.hasPositional(i));
            assertEquals(args[i], result.rawPositionalValue(i));
        }
        assertFalse(result.hasPositional(args.length));
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

        assertTrue(parseResult.hasOption("-x"));
        assertEquals("xval", parseResult.rawOptionValue("-x"));
        assertEquals("xval", parseResult.optionValue("-x", "xval"));
        assertFalse(parseResult.hasPositional(0));

        assertTrue(parseResult.hasSubcommand());
        ParseResult subResult = parseResult.subcommand();
        assertEquals(Arrays.asList("-x", "xval", "sub", "1", "2", "3"), subResult.originalArgs()); // TODO should subresult.originalArgs include the args consumed by the parent?

        assertTrue(subResult.hasPositional(0));
        assertTrue(subResult.hasPositional(1));
        assertTrue(subResult.hasPositional(2));
        assertFalse(subResult.hasPositional(3));
        assertEquals("1", subResult.rawPositionalValue(0));
        assertEquals("2", subResult.rawPositionalValue(1));
        assertEquals("3", subResult.rawPositionalValue(2));
    }

    @Test
    public void testHasPositionalByPositionalSpec() {
        class App {
            @Option(names = "-x") String x;
            @Parameters(index = "0", arity = "0..1") int index0 = -1;
            @Parameters(index = "1", arity = "0..1") int index1 = -1;
            @Parameters(index = "2", arity = "0..1") int index2 = -1;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x", "xval", "0", "1");

        List<PositionalParamSpec> all = cmd.getCommandSpec().positionalParameters();
        assertTrue(parseResult.hasPositional(all.get(0)));
        assertTrue(parseResult.hasPositional(all.get(1)));
        assertFalse(parseResult.hasPositional(all.get(2)));
    }

    @Test
    public void testPositionalParams_ReturnsOnlyMatchedPositionals() {
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

        List<PositionalParamSpec> found = parseResult.positionalParams();
        assertEquals(2, found.size());
        assertSame(all.get(0), found.get(0));
        assertSame(all.get(1), found.get(1));

        assertSame(parseResult.positional(0), found.get(0));
        assertSame(parseResult.positional(1), found.get(1));
    }

    @Test
    public void testPositional_ReturnsNullForNonMatchedPosition() {
        class App {
            @Parameters(index = "0", arity = "0..1") int index0 = -1;
            @Parameters(index = "1", arity = "0..1") int index1 = -1;
            @Parameters(index = "2", arity = "0..1") int index2 = -1;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("0", "1");

        assertNotNull(parseResult.positional(0));
        assertNotNull(parseResult.positional(1));

        assertNull(parseResult.positional(2));
        assertNull(parseResult.positional(3));
    }

    @Test
    public void testPositionalValue_ReturnsNullForNonMatchedPosition() {
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
    public void testPositionalValueWithDefault_ReturnsDefaultForNonMatchedPosition() {
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
    public void testTypedPositionalValue() {
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

        List<PositionalParamSpec> found = parseResult.positionalParams();
        assertEquals(2, found.size());
        assertEquals(Integer.valueOf(0), parseResult.positionalValue(0, 0));
        assertEquals(Integer.valueOf(1), parseResult.positionalValue(1, 1));
        assertNull(parseResult.positionalValue(2, null));

        List<PositionalParamSpec> all = cmd.getCommandSpec().positionalParameters();
        assertEquals(3, all.size());
        assertEquals(Integer.valueOf(0), parseResult.positionalValue(all.get(0), 0));
        assertEquals(Integer.valueOf(1), parseResult.positionalValue(all.get(1), 1));
        assertEquals(Integer.valueOf(-1), parseResult.positionalValue(all.get(2), -1));
        assertNull(parseResult.positionalValue(null, null));
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

        assertSame(cmd.getCommandSpec().optionsMap().get("-h"), parseResult.option('h'));

        assertTrue(parseResult.unmatched().isEmpty());
        assertTrue(parseResult.positionalParams().isEmpty());
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

        assertSame(cmd.getCommandSpec().optionsMap().get("--version"), parseResult.option('V'));
    }

    @Test
    public void testOptions_ReturnsOnlyMatchedOptions() {
        class App {
            @Option(names = "-a", arity = "0..1") String a;
            @Option(names = "-b", arity = "0..1") String b;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-a");

        List<OptionSpec> options = parseResult.options();
        assertEquals(1, options.size());

        Map<String, OptionSpec> optionsMap = cmd.getCommandSpec().optionsMap();
        assertTrue(parseResult.hasOption(optionsMap.get("-a")));
        assertFalse(parseResult.hasOption(optionsMap.get("-b")));
    }

    @Test
    public void testOption_ReturnsOnlyMatchedOptions() {
        class App {
            @Option(names = "-a", arity = "0..1") String a;
            @Option(names = "-b", arity = "0..1") String b;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-a");

        assertNotNull(parseResult.option('a'));
        assertNotNull(parseResult.option("a"));
        assertNotNull(parseResult.option("-a"));

        assertNull(parseResult.option('b'));
        assertNull(parseResult.option("b"));
        assertNull(parseResult.option("-b"));
    }

    @Test
    public void testHasOptionByOptionSpec() {
        class App {
            @Option(names = "-x", arity = "0..1") String x;
            @Option(names = "-y", arity = "0..1") String y;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x");

        Map<String, OptionSpec> optionsMap = cmd.getCommandSpec().optionsMap();
        assertTrue(parseResult.hasOption(optionsMap.get("-x")));
        assertFalse(parseResult.hasOption(optionsMap.get("-y")));
    }

    @Test
    public void testHasOptionByShortName() {
        class App {
            @Option(names = "-x") String[] x;
            @Option(names = "-y") String y;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x", "value1", "-x", "value2");
        assertTrue(parseResult.hasOption('x'));
        assertFalse(parseResult.hasOption('y'));
    }

    @Test
    public void testHasOptionByName_VariousPrefixes() {
        class App {
            @Option(names = {"-x", "++XX", "/XXX"}) String[] x;
            @Option(names = "-y") String y;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x", "value1", "-x", "value2");
        assertTrue(parseResult.hasOption("x"));
        assertTrue(parseResult.hasOption("-x"));
        assertTrue(parseResult.hasOption("XX"));
        assertTrue(parseResult.hasOption("++XX"));
        assertTrue(parseResult.hasOption("XXX"));
        assertTrue(parseResult.hasOption("/XXX"));

        assertFalse(parseResult.hasOption("y"));
        assertFalse(parseResult.hasOption("-y"));
    }

    @Test
    public void testOptionValue() {
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
    public void testOptionValue_ByOptionSpec() {
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
    public void testOptionValue_ByShortName() {
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
    public void testOptionValueWithDefault() {
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
    public void testOptionValueWithDefault_ByShortName() {
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
    public void testOptionValueWithDefault_returnsDefaultForNonMatched() {
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
    public void testOptionValues() {
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
    public void testOptionValues_ByShortName() {
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
    public void testTypedOptionValue() {
        class App {
            @Option(names = {"-x", "++XX", "/XXX"}) int[] x;
            @Option(names = "-y") double y;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x", "123", "-x", "456", "-y", "3.14");
        int[] expected = {123, 456};
        assertArrayEquals(expected, parseResult.optionValue("x", expected));
        assertArrayEquals(expected, parseResult.optionValue("-x", expected));
        assertArrayEquals(expected, parseResult.optionValue("XX", expected));
        assertArrayEquals(expected, parseResult.optionValue("++XX", expected));
        assertArrayEquals(expected, parseResult.optionValue("XXX", expected));
        assertArrayEquals(expected, parseResult.optionValue("/XXX", expected));

        assertEquals(Double.valueOf(3.14), parseResult.optionValue("y", 3.14));
        assertEquals(Double.valueOf(3.14), parseResult.optionValue("-y", 3.14));
    }

    @Test
    public void testTypedOptionValue_ByShortName() {
        class App {
            @Option(names = {"-x", "++XX", "/XXX"}) int[] x;
            @Option(names = "-y") double y;
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs("-x", "123", "-x", "456", "-y", "3.14");
        int[] expected = {123, 456};
        assertArrayEquals(expected,parseResult.optionValue('x', expected));
        assertEquals(Double.valueOf(3.14), parseResult.optionValue('y', 3.14));
        assertNull(parseResult.optionValue('%', null)); // non-existing option
    }

    @Test
    public void testTypedOptionValue_NullIfNotMatched() {
        class App {
            @Option(names = "-y") String y = "initial";
        }
        CommandLine cmd = new CommandLine(new App());
        ParseResult parseResult = cmd.parseArgs();

        assertNull(parseResult.optionValue("y", null));
        assertNull(parseResult.optionValue("-y", null));
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
        assertEquals("val", parseResult.optionValue('-', "val"));
        assertEquals("val", parseResult.optionValue("-", "val"));

        assertNull("empty string should not match", parseResult.rawOptionValue(""));
        assertNull("empty string should not match", parseResult.optionValue("", null));
    }
}
