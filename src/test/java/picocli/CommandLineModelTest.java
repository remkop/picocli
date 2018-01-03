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

import picocli.CommandLine.CommandSpec;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.InitializationException;
import picocli.CommandLine.OptionSpec;
import picocli.CommandLine.PositionalParamSpec;
import picocli.CommandLine.Range;
import picocli.CommandLine.UnmatchedArgumentException;

import static org.junit.Assert.*;
import static picocli.HelpTestUtil.setTraceLevel;
import static picocli.HelpTestUtil.usageString;

public class CommandLineModelTest {
    @Test
    public void testEmptyModelHelp() throws Exception {
        CommandSpec spec = new CommandSpec();
        CommandLine commandLine = new CommandLine(spec);
        String actual = usageString(commandLine, Ansi.OFF);
        assertEquals(String.format("Usage: <main class>%n"), actual);
    }

    @Test
    public void testEmptyModelParse() throws Exception {
        setTraceLevel("OFF");
        CommandSpec spec = new CommandSpec();
        CommandLine commandLine = new CommandLine(spec);
        commandLine.setUnmatchedArgumentsAllowed(true);
        commandLine.parse("-p", "123", "abc");
        assertEquals(Arrays.asList("-p", "123", "abc"), commandLine.getUnmatchedArguments());
    }

    @Test
    public void testModelHelp() throws Exception {
        CommandSpec spec = new CommandSpec();
        spec.add(new OptionSpec("-h", "--help").usageHelp(true).description("show help and exit"));
        spec.add(new OptionSpec("-V", "--version").versionHelp(true).description("show help and exit"));
        spec.add(new OptionSpec("-c", "--count").paramLabel("COUNT").arity("1").type(int.class).description("number of times to execute"));
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
        CommandSpec spec = new CommandSpec();
        spec.add(new OptionSpec("-h", "--help").usageHelp(true).description("show help and exit"));
        spec.add(new OptionSpec("-V", "--version").versionHelp(true).description("show help and exit"));
        spec.add(new OptionSpec("-c", "--count").paramLabel("COUNT").arity("1").type(int.class).description("number of times to execute"));
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("-c", "33");
        assertEquals(33, spec.optionsMap().get("-c").getValue());
    } // TODO parse method should return an object offering only the options/positionals that were matched

    @Test
    public void testMultiValueOptionArityAloneIsInsufficient() throws Exception {
        CommandSpec spec = new CommandSpec();
        OptionSpec option = new OptionSpec("-c", "--count").arity("3").type(int.class);
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
        CommandSpec spec = new CommandSpec();
        PositionalParamSpec positional = new PositionalParamSpec().index("0").arity("3").type(int.class);
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
        CommandSpec spec = new CommandSpec();
        OptionSpec option = new OptionSpec("-c", "--count").arity("3").type(int[].class);
        assertTrue(option.isMultiValue());

        spec.add(option);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("-c", "1", "2", "3");
        assertArrayEquals(new int[] {1, 2, 3}, (int[]) spec.optionsMap().get("-c").getValue());
    }

    @Test
    public void testMultiValuePositionalParamWithArray() throws Exception {
        CommandSpec spec = new CommandSpec();
        PositionalParamSpec positional = new PositionalParamSpec().index("0").arity("3").type(int[].class);
        assertTrue(positional.isMultiValue());

        spec.add(positional);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("1", "2", "3");
        assertArrayEquals(new int[] {1, 2, 3}, (int[]) spec.positionalParameters().get(0).getValue());
    }

    @Test
    public void testMultiValueOptionWithListAndAuxTypes() throws Exception {
        CommandSpec spec = new CommandSpec();
        OptionSpec option = new OptionSpec("-c", "--count").arity("3").type(List.class).auxiliaryTypes(Integer.class);
        assertTrue(option.isMultiValue());

        spec.add(option);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("-c", "1", "2", "3");
        assertEquals(Arrays.asList(1, 2, 3), spec.optionsMap().get("-c").getValue());
    }

    @Test
    public void testMultiValuePositionalParamWithListAndAuxTypes() throws Exception {
        CommandSpec spec = new CommandSpec();
        PositionalParamSpec positional = new PositionalParamSpec().index("0").arity("3").type(List.class).auxiliaryTypes(Integer.class);
        assertTrue(positional.isMultiValue());

        spec.add(positional);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("1", "2", "3");
        assertEquals(Arrays.asList(1, 2, 3), spec.positionalParameters().get(0).getValue());
    }

    @Test
    public void testMultiValueOptionWithListWithoutAuxTypes() throws Exception {
        CommandSpec spec = new CommandSpec();
        OptionSpec option = new OptionSpec("-c", "--count").arity("3").type(List.class);
        assertTrue(option.isMultiValue());

        spec.add(option);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("-c", "1", "2", "3");
        assertEquals(Arrays.asList("1", "2", "3"), spec.optionsMap().get("-c").getValue());
    }

    @Test
    public void testMultiValuePositionalParamWithListWithoutAuxTypes() throws Exception {
        CommandSpec spec = new CommandSpec();
        PositionalParamSpec positional = new PositionalParamSpec().index("0").arity("3").type(List.class);
        assertTrue(positional.isMultiValue());

        spec.add(positional);
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("1", "2", "3");
        assertEquals(Arrays.asList("1", "2", "3"), spec.positionalParameters().get(0).getValue());
    }

    @Test
    public void testMultiValueOptionWithMapAndAuxTypes() throws Exception {
        CommandSpec spec = new CommandSpec();
        OptionSpec option = new OptionSpec("-c", "--count").arity("3").type(Map.class).auxiliaryTypes(Integer.class, Double.class);
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
        CommandSpec spec = new CommandSpec();
        PositionalParamSpec positional = new PositionalParamSpec().index("0").arity("3").type(Map.class).auxiliaryTypes(Integer.class, Double.class);
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
        CommandSpec spec = new CommandSpec();
        OptionSpec option = new OptionSpec("-c", "--count").arity("3").type(Map.class);
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
        CommandSpec spec = new CommandSpec();
        PositionalParamSpec positional = new PositionalParamSpec().index("0").arity("3").type(Map.class);
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
        assertTrue(new OptionSpec("-x").isOption());
    }

    @Test
    public void testOptionIsNotPositional() throws Exception {
        assertFalse(new OptionSpec("-x").isPositional());
    }

    @Test
    public void testPositionalParamSpecIsNotOption() throws Exception {
        assertFalse(new PositionalParamSpec().isOption());
    }

    @Test
    public void testPositionalParamSpecIsPositional() throws Exception {
        assertTrue(new PositionalParamSpec().isPositional());
    }

    @Test
    public void testOptionAfterValidateDefaultUsageHelpIsFalse() throws Exception {
        assertFalse(new OptionSpec("-x").validate().usageHelp());
    }
    @Test
    public void testOptionAfterValidateDefaultVersionHelpIsFalse() throws Exception {
        assertFalse(new OptionSpec("-x").validate().versionHelp());
    }
    @Deprecated
    @Test
    public void testOptionAfterValidateDefaultHelpIsFalse() throws Exception {
        assertFalse(new OptionSpec("-x").validate().help());
    }
    @Test
    public void testOptionAfterValidateDefaultHiddenIsFalse() throws Exception {
        assertFalse(new OptionSpec("-x").validate().hidden());
    }
    @Test
    public void testPositionalAfterValidateDefaultHiddenIsFalse() throws Exception {
        assertFalse(new PositionalParamSpec().validate().hidden());
    }
    @Test
    public void testOptionAfterValidateDefaultRequiredIsFalse() throws Exception {
        assertFalse(new OptionSpec("-x").validate().required());
    }
    @Test
    public void testPositionalAfterValidateDefaultRequiredIsFalse() throws Exception {
        assertFalse(new PositionalParamSpec().validate().required());
    }

    @Test
    public void testOptionBeforeValidateDefaultTypeIsNull() throws Exception {
        assertNull(new OptionSpec("-x").type());
    }

    @Test
    public void testOptionBeforeValidateDefaultAuxiliaryTypesIsNull() throws Exception {
        assertNull(new OptionSpec("-x").auxiliaryTypes());
    }

    @Test
    public void testOptionBeforeValidateDefaultArityIsNull() throws Exception {
        assertNull(new OptionSpec("-x").arity());
    }

    @Test
    public void testOptionBeforeValidateDefaultParamLabelIsNull() throws Exception {
        assertNull(new OptionSpec("-x").paramLabel());
    }

    @Test
    public void testOptionBeforeValidateDefaultToStringIsNull() throws Exception {
        assertNull(new OptionSpec("-x").toString());
    }

    @Test
    public void testOptionBeforeValidateDefaultSplitRegexIsNull() throws Exception {
        assertNull(new OptionSpec("-x").splitRegex());
    }

    @Test
    public void testOptionBeforeValidateDefaultConvertersIsNull() throws Exception {
        assertNull(new OptionSpec("-x").converters());
    }

    @Test
    public void testOptionBeforeValidateDefaultDescriptionIsNull() throws Exception {
        assertNull(new OptionSpec("-x").description());
    }

    @Test
    public void testOptionBeforeValidateDefaultGetterIsObjectGetterSetter() throws Exception {
        assertEquals("picocli.CommandLine$ArgSpec$ObjectGetterSetter", new OptionSpec("-x").getter().getClass().getName());
    }

    @Test
    public void testOptionBeforeValidateDefaultSetterIsObjectGetterSetter() throws Exception {
        assertEquals("picocli.CommandLine$ArgSpec$ObjectGetterSetter", new OptionSpec("-x").setter().getClass().getName());
    }

    @Test
    public void testOptionBeforeValidateDefaultDefaultValueIsNull() throws Exception {
        assertNull(new OptionSpec("-x").defaultValue());
    }

    @Test
    public void testOptionBeforeValidateDefaultGetValueIsNull() throws Exception {
        assertNull(new OptionSpec("-x").getValue());
    }


    @Test
    public void testPositionalBeforeValidateDefaultTypeIsNull() throws Exception {
        assertNull(new PositionalParamSpec().type());
    }

    @Test
    public void testPositionalBeforeValidateDefaultAuxiliaryTypesIsNull() throws Exception {
        assertNull(new PositionalParamSpec().auxiliaryTypes());
    }

    @Test
    public void testPositionalBeforeValidateDefaultArityIsNull() throws Exception {
        assertNull(new PositionalParamSpec().arity());
    }

    @Test
    public void testPositionalBeforeValidateDefaultParamLabelIsNull() throws Exception {
        assertNull(new PositionalParamSpec().paramLabel());
    }

    @Test
    public void testPositionalBeforeValidateDefaultToStringIsNull() throws Exception {
        assertNull(new PositionalParamSpec().toString());
    }

    @Test
    public void testPositionalBeforeValidateDefaultSplitRegexIsNull() throws Exception {
        assertNull(new PositionalParamSpec().splitRegex());
    }

    @Test
    public void testPositionalBeforeValidateDefaultConvertersIsNull() throws Exception {
        assertNull(new PositionalParamSpec().converters());
    }

    @Test
    public void testPositionalBeforeValidateDefaultDescriptionIsNull() throws Exception {
        assertNull(new PositionalParamSpec().description());
    }

    @Test
    public void testPositionalBeforeValidateDefaultGetterIsObjectGetterSetter() throws Exception {
        assertEquals("picocli.CommandLine$ArgSpec$ObjectGetterSetter", new PositionalParamSpec().getter().getClass().getName());
    }

    @Test
    public void testPositionalBeforeValidateDefaultSetterIsObjectGetterSetter() throws Exception {
        assertEquals("picocli.CommandLine$ArgSpec$ObjectGetterSetter", new PositionalParamSpec().setter().getClass().getName());
    }

    @Test
    public void testPositionalBeforeValidateDefaultDefaultValueIsNull() throws Exception {
        assertNull(new PositionalParamSpec().defaultValue());
    }

    @Test
    public void testPositionalBeforeValidateDefaultGetValueIsNull() throws Exception {
        assertNull(new PositionalParamSpec().getValue());
    }

    @Test
    public void testPositionalBeforeValidateDefaultIndexIsNull() throws Exception {
        assertNull(new PositionalParamSpec().index());
    }


    @Test
    public void testOptionAfterValidateDefaultTypeIsBoolean() throws Exception {
        assertEquals(boolean.class, new OptionSpec("-x").validate().type());
    }

    @Test
    public void testOptionAfterValidateDefaultArityIsZeroIfUntyped() throws Exception {
        assertEquals(Range.valueOf("0"), new OptionSpec("-x").validate().arity());
    }

    @Test
    public void testOptionAfterValidateDefaultArityIsZeroIfTypeBoolean() throws Exception {
        assertEquals(Range.valueOf("0"), new OptionSpec("-x").type(boolean.class).validate().arity());
        assertEquals(Range.valueOf("0"), new OptionSpec("-x").type(Boolean.class).validate().arity());
    }

    @Test
    public void testOptionAfterValidateDefaultArityIsOneIfTypeNonBoolean() throws Exception {
        assertEquals(Range.valueOf("1"), new OptionSpec("-x").type(int.class).validate().arity());
        assertEquals(Range.valueOf("1"), new OptionSpec("-x").type(Integer.class).validate().arity());
        assertEquals(Range.valueOf("1"), new OptionSpec("-x").type(Byte.class).validate().arity());
        assertEquals(Range.valueOf("1"), new OptionSpec("-x").type(String.class).validate().arity());
    }

    @Test
    public void testPositionalAfterValidateDefaultArityIsZeroIfUntyped() throws Exception {
        assertEquals(Range.valueOf("1"), new PositionalParamSpec().validate().arity());
    }

    @Test
    public void testPositionalAfterValidateDefaultArityIsZeroIfTypeBoolean() throws Exception {
        assertEquals(Range.valueOf("1"), new PositionalParamSpec().type(boolean.class).validate().arity());
        assertEquals(Range.valueOf("1"), new PositionalParamSpec().type(Boolean.class).validate().arity());
    }

    @Test
    public void testPositionalAfterValidateDefaultArityIsOneIfTypeNonBoolean() throws Exception {
        assertEquals(Range.valueOf("1"), new PositionalParamSpec().type(int.class).validate().arity());
        assertEquals(Range.valueOf("1"), new PositionalParamSpec().type(Integer.class).validate().arity());
        assertEquals(Range.valueOf("1"), new PositionalParamSpec().type(Byte.class).validate().arity());
        assertEquals(Range.valueOf("1"), new PositionalParamSpec().type(String.class).validate().arity());
    }

    @Test
    public void testOptionAfterValidateDefaultSplitRegexIsEmptyString() throws Exception {
        assertEquals("", new OptionSpec("-x").validate().splitRegex());
    }
    @Test
    public void testPositionalAfterValidateDefaultSplitRegexIsEmptyString() throws Exception {
        assertEquals("", new PositionalParamSpec().validate().splitRegex());
    }

    @Test
    public void testOptionAfterValidateDefaultDescriptionIsEmptyArray() throws Exception {
        assertArrayEquals(new String[0], new OptionSpec("-x").validate().description());
    }
    @Test
    public void testPositionalAfterValidateDefaultDescriptionIsEmptyArray() throws Exception {
        assertArrayEquals(new String[0], new PositionalParamSpec().validate().description());
    }

    @Test
    public void testOptionAfterValidateDefaultParamLabel() throws Exception {
        assertEquals("PARAM", new OptionSpec("-x").validate().paramLabel());
    }
    @Test
    public void testPositionalAfterValidateDefaultParamLabel() throws Exception {
        assertEquals("PARAM", new PositionalParamSpec().validate().paramLabel());
    }

    @Test
    public void testOptionAfterValidateDefaultAuxiliaryTypesIsDerivedFromType() throws Exception {
        assertArrayEquals(new Class[] {boolean.class}, new OptionSpec("-x").validate().auxiliaryTypes());
        assertArrayEquals(new Class[] {int.class}, new OptionSpec("-x").type(int.class).validate().auxiliaryTypes());
    }
    @Test
    public void testPositionalAfterValidateDefaultAuxiliaryTypesIsDerivedFromType() throws Exception {
        assertArrayEquals(new Class[] {String.class}, new PositionalParamSpec().validate().auxiliaryTypes());
        assertArrayEquals(new Class[] {int.class}, new PositionalParamSpec().type(int.class).validate().auxiliaryTypes());
    }

    @Test
    public void testOptionAfterValidateWithArityHasDefaultTypeBoolean() throws Exception {
        assertEquals(boolean.class, new OptionSpec("-x").arity("0").validate().type());
        assertEquals(boolean.class, new OptionSpec("-x").arity("1").validate().type());
        assertEquals(boolean.class, new OptionSpec("-x").arity("0..1").validate().type());
        assertEquals(boolean[].class, new OptionSpec("-x").arity("2").validate().type());
        assertEquals(boolean[].class, new OptionSpec("-x").arity("0..2").validate().type());
        assertEquals(boolean[].class, new OptionSpec("-x").arity("*").validate().type());
    }

    @Test
    public void testOptionAfterValidateAuxiliaryTypeOverridesDefaultType() throws Exception {
        assertEquals(int.class, new OptionSpec("-x").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new OptionSpec("-x").arity("0").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new OptionSpec("-x").arity("1").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new OptionSpec("-x").arity("0..1").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new OptionSpec("-x").arity("2").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new OptionSpec("-x").arity("0..2").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new OptionSpec("-x").arity("*").auxiliaryTypes(int.class).validate().type());
    }

    @Test
    public void testPositionalAfterValidateDefaultTypeIsString() throws Exception {
        assertEquals(String.class, new PositionalParamSpec().validate().type());
    }

    @Test
    public void testPositionalAfterValidateDefaultIndexIsAll() throws Exception {
        assertEquals(Range.valueOf("*"), new PositionalParamSpec().validate().index());
    }

    @Test
    public void testPositionalAfterValidateDefaultArityIsOne() throws Exception {
        assertEquals(Range.valueOf("1"), new PositionalParamSpec().validate().arity());
    }

    @Test
    public void testPositionalAfterValidateWithArityHasDefaultTypeString() throws Exception {
        assertEquals(String.class, new PositionalParamSpec().arity("0").validate().type());
        assertEquals(String.class, new PositionalParamSpec().arity("1").validate().type());
        assertEquals(String.class, new PositionalParamSpec().arity("0..1").validate().type());
        assertEquals(String[].class, new PositionalParamSpec().arity("2").validate().type());
        assertEquals(String[].class, new PositionalParamSpec().arity("0..2").validate().type());
        assertEquals(String[].class, new PositionalParamSpec().arity("*").validate().type());
    }

    @Test
    public void testPositionalAfterValidateAuxiliaryTypeOverridesDefaultType() throws Exception {
        assertEquals(int.class, new PositionalParamSpec().auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new PositionalParamSpec().arity("0").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new PositionalParamSpec().arity("1").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new PositionalParamSpec().arity("0..1").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new PositionalParamSpec().arity("2").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new PositionalParamSpec().arity("0..2").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new PositionalParamSpec().arity("*").auxiliaryTypes(int.class).validate().type());
    }

    @Test
    public void testOptionAfterValidateDefaultConvertersIsEmpty() throws Exception {
        assertArrayEquals(new ITypeConverter[0], new OptionSpec("-x").validate().converters());
    }
    @Test
    public void testPositionalAfterValidateDefaultConvertersIsEmpty() throws Exception {
        assertArrayEquals(new ITypeConverter[0], new PositionalParamSpec().validate().converters());
    }

    @Test
    public void testOptionConvertersOverridesRegisteredTypeConverter() throws Exception {
        CommandSpec spec = new CommandSpec();
        spec.add(new OptionSpec("-c", "--count").paramLabel("COUNT").arity("1").type(int.class).description("number of times to execute"));
        spec.add(new OptionSpec("-s", "--sql").paramLabel("SQLTYPE").type(int.class).converters(
                new CommandLineTypeConversionTest.SqlTypeConverter()).description("sql type converter"));
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("-c", "33", "-s", "BLOB");
        assertEquals(33, spec.optionsMap().get("-c").getValue());
        assertEquals(Types.BLOB, spec.optionsMap().get("-s").getValue());
    }
    @Test
    public void testPositionalConvertersOverridesRegisteredTypeConverter() throws Exception {
        CommandSpec spec = new CommandSpec();
        spec.add(new PositionalParamSpec().paramLabel("COUNT").index("0").type(int.class).description("number of times to execute"));
        spec.add(new PositionalParamSpec().paramLabel("SQLTYPE").index("1").type(int.class).converters(
                new CommandLineTypeConversionTest.SqlTypeConverter()).description("sql type converter"));
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("33", "BLOB");
        assertEquals(33, spec.positionalParameters().get(0).getValue());
        assertEquals(Types.BLOB, spec.positionalParameters().get(1).getValue());
    }

    @Test
    public void testOptionSpecRequiresAtLeastOneName() throws Exception {
        try {
            new OptionSpec(new String[0]).validate();
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("Invalid names: []", ex.getMessage());
        }
    }

    @Test
    public void testConversion() {
        // TODO convertion with aux types (abstract field types, generic map with and without explicit type attribute etc)
    }
}
