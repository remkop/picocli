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

import java.sql.Types;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import picocli.CommandLine.*;
import picocli.CommandLine.Model.*;
import picocli.CommandLine.Help.Ansi;

import static org.junit.Assert.*;
import static picocli.HelpTestUtil.setTraceLevel;
import static picocli.HelpTestUtil.usageString;

public class CommandLineModelTest {
    @Test
    public void testEmptyModelHelp() throws Exception {
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
    public void testModelHelp() throws Exception {
        CommandSpec spec = CommandSpec.create();
        spec.add(OptionSpec.builder("-h", "--help").usageHelp(true).description("show help and exit").build());
        spec.add(OptionSpec.builder("-V", "--version").versionHelp(true).description("show help and exit").build());
        spec.add(OptionSpec.builder("-c", "--count").paramLabel("COUNT").arity("1").type(int.class).description("number of times to execute").build());
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
    public void testModelParse() throws Exception {
        CommandSpec spec = CommandSpec.create();
        spec.add(OptionSpec.builder("-h", "--help").usageHelp(true).description("show help and exit").build());
        spec.add(OptionSpec.builder("-V", "--version").versionHelp(true).description("show help and exit").build());
        spec.add(OptionSpec.builder("-c", "--count").paramLabel("COUNT").arity("1").type(int.class).description("number of times to execute").build());
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("-c", "33");
        assertEquals(33, spec.optionsMap().get("-c").getValue());
    } // TODO parse method should return an object offering only the options/positionals that were matched

    @Test
    public void testMultiValueOptionArityAloneIsInsufficient() throws Exception {
        CommandSpec spec = CommandSpec.create();
        OptionSpec option = OptionSpec.builder("-c", "--count").arity("3").type(int.class).build();
        assertFalse(option.isMultiValue());

        spec.add(option);
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

        spec.add(positional);
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

        spec.add(option);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("-c", "1", "2", "3");
        assertArrayEquals(new int[] {1, 2, 3}, (int[]) spec.optionsMap().get("-c").getValue());
    }

    @Test
    public void testMultiValuePositionalParamWithArray() throws Exception {
        CommandSpec spec = CommandSpec.create();
        PositionalParamSpec positional = PositionalParamSpec.builder().index("0").arity("3").type(int[].class).build();
        assertTrue(positional.isMultiValue());

        spec.add(positional);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("1", "2", "3");
        assertArrayEquals(new int[] {1, 2, 3}, (int[]) spec.positionalParameters().get(0).getValue());
    }

    @Test
    public void testMultiValueOptionWithListAndAuxTypes() throws Exception {
        CommandSpec spec = CommandSpec.create();
        OptionSpec option = OptionSpec.builder("-c", "--count").arity("3").type(List.class).auxiliaryTypes(Integer.class).build();
        assertTrue(option.isMultiValue());

        spec.add(option);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("-c", "1", "2", "3");
        assertEquals(Arrays.asList(1, 2, 3), spec.optionsMap().get("-c").getValue());
    }

    @Test
    public void testMultiValuePositionalParamWithListAndAuxTypes() throws Exception {
        CommandSpec spec = CommandSpec.create();
        PositionalParamSpec positional = PositionalParamSpec.builder().index("0").arity("3").type(List.class).auxiliaryTypes(Integer.class).build();
        assertTrue(positional.isMultiValue());

        spec.add(positional);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("1", "2", "3");
        assertEquals(Arrays.asList(1, 2, 3), spec.positionalParameters().get(0).getValue());
    }

    @Test
    public void testMultiValueOptionWithListWithoutAuxTypes() throws Exception {
        CommandSpec spec = CommandSpec.create();
        OptionSpec option = OptionSpec.builder("-c", "--count").arity("3").type(List.class).build();
        assertTrue(option.isMultiValue());

        spec.add(option);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("-c", "1", "2", "3");
        assertEquals(Arrays.asList("1", "2", "3"), spec.optionsMap().get("-c").getValue());
    }

    @Test
    public void testMultiValuePositionalParamWithListWithoutAuxTypes() throws Exception {
        CommandSpec spec = CommandSpec.create();
        PositionalParamSpec positional = PositionalParamSpec.builder().index("0").arity("3").type(List.class).build();
        assertTrue(positional.isMultiValue());

        spec.add(positional);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("1", "2", "3");
        assertEquals(Arrays.asList("1", "2", "3"), spec.positionalParameters().get(0).getValue());
    }

    @Test
    public void testMultiValueOptionWithMapAndAuxTypes() throws Exception {
        CommandSpec spec = CommandSpec.create();
        OptionSpec option = OptionSpec.builder("-c", "--count").arity("3").type(Map.class).auxiliaryTypes(Integer.class, Double.class).build();
        assertTrue(option.isMultiValue());

        spec.add(option);
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

        spec.add(positional);
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

        spec.add(option);
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

        spec.add(positional);
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
    public void testOptionDefaultTypeIsBoolean() throws Exception {
        assertEquals(boolean.class, OptionSpec.builder("-x").build().type());
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
    public void testPositionalDefaultArityIsZeroIfUntyped() throws Exception {
        assertEquals(Range.valueOf("1"), PositionalParamSpec.builder().build().arity());
    }

    @Test
    public void testPositionalDefaultArityIsZeroIfTypeBoolean() throws Exception {
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
    public void testOptionDefaultAuxiliaryTypesIsDerivedFromType() throws Exception {
        assertArrayEquals(new Class[] {boolean.class}, OptionSpec.builder("-x").build().auxiliaryTypes());
        assertArrayEquals(new Class[] {int.class}, OptionSpec.builder("-x").type(int.class).build().auxiliaryTypes());
    }
    @Test
    public void testPositionalDefaultAuxiliaryTypesIsDerivedFromType() throws Exception {
        assertArrayEquals(new Class[] {String.class}, PositionalParamSpec.builder().build().auxiliaryTypes());
        assertArrayEquals(new Class[] {int.class}, PositionalParamSpec.builder().type(int.class).build().auxiliaryTypes());
    }

    @Test
    public void testOptionWithArityHasDefaultTypeBoolean() throws Exception {
        assertEquals(boolean.class, OptionSpec.builder("-x").arity("0").build().type());
        assertEquals(boolean.class, OptionSpec.builder("-x").arity("1").build().type());
        assertEquals(boolean.class, OptionSpec.builder("-x").arity("0..1").build().type());
        assertEquals(boolean[].class, OptionSpec.builder("-x").arity("2").build().type());
        assertEquals(boolean[].class, OptionSpec.builder("-x").arity("0..2").build().type());
        assertEquals(boolean[].class, OptionSpec.builder("-x").arity("*").build().type());
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
    public void testPositionalDefaultTypeIsString() throws Exception {
        assertEquals(String.class, PositionalParamSpec.builder().build().type());
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
        spec.add(OptionSpec.builder("-c", "--count").paramLabel("COUNT").arity("1").type(int.class).description("number of times to execute").build());
        spec.add(OptionSpec.builder("-s", "--sql").paramLabel("SQLTYPE").type(int.class).converters(
                new CommandLineTypeConversionTest.SqlTypeConverter()).description("sql type converter").build());
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("-c", "33", "-s", "BLOB");
        assertEquals(33, spec.optionsMap().get("-c").getValue());
        assertEquals(Types.BLOB, spec.optionsMap().get("-s").getValue());
    }
    @Test
    public void testPositionalConvertersOverridesRegisteredTypeConverter() throws Exception {
        CommandSpec spec = CommandSpec.create();
        spec.add(PositionalParamSpec.builder().paramLabel("COUNT").index("0").type(int.class).description("number of times to execute").build());
        spec.add(PositionalParamSpec.builder().paramLabel("SQLTYPE").index("1").type(int.class).converters(
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

}
