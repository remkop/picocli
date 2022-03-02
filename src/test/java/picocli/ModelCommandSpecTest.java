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
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TestRule;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.InitializationException;
import picocli.CommandLine.MissingParameterException;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.IAnnotatedElement;
import picocli.CommandLine.Model.ISetter;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.ParserSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Model.UsageMessageSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Tracer;
import picocli.CommandLine.UnmatchedArgumentException;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Set;

import static org.junit.Assert.*;
import static picocli.TestUtil.setTraceLevel;
import static picocli.TestUtil.versionString;


public class ModelCommandSpecTest {
    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void testEmptyModelParse() {
        setTraceLevel(CommandLine.TraceLevel.OFF);
        CommandSpec spec = CommandSpec.create();
        CommandLine commandLine = new CommandLine(spec);
        commandLine.setUnmatchedArgumentsAllowed(true);
        commandLine.parseArgs("-p", "123", "abc");
        assertEquals(Arrays.asList("-p", "123", "abc"), commandLine.getUnmatchedArguments());
    }

    @Test
    public void testVersionHelp_basic() {
        CommandSpec spec = CommandSpec.create().version("1.0", "copyright etc");
        CommandLine commandLine = new CommandLine(spec);
        String actual = versionString(commandLine, Ansi.OFF);
        String expected = String.format("" +
                "1.0%n" +
                "copyright etc%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testVersionHelp_helpCommand() {
        CommandSpec helpCommand = CommandSpec.create().helpCommand(true);
        assertTrue(helpCommand.helpCommand());

        CommandSpec parent = CommandSpec.create().addOption(OptionSpec.builder("-x").type(String.class).required(true).build());
        parent.addSubcommand("help", helpCommand);

        CommandLine commandLine = new CommandLine(parent);
        commandLine.parseArgs("help"); // no missing param exception

        try {
            commandLine.parseArgs();
        } catch (MissingParameterException ex) {
            assertEquals("Missing required option: '-x=PARAM'", ex.getMessage());
            assertEquals(1, ex.getMissing().size());
            assertSame(ex.getMissing().get(0).toString(), parent.posixOptionsMap().get('x'), ex.getMissing().get(0));
        }
    }

    @Test
    public void testModelParse() {
        CommandSpec spec = CommandSpec.create();
        spec.addOption(OptionSpec.builder("-h", "--help").usageHelp(true).description("show help and exit").build());
        spec.addOption(OptionSpec.builder("-V", "--version").versionHelp(true).description("show help and exit").build());
        spec.addOption(OptionSpec.builder("-c", "--count").paramLabel("COUNT").arity("1").type(int.class).description("number of times to execute").build());
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parseArgs("-c", "33");
        assertEquals(Integer.valueOf(33), spec.optionsMap().get("-c").getValue());
    } // TODO parse method should return an object offering only the options/positionals that were matched

    @Test
    public void testMultiValueOptionArityAloneIsInsufficient() {
        CommandSpec spec = CommandSpec.create();
        OptionSpec option = OptionSpec.builder("-c", "--count").arity("3").type(int.class).build();
        assertFalse(option.isMultiValue());

        spec.addOption(option);
        CommandLine commandLine = new CommandLine(spec);
        try {
            commandLine.parseArgs("-c", "1", "2", "3");
            fail("Expected exception");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched arguments from index 2: '2', '3'", ex.getMessage());
        }
    }

    @Test
    public void testMultiValuePositionalParamArityAloneIsInsufficient() {
        CommandSpec spec = CommandSpec.create();
        PositionalParamSpec positional = PositionalParamSpec.builder().index("0").arity("3").type(int.class).build();
        assertFalse(positional.isMultiValue());

        spec.addPositional(positional);
        CommandLine commandLine = new CommandLine(spec);
        try {
            commandLine.parseArgs("1", "2", "3");
            fail("Expected exception");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched arguments from index 1: '2', '3'", ex.getMessage());
        }
    }

    @Test
    public void testMultiValueOptionWithArray() {
        CommandSpec spec = CommandSpec.create();
        OptionSpec option = OptionSpec.builder("-c", "--count").arity("3").type(int[].class).build();
        assertTrue(option.isMultiValue());

        spec.addOption(option);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parseArgs("-c", "1", "2", "3");
        assertArrayEquals(new int[] {1, 2, 3}, (int[]) spec.optionsMap().get("-c").getValue());
    }

    @Test
    public void testMultiValuePositionalParamWithArray() {
        CommandSpec spec = CommandSpec.create();
        PositionalParamSpec positional = PositionalParamSpec.builder().index("0").arity("3").type(int[].class).build();
        assertTrue(positional.isMultiValue());

        spec.addPositional(positional);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parseArgs("1", "2", "3");
        assertArrayEquals(new int[] {1, 2, 3}, (int[]) spec.positionalParameters().get(0).getValue());
    }

    @Test
    public void testMultiValueOptionWithListAndAuxTypes() {
        CommandSpec spec = CommandSpec.create();
        OptionSpec option = OptionSpec.builder("-c", "--count").arity("3").type(List.class).auxiliaryTypes(Integer.class).build();
        assertTrue(option.isMultiValue());

        spec.addOption(option);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parseArgs("-c", "1", "2", "3");
        assertEquals(Arrays.asList(1, 2, 3), spec.optionsMap().get("-c").getValue());
    }

    @Test
    public void testMultiValuePositionalParamWithListAndAuxTypes() {
        CommandSpec spec = CommandSpec.create();
        PositionalParamSpec positional = PositionalParamSpec.builder().index("0").arity("3").type(List.class).auxiliaryTypes(Integer.class).build();
        assertTrue(positional.isMultiValue());

        spec.addPositional(positional);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parseArgs("1", "2", "3");
        assertEquals(Arrays.asList(1, 2, 3), spec.positionalParameters().get(0).getValue());
    }

    @Test
    public void testMultiValueOptionWithListWithoutAuxTypes() {
        CommandSpec spec = CommandSpec.create();
        OptionSpec option = OptionSpec.builder("-c", "--count").arity("3").type(List.class).build();
        assertTrue(option.isMultiValue());

        spec.addOption(option);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parseArgs("-c", "1", "2", "3");
        assertEquals(Arrays.asList("1", "2", "3"), spec.optionsMap().get("-c").getValue());
    }

    @Test
    public void testMultiValuePositionalParamWithListWithoutAuxTypes() {
        CommandSpec spec = CommandSpec.create();
        PositionalParamSpec positional = PositionalParamSpec.builder().index("0").arity("3").type(List.class).build();
        assertTrue(positional.isMultiValue());

        spec.addPositional(positional);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parseArgs("1", "2", "3");
        assertEquals(Arrays.asList("1", "2", "3"), spec.positionalParameters().get(0).getValue());
    }

    @Test
    public void testMultiValueOptionWithMapAndAuxTypes() {
        CommandSpec spec = CommandSpec.create();
        OptionSpec option = OptionSpec.builder("-c", "--count").arity("3").type(Map.class).auxiliaryTypes(Integer.class, Double.class).build();
        assertTrue(option.isMultiValue());

        spec.addOption(option);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parseArgs("-c", "1=1.0", "2=2.0", "3=3.0");
        Map<Integer, Double> expected = new LinkedHashMap<Integer, Double>();
        expected.put(1, 1.0);
        expected.put(2, 2.0);
        expected.put(3, 3.0);
        assertEquals(expected, spec.optionsMap().get("-c").getValue());
    }

    @Test
    public void testMultiValuePositionalParamWithMapAndAuxTypes() {
        CommandSpec spec = CommandSpec.create();
        PositionalParamSpec positional = PositionalParamSpec.builder().index("0").arity("3").type(Map.class).auxiliaryTypes(Integer.class, Double.class).build();
        assertTrue(positional.isMultiValue());

        spec.addPositional(positional);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parseArgs("1=1.0", "2=2.0", "3=3.0");
        Map<Integer, Double> expected = new LinkedHashMap<Integer, Double>();
        expected.put(1, 1.0);
        expected.put(2, 2.0);
        expected.put(3, 3.0);
        assertEquals(expected, spec.positionalParameters().get(0).getValue());
    }

    @Test
    public void testMultiValueOptionWithMapWithoutAuxTypes() {
        CommandSpec spec = CommandSpec.create();
        OptionSpec option = OptionSpec.builder("-c", "--count").arity("3").type(Map.class).build();
        assertTrue(option.isMultiValue());

        spec.addOption(option);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parseArgs("-c", "1=1.0", "2=2.0", "3=3.0");
        Map<String, String> expected = new LinkedHashMap<String, String>();
        expected.put("1", "1.0");
        expected.put("2", "2.0");
        expected.put("3", "3.0");
        assertEquals(expected, spec.optionsMap().get("-c").getValue());
    }

    @Test
    public void testMultiValuePositionalParamWithMapWithoutAuxTypes() {
        CommandSpec spec = CommandSpec.create();
        PositionalParamSpec positional = PositionalParamSpec.builder().index("0").arity("3").type(Map.class).build();
        assertTrue(positional.isMultiValue());

        spec.addPositional(positional);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parseArgs("1=1.0", "2=2.0", "3=3.0");
        Map<String, String> expected = new LinkedHashMap<String, String>();
        expected.put("1", "1.0");
        expected.put("2", "2.0");
        expected.put("3", "3.0");
        assertEquals(expected, spec.positionalParameters().get(0).getValue());
    }

    @Test
    public void testOptionConvertersOverridesRegisteredTypeConverter() {
        CommandSpec spec = CommandSpec.create();
        spec.addOption(OptionSpec.builder("-c", "--count").paramLabel("COUNT").arity("1").type(int.class).description("number of times to execute").build());
        spec.addOption(OptionSpec.builder("-s", "--sql").paramLabel("SQLTYPE").type(int.class).converters(
                new TypeConversionTest.SqlTypeConverter()).description("sql type converter").build());
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parseArgs("-c", "33", "-s", "BLOB");
        assertEquals(Integer.valueOf(33), spec.optionsMap().get("-c").getValue());
        assertEquals(Integer.valueOf(Types.BLOB), spec.optionsMap().get("-s").getValue());
    }
    @Test
    public void testPositionalConvertersOverridesRegisteredTypeConverter() {
        CommandSpec spec = CommandSpec.create();
        spec.addPositional(PositionalParamSpec.builder().paramLabel("COUNT").index("0").type(int.class).description("number of times to execute").build());
        spec.addPositional(PositionalParamSpec.builder().paramLabel("SQLTYPE").index("1").type(int.class).converters(
                new TypeConversionTest.SqlTypeConverter()).description("sql type converter").build());
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parseArgs("33", "BLOB");
        assertEquals(Integer.valueOf(33), spec.positionalParameters().get(0).getValue());
        assertEquals(Integer.valueOf(Types.BLOB), spec.positionalParameters().get(1).getValue());
    }

    @Test
    public void testConversion_TODO() {
        // TODO convertion with aux types (abstract field types, generic map with and without explicit type attribute etc)
    }

    @Test
    public void testTypedValues() {
        class App {
            @Option(names="-x") int x;
        }
        ParseResult result1 = new CommandLine(new App()).parseArgs();// not specified
        assertFalse(result1.hasMatchedOption('x'));
        assertTrue(result1.commandSpec().findOption('x').typedValues().isEmpty());

        ParseResult result2 = new CommandLine(new App()).parseArgs("-x", "123");
        assertTrue(result2.hasMatchedOption('x'));
        assertEquals(Integer.valueOf(123), result2.matchedOptionValue('x', 0));

        ParseResult result3 = new CommandLine(new App())
                .setOverwrittenOptionsAllowed(true)
                .parseArgs("-x", "1", "-x", "2", "-x", "3");
        assertTrue(result3.hasMatchedOption('x'));
        assertEquals(Integer.valueOf(3), result3.matchedOptionValue('x', 0));
        assertEquals(Arrays.asList("1", "2", "3"), result3.matchedOption('x').stringValues());
        assertEquals(Arrays.asList(1, 2, 3), result3.matchedOption('x').typedValues());
    }

    /** see <a href="https://github.com/remkop/picocli/issues/279">issue #279</a>  */
    @SuppressWarnings("deprecation")
    @Test
    public void testSingleValueFieldWithOptionalParameter_279() {
        @Command(name="sample")
        class Sample {
            @Option(names="--foo", arity="0..1") String foo;
        }
        List<CommandLine> parsed1 = new CommandLine(new Sample()).parse();// not specified
        OptionSpec option1 = parsed1.get(0).getCommandSpec().optionsMap().get("--foo");
        assertNull("optional option is null when option not specified", option1.getValue());
        assertTrue("optional option has no string value when option not specified", option1.stringValues().isEmpty());
        assertTrue("optional option has no typed value when option not specified", option1.typedValues().isEmpty());

        List<CommandLine> parsed2 = new CommandLine(new Sample()).parse("--foo");// specified without value
        OptionSpec option2 = parsed2.get(0).getCommandSpec().optionsMap().get("--foo");
        assertEquals("optional option is empty string when specified without args", "", option2.getValue());
        assertEquals("optional option string value when specified without args", "", option2.stringValues().get(0));
        assertEquals("optional option typed value when specified without args", "", option2.typedValues().get(0));

        List<CommandLine> parsed3 = new CommandLine(new Sample()).parse("--foo", "value");// specified with value
        OptionSpec option3 = parsed3.get(0).getCommandSpec().optionsMap().get("--foo");
        assertEquals("optional option is empty string when specified with args", "value", option3.getValue());
        assertEquals("optional option string value when specified with args", "value", option3.stringValues().get(0));
        assertEquals("optional option typed value when specified with args", "value", option3.typedValues().get(0));
    }

    /** see <a href="https://github.com/remkop/picocli/issues/280">issue #280</a>  */
    @SuppressWarnings("deprecation")
    @Test
    public void testSingleValueFieldWithOptionalParameter_280() {
        @Command(name="sample")
        class Sample {
            @Option(names="--foo", arity="0..1", fallbackValue = "123") Integer foo;
        }
        List<CommandLine> parsed1 = new CommandLine(new Sample()).parse();// not specified
        OptionSpec option1 = parsed1.get(0).getCommandSpec().optionsMap().get("--foo");
        assertNull("optional option is null when option not specified", option1.getValue());
        assertTrue("optional option has no string value when option not specified", option1.stringValues().isEmpty());
        assertTrue("optional option has no typed value when option not specified", option1.typedValues().isEmpty());

        List<CommandLine> parsed2 = new CommandLine(new Sample()).parse("--foo");// specified without value
        OptionSpec option2 = parsed2.get(0).getCommandSpec().optionsMap().get("--foo");
        assertEquals("optional option is fallback when specified without args", Integer.valueOf(123), option2.getValue());
        assertEquals("optional option string fallback value when specified without args", "123", option2.stringValues().get(0));
        assertEquals("optional option typed value when specified without args", 123, option2.typedValues().get(0));

        List<CommandLine> parsed3 = new CommandLine(new Sample()).parse("--foo", "999");// specified with value
        OptionSpec option3 = parsed3.get(0).getCommandSpec().optionsMap().get("--foo");
        assertEquals("optional option is empty string when specified with args", Integer.valueOf(999), option3.getValue());
        assertEquals("optional option string value when specified with args", "999", option3.stringValues().get(0));
        assertEquals("optional option typed value when specified with args", 999, option3.typedValues().get(0));
    }

    /** see <a href="https://github.com/remkop/picocli/issues/279">issue #279</a>  */
    @SuppressWarnings("deprecation")
    @Test
    public void testSingleValueFieldWithOptionalParameterFollowedByOption_279() {
        @Command(name="sample")
        class Sample {
            @Option(names = "-x") boolean x;
            @Option(names="--foo", arity="0..1") String foo;
        }

        Sample sample = new Sample();
        List<CommandLine> parsed3 = new CommandLine(sample).parse("--foo", "-x");// specified without value
        OptionSpec option3 = parsed3.get(0).getCommandSpec().optionsMap().get("--foo");
        assertEquals("optional option is empty string when specified without args", "", option3.getValue());
        assertEquals("optional option string value when specified without args", "", option3.stringValues().get(0));
        assertEquals("optional option typed value when specified without args", "", option3.typedValues().get(0));
        assertEquals("", sample.foo);
        assertEquals(true, sample.x);
    }

    /** see <a href="https://github.com/remkop/picocli/issues/280">issue #280</a>  */
    @SuppressWarnings("deprecation")
    @Test
    public void testSingleValueFieldWithOptionalParameterFollowedByOption_280() {
        @Command(name="sample")
        class Sample {
            @Option(names = "-x") boolean x;
            @Option(names="--foo", arity="0..1", fallbackValue = "-1") Long foo;
        }

        Sample sample = new Sample();
        List<CommandLine> parsed3 = new CommandLine(sample).parse("--foo", "-x");// specified without value
        OptionSpec option3 = parsed3.get(0).getCommandSpec().optionsMap().get("--foo");
        assertEquals("optional option is fallback typed value when specified without args", Long.valueOf(-1L), option3.getValue());
        assertEquals("optional option fallback string value when specified without args", "-1", option3.stringValues().get(0));
        assertEquals("optional option fallback typed value when specified without args", -1L, option3.typedValues().get(0));
        assertEquals(Long.valueOf(-1L), sample.foo);
        assertEquals(true, sample.x);
    }

    @Test
    public void testMixinStandardHelpOptions_FalseByDefault() {
        CommandSpec spec = CommandSpec.create();
        assertFalse(spec.mixinStandardHelpOptions());
    }

    @Test
    public void testMixinStandardHelpOptions_SettingToTrueAddsHelpOptions() {
        CommandSpec spec = CommandSpec.create();
        assertTrue(spec.mixins().isEmpty());
        assertTrue(spec.optionsMap().isEmpty());
        assertTrue(spec.posixOptionsMap().isEmpty());
        assertTrue(spec.options().isEmpty());

        spec.mixinStandardHelpOptions(true);
        assertFalse(spec.mixins().isEmpty());
        assertFalse(spec.optionsMap().isEmpty());
        assertFalse(spec.posixOptionsMap().isEmpty());
        assertFalse(spec.options().isEmpty());
        assertTrue(spec.mixinStandardHelpOptions());

        OptionSpec usageHelp = spec.posixOptionsMap().get('h');
        assertSame(usageHelp, spec.optionsMap().get("--help"));
        assertTrue(usageHelp.usageHelp());

        OptionSpec versionHelp = spec.posixOptionsMap().get('V');
        assertSame(versionHelp, spec.optionsMap().get("--version"));
        assertTrue(versionHelp.versionHelp());
    }

    @Test
    public void testMixinStandardHelpOptions_SettingToFalseRemovesHelpOptions() {
        CommandSpec spec = CommandSpec.create();

        spec.mixinStandardHelpOptions(true);
        assertFalse(spec.mixins().isEmpty());
        assertFalse(spec.optionsMap().isEmpty());
        assertFalse(spec.posixOptionsMap().isEmpty());
        assertFalse(spec.options().isEmpty());
        assertTrue(spec.mixinStandardHelpOptions());

        assertNotNull(spec.posixOptionsMap().get('h'));
        assertNotNull(spec.optionsMap().get("--help"));

        assertNotNull(spec.posixOptionsMap().get('V'));
        assertNotNull(spec.optionsMap().get("--version"));

        spec.mixinStandardHelpOptions(false);
        assertTrue(spec.mixins().isEmpty());
        assertTrue(spec.optionsMap().isEmpty());
        assertTrue(spec.posixOptionsMap().isEmpty());
        assertTrue(spec.options().isEmpty());
        assertFalse(spec.mixinStandardHelpOptions());
    }

    @Test
    public void testCommandSpec_forAnnotatedObject_requiresPicocliAnnotation() {
        Object userObject = new Object();
        try {
            CommandSpec.forAnnotatedObject(userObject);
            fail("Expected error");
        } catch (InitializationException ok) {
            assertEquals(userObject + " is not a command: it has no @Command, @Option, @Parameters or @Unmatched annotations", ok.getMessage());
        }
    }

    @Test
    public void testCommandSpec_forAnnotatedObjectLenient_doesNotRequirePicocliAnnotation() {
        CommandSpec.forAnnotatedObjectLenient(new Object()); // no error
    }

    @Test
    public void testCommandSpec_forAnnotatedObjectLenient_returnsEmptyCommandSpec() {
        CommandSpec spec = CommandSpec.forAnnotatedObjectLenient(new Object());
        assertTrue(spec.optionsMap().isEmpty());
        assertTrue(spec.posixOptionsMap().isEmpty());
        assertTrue(spec.options().isEmpty());
        assertTrue(spec.positionalParameters().isEmpty());
        assertTrue(spec.unmatchedArgsBindings().isEmpty());
        assertTrue(spec.subcommands().isEmpty());
        assertTrue(spec.mixins().isEmpty());
        assertTrue(spec.requiredArgs().isEmpty());
        assertFalse(spec.mixinStandardHelpOptions());
        assertFalse(spec.helpCommand());
        assertEquals("<main class>", spec.name());
        assertArrayEquals(new String[0], spec.version());
        assertNull(spec.versionProvider());
    }

    @Test
    public void testOptionSpec_setsDefaultValue_ifNotMatched() {
        CommandSpec cmd = CommandSpec.create().addOption(OptionSpec.builder("-x").defaultValue("123").type(int.class).build());

        ParseResult parseResult = new CommandLine(cmd).parseArgs();
        assertFalse(parseResult.hasMatchedOption('x'));
        // TODO this method should be renamed to matchedOptionValue
        assertEquals(Integer.valueOf(-1), parseResult.matchedOptionValue('x', -1));

        // TODO optionValue should return the value of the option, matched or not
        //assertEquals(Integer.valueOf(123), parseResult.optionValue('x'));
        assertEquals(Integer.valueOf(123), parseResult.commandSpec().findOption('x').getValue());
    }

    @Test
    public void testPositionalParamSpec_setsDefaultValue_ifNotMatched() {
        CommandSpec cmd = CommandSpec.create().add(PositionalParamSpec.builder().defaultValue("123").type(int.class).build());

        ParseResult parseResult = new CommandLine(cmd).parseArgs();
        assertFalse(parseResult.hasMatchedPositional(0));
        // TODO this method should be renamed to matchedPositionalValue
        assertEquals(Integer.valueOf(-1), parseResult.matchedPositionalValue(0, -1));

        // TODO positionalValue should return the value of the option, matched or not
        //assertEquals(Integer.valueOf(123), parseResult.positionalValue(0));
        assertEquals(Integer.valueOf(123), parseResult.commandSpec().positionalParameters().get(0).getValue());
    }

    @Test
    public void testOptionSpec_defaultValue_overwritesInitialValue() {
        class Params {
            @Option(names = "-x") int num = 12345;
        }
        CommandLine cmd = new CommandLine(new Params());
        OptionSpec x = cmd.getCommandSpec().posixOptionsMap().get('x').toBuilder().defaultValue("54321").build();

        cmd = new CommandLine(CommandSpec.create().addOption(x));
        ParseResult parseResult = cmd.parseArgs();
        assertFalse(parseResult.hasMatchedOption('x'));
        // TODO this method should be renamed to matchedOptionValue
        assertEquals(Integer.valueOf(-1), parseResult.matchedOptionValue('x', -1));

        // TODO optionValue should return the value of the option, matched or not
        //assertEquals(Integer.valueOf(54321), parseResult.optionValue('x'));
        assertEquals(Integer.valueOf(54321), parseResult.commandSpec().findOption('x').getValue());
    }

    @Test
    public void testPositionalParamSpec_defaultValue_overwritesInitialValue() {
        class Params {
            @Parameters int num = 12345;
        }
        CommandLine cmd = new CommandLine(new Params());
        PositionalParamSpec x = cmd.getCommandSpec().positionalParameters().get(0).toBuilder().defaultValue("54321").build();

        cmd = new CommandLine(CommandSpec.create().add(x));
        ParseResult parseResult = cmd.parseArgs();

        // default not in the parse result
        assertFalse(parseResult.hasMatchedPositional(0));
        assertEquals(Integer.valueOf(-1), parseResult.matchedPositionalValue(0, -1));

        // but positional spec does have the default value
        assertEquals(Integer.valueOf(54321), parseResult.commandSpec().positionalParameters().get(0).getValue());

    }

    @Test
    public void testOptionSpec_notRequiredIfNonNullDefaultValue() {
        assertTrue(OptionSpec.builder("-x").required(true).build().required());
        assertFalse(OptionSpec.builder("-x").defaultValue("123").required(true).build().required());
    }

    @Test
    public void testPositionalParamSpec_notRequiredIfNonNullDefaultValue() {
        assertTrue(PositionalParamSpec.builder().required(true).build().required());
        assertFalse(PositionalParamSpec.builder().defaultValue("123").required(true).build().required());
    }

    @Test
    public void testOptionSpec_DefaultValue_single_replacedByCommandLineValue() {
        CommandSpec cmd = CommandSpec.create().addOption(OptionSpec.builder("-x").defaultValue("123").type(int.class).build());

        ParseResult parseResult = new CommandLine(cmd).parseArgs("-x", "456");
        assertEquals(Integer.valueOf(456), parseResult.matchedOptionValue('x', -1));
    }

    @Test
    public void testPositionalParamSpec_DefaultValue_single_replacedByCommandLineValue() {
        CommandSpec cmd = CommandSpec.create().add(PositionalParamSpec.builder().defaultValue("123").type(int.class).build());

        ParseResult parseResult = new CommandLine(cmd).parseArgs("456");
        assertEquals(Integer.valueOf(456), parseResult.matchedPositionalValue(0, -1));
    }

    @Test
    public void testOptionSpec_DefaultValue_array_replacedByCommandLineValue() {
        CommandSpec cmd = CommandSpec.create().addOption(OptionSpec
                .builder("-x").defaultValue("1,2,3").splitRegex(",").type(int[].class).build());

        ParseResult parseResult = new CommandLine(cmd).parseArgs("-x", "4,5,6");
        assertArrayEquals(new int[]{4, 5, 6}, parseResult.matchedOptionValue('x', new int[0]));
    }

    @Test
    public void testOptionSpec_DefaultValue_list_replacedByCommandLineValue() {
        CommandSpec cmd = CommandSpec.create().addOption(OptionSpec
                .builder("-x").defaultValue("1,2,3").splitRegex(",").type(List.class).auxiliaryTypes(Integer.class).build());

        ParseResult parseResult = new CommandLine(cmd).parseArgs("-x", "4,5,6");
        assertEquals(Arrays.asList(4, 5, 6), parseResult.matchedOptionValue('x', Collections.emptyList()));
    }

    @Test
    public void testOptionSpec_DefaultValue_map_replacedByCommandLineValue() {
        CommandSpec cmd = CommandSpec.create().add(OptionSpec
                .builder("-x").defaultValue("1=A,2=B,3=C").splitRegex(",").type(Map.class).auxiliaryTypes(Integer.class, String.class).build());

        ParseResult parseResult = new CommandLine(cmd).parseArgs("-x", "4=X,5=Y,6=Z");
        Map<Integer, String> expected = new HashMap<Integer, String>();
        expected.put(4, "X");
        expected.put(5, "Y");
        expected.put(6, "Z");
        assertEquals(expected, parseResult.matchedOptionValue('x', Collections.emptyMap()));
    }

    @Test
    public void testPositionalParamSpec_DefaultValue_array_replacedByCommandLineValue() {
        CommandSpec cmd = CommandSpec.create().add(PositionalParamSpec
                .builder().defaultValue("1,2,3").splitRegex(",").type(int[].class).build());

        ParseResult parseResult = new CommandLine(cmd).parseArgs("4,5,6");
        assertArrayEquals(new int[]{4, 5, 6}, parseResult.matchedPositionalValue(0, new int[0]));
    }

    @Test
    public void testPositionalParamSpec_DefaultValue_list_replacedByCommandLineValue() {
        CommandSpec cmd = CommandSpec.create().add(PositionalParamSpec
                .builder().defaultValue("1,2,3").splitRegex(",").type(List.class).auxiliaryTypes(Integer.class).build());

        ParseResult parseResult = new CommandLine(cmd).parseArgs("4,5,6");
        assertEquals(Arrays.asList(4, 5, 6), parseResult.matchedPositionalValue(0, Collections.emptyList()));
    }

    @Test
    public void testPositionalParamSpec_DefaultValue_map_replacedByCommandLineValue() {
        CommandSpec cmd = CommandSpec.create().add(PositionalParamSpec
                .builder().defaultValue("1=A,2=B,3=C").splitRegex(",").type(Map.class).auxiliaryTypes(Integer.class, String.class).build());

        ParseResult parseResult = new CommandLine(cmd).parseArgs("4=X,5=Y,6=Z");
        Map<Integer, String> expected = new HashMap<Integer, String>();
        expected.put(4, "X");
        expected.put(5, "Y");
        expected.put(6, "Z");
        assertEquals(expected, parseResult.matchedPositionalValue(0, Collections.emptyMap()));
    }

    @Test
    public void testMultipleUsageHelpOptions() {
        setTraceLevel(CommandLine.TraceLevel.WARN);
        CommandSpec cmd = CommandSpec.create()
                .add(OptionSpec.builder("-x").type(boolean.class).usageHelp(true).build())
                .add(OptionSpec.builder("-h").type(boolean.class).usageHelp(true).build());

        assertEquals("", systemErrRule.getLog());
        systemErrRule.clearLog();
        new CommandLine(cmd);
        assertEquals("", systemOutRule.getLog());
        assertEquals(String.format("[picocli WARN] Multiple options [-x, -h] are marked as 'usageHelp=true'. Usually a command has only one --help option that triggers display of the usage help message. Alternatively, consider using @Command(mixinStandardHelpOptions = true) on your command instead.%n"), systemErrRule.getLog());
    }

    @Test
    public void testMultipleVersionHelpOptions() {
        setTraceLevel(CommandLine.TraceLevel.WARN);
        CommandSpec cmd = CommandSpec.create()
                .add(OptionSpec.builder("-x").type(boolean.class).versionHelp(true).build())
                .add(OptionSpec.builder("-V").type(boolean.class).versionHelp(true).build());

        assertEquals("", systemErrRule.getLog());
        systemErrRule.clearLog();
        new CommandLine(cmd);
        assertEquals("", systemOutRule.getLog());
        assertEquals(String.format("[picocli WARN] Multiple options [-x, -V] are marked as 'versionHelp=true'. Usually a command has only one --version option that triggers display of the version information. Alternatively, consider using @Command(mixinStandardHelpOptions = true) on your command instead.%n"), systemErrRule.getLog());
    }

    @Test
    public void testNonBooleanUsageHelpOptions() {
        CommandSpec cmd = CommandSpec.create().add(OptionSpec.builder("-z").type(int.class).usageHelp(true).build());
        try {
            new CommandLine(cmd);
        } catch (InitializationException ex) {
            assertEquals("Non-boolean options like [-z] should not be marked as 'usageHelp=true'. Usually a command has one --help boolean flag that triggers display of the usage help message. Alternatively, consider using @Command(mixinStandardHelpOptions = true) on your command instead.", ex.getMessage());
        }
    }

    @Test
    public void testNonBooleanVersionHelpOptions() {
        CommandSpec cmd = CommandSpec.create().add(OptionSpec.builder("-x").type(int.class).versionHelp(true).build());
        try {
            new CommandLine(cmd);
        } catch (InitializationException ex) {
            assertEquals("Non-boolean options like [-x] should not be marked as 'versionHelp=true'. Usually a command has one --version boolean flag that triggers display of the version information. Alternatively, consider using @Command(mixinStandardHelpOptions = true) on your command instead.", ex.getMessage());
        }
    }

    @Test
    public void testBooleanObjectUsageHelpOptions() {
        CommandSpec cmd = CommandSpec.create().add(OptionSpec.builder("-z").type(Boolean.class).usageHelp(true).build());
        assertTrue(new CommandLine(cmd).parseArgs("-z").isUsageHelpRequested());
    }

    @Test
    public void testBooleanObjectVersionHelpOptions() {
        CommandSpec cmd = CommandSpec.create().add(OptionSpec.builder("-x").type(Boolean.class).versionHelp(true).build());
        assertTrue(new CommandLine(cmd).parseArgs("-x").isVersionHelpRequested());
    }

    @Test
    public void testParseResetsRawAndOriginalStringValues() {
        CommandSpec spec = CommandSpec.create()
                .addOption(OptionSpec.builder("-x").type(String.class).build())
                .addPositional(PositionalParamSpec.builder().build());
        CommandLine cmd = new CommandLine(spec);
        ParseResult parseResult = cmd.parseArgs("-x", "XVAL", "POSITIONAL");
        assertEquals("XVAL", parseResult.matchedOption('x').getValue());
        assertEquals(Arrays.asList("XVAL"), parseResult.matchedOption('x').stringValues());
        assertEquals(Arrays.asList("XVAL"), parseResult.matchedOption('x').originalStringValues());
        assertEquals("POSITIONAL", parseResult.matchedPositional(0).getValue());
        assertEquals(Arrays.asList("POSITIONAL"), parseResult.matchedPositional(0).stringValues());
        assertEquals(Arrays.asList("POSITIONAL"), parseResult.matchedPositional(0).originalStringValues());

        ParseResult parseResult2 = cmd.parseArgs("-x", "222", "$$$$");
        assertEquals("222", parseResult2.matchedOption('x').getValue());
        assertEquals(Arrays.asList("222"), parseResult2.matchedOption('x').stringValues());
        assertEquals(Arrays.asList("222"), parseResult2.matchedOption('x').originalStringValues());
        assertEquals("$$$$", parseResult2.matchedPositional(0).getValue());
        assertEquals(Arrays.asList("$$$$"), parseResult2.matchedPositional(0).stringValues());
        assertEquals(Arrays.asList("$$$$"), parseResult2.matchedPositional(0).originalStringValues());

    }

    @Test
    public void testInitializingDefaultsShouldNotAddOptionToParseResult() {
        CommandSpec spec = CommandSpec.create()
                .addOption(OptionSpec.builder("-x").type(String.class).defaultValue("xyz").build());
        CommandLine cmd = new CommandLine(spec);
        ParseResult parseResult = cmd.parseArgs();
        assertFalse(parseResult.hasMatchedOption('x'));
    }

    @Test
    public void testInitializingDefaultsShouldNotAddPositionalToParseResult() {
        CommandSpec spec = CommandSpec.create()
                .addPositional(PositionalParamSpec.builder().defaultValue("xyz").build());
        CommandLine cmd = new CommandLine(spec);
        ParseResult parseResult = cmd.parseArgs();
        assertFalse(parseResult.hasMatchedPositional(0));
    }

    @Test
    public void testOptionLongestName_oneName() {
        assertEquals("-x", OptionSpec.builder("-x").build().longestName());
    }

    @Test
    public void testOptionLongestName_multipleEqualLength_returnsFirst() {
        assertEquals("-x", OptionSpec.builder("-x", "-a").build().longestName());
    }

    @Test
    public void testOptionLongestName_returnsLongest() {
        assertEquals("-xxx", OptionSpec.builder("-x", "-xx", "-xxx").build().longestName());
        assertEquals("-aaa", OptionSpec.builder("-x", "-xx", "-aaa").build().longestName());
        assertEquals("-abcd", OptionSpec.builder("-x", "-abcd", "-aaa").build().longestName());
    }

    @Test
    public void testClearArrayOptionOldValueBeforeParse() {
        CommandSpec cmd = CommandSpec.create();
        cmd.addOption(OptionSpec.builder("-x").arity("2..3").initialValue(new String[] {"ABC"}).build());

        CommandLine cl = new CommandLine(cmd);
        cl.parseArgs("-x", "1", "2", "3");
        assertArrayEquals(new String[] {"1", "2", "3"}, (String[]) cmd.findOption("x").getValue());
        assertArrayEquals(new String[] {"1", "2", "3"}, (String[]) cmd.findOption('x').getValue());

        cl.parseArgs("-x", "4", "5");
        assertArrayEquals(new String[] {"4", "5"}, (String[]) cmd.findOption("x").getValue());
        assertArrayEquals(new String[] {"4", "5"}, (String[]) cmd.findOption('x').getValue());

        cl.parseArgs();
        assertArrayEquals(new String[] {"ABC"}, (String[]) cmd.findOption("x").getValue());
        assertArrayEquals(new String[] {"ABC"}, (String[]) cmd.findOption('x').getValue());
    }

    @Test
    public void testDontClearArrayOptionOldValueBeforeParse() {
        CommandSpec cmd = CommandSpec.create();
        cmd.addOption(OptionSpec.builder("-x").arity("2..3").initialValue(new String[] {"ABC"}).hasInitialValue(false).build());

        CommandLine cl = new CommandLine(cmd);
        cl.parseArgs("-x", "1", "2", "3");
        assertArrayEquals(new String[] {"1", "2", "3"}, (String[]) cmd.findOption("x").getValue());

        cl.parseArgs("-x", "4", "5");
        assertArrayEquals(new String[] {"4", "5"}, (String[]) cmd.findOption("x").getValue());

        cl.parseArgs();
        assertArrayEquals(new String[] {"4", "5"}, (String[]) cmd.findOption("x").getValue());
    }

    @Test
    public void testClearListOptionOldValueBeforeParse() {
        CommandSpec cmd = CommandSpec.create();
        cmd.addOption(OptionSpec.builder("-x").type(List.class).initialValue(Arrays.asList("ABC")).build());

        CommandLine cl = new CommandLine(cmd);
        cl.parseArgs("-x", "1", "-x", "2", "-x", "3");
        assertEquals(Arrays.asList("1", "2", "3"), cmd.findOption("x").getValue());

        cl.parseArgs("-x", "4", "-x", "5");
        assertEquals(Arrays.asList("4", "5"), cmd.findOption("x").getValue());

        cl.parseArgs();
        assertEquals(Arrays.asList("ABC"), cmd.findOption("x").getValue());
    }

    @Test
    public void testDontClearListOptionOldValueBeforeParseIfInitialValueFalse() {
        CommandSpec cmd = CommandSpec.create();
        cmd.addOption(OptionSpec.builder("-x").type(List.class).initialValue(Arrays.asList("ABC")).hasInitialValue(false).build());

        CommandLine cl = new CommandLine(cmd);
        cl.parseArgs("-x", "1", "-x", "2", "-x", "3");
        assertEquals(Arrays.asList("1", "2", "3"), cmd.findOption("x").getValue());

        cl.parseArgs("-x", "4", "-x", "5");
        assertEquals(Arrays.asList("4", "5"), cmd.findOption("x").getValue());

        cl.parseArgs();
        assertEquals(Arrays.asList("4", "5"), cmd.findOption("x").getValue());
    }

    @Test
    public void testClearMapOptionOldValueBeforeParse() {
        CommandSpec cmd = CommandSpec.create();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ABC", "XYZ");
        cmd.addOption(OptionSpec.builder("-x").type(Map.class).initialValue(map).build());

        CommandLine cl = new CommandLine(cmd);
        cl.parseArgs("-x", "A=1", "-x", "B=2", "-x", "C=3");
        Map<String, String> expected = new LinkedHashMap<String, String>();
        expected.put("A", "1");
        expected.put("B", "2");
        expected.put("C", "3");
        assertEquals(expected, cmd.findOption("x").getValue());

        cl.parseArgs("-x", "D=4", "-x", "E=5");
        expected = new LinkedHashMap<String, String>();
        expected.put("D", "4");
        expected.put("E", "5");
        assertEquals(expected, cmd.findOption("x").getValue());

        cl.parseArgs();
        expected = new LinkedHashMap<String, String>();
        assertEquals(map, cmd.findOption("x").getValue());
    }

    @Test
    public void testDontClearInitialValueBeforeParseIfInitialValueFalse() {
        CommandSpec cmd = CommandSpec.create();
        Map<String, String> map = new HashMap<String, String>();
        map.put("ABC", "XYZ");
        cmd.addOption(OptionSpec.builder("-x").type(Map.class).initialValue(map).hasInitialValue(true).build());

        CommandLine cl = new CommandLine(cmd);
        cl.parseArgs("-x", "A=1", "-x", "B=2", "-x", "C=3");
        Map<String, String> expected = new LinkedHashMap<String, String>();
        expected.put("A", "1");
        expected.put("B", "2");
        expected.put("C", "3");
        assertEquals(expected, cmd.findOption("x").getValue());

        cl.parseArgs("-x", "D=4", "-x", "E=5");
        expected = new LinkedHashMap<String, String>();
        expected.put("D", "4");
        expected.put("E", "5");
        assertEquals(expected, cmd.findOption("x").getValue());

        cl.parseArgs();
        expected = new LinkedHashMap<String, String>();
        expected.put("ABC", "XYZ");
        assertEquals(expected, cmd.findOption("x").getValue());
    }

    @Test
    public void testClearScalarOptionOldValueBeforeParse() {
        CommandSpec cmd = CommandSpec.create();
        cmd.addOption(OptionSpec.builder("-x").type(String.class).initialValue(null).build());

        CommandLine cl = new CommandLine(cmd);
        cl.parseArgs("-x", "1");
        assertEquals("1", cmd.findOption("x").getValue());

        cl.parseArgs("-x", "2");
        assertEquals("2", cmd.findOption("x").getValue());

        cl.parseArgs();
        assertNull(cmd.findOption("x").getValue());
    }

    @Test
    public void testDontClearScalarOptionOldValueBeforeParseIfInitialValueFalse() {
        CommandSpec cmd = CommandSpec.create();
        cmd.addOption(OptionSpec.builder("-x").type(String.class).initialValue(null).hasInitialValue(false).build());

        CommandLine cl = new CommandLine(cmd);
        cl.parseArgs("-x", "1");
        assertEquals("1", cmd.findOption("x").getValue());

        cl.parseArgs("-x", "2");
        assertEquals("2", cmd.findOption("x").getValue());

        cl.parseArgs();
        assertEquals("2", cmd.findOption("x").getValue());
    }

    @Test
    public void testOptionClearCustomSetterBeforeParse() {
        CommandSpec cmd = CommandSpec.create();
        final List<Object> values = new ArrayList<Object>();
        ISetter setter = new ISetter() {
            public <T> T set(T value) {
                values.add(value);
                return null;
            }
        };
        cmd.addOption(OptionSpec.builder("-x").type(String.class).setter(setter).build());

        CommandLine cl = new CommandLine(cmd);
        assertTrue(values.isEmpty());
        cl.parseArgs("-x", "1");
        assertEquals(2, values.size());
        assertEquals(null, values.get(0));
        assertEquals("1", values.get(1));

        values.clear();
        cl.parseArgs("-x", "2");
        assertEquals(2, values.size());
        assertEquals(null, values.get(0));
        assertEquals("2", values.get(1));
    }

    @Test
    public void testPositionalClearCustomSetterBeforeParse() {
        CommandSpec cmd = CommandSpec.create();
        final List<Object> values = new ArrayList<Object>();
        ISetter setter = new ISetter() {
            public <T> T set(T value) {
                values.add(value);
                return null;
            }
        };
        cmd.add(PositionalParamSpec.builder().type(String.class).setter(setter).build());

        CommandLine cl = new CommandLine(cmd);
        assertTrue(values.isEmpty());
        cl.parseArgs("1");
        assertEquals(2, values.size());
        assertEquals(null, values.get(0));
        assertEquals("1", values.get(1));

        values.clear();
        cl.parseArgs("2");
        assertEquals(2, values.size());
        assertEquals(null, values.get(0));
        assertEquals("2", values.get(1));
    }

    @Test
    public void test381_NPE_whenAddingSubcommand() {
        CommandSpec toplevel = CommandSpec.create();
        toplevel.addOption(OptionSpec.builder("-o").description("o option").build());

        CommandSpec sub = CommandSpec.create();
        sub.addOption(OptionSpec.builder("-x").description("x option").build());

        CommandLine commandLine = new CommandLine(toplevel);
        commandLine.addSubcommand("sub", sub); // NPE here
        commandLine.usage(System.out);

        String expected = String.format("" +
                "Usage: <main class> [-o] [COMMAND]%n" +
                "  -o     o option%n" +
                "Commands:%n" +
                "  sub%n");
        assertEquals(expected, systemOutRule.getLog());
    }

    @Test
    public void testSubcommandNameIsInitializedWhenAddedToParent() {
        CommandSpec toplevel = CommandSpec.create();
        toplevel.addOption(OptionSpec.builder("-o").description("o option").build());

        CommandSpec sub = CommandSpec.create();
        sub.addOption(OptionSpec.builder("-x").description("x option").build());

        CommandLine commandLine = new CommandLine(toplevel);
        CommandLine subCommandLine = new CommandLine(sub);
        assertEquals("<main class>", sub.name());
        assertEquals("<main class>", subCommandLine.getCommandName());

        commandLine.addSubcommand("sub", subCommandLine);
        assertEquals("sub", sub.name());
        assertEquals("sub", subCommandLine.getCommandName());

        subCommandLine.usage(System.out);

        String expected = String.format("" +
                "Usage: <main class> sub [-x]%n" +
                "  -x     x option%n");
        assertEquals(expected, systemOutRule.getLog());
    }

    @Test
    public void testSubcommandNameNotOverwrittenWhenAddedToParent() {
        CommandSpec toplevel = CommandSpec.create();
        toplevel.addOption(OptionSpec.builder("-o").description("o option").build());

        CommandSpec sub = CommandSpec.create().name("SOMECOMMAND");
        sub.addOption(OptionSpec.builder("-x").description("x option").build());

        CommandLine commandLine = new CommandLine(toplevel);
        CommandLine subCommandLine = new CommandLine(sub);
        assertEquals("SOMECOMMAND", sub.name());
        assertEquals("SOMECOMMAND", subCommandLine.getCommandName());

        commandLine.addSubcommand("sub", subCommandLine);
        assertEquals("SOMECOMMAND", sub.name());
        assertEquals("SOMECOMMAND", subCommandLine.getCommandName());

        subCommandLine.usage(System.out);

        String expected = String.format("" +
                "Usage: <main class> SOMECOMMAND [-x]%n" +
                "  -x     x option%n");
        assertEquals(expected, systemOutRule.getLog());
    }

    @Test
    public void testInject_AnnotatedFieldInjected() {
        class Injected {
            @Spec CommandSpec commandSpec;
            @Parameters String[] params;
        }
        Injected injected = new Injected();
        assertNull(injected.commandSpec);

        CommandLine cmd = new CommandLine(injected);
        assertSame(cmd.getCommandSpec(), injected.commandSpec);
    }

    @Test
    public void testInject_AnnotatedFieldInjectedForSubcommand() {
        class Injected {
            @Spec CommandSpec commandSpec;
            @Parameters String[] params;
        }
        Injected injected = new Injected();
        Injected sub = new Injected();

        assertNull(injected.commandSpec);
        assertNull(sub.commandSpec);

        CommandLine cmd = new CommandLine(injected);
        assertSame(cmd.getCommandSpec(), injected.commandSpec);

        CommandLine subcommand = new CommandLine(sub);
        assertSame(subcommand.getCommandSpec(), sub.commandSpec);
    }

    @Test
    public void testInject_FieldMustBeCommandSpec() {
        class Injected {
            @Spec CommandLine commandLine;
            @Parameters String[] params;
        }
        Injected injected = new Injected();
        try {
            new CommandLine(injected);
            fail("Expect exception");
        } catch (InitializationException ex) {
            assertEquals("@picocli.CommandLine.Spec annotation is only supported on fields of type picocli.CommandLine$Model$CommandSpec", ex.getMessage());
        }
    }

    @Test
    public void testCommandSpecQualifiedName_topLevelCommand() {
        CommandLine cmd = new CommandLine(new I18nCommand());
        assertEquals("i18n-top", cmd.getCommandSpec().qualifiedName("."));
        Map<String, CommandLine> subcommands = cmd.getSubcommands();
        assertEquals("i18n-top.help", subcommands.get("help").getCommandSpec().qualifiedName("."));

        CommandLine sub = subcommands.get("i18n-sub");
        assertEquals("i18n-top.i18n-sub", sub.getCommandSpec().qualifiedName("."));
        assertEquals("i18n-top.i18n-sub.help", sub.getSubcommands().get("help").getCommandSpec().qualifiedName("."));
    }

    @Test
    public void testCommandSpecParserSetter() {
        CommandSpec spec = CommandSpec.wrapWithoutInspection(null);
        ParserSpec old = spec.parser();
        assertSame(old, spec.parser());
        assertFalse(spec.parser().collectErrors());
        assertFalse(spec.parser().caseInsensitiveEnumValuesAllowed());

        ParserSpec update = new ParserSpec().collectErrors(true).caseInsensitiveEnumValuesAllowed(true);
        spec.parser(update);
        assertSame(old, spec.parser());
        assertTrue(spec.parser().collectErrors());
        assertTrue(spec.parser().caseInsensitiveEnumValuesAllowed());
    }

    @Test
    public void testCommandSpecUsageMessageSetter() {
        CommandSpec spec = CommandSpec.wrapWithoutInspection(null);
        UsageMessageSpec old = spec.usageMessage();
        assertSame(old, spec.usageMessage());
        assertArrayEquals(new String[0], spec.usageMessage().description());

        UsageMessageSpec update = new UsageMessageSpec().description("hi");
        spec.usageMessage(update);
        assertSame(old, spec.usageMessage());
        assertArrayEquals(new String[] {"hi"}, spec.usageMessage().description());
    }

    @Test
    public void testCommandSpecAddSubcommand_DisallowsDuplicateSubcommandNames() {
        CommandSpec spec = CommandSpec.wrapWithoutInspection(null);
        CommandSpec sub = CommandSpec.wrapWithoutInspection(null);

        spec.addSubcommand("a", new CommandLine(sub));
        try {
            spec.addSubcommand("a", new CommandLine(sub));
        } catch (InitializationException ex) {
            assertEquals("Another subcommand named 'a' already exists for command '<main class>'", ex.getMessage());
        }
    }

    @Test
    public void testCommandSpecAddSubcommand_DisallowsDuplicateSubcommandAliases() {
        CommandSpec spec = CommandSpec.wrapWithoutInspection(null);
        CommandSpec sub = CommandSpec.wrapWithoutInspection(null);

        spec.addSubcommand("a", new CommandLine(sub));

        CommandSpec sub2 = CommandSpec.wrapWithoutInspection(null);
        sub2.aliases("a");
        try {
            spec.addSubcommand("x", new CommandLine(sub2));
        } catch (InitializationException ex) {
            assertEquals("Alias 'a' for subcommand 'x' is already used by another subcommand of '<main class>'", ex.getMessage());
        }
    }

    @Test
    public void testCommandSpecAddSubcommand_SubcommandInheritsResourceBundle() {
        ResourceBundle rb = ResourceBundle.getBundle("picocli.SharedMessages");
        CommandSpec spec = CommandSpec.wrapWithoutInspection(null);
        spec.resourceBundle(rb);
        assertSame(rb, spec.resourceBundle());

        CommandSpec sub = CommandSpec.wrapWithoutInspection(null);
        spec.addSubcommand("a", new CommandLine(sub));

        assertSame(rb, sub.resourceBundle());
    }

    @Test
    public void testAliasesWithEmptyArray() {
        CommandSpec spec = CommandSpec.wrapWithoutInspection(null);
        assertArrayEquals(new String[0], spec.aliases());
        spec.aliases((String[]) null);
        assertArrayEquals(new String[0], spec.aliases());
    }

    @Test
    public void testNamesIncludesAliases() {
        CommandSpec spec = CommandSpec.wrapWithoutInspection(null);
        spec.aliases("a", "b", "d");
        spec.name("c");
        Set<String> all = spec.names();
        assertArrayEquals(new String[] {"c", "a", "b", "d"}, all.toArray(new String[0]));
    }

    @Test
    public void testInitHelpCommand() {
        CommandSpec spec = CommandSpec.wrapWithoutInspection(null);
        assertFalse(spec.helpCommand());

        CommandSpec mixin = CommandSpec.wrapWithoutInspection(null);
        mixin.helpCommand(true);

        spec.addMixin("helper", mixin);
        assertTrue(spec.helpCommand());
    }

    @Test
    public void testResemblesOption_WhenUnmatchedArePositional() {
        CommandSpec spec = CommandSpec.wrapWithoutInspection(null);
        spec.parser().unmatchedOptionsArePositionalParams(true);
        assertFalse(spec.resemblesOption("blah"));

        assertFalse(spec.resemblesOption("blah"));

        assertFalse(spec.resemblesOption("blah"));
    }

    @Test
    public void testResemblesOption_WithoutOptions() {
        CommandSpec spec = CommandSpec.wrapWithoutInspection(null);
        spec.parser().unmatchedOptionsArePositionalParams(false);
        assertFalse(spec.resemblesOption("blah"));

        assertFalse(spec.resemblesOption("blah"));
        assertTrue(spec.resemblesOption("-a"));

        assertFalse(spec.resemblesOption("blah"));
        assertTrue(spec.resemblesOption("-a"));
    }

    @Test
    public void testResemblesOption_WithOptionsDash() {
        CommandSpec spec = CommandSpec.wrapWithoutInspection(null);
        spec.addOption(OptionSpec.builder("-x").build());

        spec.parser().unmatchedOptionsArePositionalParams(false);
        assertFalse(spec.resemblesOption("blah"));

        assertFalse(spec.resemblesOption("blah"));
        assertTrue(spec.resemblesOption("-a"));
        assertFalse(spec.resemblesOption("/a"));

        assertFalse(spec.resemblesOption("blah"));
        assertTrue(spec.resemblesOption("-a"));
        assertFalse(spec.resemblesOption("/a"));
    }

    @Test
    public void testResemblesOption_WithOptionsNonDash() {
        CommandSpec spec = CommandSpec.wrapWithoutInspection(null);
        spec.addOption(OptionSpec.builder("/x").build());

        spec.parser().unmatchedOptionsArePositionalParams(false);
        assertFalse(spec.resemblesOption("blah"));

        assertFalse(spec.resemblesOption("blah"));
        assertFalse(spec.resemblesOption("-a"));
        assertTrue(spec.resemblesOption("/a"));

        assertFalse(spec.resemblesOption("blah"));
        assertFalse(spec.resemblesOption("-a"));
        assertTrue(spec.resemblesOption("/a"));
    }

    @Test
    public void testUsageSpec_CustomSynopsisSetter() {
        UsageMessageSpec usage = new UsageMessageSpec();
        assertArrayEquals(new String[0], usage.customSynopsis());

        usage.customSynopsis("abc", "def");
        assertArrayEquals(new String[] {"abc", "def"}, usage.customSynopsis());
    }

    @Test
    public void testUsageSpec_HiddenSetter() {
        UsageMessageSpec usage = new UsageMessageSpec();
        assertFalse(usage.hidden());

        usage.hidden(true);
        assertTrue(usage.hidden());
    }

    @Test
    public void testUsageSpec_commandListHeading() {
        UsageMessageSpec usage = new UsageMessageSpec();
        assertEquals("Commands:%n", usage.commandListHeading());

        usage.commandListHeading("abcdef");
        assertEquals("abcdef", usage.commandListHeading());
    }

    @Test
    public void testUsageSpec_InitFromMixin() {
        UsageMessageSpec usage = new UsageMessageSpec();
        assertFalse(usage.hidden());

        UsageMessageSpec mixin = new UsageMessageSpec();
        mixin.hidden(true);
        usage.initFromMixin(mixin, null);

        assertTrue(usage.hidden());
    }

    @Test
    public void testIsAddMethodSubcommandsTrueByDefault() {
        CommandSpec spec = CommandSpec.create();
        assertTrue(spec.isAddMethodSubcommands());
    }

    @Test
    public void testIsAddMethodSubcommandsReturnsSetValue() {
        CommandSpec spec = CommandSpec.create();
        spec.setAddMethodSubcommands(false);
        assertFalse(spec.isAddMethodSubcommands());

        spec.setAddMethodSubcommands(true);
        assertTrue(spec.isAddMethodSubcommands());

        spec.setAddMethodSubcommands(null);
        assertTrue(spec.isAddMethodSubcommands());
    }

    @Test
    public void testRemoveArg() {
        class MyApp {
            // hide this option on all OS's other than Darwin (OSX)
            @Option(names = {"-o", "--open"},
                    description = "On OSX open the finder to the data dir that was downloaded.")
            boolean open = false;
        }
        MyApp myApp = new MyApp();
        CommandLine cmd = new CommandLine(myApp);

        String expectBefore = String.format("" +
                "Usage: <main class> [-o]%n" +
                "  -o, --open   On OSX open the finder to the data dir that was downloaded.%n");
        assertEquals(expectBefore, cmd.getUsageMessage(Ansi.OFF));

        // replace the old "--open" option with a different one that is hidden
        CommandSpec spec = cmd.getCommandSpec();
        OptionSpec old = spec.findOption("--open");
        OptionSpec newOpenOption = OptionSpec.builder(old).hidden(true).build();
        spec.remove(old);
        spec.add(newOpenOption);

        assertFalse(spec.args().contains(old));
        String expectAfter = String.format("" +
                "Usage: <main class>%n");
        assertEquals(expectAfter, cmd.getUsageMessage(Ansi.OFF));
        assertFalse(myApp.open);
        cmd.parseArgs("--open");
        assertTrue(myApp.open);
    }

    @Test
    public void testCommandSpecRemoveSuccess() {
        class Positional {
            @Parameters(index = "0") String first;
            @Parameters(index = "1") String second;
        }
        CommandLine cmd = new CommandLine(new Positional());
        CommandSpec spec = cmd.getCommandSpec();
        PositionalParamSpec first = spec.positionalParameters().get(0);
        assertEquals(CommandLine.Range.valueOf("0"), first.index());
        PositionalParamSpec second = spec.positionalParameters().get(1);
        assertEquals(CommandLine.Range.valueOf("1"), second.index());

        spec.remove(second);
        assertFalse(spec.args().contains(second));
        assertEquals(1, spec.positionalParameters().size());
        assertSame(first, spec.positionalParameters().get(0));
    }

    @Test
    public void testCommandSpecRemoveFailIfInGroup() {
        class Args {
            @Option(names = "-x") int x;
        }
        class App {
            @ArgGroup(exclusive = false, validate = false, multiplicity = "1",
                    headingKey = "headingKeyXXX", heading = "headingXXX", order = 123)
            Args args;
        }

        CommandLine cmd = new CommandLine(new App(), new InnerClassFactory(this));
        CommandSpec spec = cmd.getCommandSpec();

        try {
            spec.remove(spec.findOption("-x"));
            fail("Expected exception");
        } catch (UnsupportedOperationException ex) {
            assertEquals("Cannot remove ArgSpec that is part of an ArgGroup", ex.getMessage());
        }
    }

    @Test
    public void testCommandSpecRemoveFailIfNotExist() {
        class Positional {
            @Parameters(index = "0") String first;
            @Parameters(index = "1") String second;
        }
        CommandLine cmd = new CommandLine(new Positional());
        CommandSpec spec = cmd.getCommandSpec();
        PositionalParamSpec first = spec.positionalParameters().get(0);
        assertEquals(CommandLine.Range.valueOf("0"), first.index());
        PositionalParamSpec second = spec.positionalParameters().get(1);
        assertEquals(CommandLine.Range.valueOf("1"), second.index());

        CommandLine.Model.ArgSpec other = CommandLine.Model.OptionSpec.builder("--name").build();

        try {
            spec.remove(other);
            fail("Expected exception");
        } catch (NoSuchElementException ex) {
            assertEquals("option --name", ex.getMessage());
        }
    }

    @Test
    public void testCommandSpec_SpecElements() {
        class Positional {
            @CommandLine.Spec CommandSpec spec1;
            @CommandLine.Spec CommandSpec spec2;
            @Parameters(index = "0") String first;
            @Parameters(index = "1") String second;
        }
        CommandLine cmd = new CommandLine(new Positional());
        CommandSpec spec = cmd.getCommandSpec();
        List<IAnnotatedElement> specElements = spec.specElements();
        assertEquals(2, specElements.size());
        assertEquals("spec1", specElements.get(0).getName());
        assertEquals("spec2", specElements.get(1).getName());
    }

    @Test
    public void testUsageMessageFromProgrammaticCommandSpec() {
        CommandSpec spec = CommandSpec.create();
        spec.addOption(OptionSpec.builder("-x").splitRegex("").build());
        spec.addPositional(PositionalParamSpec.builder().splitRegex("").build());
        spec.mixinStandardHelpOptions(true);
        String actual = new CommandLine(spec).getUsageMessage(Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> [-hVx] PARAM%n" +
                "      PARAM%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "  -x%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testUsageMessageFromProgrammaticCommandSpecWithSplitRegexSynopsisLabel() {
        CommandSpec spec = CommandSpec.create();
        spec.addOption(OptionSpec.builder("-x").type(String[].class).splitRegex("").splitRegexSynopsisLabel(";").build());
        spec.addPositional(PositionalParamSpec.builder().type(String[].class).splitRegex("").splitRegexSynopsisLabel(";").build());
        spec.mixinStandardHelpOptions(true);
        String actual = new CommandLine(spec).getUsageMessage(Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> [-hV] [-x=PARAM]... PARAM...%n" +
                "      PARAM...%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "  -x=PARAM%n");
        assertEquals(expected, actual);
    }
}
