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
        System.setProperty("picocli.trace", "OFF");
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
        spec.add(new OptionSpec("-c", "--count").arity("3").type(int.class));
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
        spec.add(new PositionalParamSpec().index("0").arity("3").type(int.class));
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
        spec.add(new OptionSpec("-c", "--count").arity("3").type(int[].class));
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("-c", "1", "2", "3");
        assertArrayEquals(new int[] {1, 2, 3}, (int[]) spec.optionsMap().get("-c").getValue());
    }

    @Test
    public void testMultiValuePositionalParamWithArray() throws Exception {
        CommandSpec spec = new CommandSpec();
        spec.add(new PositionalParamSpec().index("0").arity("3").type(int[].class));
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("1", "2", "3");
        assertArrayEquals(new int[] {1, 2, 3}, (int[]) spec.positionalParameters().get(0).getValue());
    }

    @Test
    public void testMultiValueOptionWithListAndAuxTypes() throws Exception {
        CommandSpec spec = new CommandSpec();
        spec.add(new OptionSpec("-c", "--count").arity("3").type(List.class).auxiliaryTypes(Integer.class));
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("-c", "1", "2", "3");
        assertEquals(Arrays.asList(1, 2, 3), spec.optionsMap().get("-c").getValue());
    }

    @Test
    public void testMultiValuePositionalParamWithListAndAuxTypes() throws Exception {
        CommandSpec spec = new CommandSpec();
        spec.add(new PositionalParamSpec().index("0").arity("3").type(List.class).auxiliaryTypes(Integer.class));
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("1", "2", "3");
        assertEquals(Arrays.asList(1, 2, 3), spec.positionalParameters().get(0).getValue());
    }

    @Test
    public void testMultiValueOptionWithListWithoutAuxTypes() throws Exception {
        CommandSpec spec = new CommandSpec();
        spec.add(new OptionSpec("-c", "--count").arity("3").type(List.class));
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("-c", "1", "2", "3");
        assertEquals(Arrays.asList("1", "2", "3"), spec.optionsMap().get("-c").getValue());
    }

    @Test
    public void testMultiValuePositionalParamWithListWithoutAuxTypes() throws Exception {
        CommandSpec spec = new CommandSpec();
        spec.add(new PositionalParamSpec().index("0").arity("3").type(List.class));
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("1", "2", "3");
        assertEquals(Arrays.asList("1", "2", "3"), spec.positionalParameters().get(0).getValue());
    }

    @Test
    public void testMultiValueOptionWithMapAndAuxTypes() throws Exception {
        CommandSpec spec = new CommandSpec();
        spec.add(new OptionSpec("-c", "--count").arity("3").type(Map.class).auxiliaryTypes(Integer.class, Double.class));
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
        spec.add(new PositionalParamSpec().index("0").arity("3").type(Map.class).auxiliaryTypes(Integer.class, Double.class));
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
        spec.add(new OptionSpec("-c", "--count").arity("3").type(Map.class));
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
        spec.add(new PositionalParamSpec().index("0").arity("3").type(Map.class));
        CommandLine commandLine = new CommandLine(spec);
        commandLine.parse("1=1.0", "2=2.0", "3=3.0");
        Map<String, String> expected = new LinkedHashMap<String, String>();
        expected.put("1", "1.0");
        expected.put("2", "2.0");
        expected.put("3", "3.0");
        assertEquals(expected, spec.positionalParameters().get(0).getValue());
    }


    @Test
    public void testArityAloneDoesNotMakePositionalParamMultiValue() throws Exception {
        CommandSpec spec = new CommandSpec();
        spec.add(new PositionalParamSpec().index("0").arity("3").type(int.class));
        CommandLine commandLine = new CommandLine(spec);
        try {
            commandLine.parse("1", "2", "3");
            fail("Expected exception");
        } catch (UnmatchedArgumentException ex) {
            assertEquals("Unmatched arguments [2, 3]", ex.getMessage());
        }
    }

    // TODO tests that verify CommandSpec.validate()

    @Test
    public void testOptionDefaultTypeIsBoolean() throws Exception {
        assertEquals(boolean.class, new OptionSpec("-x").validate().type());
    }

    @Test
    public void testOptionDefaultArityIsZeroIfUntyped() throws Exception {
        assertEquals(Range.valueOf("0"), new OptionSpec("-x").validate().arity());
    }

    @Test
    public void testOptionDefaultArityIsZeroIfTypeBoolean() throws Exception {
        assertEquals(Range.valueOf("0"), new OptionSpec("-x").type(boolean.class).validate().arity());
        assertEquals(Range.valueOf("0"), new OptionSpec("-x").type(Boolean.class).validate().arity());
    }

    @Test
    public void testOptionDefaultArityIsOneIfTypeNonBoolean() throws Exception {
        assertEquals(Range.valueOf("1"), new OptionSpec("-x").type(int.class).validate().arity());
        assertEquals(Range.valueOf("1"), new OptionSpec("-x").type(Integer.class).validate().arity());
        assertEquals(Range.valueOf("1"), new OptionSpec("-x").type(Byte.class).validate().arity());
        assertEquals(Range.valueOf("1"), new OptionSpec("-x").type(String.class).validate().arity());
    }

    @Test
    public void testPositionalDefaultArityIsZeroIfUntyped() throws Exception {
        assertEquals(Range.valueOf("1"), new PositionalParamSpec().validate().arity());
    }

    @Test
    public void testPositionalDefaultArityIsZeroIfTypeBoolean() throws Exception {
        assertEquals(Range.valueOf("1"), new PositionalParamSpec().type(boolean.class).validate().arity());
        assertEquals(Range.valueOf("1"), new PositionalParamSpec().type(Boolean.class).validate().arity());
    }

    @Test
    public void testPositionalDefaultArityIsOneIfTypeNonBoolean() throws Exception {
        assertEquals(Range.valueOf("1"), new PositionalParamSpec().type(int.class).validate().arity());
        assertEquals(Range.valueOf("1"), new PositionalParamSpec().type(Integer.class).validate().arity());
        assertEquals(Range.valueOf("1"), new PositionalParamSpec().type(Byte.class).validate().arity());
        assertEquals(Range.valueOf("1"), new PositionalParamSpec().type(String.class).validate().arity());
    }

    @Test
    public void testOptionDefaultSplitRegexIsEmptyString() throws Exception {
        assertEquals("", new OptionSpec("-x").validate().splitRegex());
    }
    @Test
    public void testPositionalDefaultSplitRegexIsEmptyString() throws Exception {
        assertEquals("", new PositionalParamSpec().validate().splitRegex());
    }

    @Test
    public void testOptionDefaultDescriptionIsEmptyArray() throws Exception {
        assertArrayEquals(new String[0], new OptionSpec("-x").validate().description());
    }
    @Test
    public void testPositionalDefaultDescriptionIsEmptyArray() throws Exception {
        assertArrayEquals(new String[0], new PositionalParamSpec().validate().description());
    }

    @Test
    public void testOptionDefaultParamLabel() throws Exception {
        assertEquals("PARAM", new OptionSpec("-x").validate().paramLabel());
    }
    @Test
    public void testPositionalDefaultParamLabel() throws Exception {
        assertEquals("PARAM", new PositionalParamSpec().validate().paramLabel());
    }

    @Test
    public void testOptionDefaultAuxiliaryTypesIsDerivedFromType() throws Exception {
        assertArrayEquals(new Class[] {boolean.class}, new OptionSpec("-x").validate().auxiliaryTypes());
        assertArrayEquals(new Class[] {int.class}, new OptionSpec("-x").type(int.class).validate().auxiliaryTypes());
    }
    @Test
    public void testPositionalDefaultAuxiliaryTypesIsDerivedFromType() throws Exception {
        assertArrayEquals(new Class[] {String.class}, new PositionalParamSpec().validate().auxiliaryTypes());
        assertArrayEquals(new Class[] {int.class}, new PositionalParamSpec().type(int.class).validate().auxiliaryTypes());
    }

    @Test
    public void testOptionWithArityHasDefaultTypeBoolean() throws Exception {
        assertEquals(boolean.class, new OptionSpec("-x").arity("0").validate().type());
        assertEquals(boolean.class, new OptionSpec("-x").arity("1").validate().type());
        assertEquals(boolean.class, new OptionSpec("-x").arity("0..1").validate().type());
        assertEquals(boolean[].class, new OptionSpec("-x").arity("2").validate().type());
        assertEquals(boolean[].class, new OptionSpec("-x").arity("0..2").validate().type());
        assertEquals(boolean[].class, new OptionSpec("-x").arity("*").validate().type());
    }

    @Test
    public void testOptionAuxiliaryTypeOverridesDefaultType() throws Exception {
        assertEquals(int.class, new OptionSpec("-x").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new OptionSpec("-x").arity("0").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new OptionSpec("-x").arity("1").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new OptionSpec("-x").arity("0..1").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new OptionSpec("-x").arity("2").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new OptionSpec("-x").arity("0..2").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new OptionSpec("-x").arity("*").auxiliaryTypes(int.class).validate().type());
    }

    @Test
    public void testPositionalDefaultTypeIsString() throws Exception {
        assertEquals(String.class, new PositionalParamSpec().validate().type());
    }

    @Test
    public void testPositionalDefaultIndexIsAll() throws Exception {
        assertEquals(Range.valueOf("*"), new PositionalParamSpec().validate().index());
    }

    @Test
    public void testPositionalDefaultArityIsOne() throws Exception {
        assertEquals(Range.valueOf("1"), new PositionalParamSpec().validate().arity());
    }

    @Test
    public void testPositionalWithArityHasDefaultTypeString() throws Exception {
        assertEquals(String.class, new PositionalParamSpec().arity("0").validate().type());
        assertEquals(String.class, new PositionalParamSpec().arity("1").validate().type());
        assertEquals(String.class, new PositionalParamSpec().arity("0..1").validate().type());
        assertEquals(String[].class, new PositionalParamSpec().arity("2").validate().type());
        assertEquals(String[].class, new PositionalParamSpec().arity("0..2").validate().type());
        assertEquals(String[].class, new PositionalParamSpec().arity("*").validate().type());
    }

    @Test
    public void testPositionalAuxiliaryTypeOverridesDefaultType() throws Exception {
        assertEquals(int.class, new PositionalParamSpec().auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new PositionalParamSpec().arity("0").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new PositionalParamSpec().arity("1").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new PositionalParamSpec().arity("0..1").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new PositionalParamSpec().arity("2").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new PositionalParamSpec().arity("0..2").auxiliaryTypes(int.class).validate().type());
        assertEquals(int.class, new PositionalParamSpec().arity("*").auxiliaryTypes(int.class).validate().type());
    }

    @Test
    public void testOptionDefaultConvertersIsEmpty() throws Exception {
        assertArrayEquals(new ITypeConverter[0], new OptionSpec("-x").validate().converters());
    }
    @Test
    public void testPositionalDefaultConvertersIsEmpty() throws Exception {
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

    // TODO split CommandSpecTest, OptionSpecTest and PositionalParamSpecTest into separate classes?
    @Test
    public void testOptionSpecRequiresAtLeastOneName() throws Exception {
        try {
            new OptionSpec(new String[0]).validate();
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("Invalid names: []", ex.getMessage());
        }
    }
}
