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

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi.IStyle;
import picocli.CommandLine.Help.Ansi.Style;
import picocli.CommandLine.Help.Ansi.Text;
import picocli.CommandLine.Help.ColorScheme;
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
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static org.junit.Assert.*;

/**
 * Tests for picoCLI's "Usage" help functionality.
 */
public class CommandLineHelpTest {
    private static final String LINESEP = System.getProperty("line.separator");

    @After
    public void after() {
        System.getProperties().remove("picocli.color.commands");
        System.getProperties().remove("picocli.color.options");
        System.getProperties().remove("picocli.color.parameters");
        System.getProperties().remove("picocli.color.optionParams");
    }
    private static String usageString(Object annotatedObject, Help.Ansi ansi) throws UnsupportedEncodingException {
        return usageString(new CommandLine(annotatedObject), ansi);
    }
    private static String usageString(CommandLine commandLine, Help.Ansi ansi) throws UnsupportedEncodingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        commandLine.usage(new PrintStream(baos, true, "UTF8"), ansi);
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
        String result = usageString(new Params(), Help.Ansi.OFF);
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
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                        "Usage: <main class> -f=<file>%n" +
                        "  -f, --file=<file>           the file to use%n" +
                        "                                Default: theDefault.txt%n"), result);
    }

    @Test
    public void testUsageSeparatorWithoutDefault() throws Exception {
        @Command()
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use") File file = new File("def.txt");
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
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
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                        "Usage: <main class> -f=<file>%n" +
                        "  -f, --file=<file>           the file to use%n" +
                        "                                Default: def.txt%n",
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
        String result = usageString(new ParamLabels(), Help.Ansi.OFF);
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
        Help help = new Help(new Example(), Help.defaultColorScheme(Help.Ansi.ON));
        Help.IParamLabelRenderer parameterRenderer = help.createDefaultParamLabelRenderer();
        Field field = help.optionFields.get(0);
        Text[][] row1 = renderer.render(field.getAnnotation(Option.class), field, parameterRenderer, Help.defaultColorScheme(
                help.ansi()));
        assertEquals(1, row1.length);
        //assertArrayEquals(new String[]{"---long=<longField>", "long description"}, row1[0]);
        assertArrayEquals(new Text[]{
                help.ansi().new Text(format("%s---long%s=%s<longField>%s", "@|fg(yellow) ", "|@", "@|italic ", "|@")),
                help.ansi().new Text("long description")}, row1[0]);

        field = help.optionFields.get(1);
        Text[][] row2 = renderer.render(field.getAnnotation(Option.class), field, parameterRenderer, Help.defaultColorScheme(
                help.ansi()));
        assertEquals(1, row2.length);
        //assertArrayEquals(new String[]{"-b=<otherField>", "other"}, row2[0]);
        assertArrayEquals(new Text[]{
                help.ansi().new Text(format("%s-b%s=%s<otherField>%s", "@|fg(yellow) ", "|@", "@|italic ", "|@")),
                help.ansi().new Text("other")}, row2[0]);
    }

    @Test
    public void testCreateDefaultOptionRenderer_ReturnsDefaultOptionRenderer() {
        assertEquals(Help.DefaultOptionRenderer.class, new Help(new UsageDemo()).createDefaultOptionRenderer().getClass());
    }

    private static Text[] textArray(Help help, String... str) {
        return textArray(help.ansi(), str);
    }
    private static Text[] textArray(Help.Ansi ansi, String... str) {
        Text[] result = new Text[str.length];
        for (int i = 0; i < str.length; i++) {
            result[i] = str[i] == null ? Help.Ansi.EMPTY_TEXT : ansi.new Text(str[i]);
        }
        return result;
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
        Text[][] row1 = renderer.render(field.getAnnotation(Option.class), field, parameterRenderer, help.colorScheme);
        assertEquals(2, row1.length);
        assertArrayEquals(Arrays.toString(row1[0]), textArray(help, "", "-L", ",", "---long=<longField>", "long description"), row1[0]);
        assertArrayEquals(Arrays.toString(row1[1]), textArray(help, "", "", "", "", "  Default: null"), row1[1]);

        field = help.optionFields.get(1);
        Text[][] row2 = renderer.render(field.getAnnotation(Option.class), field, parameterRenderer, help.colorScheme);
        assertEquals(2, row2.length);
        assertArrayEquals(Arrays.toString(row2[0]), textArray(help, "", "-b", ",", "-a, --alpha=<otherField>", "other"), row2[0]);
        assertArrayEquals(Arrays.toString(row2[1]), textArray(help, "", "", "", "", "  Default: abc"), row2[1]);
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
        Text[][] row = renderer.render(field.getAnnotation(Option.class), field, parameterRenderer, help.colorScheme);
        assertEquals(2, row.length);
        assertArrayEquals(Arrays.toString(row[0]), textArray(help, "*", "-b", ",", "-a, --alpha=<otherField>", "other"), row[0]);
        assertArrayEquals(Arrays.toString(row[1]), textArray(help, "", "", "", "", "  Default: abc"), row[1]);
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
        Text[][] row = renderer.render(field.getAnnotation(Option.class), field, parameterRenderer, help.colorScheme);
        assertEquals(1, row.length);
        assertArrayEquals(Arrays.toString(row[0]), textArray(help, "*", "-b", ",", "-a, --alpha=<otherField>", "other"), row[0]);
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
        Text[][] row = renderer.render(field.getAnnotation(Option.class), field, parameterRenderer, help.colorScheme);
        assertEquals(1, row.length);
        assertArrayEquals(Arrays.toString(row[0]), textArray(help, " ", "-b", ",", "-a, --alpha=<otherField>", "other"), row[0]);
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
        Text[][] row = renderer.render(field.getAnnotation(Option.class), field, parameterRenderer, help.colorScheme);
        assertEquals(2, row.length);
        assertArrayEquals(Arrays.toString(row[0]), textArray(help, " ", "-b", ",", "-a, --alpha=<otherField>", "other"), row[0]);
        assertArrayEquals(Arrays.toString(row[1]), textArray(help, "",    "", "",  "", "  Default: null"), row[1]);
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
        Text[][] row1 = renderer.render(field.getAnnotation(Parameters.class), field, parameterRenderer, help.colorScheme);
        assertEquals(1, row1.length);
        assertArrayEquals(Arrays.toString(row1[0]), textArray(help, " ", "", "", "required", "required"), row1[0]);
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
        Text[][] row1 = renderer.render(field.getAnnotation(Parameters.class), field, parameterRenderer, help.colorScheme);
        assertEquals(1, row1.length);
        assertArrayEquals(Arrays.toString(row1[0]), textArray(help, "*", "", "", "required", "required"), row1[0]);
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
        Text[][] row1 = renderer.render(field.getAnnotation(Parameters.class), field, parameterRenderer, help.colorScheme);
        assertEquals(1, row1.length);
        assertArrayEquals(Arrays.toString(row1[0]), textArray(help, "", "", "", "optional", "optional"), row1[0]);
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
            Text[][] row = renderer.render(field.getAnnotation(Option.class), field, parameterRenderer, help.colorScheme);
            assertEquals(1, row.length);
            assertArrayEquals(Arrays.toString(row[0]), textArray(help, expected[++i]), row[0]);
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
            Text[][] row = renderer.render(field.getAnnotation(Option.class), field, parameterRenderer, help.colorScheme);
            assertEquals(rowCount[++i], row.length);
            assertArrayEquals(Arrays.toString(row[0]), textArray(help, expected[rowIndex]), row[0]);
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
            Text withSpace = spaceSeparatedParameterRenderer.renderParameterLabel(field, help.ansi(), Collections.<IStyle>emptyList());
            assertEquals(withSpace.toString(), " " + expected[i], withSpace.toString());
            Text withEquals = equalSeparatedParameterRenderer.renderParameterLabel(field, help.ansi(), Collections.<IStyle>emptyList());
            assertEquals(withEquals.toString(), "=" + expected[i], withEquals.toString());
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

        Text withSpace = spaced.renderParameterLabel(withLabel.positionalParametersFields.get(0), withLabel.ansi(), Collections.<IStyle>emptyList());
        assertEquals(withSpace.toString(), "POSITIONAL_ARGS", withSpace.toString());
        Text withEquals = equals.renderParameterLabel(withLabel.positionalParametersFields.get(0), withLabel.ansi(), Collections.<IStyle>emptyList());
        assertEquals(withEquals.toString(), "POSITIONAL_ARGS", withEquals.toString());

        Help withoutLabel = new Help(new WithoutLabel());
        withSpace = spaced.renderParameterLabel(withoutLabel.positionalParametersFields.get(0), withoutLabel.ansi(), Collections.<IStyle>emptyList());
        assertEquals(withSpace.toString(), "<positional>", withSpace.toString());
        withEquals = equals.renderParameterLabel(withoutLabel.positionalParametersFields.get(0), withoutLabel.ansi(), Collections.<IStyle>emptyList());
        assertEquals(withEquals.toString(), "<positional>", withEquals.toString());
    }

    @Test
    public void testDefaultLayout_addsEachRowToTable() {
        final Text[][] values = {
                textArray(Help.Ansi.OFF, "a", "b", "c", "d"),
                textArray(Help.Ansi.OFF, "1", "2", "3", "4")
        };
        final int[] count = {0};
        TextTable tt = new TextTable(Help.Ansi.OFF) {
            @Override public void addRowValues(Text[] columnValues) {
                assertArrayEquals(values[count[0]], columnValues);
                count[0]++;
            }
        };
        Help.Layout layout = new Help.Layout(Help.defaultColorScheme(Help.Ansi.OFF), tt);
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
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [OPTIONS]" + LINESEP, help.synopsis());
    }

    @Test
    public void testAbreviatedSynopsis_withoutParameters_ANSI() {
        @CommandLine.Command(abbreviateSynopsis = true)
        class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.defaultColorScheme(Help.Ansi.ON));
        assertEquals(Help.Ansi.ON.new Text("@|bold <main class>|@ [OPTIONS]" + LINESEP).toString(), help.synopsis());
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
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [OPTIONS] [<files>...]" + LINESEP, help.synopsis());
    }

    @Test
    public void testAbreviatedSynopsis_withParameters_ANSI() {
        @CommandLine.Command(abbreviateSynopsis = true)
        class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters File[] files;
        }
        Help help = new Help(new App(), Help.defaultColorScheme(Help.Ansi.ON));
        assertEquals(Help.Ansi.ON.new Text("@|bold <main class>|@ [OPTIONS] [@|yellow <files>|@...]" + LINESEP).toString(), help.synopsis());
    }

    @Test
    public void testSynopsis_optionalOptionArity1_n_withDefaultSeparator() {
        @Command() class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, arity = "1..*") int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [-v] [-c=<count> [<count>...]]" + LINESEP, help.synopsis());
    }

    @Test
    public void testSynopsis_optionalOptionArity1_n_withDefaultSeparator_ANSI() {
        @Command() class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, arity = "1..*") int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.defaultColorScheme(Help.Ansi.ON));
        assertEquals(Help.Ansi.ON.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@=@|italic <count>|@ [@|italic <count>|@...]]" + LINESEP),
                help.synopsis());
    }

    @Test
    public void testSynopsis_optionalOptionArity0_1_withSpaceSeparator() {
        @CommandLine.Command(separator = " ") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, arity = "0..1") int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [-v] [-c [<count>]]" + LINESEP, help.synopsis());
    }

    @Test
    public void testSynopsis_optionalOptionArity0_1_withSpaceSeparator_ANSI() {
        @CommandLine.Command(separator = " ") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, arity = "0..1") int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.defaultColorScheme(Help.Ansi.ON));
        assertEquals(Help.Ansi.ON.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@ [@|italic <count>|@]]" + LINESEP), help.synopsis());
    }

    @Test
    public void testSynopsis_requiredOptionWithSeparator() {
        @Command() class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, required = true) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [-v] -c=<count>" + LINESEP, help.synopsis());
    }

    @Test
    public void testSynopsis_requiredOptionWithSeparator_ANSI() {
        @Command() class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, required = true) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.defaultColorScheme(Help.Ansi.ON));
        assertEquals(Help.Ansi.ON.new Text("@|bold <main class>|@ [@|yellow -v|@] @|yellow -c|@=@|italic <count>|@" + LINESEP), help.synopsis());
    }

    @Test
    public void testSynopsis_optionalOption_withSpaceSeparator() {
        @CommandLine.Command(separator = " ") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [-v] [-c <count>]" + LINESEP, help.synopsis());
    }

    @Test
    public void testSynopsis_optionalOptionArity0_1__withSeparator() {
        class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, arity = "0..1") int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [-v] [-c[=<count>]]" + LINESEP, help.synopsis());
    }

    @Test
    public void testSynopsis_optionalOptionArity0_n__withSeparator() {
        @CommandLine.Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, arity = "0..*") int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [-v] [-c[=<count>...]]" + LINESEP, help.synopsis());
    }

    @Test
    public void testSynopsis_optionalOptionArity1_n__withSeparator() {
        @CommandLine.Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, arity = "1..*") int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [-v] [-c=<count> [<count>...]]" + LINESEP, help.synopsis());
    }

    @Test
    public void testSynopsis_withSeparator_withParameters() {
        @CommandLine.Command(separator = ":") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters File[] files;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [-v] [-c:<count>] [<files>...]" + LINESEP, help.synopsis());
    }

    @Test
    public void testSynopsis_withSeparator_withParameters_ANSI() {
        @CommandLine.Command(separator = ":") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters File[] files;
        }
        Help help = new Help(new App(), Help.defaultColorScheme(Help.Ansi.ON));
        assertEquals(Help.Ansi.ON.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@:@|italic <count>|@] [@|yellow <files>|@...]" + LINESEP),
                help.synopsis());
    }

    @Test
    public void testSynopsis_withSeparator_withLabeledParameters() {
        @Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters(paramLabel = "FILE") File[] files;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [-v] [-c=<count>] [FILE...]" + LINESEP, help.synopsis());
    }

    @Test
    public void testSynopsis_withSeparator_withLabeledParameters_ANSI() {
        @Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters(paramLabel = "FILE") File[] files;
        }
        Help help = new Help(new App(), Help.defaultColorScheme(Help.Ansi.ON));
        assertEquals(Help.Ansi.ON.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@=@|italic <count>|@] [@|yellow FILE|@...]" + LINESEP),
                help.synopsis());
    }

    @Test
    public void testSynopsis_withSeparator_withLabeledRequiredParameters() {
        @CommandLine.Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters(paramLabel = "FILE", arity = "1..*") File[] files;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [-v] [-c=<count>] FILE [FILE...]" + LINESEP, help.synopsis());
    }

    @Test
    public void testSynopsis_withSeparator_withLabeledRequiredParameters_ANSI() {
        @CommandLine.Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters(paramLabel = "FILE", arity = "1..*") File[] files;
        }
        Help help = new Help(new App(), Help.Ansi.ON);
        assertEquals(Help.Ansi.ON.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@=@|italic <count>|@] @|yellow FILE|@ [@|yellow FILE|@...]" + LINESEP),
                help.synopsis());
    }

    @Test
    public void testSynopsis_clustersBooleanOptions() {
        @Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--aaaa", "-a"}) boolean aBoolean;
            @Option(names = {"--xxxx", "-x"}) Boolean xBoolean;
            @Option(names = {"--count", "-c"}, paramLabel = "COUNT") int count;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [-avx] [-c=COUNT]" + LINESEP, help.synopsis());
    }

    @Test
    public void testSynopsis_clustersRequiredBooleanOptions() {
        @CommandLine.Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}, required = true) boolean verbose;
            @Option(names = {"--aaaa", "-a"}, required = true) boolean aBoolean;
            @Option(names = {"--xxxx", "-x"}, required = true) Boolean xBoolean;
            @Option(names = {"--count", "-c"}, paramLabel = "COUNT") int count;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> -avx [-c=COUNT]" + LINESEP, help.synopsis());
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
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> -AVX [-avx] [-c=COUNT]" + LINESEP, help.synopsis());
    }

    @Test
    public void testSynopsis_clustersRequiredBooleanOptionsSeparately_ANSI() {
        @CommandLine.Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--aaaa", "-a"}) boolean aBoolean;
            @Option(names = {"--xxxx", "-x"}) Boolean xBoolean;
            @Option(names = {"--Verbose", "-V"}, required = true) boolean requiredVerbose;
            @Option(names = {"--Aaaa", "-A"}, required = true) boolean requiredABoolean;
            @Option(names = {"--Xxxx", "-X"}, required = true) Boolean requiredXBoolean;
            @Option(names = {"--count", "-c"}, paramLabel = "COUNT") int count;
        }
        Help help = new Help(new App(), Help.defaultColorScheme(Help.Ansi.ON));
        assertEquals(Help.Ansi.ON.new Text("@|bold <main class>|@ @|yellow -AVX|@ [@|yellow -avx|@] [@|yellow -c|@=@|italic COUNT|@]" + LINESEP),
                help.synopsis());
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
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals(String.format(
                "<best-app-ever> [--another-long-option-name=<another-long-option-value>]%n" +
                "                [--fourth-long-option-name=<fourth-long-option-value>]%n" +
                "                [--long-option-name=<long-option-value>]%n" +
                "                [--third-long-option-name=<third-long-option-value>]%n"),
                help.synopsis());
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
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals(String.format(
                "<best-app-ever> [--another-long-option-name=^[<another-long-option-value>]]%n" +
                "                [--fourth-long-option-name=<fourth-long-option-value>]%n" +
                "                [--long-option@-name=<long-option-valu@@e>]%n" +
                "                [--third-long-option-name=<third-long-option-value>]%n"),
                help.synopsis());
    }

    @Test
    public void testLongMultiLineSynopsisWithAtMarkIndented_ANSI() {
        @Command(name = "<best-app-ever>")
        class App {
            @Option(names = "--long-option@-name", paramLabel = "<long-option-valu@@e>") int a;
            @Option(names = "--another-long-option-name", paramLabel = "^[<another-long-option-value>]") int b;
            @Option(names = "--third-long-option-name", paramLabel = "<third-long-option-value>") int c;
            @Option(names = "--fourth-long-option-name", paramLabel = "<fourth-long-option-value>") int d;
        }
        Help help = new Help(new App(), Help.defaultColorScheme(Help.Ansi.ON));
        assertEquals(Help.Ansi.ON.new Text(String.format(
                "@|bold <best-app-ever>|@ [@|yellow --another-long-option-name|@=@|italic ^[<another-long-option-value>]|@]%n" +
                        "                [@|yellow --fourth-long-option-name|@=@|italic <fourth-long-option-value>|@]%n" +
                        "                [@|yellow --long-option@-name|@=@|italic <long-option-valu@@e>|@]%n" +
                        "                [@|yellow --third-long-option-name|@=@|italic <third-long-option-value>|@]%n")),
                help.synopsis());
    }

    @Test
    public void testCustomSynopsis() {
        @Command(customSynopsis = {
                "<the-app> --number=NUMBER --other-option=<aargh>",
                "          --more=OTHER --and-other-option=<aargh>",
                "<the-app> --number=NUMBER --and-other-option=<aargh>",
        })
        class App {@Option(names = "--ignored") boolean ignored;}
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals(String.format(
                "<the-app> --number=NUMBER --other-option=<aargh>%n" +
                "          --more=OTHER --and-other-option=<aargh>%n" +
                "<the-app> --number=NUMBER --and-other-option=<aargh>%n"),
                help.synopsis());
    }
    @Test
    public void testTextTable() {
        TextTable table = new TextTable(Help.Ansi.OFF);
        table.addRowValues(textArray(Help.Ansi.OFF, "", "-v", ",", "--verbose", "show what you're doing while you are doing it"));
        table.addRowValues(textArray(Help.Ansi.OFF, "", "-p", null, null, "the quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog."));
        assertEquals(String.format(
                "  -v, --verbose               show what you're doing while you are doing it%n" +
                "  -p                          the quick brown fox jumped over the lazy dog. The%n" +
                "                                quick brown fox jumped over the lazy dog.%n"
                ,""), table.toString(new StringBuilder()).toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTextTableAddsNewRowWhenTooManyValuesSpecified() {
        TextTable table = new TextTable(Help.Ansi.OFF);
        table.addRowValues(textArray(Help.Ansi.OFF, "", "-c", ",", "--create", "description", "INVALID", "Row 3"));
//        assertEquals(String.format("" +
//                        "  -c, --create                description                                       %n" +
//                        "                                INVALID                                         %n" +
//                        "                                Row 3                                           %n"
//                ,""), table.toString(new StringBuilder()).toString());
    }

    @Test
    public void testTextTableAddsNewRowWhenAnyColumnTooLong() {
        TextTable table = new TextTable(Help.Ansi.OFF);
        table.addRowValues("*", "-c", ",",
                "--create, --create2, --create3, --create4, --create5, --create6, --create7, --create8",
                "description");
        assertEquals(String.format("" +
                        "* -c, --create, --create2, --create3, --create4, --create5, --create6,%n" +
                        "        --create7, --create8%n" +
                        "                              description%n"
                ,""), table.toString(new StringBuilder()).toString());

        table = new TextTable(Help.Ansi.OFF);
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
        CommandLine.usage(new Cat(), new PrintStream(baos), Help.Ansi.OFF);
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
    public void testZipUsageFormat() {
        String expected  = String.format("" +
                "Copyright (c) 1990-2008 Info-ZIP - Type 'zip \"-L\"' for software license.%n" +
                "Zip 3.0 (July 5th 2008). Command:%n" +
                "zip [-options] [-b path] [-t mmddyyyy] [-n suffixes] [zipfile list] [-xi list]%n" +
                "  The default action is to add or replace zipfile entries from list, which%n" +
                "  can include the special name - to compress standard input.%n" +
                "  If zipfile and list are omitted, zip compresses stdin to stdout.%n" +
                "  -f   freshen: only changed files  -u   update: only changed or new files%n" +
                "  -d   delete entries in zipfile    -m   move into zipfile (delete OS files)%n" +
                "  -r   recurse into directories     -j   junk (don't record) directory names%n" +
                "  -0   store only                   -l   convert LF to CR LF (-ll CR LF to LF)%n" +
                "  -1   compress faster              -9   compress better%n" +
                "  -q   quiet operation              -v   verbose operation/print version info%n" +
                "  -c   add one-line comments        -z   add zipfile comment%n" +
                "  -@   read names from stdin        -o   make zipfile as old as latest entry%n" +
                "  -x   exclude the following names  -i   include only the following names%n" +
                "  -F   fix zipfile (-FF try harder) -D   do not add directory entries%n" +
                "  -A   adjust self-extracting exe   -J   junk zipfile prefix (unzipsfx)%n" +
                "  -T   test zipfile integrity       -X   eXclude eXtra file attributes%n" +
                "  -y   store symbolic links as the link instead of the referenced file%n" +
                "  -e   encrypt                      -n   don't compress these suffixes%n" +
                "  -h2  show more help%n");
        assertEquals(expected, CustomLayoutDemo.createZipUsageFormat(Help.Ansi.OFF));
    }
    @Test
    public void testNetstatUsageFormat() {
        String expected = String.format("" +
                        "Displays protocol statistics and current TCP/IP network connections.%n" +
                        "%n" +
                        "NETSTAT [-a] [-b] [-e] [-f] [-n] [-o] [-p proto] [-q] [-r] [-s] [-t] [-x] [-y]%n" +
                        "        [interval]%n" +
                        "%n" +
                        "  -a            Displays all connections and listening ports.%n" +
                        "  -b            Displays the executable involved in creating each connection or%n" +
                        "                listening port. In some cases well-known executables host%n" +
                        "                multiple independent components, and in these cases the%n" +
                        "                sequence of components involved in creating the connection or%n" +
                        "                listening port is displayed. In this case the executable name%n" +
                        "                is in [] at the bottom, on top is the component it called, and%n" +
                        "                so forth until TCP/IP was reached. Note that this option can be%n" +
                        "                time-consuming and will fail unless you have sufficient%n" +
                        "                permissions.%n" +
                        "  -e            Displays Ethernet statistics. This may be combined with the -s%n" +
                        "                option.%n" +
                        "  -f            Displays Fully Qualified Domain Names (FQDN) for foreign%n" +
                        "                addresses.%n" +
                        "  -n            Displays addresses and port numbers in numerical form.%n" +
                        "  -o            Displays the owning process ID associated with each connection.%n" +
                        "  -p proto      Shows connections for the protocol specified by proto; proto%n" +
                        "                may be any of: TCP, UDP, TCPv6, or UDPv6.  If used with the -s%n" +
                        "                option to display per-protocol statistics, proto may be any of:%n" +
                        "                IP, IPv6, ICMP, ICMPv6, TCP, TCPv6, UDP, or UDPv6.%n" +
                        "  -q            Displays all connections, listening ports, and bound%n" +
                        "                nonlistening TCP ports. Bound nonlistening ports may or may not%n" +
                        "                be associated with an active connection.%n" +
                        "  -r            Displays the routing table.%n" +
                        "  -s            Displays per-protocol statistics.  By default, statistics are%n" +
                        "                shown for IP, IPv6, ICMP, ICMPv6, TCP, TCPv6, UDP, and UDPv6;%n" +
                        "                the -p option may be used to specify a subset of the default.%n" +
                        "  -t            Displays the current connection offload state.%n" +
                        "  -x            Displays NetworkDirect connections, listeners, and shared%n" +
                        "                endpoints.%n" +
                        "  -y            Displays the TCP connection template for all connections.%n" +
                        "                Cannot be combined with the other options.%n" +
                        "  interval      Redisplays selected statistics, pausing interval seconds%n" +
                        "                between each display.  Press CTRL+C to stop redisplaying%n" +
                        "                statistics.  If omitted, netstat will print the current%n" +
                        "                configuration information once.%n"
                , "");
        assertEquals(expected, CustomLayoutDemo.createNetstatUsageFormat(Help.Ansi.OFF));
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
        String actual = usageString(new App(), Help.Ansi.OFF);
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
    @Command(name = "base", abbreviateSynopsis = true, commandListHeading = "c o m m a n d s",
            customSynopsis = "cust", description = "base description", descriptionHeading = "base descr heading",
            footer = "base footer", footerHeading = "base footer heading",
            header = "base header", headerHeading = "base header heading",
            optionListHeading = "base option heading", parameterListHeading = "base param heading",
            requiredOptionMarker = '&', separator = ";", showDefaultValues = true,
            sortOptions = false, synopsisHeading = "abcd")
    class Base { }

    @Test
    public void testAttributesInheritedWhenSubclassingForReuse() throws UnsupportedEncodingException {
        @Command
        class EmptySub extends Base {}
        Help help = new Help(new EmptySub());
        assertEquals("base", help.commandName);
        assertEquals(String.format("cust%n"), help.synopsis());
        assertEquals(String.format("cust%n"), help.customSynopsis());
        assertEquals(String.format("base%n"), help.abbreviatedSynopsis());
        assertEquals(String.format("base%n"), help.detailedSynopsis(null, true));
        assertEquals("abcd", help.synopsisHeading);
        assertEquals("", help.commandList());
        assertEquals("c o m m a n d s", help.commandListHeading);
        assertEquals(String.format("base description%n"), help.description());
        assertEquals("base descr heading", help.descriptionHeading);
        assertEquals(String.format("base footer%n"), help.footer());
        assertEquals("base footer heading", help.footerHeading);
        assertEquals(String.format("base header%n"), help.header());
        assertEquals("base header heading", help.headerHeading);
        assertEquals("", help.optionList());
        assertEquals("base option heading", help.optionListHeading);
        assertEquals("", help.parameterList());
        assertEquals("base param heading", help.parameterListHeading);

        // these values NOT inherited!!
        assertEquals("=", help.separator);
        assertEquals(' ', help.requiredOptionMarker.charValue());
        assertFalse(help.abbreviateSynopsis);
        assertFalse(help.showDefaultValues);
        assertTrue(help.sortOptions);
    }

    @Test
    public void testSubclassAttributesOverrideEmptySuper() {
        @Command
        class EmptyBase {}
        @Command(name = "base", abbreviateSynopsis = true, commandListHeading = "c o m m a n d s",
                customSynopsis = "cust", description = "base description", descriptionHeading = "base descr heading",
                footer = "base footer", footerHeading = "base footer heading",
                header = "base header", headerHeading = "base header heading",
                optionListHeading = "base option heading", parameterListHeading = "base param heading",
                requiredOptionMarker = '&', separator = ";", showDefaultValues = true,
                sortOptions = false, synopsisHeading = "abcd")
        class FullBase extends EmptyBase{ }
        Help help = new Help(new FullBase());
        assertEquals("base", help.commandName);
        assertEquals(String.format("cust%n"), help.synopsis());
        assertEquals(String.format("cust%n"), help.customSynopsis());
        assertEquals(String.format("base%n"), help.abbreviatedSynopsis());
        assertEquals(String.format("base%n"), help.detailedSynopsis(null, true));
        assertEquals("abcd", help.synopsisHeading);
        assertEquals("", help.commandList());
        assertEquals("c o m m a n d s", help.commandListHeading);
        assertEquals(String.format("base description%n"), help.description());
        assertEquals("base descr heading", help.descriptionHeading);
        assertEquals(String.format("base footer%n"), help.footer());
        assertEquals("base footer heading", help.footerHeading);
        assertEquals(String.format("base header%n"), help.header());
        assertEquals("base header heading", help.headerHeading);
        assertEquals("", help.optionList());
        assertEquals("base option heading", help.optionListHeading);
        assertEquals("", help.parameterList());
        assertEquals("base param heading", help.parameterListHeading);
        assertTrue(help.abbreviateSynopsis);
        assertTrue(help.showDefaultValues);
        assertFalse(help.sortOptions);
        assertEquals(";", help.separator);
        assertEquals('&', help.requiredOptionMarker.charValue());
    }
    @Test
    public void testSubclassAttributesOverrideSuperValues() {
        @Command(name = "sub", abbreviateSynopsis = false, commandListHeading = "subc o m m a n d s",
                customSynopsis = "subcust", description = "sub description", descriptionHeading = "sub descr heading",
                footer = "sub footer", footerHeading = "sub footer heading",
                header = "sub header", headerHeading = "sub header heading",
                optionListHeading = "sub option heading", parameterListHeading = "sub param heading",
                requiredOptionMarker = '%', separator = ":", showDefaultValues = false,
                sortOptions = true, synopsisHeading = "xyz")
        class FullSub extends Base{ }
        Help help = new Help(new FullSub());
        assertEquals("sub", help.commandName);
        assertEquals(String.format("subcust%n"), help.synopsis());
        assertEquals(String.format("subcust%n"), help.customSynopsis());
        assertEquals(String.format("sub%n"), help.abbreviatedSynopsis());
        assertEquals(String.format("sub%n"), help.detailedSynopsis(null, true));
        assertEquals("xyz", help.synopsisHeading);
        assertEquals("", help.commandList());
        assertEquals("subc o m m a n d s", help.commandListHeading);
        assertEquals(String.format("sub description%n"), help.description());
        assertEquals("sub descr heading", help.descriptionHeading);
        assertEquals(String.format("sub footer%n"), help.footer());
        assertEquals("sub footer heading", help.footerHeading);
        assertEquals(String.format("sub header%n"), help.header());
        assertEquals("sub header heading", help.headerHeading);
        assertEquals("", help.optionList());
        assertEquals("sub option heading", help.optionListHeading);
        assertEquals("", help.parameterList());
        assertEquals("sub param heading", help.parameterListHeading);
        assertFalse(help.abbreviateSynopsis);
        assertFalse(help.showDefaultValues);
        assertTrue(help.sortOptions);
        assertEquals(":", help.separator);
        assertEquals('%', help.requiredOptionMarker.charValue());
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
        String actual = usageString(new ChildOption(), Help.Ansi.OFF);
        assertEquals(String.format(
                "Usage: child%n" +
                "child description%n"), actual);
    }

    @Test
    public void testSynopsisOrderCorrectWhenParametersDeclaredOutOfOrder() {
        class WithParams {
            @Parameters(index = "1") String param1;
            @Parameters(index = "0") String param0;
        }
        Help help = new Help(new WithParams());
        assertEquals(format("<main class> <param0> <param1>%n"), help.synopsis());
    }

    @Test
    public void testSynopsisOrderCorrectWhenSubClassAddsParameters() {
        class BaseWithParams {
            @Parameters(index = "1") String param1;
            @Parameters(index = "0") String param0;
        }
        class SubWithParams extends BaseWithParams {
            @Parameters(index = "3") String param3;
            @Parameters(index = "2") String param2;
        }
        Help help = new Help(new SubWithParams());
        assertEquals(format("<main class> <param0> <param1> <param2> <param3>%n"), help.synopsis());
    }

    @Test
    public void testUsageMainCommand_NoAnsi() throws Exception {
        String actual = usageString(Demo.mainCommand(), Help.Ansi.OFF);
        assertEquals(String.format(Demo.EXPECTED_USAGE_MAIN), actual);
    }

    @Test
    public void testUsageMainCommand_ANSI() throws Exception {
        String actual = usageString(Demo.mainCommand(), Help.Ansi.ON);
        assertEquals(Help.Ansi.ON.new Text(String.format(Demo.EXPECTED_USAGE_MAIN_ANSI)), actual);
    }

    @Test
    public void testUsageSubcommandGitStatus_NoAnsi() throws Exception {
        String actual = usageString(new Demo.GitStatus(), Help.Ansi.OFF);
        assertEquals(String.format(Demo.EXPECTED_USAGE_GITSTATUS), actual);
    }

    @Test
    public void testUsageSubcommandGitStatus_ANSI() throws Exception {
        String actual = usageString(new Demo.GitStatus(), Help.Ansi.ON);
        assertEquals(Help.Ansi.ON.new Text(String.format(Demo.EXPECTED_USAGE_GITSTATUS_ANSI)), actual);
    }

    @Test
    public void testUsageSubcommandGitCommit_NoAnsi() throws Exception {
        String actual = usageString(new Demo.GitCommit(), Help.Ansi.OFF);
        assertEquals(String.format(Demo.EXPECTED_USAGE_GITCOMMIT), actual);
    }

    @Test
    public void testUsageSubcommandGitCommit_ANSI() throws Exception {
        String actual = usageString(new Demo.GitCommit(), Help.Ansi.ON);
        assertEquals(Help.Ansi.ON.new Text(String.format(Demo.EXPECTED_USAGE_GITCOMMIT_ANSI)), actual);
    }

    @Test
    public void testTextConstructorPlain() {
        assertEquals("--NoAnsiFormat", Help.Ansi.ON.new Text("--NoAnsiFormat").toString());
    }

    @Test
    public void testTextConstructorWithStyle() {
        assertEquals("\u001B[1m--NoAnsiFormat\u001B[21m\u001B[0m", Help.Ansi.ON.new Text("@|bold --NoAnsiFormat|@").toString());
    }

    @Ignore("Until nested styles are supported")
    @Test
    public void testTextConstructorWithNestedStyle() {
        assertEquals("\u001B[1mfirst \u001B[2msecond\u001B[22m\u001B[21m", Help.Ansi.ON.new Text("@|bold first @|underline second|@|@").toString());
        assertEquals("\u001B[1mfirst \u001B[4msecond\u001B[24m third\u001B[21m", Help.Ansi.ON.new Text("@|bold first @|underline second|@ third|@").toString());
    }

    @Test
    public void testTextApply() {
        Text txt = Help.Ansi.ON.apply("--p", Arrays.<IStyle>asList(Style.fg_red, Style.bold));
        assertEquals(Help.Ansi.ON.new Text("@|fg(red),bold --p|@"), txt);
    }

    @Test
    public void testTextDefaultColorScheme() {
        Help.Ansi ansi = Help.Ansi.ON;
        ColorScheme scheme = Help.defaultColorScheme(ansi);
        assertEquals(scheme.ansi().new Text("@|yellow -p|@"),      scheme.optionText("-p"));
        assertEquals(scheme.ansi().new Text("@|bold command|@"),  scheme.commandText("command"));
        assertEquals(scheme.ansi().new Text("@|yellow FILE|@"),   scheme.parameterText("FILE"));
        assertEquals(scheme.ansi().new Text("@|italic NUMBER|@"), scheme.optionParamText("NUMBER"));
    }

    @Test
    public void testTextSubString() {
        Help.Ansi ansi = Help.Ansi.ON;
        Text txt =   ansi.new Text("@|bold 01234|@").append("56").append("@|underline 7890|@");
        assertEquals(ansi.new Text("@|bold 01234|@56@|underline 7890|@"), txt.substring(0));
        assertEquals(ansi.new Text("@|bold 1234|@56@|underline 7890|@"), txt.substring(1));
        assertEquals(ansi.new Text("@|bold 234|@56@|underline 7890|@"), txt.substring(2));
        assertEquals(ansi.new Text("@|bold 34|@56@|underline 7890|@"), txt.substring(3));
        assertEquals(ansi.new Text("@|bold 4|@56@|underline 7890|@"), txt.substring(4));
        assertEquals(ansi.new Text("56@|underline 7890|@"), txt.substring(5));
        assertEquals(ansi.new Text("6@|underline 7890|@"), txt.substring(6));
        assertEquals(ansi.new Text("@|underline 7890|@"), txt.substring(7));
        assertEquals(ansi.new Text("@|underline 890|@"), txt.substring(8));
        assertEquals(ansi.new Text("@|underline 90|@"), txt.substring(9));
        assertEquals(ansi.new Text("@|underline 0|@"), txt.substring(10));
        assertEquals(ansi.new Text(""), txt.substring(11));
        assertEquals(ansi.new Text("@|bold 01234|@56@|underline 7890|@"), txt.substring(0, 11));
        assertEquals(ansi.new Text("@|bold 01234|@56@|underline 789|@"), txt.substring(0, 10));
        assertEquals(ansi.new Text("@|bold 01234|@56@|underline 78|@"), txt.substring(0, 9));
        assertEquals(ansi.new Text("@|bold 01234|@56@|underline 7|@"), txt.substring(0, 8));
        assertEquals(ansi.new Text("@|bold 01234|@56"), txt.substring(0, 7));
        assertEquals(ansi.new Text("@|bold 01234|@5"), txt.substring(0, 6));
        assertEquals(ansi.new Text("@|bold 01234|@"), txt.substring(0, 5));
        assertEquals(ansi.new Text("@|bold 0123|@"), txt.substring(0, 4));
        assertEquals(ansi.new Text("@|bold 012|@"), txt.substring(0, 3));
        assertEquals(ansi.new Text("@|bold 01|@"), txt.substring(0, 2));
        assertEquals(ansi.new Text("@|bold 0|@"), txt.substring(0, 1));
        assertEquals(ansi.new Text(""), txt.substring(0, 0));
        assertEquals(ansi.new Text("@|bold 1234|@56@|underline 789|@"), txt.substring(1, 10));
        assertEquals(ansi.new Text("@|bold 234|@56@|underline 78|@"), txt.substring(2, 9));
        assertEquals(ansi.new Text("@|bold 34|@56@|underline 7|@"), txt.substring(3, 8));
        assertEquals(ansi.new Text("@|bold 4|@56"), txt.substring(4, 7));
        assertEquals(ansi.new Text("5"), txt.substring(5, 6));
        assertEquals(ansi.new Text("@|bold 2|@"), txt.substring(2, 3));
        assertEquals(ansi.new Text("@|underline 8|@"), txt.substring(8, 9));

        Text txt2 =  ansi.new Text("@|bold abc|@@|underline DEF|@");
        assertEquals(ansi.new Text("@|bold abc|@@|underline DEF|@"), txt2.substring(0));
        assertEquals(ansi.new Text("@|bold bc|@@|underline DEF|@"), txt2.substring(1));
        assertEquals(ansi.new Text("@|bold abc|@@|underline DE|@"), txt2.substring(0,5));
        assertEquals(ansi.new Text("@|bold bc|@@|underline DE|@"), txt2.substring(1,5));
    }

    @Test
    public void testTextWithMultipleStyledSections() {
        assertEquals("\u001B[1m<main class>\u001B[21m\u001B[0m [\u001B[33m-v\u001B[39m\u001B[0m] [\u001B[33m-c\u001B[39m\u001B[0m [\u001B[3m<count>\u001B[23m\u001B[0m]]",
                Help.Ansi.ON.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@ [@|italic <count>|@]]").toString());
    }

    @Test
    public void testAdjacentStyles() {
        assertEquals("\u001B[3m<commit\u001B[23m\u001B[0m\u001B[3m>\u001B[23m\u001B[0m%n\u001B[0m",
                Help.Ansi.ON.new Text("@|italic <commit|@@|italic >|@%n").toString());
    }

    @Test
    public void testSystemPropertiesOverrideDefaultColorScheme() {
        @CommandLine.Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters(paramLabel = "FILE", arity = "1..*") File[] files;
        }
        Help.Ansi ansi = Help.Ansi.ON;
        // default color scheme
        assertEquals(ansi.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@=@|italic <count>|@] @|yellow FILE|@ [@|yellow FILE|@...]" + LINESEP),
                new Help(new App(), ansi).synopsis());

        System.setProperty("picocli.color.commands", "blue");
        System.setProperty("picocli.color.options", "green");
        System.setProperty("picocli.color.parameters", "cyan");
        System.setProperty("picocli.color.optionParams", "magenta");
        assertEquals(ansi.new Text("@|blue <main class>|@ [@|green -v|@] [@|green -c|@=@|magenta <count>|@] @|cyan FILE|@ [@|cyan FILE|@...]" + LINESEP),
                new Help(new App(), ansi).synopsis());
    }

    @Test
    public void testSystemPropertiesOverrideExplicitColorScheme() {
        @CommandLine.Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters(paramLabel = "FILE", arity = "1..*") File[] files;
        }
        Help.Ansi ansi = Help.Ansi.ON;
        ColorScheme explicit = new ColorScheme(ansi)
                .commands(Style.faint, Style.bg_magenta)
                .options(Style.bg_red)
                .parameters(Style.reverse)
                .optionParams(Style.bg_green);
        // default color scheme
        assertEquals(ansi.new Text("@|faint,bg(magenta) <main class>|@ [@|bg(red) -v|@] [@|bg(red) -c|@=@|bg(green) <count>|@] @|reverse FILE|@ [@|reverse FILE|@...]" + LINESEP),
                new Help(new App(), explicit).synopsis());

        System.setProperty("picocli.color.commands", "blue");
        System.setProperty("picocli.color.options", "blink");
        System.setProperty("picocli.color.parameters", "red");
        System.setProperty("picocli.color.optionParams", "magenta");
        assertEquals(ansi.new Text("@|blue <main class>|@ [@|blink -v|@] [@|blink -c|@=@|magenta <count>|@] @|red FILE|@ [@|red FILE|@...]" + LINESEP),
                new Help(new App(), explicit).synopsis());
    }

}
