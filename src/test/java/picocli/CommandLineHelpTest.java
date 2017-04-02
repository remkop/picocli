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
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.TextTable;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.String;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static org.junit.Assert.*;

/**
 * Tests for picoCLI's "Usage" help functionality.
 */
public class CommandLineHelpTest {
    private static final String LINESEP = System.getProperty("line.separator");
    private static String usageString(Object annotatedObject) throws UnsupportedEncodingException {
        return usageString(new CommandLine(annotatedObject));
    }
    private static String usageString(CommandLine commandLine) throws UnsupportedEncodingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        commandLine.usage(new PrintStream(baos, true, "UTF8"));
        String result = baos.toString("UTF8");
        return result;
    }
    private static Field field(Class<?> cls, String fieldName) throws NoSuchFieldException {
        return cls.getDeclaredField(fieldName);
    }
    private static Field[] fields(Class<?> cls, String... fieldNames) throws NoSuchFieldException {
        Field[] result = new Field[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            result[i] = cls.getDeclaredField(fieldNames[i]);
        }
        return result;
    }

    @Test
    public void testUsageAnnotationDetailedUsageWithoutDefaultValue() throws Exception {
        @CommandLine.Command()
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use") File file;
        }
        String result = usageString(new Params());
        assertEquals(format("" +
                        "Usage: <main class> -f=<file>%n" +
                        "  -f, --file=<file>           the file to use%n",
                ""), result);
    }

    @Test
    public void testUsageAnnotationDetailedUsageWithDefaultValue() throws Exception {
        @CommandLine.Command(showDefaultValues = true)
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use")
            File file = new File("theDefault.txt");
        }
        String result = usageString(new Params());
        assertEquals(format("" +
                        "Usage: <main class> -f=<file>%n" +
                        "  -f, --file=<file>           the file to use%n" +
                        "                              Default: theDefault.txt%n"), result);
    }

    @Test
    public void testUsageSeparatorWithoutDefault() throws Exception {
        @Command()
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use") File file = new File("def.txt");
        }
        String result = usageString(new Params());
        assertEquals(format("" +
                        "Usage: <main class> -f=<file>%n" +
                        "  -f, --file=<file>           the file to use%n",
                ""), result);
    }

    @Test
    public void testUsageSeparator() throws Exception {
        @Command(showDefaultValues = true)
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use") File file = new File("def.txt");
        }
        String result = usageString(new Params());
        assertEquals(format("" +
                        "Usage: <main class> -f=<file>%n" +
                        "  -f, --file=<file>           the file to use%n" +
                        "                              Default: def.txt%n",
                ""), result);
    }

    @Test
    public void testUsageParamLabels() throws Exception {
        @Command()
        class ParamLabels {
            @Option(names = "-f", paramLabel = "FILE", description = "a file") File f;
            @Option(names = "-n", description = "a number") int number;
            @Parameters(index = "0", paramLabel = "NUM", description = "number param") int n;
            @Parameters(index = "1", description = "the host") InetAddress host;
        }
        String result = usageString(new ParamLabels());
        assertEquals(format("" +
                        "Usage: <main class> [-f=FILE] [-n=<number>] NUM <host>%n" +
                        "      NUM                     number param%n" +
                        "      host                    the host%n" +
                        "  -f= FILE                    a file%n" +
                        "  -n= <number>                a number%n",
                ""), result);
    }

    @Test
    public void testShortestFirstComparator_sortsShortestFirst() {
        String[] values = {"12345", "12", "123", "123456", "1", "", "1234"};
        Arrays.sort(values, new Help.ShortestFirst());
        String[] expected = {"", "1", "12", "123", "1234", "12345", "123456"};
        assertArrayEquals(expected, values);
    }

    @Test
    public void testShortestFirstComparator_sortsDeclarationOrderIfEqualLength() {
        String[] values = {"-d", "-", "-a", "--alpha", "--b", "--a", "--beta"};
        Arrays.sort(values, new Help.ShortestFirst());
        String[] expected = {"-", "-d", "-a", "--b", "--a", "--beta", "--alpha"};
        assertArrayEquals(expected, values);
    }

    @Test
    public void testSortByShortestOptionNameComparator() throws Exception {
        class App {
            @Option(names = {"-t", "--aaaa"}) boolean aaaa;
            @Option(names = {"--bbbb", "-k"}) boolean bbbb;
            @Option(names = {"-c", "--cccc"}) boolean cccc;
        }
        Field[] fields = fields(App.class, "aaaa", "bbbb", "cccc"); // -tkc
        Arrays.sort(fields, new Help.SortByShortestOptionNameAlphabetically());
        Field[] expected = fields(App.class, "cccc", "bbbb", "aaaa"); // -ckt
        assertArrayEquals(expected, fields);
    }

    @Test
    public void testSortByOptionArityAndNameComparator_sortsByMaxThenMinThenName() throws Exception {
        class App {
            @Option(names = {"-t", "--aaaa"}) boolean tImplicitArity0;
            @Option(names = {"-e", "--EEE"}, arity = "1") boolean explicitArity1;
            @Option(names = {"--bbbb", "-k"}) boolean kImplicitArity0;
            @Option(names = {"--AAAA", "-a"}) int aImplicitArity1;
            @Option(names = {"--BBBB", "-b"}) String[] bImplicitArity0_n;
            @Option(names = {"--ZZZZ", "-z"}, arity = "1..3") String[] zExplicitArity1_3;
            @Option(names = {"-f", "--ffff"}) boolean fImplicitArity0;
        }
        Field[] fields = fields(App.class, "tImplicitArity0", "explicitArity1", "kImplicitArity0",
                "aImplicitArity1", "bImplicitArity0_n", "zExplicitArity1_3", "fImplicitArity0");
        Arrays.sort(fields, new Help.SortByOptionArityAndNameAlphabetically());
        Field[] expected = fields(App.class,
                "fImplicitArity0",
                "kImplicitArity0",
                "tImplicitArity0",
                "aImplicitArity1",
                "explicitArity1",
                "zExplicitArity1_3",
                "bImplicitArity0_n");
        assertArrayEquals(expected, fields);
    }

    @Test
    public void testCreateMinimalOptionRenderer_ReturnsMinimalOptionRenderer() {
        assertEquals(Help.MinimalOptionRenderer.class, Help.createMinimalOptionRenderer().getClass());
    }

    @Test
    public void testMinimalOptionRenderer_rendersFirstDeclaredOptionNameAndDescription() {
        class Example {
            @Option(names = {"---long", "-L"}, description = "long description") String longField;
            @Option(names = {"-b", "-a", "--alpha"}, description = "other") String otherField;
        }
        Help.IOptionRenderer renderer = Help.createMinimalOptionRenderer();
        Help help = new Help(new Example());
        Help.IParamLabelRenderer parameterRenderer = help.createDefaultParamLabelRenderer();
        Field field = help.optionFields.get(0);
        String[][] row1 = renderer.render(field.getAnnotation(Option.class), field, parameterRenderer);
        assertEquals(1, row1.length);
        assertArrayEquals(new String[]{"---long=<longField>", "long description"}, row1[0]);

        field = help.optionFields.get(1);
        String[][] row2 = renderer.render(field.getAnnotation(Option.class), field, parameterRenderer);
        assertEquals(1, row2.length);
        assertArrayEquals(new String[]{"-b=<otherField>", "other"}, row2[0]);
    }

    @Test
    public void testCreateDefaultOptionRenderer_ReturnsDefaultOptionRenderer() {
        assertEquals(Help.DefaultOptionRenderer.class, new Help(new UsageDemo()).createDefaultOptionRenderer().getClass());
    }

    @Test
    public void testDefaultOptionRenderer_rendersShortestOptionNameThenOtherOptionNamesAndDescription() {
        @Command(showDefaultValues = true)
        class Example {
            @Option(names = {"---long", "-L"}, description = "long description") String longField;
            @Option(names = {"-b", "-a", "--alpha"}, description = "other") String otherField = "abc";
        }
        Help help = new Help(new Example());
        Help.IOptionRenderer renderer = help.createDefaultOptionRenderer();
        Help.IParamLabelRenderer parameterRenderer = help.createDefaultParamLabelRenderer();
        Field field = help.optionFields.get(0);
        String[][] row1 = renderer.render(field.getAnnotation(Option.class), field, parameterRenderer);
        assertEquals(2, row1.length);
        assertArrayEquals(Arrays.toString(row1[0]), new String[]{"", "-L", ",", "---long=<longField>", "long description"}, row1[0]);
        assertArrayEquals(Arrays.toString(row1[1]), new String[]{"", "", "", "", "Default: null"}, row1[1]);

        field = help.optionFields.get(1);
        String[][] row2 = renderer.render(field.getAnnotation(Option.class), field, parameterRenderer);
        assertEquals(2, row2.length);
        assertArrayEquals(Arrays.toString(row2[0]), new String[]{"", "-b", ",", "-a, --alpha=<otherField>", "other"}, row2[0]);
        assertArrayEquals(Arrays.toString(row2[1]), new String[]{"", "", "", "", "Default: abc"}, row2[1]);
    }

    @Test
    public void testDefaultOptionRenderer_rendersSpecifiedMarkerForRequiredOptionsWithDefault() {
        @Command(requiredOptionMarker = '*', showDefaultValues = true)
        class Example {
            @Option(names = {"-b", "-a", "--alpha"}, required = true, description = "other") String otherField ="abc";
        }
        Help help = new Help(new Example());
        Help.IOptionRenderer renderer = help.createDefaultOptionRenderer();
        Help.IParamLabelRenderer parameterRenderer = help.createDefaultParamLabelRenderer();
        Field field = help.optionFields.get(0);
        String[][] row = renderer.render(field.getAnnotation(Option.class), field, parameterRenderer);
        assertEquals(2, row.length);
        assertArrayEquals(Arrays.toString(row[0]), new String[]{"*", "-b", ",", "-a, --alpha=<otherField>", "other"}, row[0]);
        assertArrayEquals(Arrays.toString(row[1]), new String[]{"", "", "", "", "Default: abc"}, row[1]);
    }

    @Test
    public void testDefaultOptionRenderer_rendersSpecifiedMarkerForRequiredOptionsWithoutDefault() {
        @Command(requiredOptionMarker = '*')
        class Example {
            @Option(names = {"-b", "-a", "--alpha"}, required = true, description = "other") String otherField ="abc";
        }
        Help help = new Help(new Example());
        Help.IOptionRenderer renderer = help.createDefaultOptionRenderer();
        Help.IParamLabelRenderer parameterRenderer = help.createDefaultParamLabelRenderer();
        Field field = help.optionFields.get(0);
        String[][] row = renderer.render(field.getAnnotation(Option.class), field, parameterRenderer);
        assertEquals(1, row.length);
        assertArrayEquals(Arrays.toString(row[0]), new String[]{"*", "-b", ",", "-a, --alpha=<otherField>", "other"}, row[0]);
    }

    @Test
    public void testDefaultOptionRenderer_rendersSpacePrefixByDefaultForRequiredOptionsWithoutDefaultValue() {
        class Example {
            @Option(names = {"-b", "-a", "--alpha"}, required = true, description = "other") String otherField;
        }
        Help help = new Help(new Example());
        Help.IOptionRenderer renderer = help.createDefaultOptionRenderer();
        Help.IParamLabelRenderer parameterRenderer = help.createDefaultParamLabelRenderer();
        Field field = help.optionFields.get(0);
        String[][] row = renderer.render(field.getAnnotation(Option.class), field, parameterRenderer);
        assertEquals(1, row.length);
        assertArrayEquals(Arrays.toString(row[0]), new String[]{" ", "-b", ",", "-a, --alpha=<otherField>", "other"}, row[0]);
    }

    @Test
    public void testDefaultOptionRenderer_rendersSpacePrefixByDefaultForRequiredOptionsWithDefaultValue() {
        //@Command(showDefaultValues = true) // set programmatically
        class Example {
            @Option(names = {"-b", "-a", "--alpha"}, required = true, description = "other") String otherField;
        }
        Help help = new Help(new Example());
        help.showDefaultValues = true;
        Help.IOptionRenderer renderer = help.createDefaultOptionRenderer();
        Help.IParamLabelRenderer parameterRenderer = help.createDefaultParamLabelRenderer();
        Field field = help.optionFields.get(0);
        String[][] row = renderer.render(field.getAnnotation(Option.class), field, parameterRenderer);
        assertEquals(2, row.length);
        assertArrayEquals(Arrays.toString(row[0]), new String[]{" ", "-b", ",", "-a, --alpha=<otherField>", "other"}, row[0]);
        assertArrayEquals(Arrays.toString(row[1]), new String[]{"",    "", "",  "", "Default: null"}, row[1]);
    }

    @Test
    public void testDefaultParameterRenderer_rendersSpacePrefixByDefaultForParametersWithPositiveArity() {
        class Required {
            @Parameters(description = "required") String required;
        }
        Help help = new Help(new Required());
        Help.IParameterRenderer renderer = help.createDefaultParameterRenderer();
        Help.IParamLabelRenderer parameterRenderer = Help.createMinimalParamLabelRenderer();
        Field field = help.positionalParametersFields.get(0);
        String[][] row1 = renderer.render(field.getAnnotation(Parameters.class), field, parameterRenderer);
        assertEquals(1, row1.length);
        assertArrayEquals(Arrays.toString(row1[0]), new String[]{" ", "", "", "required", "required"}, row1[0]);
    }

    @Test
    public void testDefaultParameterRenderer_rendersSpecifiedMarkerForParametersWithPositiveArity() {
        @Command(requiredOptionMarker = '*')
        class Required {
            @Parameters(description = "required") String required;
        }
        Help help = new Help(new Required());
        Help.IParameterRenderer renderer = help.createDefaultParameterRenderer();
        Help.IParamLabelRenderer parameterRenderer = Help.createMinimalParamLabelRenderer();
        Field field = help.positionalParametersFields.get(0);
        String[][] row1 = renderer.render(field.getAnnotation(Parameters.class), field, parameterRenderer);
        assertEquals(1, row1.length);
        assertArrayEquals(Arrays.toString(row1[0]), new String[]{"*", "", "", "required", "required"}, row1[0]);
    }

    @Test
    public void testDefaultParameterRenderer_rendersSpacePrefixForParametersWithZeroArity() {
        @Command(requiredOptionMarker = '*')
        class Optional {
            @Parameters(arity = "0..1", description = "optional") String optional;
        }
        Help help = new Help(new Optional());
        Help.IParameterRenderer renderer = help.createDefaultParameterRenderer();
        Help.IParamLabelRenderer parameterRenderer = Help.createMinimalParamLabelRenderer();
        Field field = help.positionalParametersFields.get(0);
        String[][] row1 = renderer.render(field.getAnnotation(Parameters.class), field, parameterRenderer);
        assertEquals(1, row1.length);
        assertArrayEquals(Arrays.toString(row1[0]), new String[]{"", "", "", "optional", "optional"}, row1[0]);
    }

    @Test
    public void testDefaultOptionRenderer_rendersCommaOnlyIfBothShortAndLongOptionNamesExist() {
        class Example {
            @Option(names = {"-v"}, description = "shortBool") boolean shortBoolean;
            @Option(names = {"--verbose"}, description = "longBool") boolean longBoolean;
            @Option(names = {"-x", "--xeno"}, description = "combiBool") boolean combiBoolean;
            @Option(names = {"-s"}, description = "shortOnly") String shortOnlyField;
            @Option(names = {"--long"}, description = "longOnly") String longOnlyField;
            @Option(names = {"-b", "--beta"}, description = "combi") String combiField;
        }
        Help help = new Help(new Example());
        help.showDefaultValues = false; // omit default values from description column
        Help.IOptionRenderer renderer = help.createDefaultOptionRenderer();
        Help.IParamLabelRenderer parameterRenderer = help.createDefaultParamLabelRenderer();

        String[][] expected = new String[][] {
                {"", "-v", "",  "", "shortBool"},
                {"", "",   "",  "--verbose", "longBool"},
                {"", "-x", ",", "--xeno", "combiBool"},
                {"", "-s", "=",  "<shortOnlyField>", "shortOnly"},
                {"", "",   "",  "--long=<longOnlyField>", "longOnly"},
                {"", "-b", ",", "--beta=<combiField>", "combi"},
        };
        int i = -1;
        for (Field field : help.optionFields) {
            String[][] row = renderer.render(field.getAnnotation(Option.class), field, parameterRenderer);
            assertEquals(1, row.length);
            assertArrayEquals(Arrays.toString(row[0]), expected[++i], row[0]);
        }
    }

    @Test
    public void testDefaultOptionRenderer_omitsDefaultValuesForBooleanFields() {
        @Command(showDefaultValues = true)
        class Example {
            @Option(names = {"-v"}, description = "shortBool") boolean shortBoolean;
            @Option(names = {"--verbose"}, description = "longBool") Boolean longBoolean;
            @Option(names = {"-s"}, description = "shortOnly") String shortOnlyField = "short";
            @Option(names = {"--long"}, description = "longOnly") String longOnlyField = "long";
            @Option(names = {"-b", "--beta"}, description = "combi") int combiField = 123;
        }
        Help help = new Help(new Example());
        Help.IOptionRenderer renderer = help.createDefaultOptionRenderer();
        Help.IParamLabelRenderer parameterRenderer = help.createDefaultParamLabelRenderer();

        String[][] expected = new String[][] {
                {"", "-v", "",  "", "shortBool"},
                {"", "",   "",  "--verbose", "longBool"},
                {"", "-s", "=",  "<shortOnlyField>", "shortOnly"},
                {"",   "", "",  "", "Default: short"},
                {"", "",   "",  "--long=<longOnlyField>", "longOnly"},
                {"", "",   "",  "", "Default: long"},
                {"", "-b", ",", "--beta=<combiField>", "combi"},
                {"", "",   "",  "", "Default: 123"},
        };
        int[] rowCount = {1, 1, 2, 2, 2};
        int i = -1;
        int rowIndex = 0;
        for (Field field : help.optionFields) {
            String[][] row = renderer.render(field.getAnnotation(Option.class), field, parameterRenderer);
            assertEquals(rowCount[++i], row.length);
            assertArrayEquals(Arrays.toString(row[0]), expected[rowIndex], row[0]);
            rowIndex += rowCount[i];
        }
    }

    @Test
    public void testCreateDefaultParameterRenderer_ReturnsDefaultParameterRenderer() {
        assertEquals(Help.DefaultParamLabelRenderer.class, new Help(new UsageDemo()).createDefaultParamLabelRenderer().getClass());
    }

    @Test
    public void testDefaultParameterRenderer_showsParamLabelIfPresentOrFieldNameOtherwise() {
        class Example {
            @Option(names = "--without" ) String longField;
            @Option(names = "--with", paramLabel = "LABEL") String otherField;
        }
        Help help = new Help(new Example());
        Help.IParamLabelRenderer equalSeparatedParameterRenderer = help.createDefaultParamLabelRenderer();
        help.separator = " ";
        Help.IParamLabelRenderer spaceSeparatedParameterRenderer = help.createDefaultParamLabelRenderer();

        String[] expected = new String[] {
                "<longField>",
                "LABEL",
        };
        int i = -1;
        for (Field field : help.optionFields) {
            i++;
            String withSpace = spaceSeparatedParameterRenderer.renderParameterLabel(field);
            assertEquals(withSpace, " " + expected[i], withSpace);
            String withEquals = equalSeparatedParameterRenderer.renderParameterLabel(field);
            assertEquals(withEquals, "=" + expected[i], withEquals);
        }
    }

    @Test
    public void testDefaultParameterRenderer_appliesToPositionalArgumentsIgnoresSeparator() {
        class WithLabel    { @Parameters(paramLabel = "POSITIONAL_ARGS") String positional; }
        class WithoutLabel { @Parameters()                               String positional; }

        Help withLabel = new Help(new WithLabel());
        Help.IParamLabelRenderer equals = withLabel.createDefaultParamLabelRenderer();
        withLabel.separator = "=";
        Help.IParamLabelRenderer spaced = withLabel.createDefaultParamLabelRenderer();

        String withSpace = spaced.renderParameterLabel(withLabel.positionalParametersFields.get(0));
        assertEquals(withSpace, "POSITIONAL_ARGS", withSpace);
        String withEquals = equals.renderParameterLabel(withLabel.positionalParametersFields.get(0));
        assertEquals(withEquals, "POSITIONAL_ARGS", withEquals);

        Help withoutLabel = new Help(new WithoutLabel());
        withSpace = spaced.renderParameterLabel(withoutLabel.positionalParametersFields.get(0));
        assertEquals(withSpace, "<positional>", withSpace);
        withEquals = equals.renderParameterLabel(withoutLabel.positionalParametersFields.get(0));
        assertEquals(withEquals, "<positional>", withEquals);
    }

    @Test
    public void testDefaultLayout_addsEachRowToTable() {
        final String[][] values = { {"a", "b", "c", "d" }, {"1", "2", "3", "4"} };
        final int[] count = {0};
        TextTable tt = new TextTable() {
            @Override public void addRowValues(String[] columnValues) {
                assertArrayEquals(values[count[0]], columnValues);
                count[0]++;
            }
        };
        Help.Layout layout = new Help.Layout(tt);
        layout.layout(null, values);
        assertEquals(2, count[0]);
    }

    @Test
    public void testAbreviatedSynopsis_withoutParameters() {
        @CommandLine.Command(abbreviateSynopsis = true)
        class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        assertEquals("<main class> [OPTIONS]" + LINESEP, new Help(new App()).synopsis());
    }

    @Test
    public void testAbreviatedSynopsis_withParameters() {
        @CommandLine.Command(abbreviateSynopsis = true)
        class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters File[] files;
        }
        assertEquals("<main class> [OPTIONS] [<files>...]" + LINESEP, new Help(new App()).synopsis());
    }

    @Test
    public void testSynopsis_optionalOptionArity1_n_withDefaultSeparator() {
        @Command() class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, arity = "1..*") int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        assertEquals("<main class> [-v] [-c=<count> [<count>...]]" + LINESEP, new Help(new App()).synopsis());
    }

    @Test
    public void testSynopsis_optionalOptionArity0_1_withSpaceSeparator() {
        @CommandLine.Command(separator = " ") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, arity = "0..1") int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        assertEquals("<main class> [-v] [-c [<count>]]" + LINESEP, new Help(new App()).synopsis());
    }

    @Test
    public void testSynopsis_requiredOptionWithSeparator() {
        @Command() class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, required = true) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        assertEquals("<main class> [-v] -c=<count>" + LINESEP, new Help(new App()).synopsis());
    }

    @Test
    public void testSynopsis_optionalOption_withSpaceSeparator() {
        @CommandLine.Command(separator = " ") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        assertEquals("<main class> [-v] [-c <count>]" + LINESEP, new Help(new App()).synopsis());
    }

    @Test
    public void testSynopsis_optionalOptionArity0_1__withSeparator() {
        class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, arity = "0..1") int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        assertEquals("<main class> [-v] [-c[=<count>]]" + LINESEP, new Help(new App()).synopsis());
    }

    @Test
    public void testSynopsis_optionalOptionArity0_n__withSeparator() {
        @CommandLine.Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, arity = "0..*") int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        assertEquals("<main class> [-v] [-c[=<count>...]]" + LINESEP, new Help(new App()).synopsis());
    }

    @Test
    public void testSynopsis_optionalOptionArity1_n__withSeparator() {
        @CommandLine.Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, arity = "1..*") int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        assertEquals("<main class> [-v] [-c=<count> [<count>...]]" + LINESEP, new Help(new App()).synopsis());
    }

    @Test
    public void testSynopsis_withSeparator_withParameters() {
        @CommandLine.Command(separator = ":") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters File[] files;
        }
        assertEquals("<main class> [-v] [-c:<count>] [<files>...]" + LINESEP, new Help(new App()).synopsis());
    }

    @Test
    public void testSynopsis_withSeparator_withLabeledParameters() {
        @Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters(paramLabel = "FILE") File[] files;
        }
        assertEquals("<main class> [-v] [-c=<count>] [FILE...]" + LINESEP, new Help(new App()).synopsis());
    }

    @Test
    public void testSynopsis_withSeparator_withLabeledRequiredParameters() {
        @CommandLine.Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters(paramLabel = "FILE", arity = "1..*") File[] files;
        }
        assertEquals("<main class> [-v] [-c=<count>] FILE [FILE...]" + LINESEP, new Help(new App()).synopsis());
    }

    @Test
    public void testSynopsis_clustersBooleanOptions() {
        @Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--aaaa", "-a"}) boolean aBoolean;
            @Option(names = {"--xxxx", "-x"}) Boolean xBoolean;
            @Option(names = {"--count", "-c"}, paramLabel = "COUNT") int count;
        }
        assertEquals("<main class> [-avx] [-c=COUNT]" + LINESEP, new Help(new App()).synopsis());
    }

    @Test
    public void testSynopsis_clustersRequiredBooleanOptions() {
        @CommandLine.Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}, required = true) boolean verbose;
            @Option(names = {"--aaaa", "-a"}, required = true) boolean aBoolean;
            @Option(names = {"--xxxx", "-x"}, required = true) Boolean xBoolean;
            @Option(names = {"--count", "-c"}, paramLabel = "COUNT") int count;
        }
        assertEquals("<main class> -avx [-c=COUNT]" + LINESEP, new Help(new App()).synopsis());
    }

    @Test
    public void testSynopsis_clustersRequiredBooleanOptionsSeparately() {
        @CommandLine.Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--aaaa", "-a"}) boolean aBoolean;
            @Option(names = {"--xxxx", "-x"}) Boolean xBoolean;
            @Option(names = {"--Verbose", "-V"}, required = true) boolean requiredVerbose;
            @Option(names = {"--Aaaa", "-A"}, required = true) boolean requiredABoolean;
            @Option(names = {"--Xxxx", "-X"}, required = true) Boolean requiredXBoolean;
            @Option(names = {"--count", "-c"}, paramLabel = "COUNT") int count;
        }
        assertEquals("<main class> -AVX [-avx] [-c=COUNT]" + LINESEP, new Help(new App()).synopsis());
    }

    @Test
    public void testLongMultiLineSynopsisIndented() {
        @Command(name = "<best-app-ever>")
        class App {
            @Option(names = "--long-option-name", paramLabel = "<long-option-value>") int a;
            @Option(names = "--another-long-option-name", paramLabel = "<another-long-option-value>") int b;
            @Option(names = "--third-long-option-name", paramLabel = "<third-long-option-value>") int c;
            @Option(names = "--fourth-long-option-name", paramLabel = "<fourth-long-option-value>") int d;
        }
        assertEquals(String.format(
                "<best-app-ever> [--another-long-option-name=<another-long-option-value>]%n" +
                "                [--fourth-long-option-name=<fourth-long-option-value>]%n" +
                "                [--long-option-name=<long-option-value>]%n" +
                "                [--third-long-option-name=<third-long-option-value>]%n"),
                new Help(new App()).synopsis());
    }

    @Test
    public void testLongMultiLineSynopsisWithAtMarkIndented() {
        @Command(name = "<best-app-ever>")
        class App {
            @Option(names = "--long-option@-name", paramLabel = "<long-option-valu@@e>") int a;
            @Option(names = "--another-long-option-name", paramLabel = "^[<another-long-option-value>]") int b;
            @Option(names = "--third-long-option-name", paramLabel = "<third-long-option-value>") int c;
            @Option(names = "--fourth-long-option-name", paramLabel = "<fourth-long-option-value>") int d;
        }
        assertEquals(String.format(
                "<best-app-ever> [--another-long-option-name=^[<another-long-option-value>]]%n" +
                "                [--fourth-long-option-name=<fourth-long-option-value>]%n" +
                "                [--long-option@-name=<long-option-valu@@e>]%n" +
                "                [--third-long-option-name=<third-long-option-value>]%n"),
                new Help(new App()).synopsis());
    }

    @Test
    public void testCustomSynopsis() {
        @Command(customSynopsis = {
                "<the-app> --number=NUMBER --other-option=<aargh>",
                "          --more=OTHER --and-other-option=<aargh>",
                "<the-app> --number=NUMBER --and-other-option=<aargh>",
        })
        class App {@Option(names = "--ignored") boolean ignored;}
        assertEquals(String.format(
                "<the-app> --number=NUMBER --other-option=<aargh>%n" +
                "          --more=OTHER --and-other-option=<aargh>%n" +
                "<the-app> --number=NUMBER --and-other-option=<aargh>%n"),
                new Help(new App()).synopsis());
    }
    @Test
    public void testTextTable() {
        TextTable table = new TextTable();
        table.addRowValues("", "-v", ",", "--verbose", "show what you're doing while you are doing it");
        table.addRowValues("", "-p", null, null, "the quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog.");
        assertEquals(String.format(
                "  -v, --verbose               show what you're doing while you are doing it%n" +
                "  -p                          the quick brown fox jumped over the lazy dog. The%n" +
                "                                quick brown fox jumped over the lazy dog.%n"
                ,""), table.toString(new StringBuilder()).toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTextTableAddsNewRowWhenTooManyValuesSpecified() {
        TextTable table = new TextTable();
        table.addRowValues("", "-c", ",", "--create", "description", "INVALID", "Row 3");
//        assertEquals(String.format("" +
//                        "  -c, --create                description                                       %n" +
//                        "                                INVALID                                         %n" +
//                        "                                Row 3                                           %n"
//                ,""), table.toString(new StringBuilder()).toString());
    }

    @Test
    public void testTextTableAddsNewRowWhenAnyColumnTooLong() {
        TextTable table = new TextTable();
        table.addRowValues("*", "-c", ",",
                "--create, --create2, --create3, --create4, --create5, --create6, --create7, --create8",
                "description");
        assertEquals(String.format("" +
                        "* -c, --create, --create2, --create3, --create4, --create5, --create6,%n" +
                        "        --create7, --create8%n" +
                        "                              description%n"
                ,""), table.toString(new StringBuilder()).toString());

        table = new TextTable();
        table.addRowValues("", "-c", ",",
                "--create, --create2, --create3, --create4, --create5, --create6, --createAA7, --create8",
                "description");
        assertEquals(String.format("" +
                        "  -c, --create, --create2, --create3, --create4, --create5, --create6,%n" +
                        "        --createAA7, --create8%n" +
                        "                              description%n"
                ,""), table.toString(new StringBuilder()).toString());
    }

    @Test
    public void testCatUsageFormat() {
        @Command(name = "cat",
                customSynopsis = "cat [OPTIONS] [FILE...]",
                description = "Concatenate FILE(s), or standard input, to standard output.",
                footer = "Copyright(c) 2017")
        class Cat {
            @Parameters(paramLabel = "FILE", hidden = true, description = "Files whose contents to display") List<File> files;
            @Option(names = "--help",    help = true,     description = "display this help and exit") boolean help;
            @Option(names = "--version", help = true,     description = "output version information and exit") boolean version;
            @Option(names = "-u",                         description = "(ignored)") boolean u;
            @Option(names = "-t",                         description = "equivalent to -vT") boolean t;
            @Option(names = "-e",                         description = "equivalent to -vET") boolean e;
            @Option(names = {"-A", "--show-all"},         description = "equivalent to -vET") boolean showAll;
            @Option(names = {"-s", "--squeeze-blank"},    description = "suppress repeated empty output lines") boolean squeeze;
            @Option(names = {"-v", "--show-nonprinting"}, description = "use ^ and M- notation, except for LDF and TAB") boolean v;
            @Option(names = {"-b", "--number-nonblank"},  description = "number nonempty output lines, overrides -n") boolean b;
            @Option(names = {"-T", "--show-tabs"},        description = "display TAB characters as ^I") boolean T;
            @Option(names = {"-E", "--show-ends"},        description = "display $ at end of each line") boolean E;
            @Option(names = {"-n", "--number"},           description = "number all output lines") boolean n;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.usage(new Cat(), new PrintStream(baos));
        String expected = String.format(
                "Usage: cat [OPTIONS] [FILE...]%n" +
                        "Concatenate FILE(s), or standard input, to standard output.%n" +
                        "  -A, --show-all              equivalent to -vET%n" +
                        "  -b, --number-nonblank       number nonempty output lines, overrides -n%n" +
                        "  -e                          equivalent to -vET%n" +
                        "  -E, --show-ends             display $ at end of each line%n" +
                        "  -n, --number                number all output lines%n" +
                        "  -s, --squeeze-blank         suppress repeated empty output lines%n" +
                        "  -t                          equivalent to -vT%n" +
                        "  -T, --show-tabs             display TAB characters as ^I%n" +
                        "  -u                          (ignored)%n" +
                        "  -v, --show-nonprinting      use ^ and M- notation, except for LDF and TAB%n" +
                        "      --help                  display this help and exit%n" +
                        "      --version               output version information and exit%n" +
                        "Copyright(c) 2017%n", "");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testUsageIndexedPositionalParameters() throws UnsupportedEncodingException {
        @Command()
        class App {
            @Parameters(index = "0", description = "source host") InetAddress host1;
            @Parameters(index = "1", description = "source port") int port1;
            @Parameters(index = "2", description = "destination host") InetAddress host2;
            @Parameters(index = "3..4", arity = "1..2", description = "destination port range") int[] port2range;
            @Parameters(index = "4..*", description = "files to transfer") String[] files;
            @Parameters(hidden = true) String[] all;
        }
        String actual = usageString(new App());
        String expected = String.format(
                "Usage: <main class> <host1> <port1> <host2> <port2range> [<port2range>] [<files>...]%n" +
                "      host1                   source host%n" +
                "      port1                   source port%n" +
                "      host2                   destination host%n" +
                "      port2range              destination port range%n" +
                "      files                   files to transfer%n"
        );
        assertEquals(expected, actual);
    }

    static class UsageDemo {
        @Option(names = "-a", description = "boolean option with short name only")
        boolean a;

        @Option(names = "-b", paramLabel = "INT", description = "short option with a parameter")
        int b;

        @Option(names = {"-c", "--c-option"}, description = "boolean option with short and long name")
        boolean c;

        @Option(names = {"-d", "--d-option"}, paramLabel = "FILE", description = "option with parameter and short and long name")
        File d;

        @Option(names = "--e-option", description = "boolean option with only a long name")
        boolean e;

        @Option(names = "--f-option", paramLabel = "STRING", description = "option with parameter and only a long name")
        String f;

        @Option(names = {"-g", "--g-option-with-a-name-so-long-that-it-runs-into-the-descriptions-column"}, description = "boolean option with short and long name")
        boolean g;

        @Parameters(index = "0", paramLabel = "0BLAH", description = "first parameter")
        String param0;

        @Parameters(index = "1", paramLabel = "1PARAMETER-with-a-name-so-long-that-it-runs-into-the-descriptions-column", description = "2nd parameter")
        String param1;

        @Parameters(index = "2..*", paramLabel = "remaining", description = "remaining parameters")
        String param2_n;

        @Parameters(index = "*", paramLabel = "all", description = "all parameters")
        String param_n;
    }

    @Test
    public void testSubclassedCommandHelp() throws Exception {
        @Command(name = "parent", description = "parent description")
        class ParentOption {
        }
        @Command(name = "child", description = "child description")
        class ChildOption extends ParentOption {
        }
        String actual = usageString(new ChildOption());
        assertEquals(String.format(
                "Usage: child%n" +
                "child description%n"), actual);
    }

    @Test
    public void testUsageMainCommand_NoAnsi() throws Exception {
        CommandLine.ansi = false; // force ansi off
        String actual = usageString(SubcommandDemo.mainCommand());
        CommandLine.ansi = null; // back to platform-dependent ansi
        assertEquals(String.format(SubcommandDemo.EXPECTED_USAGE_MAIN), actual);
    }

    @Test
    public void testUsageSubcommandGitStatus_NoAnsi() throws Exception {
        CommandLine.ansi = false; // force ansi off
        String actual = usageString(new SubcommandDemo.GitStatus());
        CommandLine.ansi = null; // back to platform-dependent ansi
        assertEquals(String.format(SubcommandDemo.EXPECTED_USAGE_GITSTATUS), actual);
    }

    @Test
    public void testUsageSubcommandGitCommit_NoAnsi() throws Exception {
        CommandLine.ansi = false; // force ansi off
        String actual = usageString(new SubcommandDemo.GitCommit());
        CommandLine.ansi = null; // back to platform-dependent ansi
        assertEquals(String.format(SubcommandDemo.EXPECTED_USAGE_GITCOMMIT), actual);
    }
}
