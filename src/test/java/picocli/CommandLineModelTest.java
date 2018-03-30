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

import java.io.UnsupportedEncodingException;
import java.sql.Types;
import java.util.*;

import org.junit.Test;

import picocli.CommandLine.*;
import picocli.CommandLine.Model.*;
import picocli.CommandLine.Help.Ansi;

import static org.junit.Assert.*;
import static picocli.HelpTestUtil.setTraceLevel;
import static picocli.HelpTestUtil.usageString;
import static picocli.HelpTestUtil.versionString;


public class CommandLineModelTest {
    @Test
    public void testEmptyModelUsageHelp() throws Exception {
        CommandSpec spec = CommandSpec.create();
        CommandLine commandLine = new CommandLine(spec);
        String actual = usageString(commandLine, Ansi.OFF);
        assertEquals(String.format("Usage: <main class>%n"), actual);
    }

    @Test
    public void testEmptyModelParse() throws Exception {
        setTraceLevel("OFF");
        CommandSpec spec = CommandSpec.create();
        CommandLine commandLine = new CommandLine(spec);
        commandLine.setUnmatchedArgumentsAllowed(true);
        commandLine.parse("-p", "123", "abc");
        assertEquals(Arrays.asList("-p", "123", "abc"), commandLine.getUnmatchedArguments());
    }

    @Test
    public void testModelUsageHelp() throws Exception {
        CommandSpec spec = CommandSpec.create();
        spec.addOption(OptionSpec.builder("-h", "--help").usageHelp(true).description("show help and exit").build());
        spec.addOption(OptionSpec.builder("-V", "--version").versionHelp(true).description("show help and exit").build());
        spec.addOption(OptionSpec.builder("-c", "--count").paramLabel("COUNT").arity("1").type(int.class).description("number of times to execute").build());
        CommandLine commandLine = new CommandLine(spec);
        String actual = usageString(commandLine, Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> [-hV] [-c=COUNT]%n" +
                "  -c, --count=COUNT           number of times to execute%n" +
                "  -h, --help                  show help and exit%n" +
                "  -V, --version               show help and exit%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testUsageHelpPositional_empty() throws Exception {
        CommandSpec spec = CommandSpec.create();
        spec.addPositional(PositionalParamSpec.builder().build());
        CommandLine commandLine = new CommandLine(spec);
        String actual = usageString(commandLine, Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> PARAM...%n" +
                "      PARAM...%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testUsageHelpPositional_withDescription() throws Exception {
        CommandSpec spec = CommandSpec.create();
        spec.addPositional(PositionalParamSpec.builder().description("positional param").build());
        CommandLine commandLine = new CommandLine(spec);
        String actual = usageString(commandLine, Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> PARAM...%n" +
                "      PARAM...                positional param%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testUsageHelp_emptyWithAutoHelpMixin() throws Exception {
        CommandSpec spec = CommandSpec.create().addMixin("auto", CommandSpec.forAnnotatedObject(new AutoHelpMixin()));
        CommandLine commandLine = new CommandLine(spec);
        String actual = usageString(commandLine, Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> [-hV]%n" +
                "  -h, --help                  Show this help message and exit.%n" +
                "  -V, --version               Print version information and exit.%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testUsageHelp_CustomizedUsageMessage() throws Exception {
        CommandSpec spec = CommandSpec.create().addMixin("auto", CommandSpec.forAnnotatedObject(new AutoHelpMixin()));
        spec.name("the awesome util");
        spec.usageMessage()
                .descriptionHeading("Description heading%n")
                .description("description line 1", "description line 2")
                .footerHeading("Footer heading%n")
                .footer("footer line 1", "footer line 2")
                .headerHeading("Header heading%n")
                .header("header line 1", "header line 2")
                .optionListHeading("Options%n")
                .parameterListHeading("Positional Parameters%n");
        spec.addPositional(PositionalParamSpec.builder().description("positional param").build());
        CommandLine commandLine = new CommandLine(spec);
        String actual = usageString(commandLine, Ansi.OFF);
        String expected = String.format("" +
                "Header heading%n" +
                "header line 1%n" +
                "header line 2%n" +
                "Usage: the awesome util [-hV] PARAM...%n" +
                "Description heading%n" +
                "description line 1%n" +
                "description line 2%n" +
                "Positional Parameters%n" +
                "      PARAM...                positional param%n" +
                "Options%n" +
                "  -h, --help                  Show this help message and exit.%n" +
                "  -V, --version               Print version information and exit.%n" +
                "Footer heading%n" +
                "footer line 1%n" +
                "footer line 2%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testUsageHelp_abbreviateSynopsisWithoutPositional() throws UnsupportedEncodingException {
        CommandSpec spec = CommandSpec.create();
        spec.usageMessage().abbreviateSynopsis(true).requiredOptionMarker('!').sortOptions(false);
        spec.addOption(OptionSpec.builder("-x").required(true).description("required").build());
        CommandLine commandLine = new CommandLine(spec);
        String actual = usageString(commandLine, Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> [OPTIONS]%n" +
                "! -x                          required%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testUsageHelp_abbreviateSynopsisWithPositional() throws UnsupportedEncodingException {
        CommandSpec spec = CommandSpec.create();
        spec.usageMessage().abbreviateSynopsis(true).requiredOptionMarker('!').sortOptions(false);
        spec.addOption(OptionSpec.builder("-x").required(true).description("required").build());
        spec.addPositional(PositionalParamSpec.builder().arity("1").paramLabel("POSITIONAL").description("positional").build());
        CommandLine commandLine = new CommandLine(spec);
        String actual = usageString(commandLine, Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> [OPTIONS] POSITIONAL...%n" +
                "!     POSITIONAL...           positional%n" +
                "! -x                          required%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testUsageHelp_width_default80() {
        assertEquals(80, UsageMessageSpec.DEFAULT_USAGE_WIDTH);
        assertEquals(UsageMessageSpec.DEFAULT_USAGE_WIDTH, new UsageMessageSpec().width());
    }

    @Test
    public void testUsageHelp_width_configurableWithSystemProperty() {
        System.setProperty("picocli.usage.width", "67");
        try {
            assertEquals(67, new UsageMessageSpec().width());
        } finally {
            System.clearProperty("picocli.usage.width");
        }
    }

    @Test
    public void testUsageHelp_width_SystemPropertyOverrulesSetValue() {
        System.setProperty("picocli.usage.width", "67");
        try {
            assertEquals(67, new UsageMessageSpec().width(123).width());
        } finally {
            System.clearProperty("picocli.usage.width");
        }
    }

    @Test
    public void testUsageHelp_width_setter() {
        UsageMessageSpec spec = new UsageMessageSpec();
        spec.width(67);
        assertEquals(67, spec.width());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUsageHelp_width_setterDisallowsValuesBelow55() {
        new UsageMessageSpec().width(54);
    }

    @Test
    public void testUsageHelp_width_setterAllowsValuesAt55OrHigher() {
        assertEquals(55, new UsageMessageSpec().width(55).width());
        assertEquals(Integer.MAX_VALUE, new UsageMessageSpec().width(Integer.MAX_VALUE).width());
    }

    @Test
    public void testVersionHelp_basic() throws Exception {
        CommandSpec spec = CommandSpec.create().version("1.0", "copyright etc");
        CommandLine commandLine = new CommandLine(spec);
        String actual = versionString(commandLine, Ansi.OFF);
        String expected = String.format("" +
                "1.0%n" +
                "copyright etc%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testVersionHelp_versionProvider() throws Exception {
        IVersionProvider provider = new IVersionProvider() {
            public String[] getVersion() throws Exception {
                return new String[] {"2.0", "by provider"};
            }
        };
        CommandSpec spec = CommandSpec.create().versionProvider(provider);
        CommandLine commandLine = new CommandLine(spec);
        String actual = versionString(commandLine, Ansi.OFF);
        String expected = String.format("" +
                "2.0%n" +
                "by provider%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testVersionHelp_helpCommand() {
        CommandSpec helpCommand = CommandSpec.create().helpCommand(true);
        assertTrue(helpCommand.helpCommand());

        CommandSpec parent = CommandSpec.create().addOption(OptionSpec.builder("-x").required(true).build());
        parent.addSubcommand("help", helpCommand);

        CommandLine commandLine = new CommandLine(parent);
        commandLine.parse("help"); // no missing param exception

        try {
            commandLine.parse();
        } catch (MissingParameterException ex) {
            assertEquals("Missing required option '-x=PARAM'", ex.getMessage());
        }
    }

    @Test
    public void testModelParse() throws Exception {
        CommandSpec spec = CommandSpec.create();
        spec.addOption(OptionSpec.builder("-h", "--help").usageHelp(true).description("show help and exit").build());
        spec.addOption(OptionSpec.builder("-V", "--version").versionHelp(true).description("show help and exit").build());
        spec.addOption(OptionSpec.builder("-c", "--count").paramLabel("COUNT").arity("1").type(int.class).description("number of times to execute").build());
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("-c", "33");
        assertEquals(33, spec.optionsMap().get("-c").getValue());
    } // TODO parse method should return an object offering only the options/positionals that were matched

    @Test
    public void testMultiValueOptionArityAloneIsInsufficient() throws Exception {
        CommandSpec spec = CommandSpec.create();
        OptionSpec option = OptionSpec.builder("-c", "--count").arity("3").type(int.class).build();
        assertFalse(option.isMultiValue());

        spec.addOption(option);
        CommandLine commandLine = new CommandLine(spec);
        try {
            commandLine.parse("-c", "1", "2", "3");
            fail("Expected exception");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched arguments [2, 3]", ex.getMessage());
        }
    }

    @Test
    public void testMultiValuePositionalParamArityAloneIsInsufficient() throws Exception {
        CommandSpec spec = CommandSpec.create();
        PositionalParamSpec positional = PositionalParamSpec.builder().index("0").arity("3").type(int.class).build();
        assertFalse(positional.isMultiValue());

        spec.addPositional(positional);
        CommandLine commandLine = new CommandLine(spec);
        try {
            commandLine.parse("1", "2", "3");
            fail("Expected exception");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched arguments [2, 3]", ex.getMessage());
        }
    }

    @Test
    public void testMultiValueOptionWithArray() throws Exception {
        CommandSpec spec = CommandSpec.create();
        OptionSpec option = OptionSpec.builder("-c", "--count").arity("3").type(int[].class).build();
        assertTrue(option.isMultiValue());

        spec.addOption(option);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("-c", "1", "2", "3");
        assertArrayEquals(new int[] {1, 2, 3}, (int[]) spec.optionsMap().get("-c").getValue());
    }

    @Test
    public void testMultiValuePositionalParamWithArray() throws Exception {
        CommandSpec spec = CommandSpec.create();
        PositionalParamSpec positional = PositionalParamSpec.builder().index("0").arity("3").type(int[].class).build();
        assertTrue(positional.isMultiValue());

        spec.addPositional(positional);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("1", "2", "3");
        assertArrayEquals(new int[] {1, 2, 3}, (int[]) spec.positionalParameters().get(0).getValue());
    }

    @Test
    public void testMultiValueOptionWithListAndAuxTypes() throws Exception {
        CommandSpec spec = CommandSpec.create();
        OptionSpec option = OptionSpec.builder("-c", "--count").arity("3").type(List.class).auxiliaryTypes(Integer.class).build();
        assertTrue(option.isMultiValue());

        spec.addOption(option);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("-c", "1", "2", "3");
        assertEquals(Arrays.asList(1, 2, 3), spec.optionsMap().get("-c").getValue());
    }

    @Test
    public void testMultiValuePositionalParamWithListAndAuxTypes() throws Exception {
        CommandSpec spec = CommandSpec.create();
        PositionalParamSpec positional = PositionalParamSpec.builder().index("0").arity("3").type(List.class).auxiliaryTypes(Integer.class).build();
        assertTrue(positional.isMultiValue());

        spec.addPositional(positional);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("1", "2", "3");
        assertEquals(Arrays.asList(1, 2, 3), spec.positionalParameters().get(0).getValue());
    }

    @Test
    public void testMultiValueOptionWithListWithoutAuxTypes() throws Exception {
        CommandSpec spec = CommandSpec.create();
        OptionSpec option = OptionSpec.builder("-c", "--count").arity("3").type(List.class).build();
        assertTrue(option.isMultiValue());

        spec.addOption(option);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("-c", "1", "2", "3");
        assertEquals(Arrays.asList("1", "2", "3"), spec.optionsMap().get("-c").getValue());
    }

    @Test
    public void testMultiValuePositionalParamWithListWithoutAuxTypes() throws Exception {
        CommandSpec spec = CommandSpec.create();
        PositionalParamSpec positional = PositionalParamSpec.builder().index("0").arity("3").type(List.class).build();
        assertTrue(positional.isMultiValue());

        spec.addPositional(positional);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("1", "2", "3");
        assertEquals(Arrays.asList("1", "2", "3"), spec.positionalParameters().get(0).getValue());
    }

    @Test
    public void testMultiValueOptionWithMapAndAuxTypes() throws Exception {
        CommandSpec spec = CommandSpec.create();
        OptionSpec option = OptionSpec.builder("-c", "--count").arity("3").type(Map.class).auxiliaryTypes(Integer.class, Double.class).build();
        assertTrue(option.isMultiValue());

        spec.addOption(option);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("-c", "1=1.0", "2=2.0", "3=3.0");
        Map<Integer, Double> expected = new LinkedHashMap<Integer, Double>();
        expected.put(1, 1.0);
        expected.put(2, 2.0);
        expected.put(3, 3.0);
        assertEquals(expected, spec.optionsMap().get("-c").getValue());
    }

    @Test
    public void testMultiValuePositionalParamWithMapAndAuxTypes() throws Exception {
        CommandSpec spec = CommandSpec.create();
        PositionalParamSpec positional = PositionalParamSpec.builder().index("0").arity("3").type(Map.class).auxiliaryTypes(Integer.class, Double.class).build();
        assertTrue(positional.isMultiValue());

        spec.addPositional(positional);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("1=1.0", "2=2.0", "3=3.0");
        Map<Integer, Double> expected = new LinkedHashMap<Integer, Double>();
        expected.put(1, 1.0);
        expected.put(2, 2.0);
        expected.put(3, 3.0);
        assertEquals(expected, spec.positionalParameters().get(0).getValue());
    }

    @Test
    public void testMultiValueOptionWithMapWithoutAuxTypes() throws Exception {
        CommandSpec spec = CommandSpec.create();
        OptionSpec option = OptionSpec.builder("-c", "--count").arity("3").type(Map.class).build();
        assertTrue(option.isMultiValue());

        spec.addOption(option);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("-c", "1=1.0", "2=2.0", "3=3.0");
        Map<String, String> expected = new LinkedHashMap<String, String>();
        expected.put("1", "1.0");
        expected.put("2", "2.0");
        expected.put("3", "3.0");
        assertEquals(expected, spec.optionsMap().get("-c").getValue());
    }

    @Test
    public void testMultiValuePositionalParamWithMapWithoutAuxTypes() throws Exception {
        CommandSpec spec = CommandSpec.create();
        PositionalParamSpec positional = PositionalParamSpec.builder().index("0").arity("3").type(Map.class).build();
        assertTrue(positional.isMultiValue());

        spec.addPositional(positional);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("1=1.0", "2=2.0", "3=3.0");
        Map<String, String> expected = new LinkedHashMap<String, String>();
        expected.put("1", "1.0");
        expected.put("2", "2.0");
        expected.put("3", "3.0");
        assertEquals(expected, spec.positionalParameters().get(0).getValue());
    }

    @Test
    public void testOptionIsOption() throws Exception {
        assertTrue(OptionSpec.builder("-x").build().isOption());
    }

    @Test
    public void testOptionIsNotPositional() throws Exception {
        assertFalse(OptionSpec.builder("-x").build().isPositional());
    }

    @Test
    public void testPositionalParamSpecIsNotOption() throws Exception {
        assertFalse(PositionalParamSpec.builder().build().isOption());
    }

    @Test
    public void testPositionalParamSpecIsPositional() throws Exception {
        assertTrue(PositionalParamSpec.builder().build().isPositional());
    }

    @Test
    public void testOptionDefaultUsageHelpIsFalse() throws Exception {
        assertFalse(OptionSpec.builder("-x").build().usageHelp());
    }
    @Test
    public void testOptionDefaultVersionHelpIsFalse() throws Exception {
        assertFalse(OptionSpec.builder("-x").build().versionHelp());
    }
    @Deprecated
    @Test
    public void testOptionDefaultHelpIsFalse() throws Exception {
        assertFalse(OptionSpec.builder("-x").build().help());
    }
    @Test
    public void testOptionDefaultHiddenIsFalse() throws Exception {
        assertFalse(OptionSpec.builder("-x").build().hidden());
    }
    @Test
    public void testPositionalDefaultHiddenIsFalse() throws Exception {
        assertFalse(PositionalParamSpec.builder().build().hidden());
    }
    @Test
    public void testOptionDefaultRequiredIsFalse() throws Exception {
        assertFalse(OptionSpec.builder("-x").build().required());
    }
    @Test
    public void testPositionalDefaultRequiredIsFalse() throws Exception {
        assertFalse(PositionalParamSpec.builder().build().required());
    }

    @Test
    public void testOptionDefaultTypeIsBoolean_withDefaultArity() throws Exception {
        assertEquals(boolean.class, OptionSpec.builder("-x").build().type());
    }

    @Test
    public void testOptionDefaultTypeIsBoolean_withArityZero() throws Exception {
        assertEquals(boolean.class, OptionSpec.builder("-x").arity("0").build().type());
    }

    @Test
    public void testOptionDefaultTypeIsString_withArityOne() throws Exception {
        assertEquals(String.class, OptionSpec.builder("-x").arity("1").build().type());
    }

    @Test
    public void testOptionDefaultTypeIsStringArray_withArityTwo() throws Exception {
        assertEquals(String[].class, OptionSpec.builder("-x").arity("2").build().type());
    }

    @Test
    public void testOptionDefaultAuxiliaryTypesIsDerivedFromType() throws Exception {
        assertArrayEquals(new Class[] {boolean.class}, OptionSpec.builder("-x").build().auxiliaryTypes());
        assertArrayEquals(new Class[] {int.class}, OptionSpec.builder("-x").type(int.class).build().auxiliaryTypes());
    }

    @Test
    public void testOptionDefaultTypDependsOnArity() throws Exception {
        assertEquals(boolean.class, OptionSpec.builder("-x").arity("0").build().type());
        assertEquals(String.class, OptionSpec.builder("-x").arity("1").build().type());
        assertEquals(String.class, OptionSpec.builder("-x").arity("0..1").build().type());
        assertEquals(String[].class, OptionSpec.builder("-x").arity("2").build().type());
        assertEquals(String[].class, OptionSpec.builder("-x").arity("0..2").build().type());
        assertEquals(String[].class, OptionSpec.builder("-x").arity("*").build().type());
    }

    @Test
    public void testOptionAuxiliaryTypeOverridesDefaultType() throws Exception {
        assertEquals(int.class, OptionSpec.builder("-x").auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, OptionSpec.builder("-x").arity("0").auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, OptionSpec.builder("-x").arity("1").auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, OptionSpec.builder("-x").arity("0..1").auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, OptionSpec.builder("-x").arity("2").auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, OptionSpec.builder("-x").arity("0..2").auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, OptionSpec.builder("-x").arity("*").auxiliaryTypes(int.class).build().type());
    }

    @Test
    public void testPositionalDefaultTypeIsString_withDefaultArity() throws Exception {
        assertEquals(String.class, PositionalParamSpec.builder().build().type());
    }

    @Test
    public void testPositionalDefaultTypeIsString_withArityZero() throws Exception {
        assertEquals(String.class, PositionalParamSpec.builder().arity("0").build().type());
    }

    @Test
    public void testPositionalDefaultTypeIsString_withArityOne() throws Exception {
        assertEquals(String.class, PositionalParamSpec.builder().arity("1").build().type());
    }

    @Test
    public void testPositionalDefaultTypeIsStringArray_withArityTwo() throws Exception {
        assertEquals(String[].class, PositionalParamSpec.builder().arity("2").build().type());
    }

    @Test
    public void testPositionalWithArityHasDefaultTypeString() throws Exception {
        assertEquals(String.class, PositionalParamSpec.builder().arity("0").build().type());
        assertEquals(String.class, PositionalParamSpec.builder().arity("1").build().type());
        assertEquals(String.class, PositionalParamSpec.builder().arity("0..1").build().type());
        assertEquals(String[].class, PositionalParamSpec.builder().arity("2").build().type());
        assertEquals(String[].class, PositionalParamSpec.builder().arity("0..2").build().type());
        assertEquals(String[].class, PositionalParamSpec.builder().arity("*").build().type());
    }

    @Test
    public void testPositionalAuxiliaryTypeOverridesDefaultType() throws Exception {
        assertEquals(int.class, PositionalParamSpec.builder().auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, PositionalParamSpec.builder().arity("0").auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, PositionalParamSpec.builder().arity("1").auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, PositionalParamSpec.builder().arity("0..1").auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, PositionalParamSpec.builder().arity("2").auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, PositionalParamSpec.builder().arity("0..2").auxiliaryTypes(int.class).build().type());
        assertEquals(int.class, PositionalParamSpec.builder().arity("*").auxiliaryTypes(int.class).build().type());
    }
    @Test
    public void testPositionalDefaultAuxiliaryTypesIsDerivedFromType() throws Exception {
        assertArrayEquals(new Class[] {String.class}, PositionalParamSpec.builder().build().auxiliaryTypes());
        assertArrayEquals(new Class[] {int.class}, PositionalParamSpec.builder().type(int.class).build().auxiliaryTypes());
    }

    @Test
    public void testOptionDefaultArityIsZeroIfUntyped() throws Exception {
        assertEquals(Range.valueOf("0"), OptionSpec.builder("-x").build().arity());
    }

    @Test
    public void testOptionDefaultArityIsZeroIfTypeBoolean() throws Exception {
        assertEquals(Range.valueOf("0"), OptionSpec.builder("-x").type(boolean.class).build().arity());
        assertEquals(Range.valueOf("0"), OptionSpec.builder("-x").type(Boolean.class).build().arity());
    }

    @Test
    public void testOptionDefaultArityIsOneIfTypeNonBoolean() throws Exception {
        assertEquals(Range.valueOf("1"), OptionSpec.builder("-x").type(int.class).build().arity());
        assertEquals(Range.valueOf("1"), OptionSpec.builder("-x").type(Integer.class).build().arity());
        assertEquals(Range.valueOf("1"), OptionSpec.builder("-x").type(Byte.class).build().arity());
        assertEquals(Range.valueOf("1"), OptionSpec.builder("-x").type(String.class).build().arity());
    }

    @Test
    public void testPositionalDefaultArityIsOneIfUntyped() throws Exception {
        assertEquals(Range.valueOf("1"), PositionalParamSpec.builder().build().arity());
    }

    @Test
    public void testPositionalDefaultArityIsOneIfTypeBoolean() throws Exception {
        assertEquals(Range.valueOf("1"), PositionalParamSpec.builder().type(boolean.class).build().arity());
        assertEquals(Range.valueOf("1"), PositionalParamSpec.builder().type(Boolean.class).build().arity());
    }

    @Test
    public void testPositionalDefaultArityIsOneIfTypeNonBoolean() throws Exception {
        assertEquals(Range.valueOf("1"), PositionalParamSpec.builder().type(int.class).build().arity());
        assertEquals(Range.valueOf("1"), PositionalParamSpec.builder().type(Integer.class).build().arity());
        assertEquals(Range.valueOf("1"), PositionalParamSpec.builder().type(Byte.class).build().arity());
        assertEquals(Range.valueOf("1"), PositionalParamSpec.builder().type(String.class).build().arity());
    }

    @Test
    public void testOptionDefaultSplitRegexIsEmptyString() throws Exception {
        assertEquals("", OptionSpec.builder("-x").build().splitRegex());
    }
    @Test
    public void testPositionalDefaultSplitRegexIsEmptyString() throws Exception {
        assertEquals("", PositionalParamSpec.builder().build().splitRegex());
    }

    @Test
    public void testOptionDefaultDescriptionIsEmptyArray() throws Exception {
        assertArrayEquals(new String[0], OptionSpec.builder("-x").build().description());
    }
    @Test
    public void testPositionalDefaultDescriptionIsEmptyArray() throws Exception {
        assertArrayEquals(new String[0], PositionalParamSpec.builder().build().description());
    }

    @Test
    public void testOptionDefaultParamLabel() throws Exception {
        assertEquals("PARAM", OptionSpec.builder("-x").build().paramLabel());
    }
    @Test
    public void testPositionalDefaultParamLabel() throws Exception {
        assertEquals("PARAM", PositionalParamSpec.builder().build().paramLabel());
    }

    @Test
    public void testPositionalDefaultIndexIsAll() throws Exception {
        assertEquals(Range.valueOf("*"), PositionalParamSpec.builder().build().index());
    }

    @Test
    public void testPositionalDefaultArityIsOne() throws Exception {
        assertEquals(Range.valueOf("1"), PositionalParamSpec.builder().build().arity());
    }

    @Test
    public void testOptionDefaultConvertersIsEmpty() throws Exception {
        assertArrayEquals(new ITypeConverter[0], OptionSpec.builder("-x").build().converters());
    }
    @Test
    public void testPositionalDefaultConvertersIsEmpty() throws Exception {
        assertArrayEquals(new ITypeConverter[0], PositionalParamSpec.builder().build().converters());
    }

    @Test
    public void testOptionConvertersOverridesRegisteredTypeConverter() throws Exception {
        CommandSpec spec = CommandSpec.create();
        spec.addOption(OptionSpec.builder("-c", "--count").paramLabel("COUNT").arity("1").type(int.class).description("number of times to execute").build());
        spec.addOption(OptionSpec.builder("-s", "--sql").paramLabel("SQLTYPE").type(int.class).converters(
                new CommandLineTypeConversionTest.SqlTypeConverter()).description("sql type converter").build());
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("-c", "33", "-s", "BLOB");
        assertEquals(33, spec.optionsMap().get("-c").getValue());
        assertEquals(Types.BLOB, spec.optionsMap().get("-s").getValue());
    }
    @Test
    public void testPositionalConvertersOverridesRegisteredTypeConverter() throws Exception {
        CommandSpec spec = CommandSpec.create();
        spec.addPositional(PositionalParamSpec.builder().paramLabel("COUNT").index("0").type(int.class).description("number of times to execute").build());
        spec.addPositional(PositionalParamSpec.builder().paramLabel("SQLTYPE").index("1").type(int.class).converters(
                new CommandLineTypeConversionTest.SqlTypeConverter()).description("sql type converter").build());
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("33", "BLOB");
        assertEquals(33, spec.positionalParameters().get(0).getValue());
        assertEquals(Types.BLOB, spec.positionalParameters().get(1).getValue());
    }

    @Test
    public void testOptionSpecRequiresAtLeastOneName() throws Exception {
        try {
            OptionSpec.builder(new String[0]).build();
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("Invalid names: []", ex.getMessage());
        }
    }

    @Test
    public void testConversion() {
        // TODO convertion with aux types (abstract field types, generic map with and without explicit type attribute etc)
    }

    /** see <a href="https://github.com/remkop/picocli/issues/279">issue #279</a>  */
    @Test
    public void testSingleValueFieldWithOptionalParameter_279() {
        @Command(name="sample")
        class Sample {
            @Option(names="--foo", arity="0..1") String foo;
        }
        List<CommandLine> parsed1 = new CommandLine(new Sample()).parse();// not specified
        OptionSpec option1 = parsed1.get(0).getCommandSpec().optionsMap().get("--foo");
        assertNull("optional option is null when option not specified", option1.getValue());
        assertTrue("optional option has no raw string value when option not specified", option1.rawStringValues().isEmpty());

        List<CommandLine> parsed2 = new CommandLine(new Sample()).parse("--foo");// specified without value
        OptionSpec option2 = parsed2.get(0).getCommandSpec().optionsMap().get("--foo");
        assertEquals("optional option is empty string when specified without args", "", option2.getValue());
        assertEquals("optional option raw string value when specified without args", "", option2.rawStringValues().get(0));

        List<CommandLine> parsed3 = new CommandLine(new Sample()).parse("--foo", "value");// specified with value
        OptionSpec option3 = parsed3.get(0).getCommandSpec().optionsMap().get("--foo");
        assertEquals("optional option is empty string when specified without args", "value", option3.getValue());
        assertEquals("optional option raw string value when specified without args", "value", option3.rawStringValues().get(0));
    }

    @Test
    public void testOptionBuilderNamesOverwriteInitialValue() {
        OptionSpec option = OptionSpec.builder("-a", "--aaa").names("-b", "--bbb").build();
        assertArrayEquals(new String[] {"-b", "--bbb"}, option.names());
    }

    @Test
    public void testOptionCopyBuilder() {
        OptionSpec option = OptionSpec.builder("-a", "--aaa").arity("1").type(int.class).description("abc").paramLabel("ABC").build();
        OptionSpec copy = option.toBuilder().build();
        assertEquals(option, copy);
        assertNotSame(option, copy);
    }

    @Test
    public void testPositionalCopyBuilder() {
        PositionalParamSpec option = PositionalParamSpec.builder().index("0..34").arity("1").type(int.class).description("abc").paramLabel("ABC").build();
        PositionalParamSpec copy = option.toBuilder().build();
        assertEquals(option, copy);
        assertNotSame(option, copy);
    }

    @Test
    public void testUnmatchedArgsBinding_forStringArraySupplier() {
        setTraceLevel("OFF");
        class ArrayBinding implements IBinding {
            String[] array;
            public <T> T get() throws PicocliException {
                return (T) array;
            }
            public <T> T set(T value) throws PicocliException {
                T old = (T) array;
                array = (String[]) value;
                return old;
            }
        }
        ArrayBinding binding = new ArrayBinding();
        CommandSpec cmd = CommandSpec.create();
        UnmatchedArgsBinding unmatched = UnmatchedArgsBinding.forStringArraySupplier(binding);
        assertSame(binding, unmatched.binding());

        cmd.addUnmatchedArgsBinding(unmatched);
        cmd.addOption(OptionSpec.builder("-x").build());
        ParseResult result = new CommandLine(cmd).parseArgs("-x", "a", "b", "c");

        assertEquals(Arrays.asList("a", "b", "c"), result.unmatched());
        assertArrayEquals(new String[]{"a", "b", "c"}, binding.array);
        assertSame(unmatched, cmd.unmatchedArgsBindings().get(0));
        assertEquals(1, cmd.unmatchedArgsBindings().size());
    }

    @Test
    public void testUnmatchedArgsBinding_forStringListSupplier() {
        setTraceLevel("OFF");
        class ArrayBinding implements IBinding {
            List<String> list;
            public <T> T get() throws PicocliException {
                return (T) list;
            }
            public <T> T set(T value) throws PicocliException {
                T old = (T) list;
                list = (List<String>) value;
                return old;
            }
        }
        ArrayBinding binding = new ArrayBinding();
        CommandSpec cmd = CommandSpec.create();
        UnmatchedArgsBinding unmatched = UnmatchedArgsBinding.forStringListSupplier(binding);
        assertSame(binding, unmatched.binding());

        cmd.addUnmatchedArgsBinding(unmatched);
        cmd.addOption(OptionSpec.builder("-x").build());
        ParseResult result = new CommandLine(cmd).parseArgs("-x", "a", "b", "c");

        assertEquals(Arrays.asList("a", "b", "c"), result.unmatched());
        assertEquals(Arrays.asList("a", "b", "c"), binding.list);
        assertSame(unmatched, cmd.unmatchedArgsBindings().get(0));
        assertEquals(1, cmd.unmatchedArgsBindings().size());
    }

    @Test
    public void testUnmatchedArgsBinding_forStringConsumer() {
        setTraceLevel("OFF");
        class ArrayBinding implements IBinding {
            List<String> list = new ArrayList<String>();
            public <T> T get() throws PicocliException {
                throw new UnsupportedOperationException();
            }
            public <T> T set(T value) throws PicocliException {
                list.add((String) value);
                return null;
            }
        }
        ArrayBinding binding = new ArrayBinding();
        CommandSpec cmd = CommandSpec.create();
        UnmatchedArgsBinding unmatched = UnmatchedArgsBinding.forStringConsumer(binding);
        assertSame(binding, unmatched.binding());

        cmd.addUnmatchedArgsBinding(unmatched);
        cmd.addOption(OptionSpec.builder("-x").build());
        ParseResult result = new CommandLine(cmd).parseArgs("-x", "a", "b", "c");

        assertEquals(Arrays.asList("a", "b", "c"), result.unmatched());
        assertEquals(Arrays.asList("a", "b", "c"), binding.list);
        assertSame(unmatched, cmd.unmatchedArgsBindings().get(0));
        assertEquals(1, cmd.unmatchedArgsBindings().size());
    }

    @Test
    public void testUnmatchedArgsBinding_forStringArraySupplier_withInvalidBinding() {
        setTraceLevel("OFF");
        class ListBinding implements IBinding {
            List<String> list = new ArrayList<String>();
            public <T> T get() throws PicocliException {
                return (T) list;
            }
            public <T> T set(T value) throws PicocliException {
                T old = (T) list;
                list = (List<String>) value;
                return old;
            }
        }
        CommandSpec cmd = CommandSpec.create();
        cmd.addUnmatchedArgsBinding(UnmatchedArgsBinding.forStringArraySupplier(new ListBinding()));
        try {
            new CommandLine(cmd).parseArgs("-x", "a", "b", "c");
        } catch (Exception ex) {
            assertTrue(ex.getMessage().contains("while processing argument at or before arg[0] '-x' in [-x, a, b, c]: java.lang.ClassCastException: java.util.ArrayList"));
        }
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
        try {
            CommandSpec.forAnnotatedObject(new Object());
            fail("Expected error");
        } catch (InitializationException ok) {
            assertEquals("java.lang.Object is not a command: it has no @Command, @Option, @Parameters or @Unmatched annotations", ok.getMessage());
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
    public void testParser_MaxArityIsMaxTotalParams_falseByDefault() {
        assertFalse(CommandSpec.create().parser().maxArityIsMaxTotalParams());
    }

    @Test
    public void testParser_MaxArityIsMaxTotalParams_singleArguments() {
        CommandSpec cmd = CommandSpec.create().addOption(OptionSpec.builder("-x").arity("1..3").build());
        cmd.parser().maxArityIsMaxTotalParams(true);

        ParseResult parseResult = new CommandLine(cmd).parseArgs("-x 1 -x 2 -x 3".split(" "));
        assertEquals(Arrays.asList("1", "2", "3"), parseResult.rawOptionValues('x'));
        assertArrayEquals(new String[]{"1", "2", "3"}, parseResult.optionValue('x', (String[]) null));

        CommandSpec cmd2 = CommandSpec.create().addOption(OptionSpec.builder("-x").arity("1..3").build());
        cmd2.parser().maxArityIsMaxTotalParams(true);
        try {
            new CommandLine(cmd2).parseArgs("-x 1 -x 2 -x 3 -x 4".split(" "));
            fail("expected exception");
        } catch (MaxValuesExceededException ok) {
            assertEquals("option '-x' max number of values (3) exceeded: 4 elements.", ok.getMessage());
        }
    }

    @Test
    public void testParser_MaxArityIsMaxTotalParams_split() {
        CommandSpec cmd = CommandSpec.create().addOption(OptionSpec.builder("-x").arity("1..3").splitRegex(",").build());
        cmd.parser().maxArityIsMaxTotalParams(true);

        ParseResult parseResult = new CommandLine(cmd).parseArgs("-x", "1,2,3");
        assertEquals(Arrays.asList("1,2,3"), parseResult.rawOptionValues('x')); // raw is the original command line argument
        assertArrayEquals(new String[]{"1", "2", "3"}, parseResult.optionValue('x', (String[]) null));

        CommandSpec cmd2 = CommandSpec.create().addOption(OptionSpec.builder("-x").arity("1..3").splitRegex(",").build());
        cmd2.parser().maxArityIsMaxTotalParams(true);
        try {
            new CommandLine(cmd2).parseArgs("-x", "1,2,3,4");
            fail("expected exception");
        } catch (MaxValuesExceededException ok) {
            assertEquals("option '-x' max number of values (3) exceeded: 4 elements.", ok.getMessage());
        }
    }
}
