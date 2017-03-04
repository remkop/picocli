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

import org.junit.Ignore;
import org.junit.Test;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Column;
import picocli.CommandLine.Help.TextTable;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Usage;

import java.awt.Point;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.String;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.junit.Assert.*;
import static picocli.CommandLine.Help.Column.Overflow.*;

/**
 * Tests for picoCLI's "Usage" help functionality.
 */
public class CommandLineHelpTest {
    private static String usageString(Class<?> annotatedClass) throws UnsupportedEncodingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.usage(annotatedClass, new PrintStream(baos, true, "UTF8"));
        String result = baos.toString("UTF8");
        return result;
    }

    @Test
    public void testUsageAnnotationDetailedUsage() throws Exception {
        @Usage(detailedUsageHeader = true)
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use") File file;
        }
        String result = usageString(Params.class);
        assertEquals(format("" +
                        "Usage: <main class> -f <file>%n" +
                        "  -f, --file <file>           the file to use                                   %n",
                ""), result);
    }

    @Test
    public void testUsageSeparator() throws Exception {
        @Usage(separator = "=", detailedUsageHeader = true)
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use") File file;
        }
        String result = usageString(Params.class);
        assertEquals(format("" +
                        "Usage: <main class> -f=<file>%n" +
                        "  -f, --file=<file>           the file to use                                   %n",
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
        Help.IParameterRenderer parameterRenderer = Help.createDefaultParameterRenderer(" ");
        Help help = new Help(Example.class);
        Iterator<Map.Entry<Option, Field>> iterator = help.option2Field.entrySet().iterator();
        Map.Entry<Option, Field> entry = iterator.next();
        String[][] row1 = renderer.render(entry.getKey(), entry.getValue(), parameterRenderer);
        assertEquals(1, row1.length);
        assertArrayEquals(new String[]{"---long <longField>", "long description"}, row1[0]);

        entry = iterator.next();
        String[][] row2 = renderer.render(entry.getKey(), entry.getValue(), parameterRenderer);
        assertEquals(1, row2.length);
        assertArrayEquals(new String[]{"-b <otherField>", "other"}, row2[0]);
    }

    @Test
    public void testCreateDefaultOptionRenderer_ReturnsDefaultOptionRenderer() {
        assertEquals(Help.DefaultOptionRenderer.class, Help.createDefaultOptionRenderer().getClass());
    }

    @Test
    public void testDefaultOptionRenderer_rendersShortestOptionNameThenOtherOptionNamesAndDescription() {
        class Example {
            @Option(names = {"---long", "-L"}, description = "long description") String longField;
            @Option(names = {"-b", "-a", "--alpha"}, description = "other") String otherField;
        }
        Help.IOptionRenderer renderer = Help.createDefaultOptionRenderer();
        Help.IParameterRenderer parameterRenderer = Help.createDefaultParameterRenderer(" ");
        Help help = new Help(Example.class);
        Iterator<Map.Entry<Option, Field>> iterator = help.option2Field.entrySet().iterator();
        Map.Entry<Option, Field> entry = iterator.next();
        String[][] row1 = renderer.render(entry.getKey(), entry.getValue(), parameterRenderer);
        assertEquals(1, row1.length);
        assertArrayEquals(Arrays.toString(row1[0]), new String[]{"-L", ",", "---long <longField>", "long description"}, row1[0]);

        entry = iterator.next();
        String[][] row2 = renderer.render(entry.getKey(), entry.getValue(), parameterRenderer);
        assertEquals(1, row2.length);
        assertArrayEquals(Arrays.toString(row2[0]), new String[]{"-b", ",", "-a, --alpha <otherField>", "other"}, row2[0]);
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
        Help.IOptionRenderer renderer = Help.createDefaultOptionRenderer();
        Help.IParameterRenderer parameterRenderer = Help.createDefaultParameterRenderer(" ");
        Help help = new Help(Example.class);

        String[][] expected = new String[][] {
                {"-v", "",  "", "shortBool"},
                {"",   "",  "--verbose", "longBool"},
                {"-x", ",", "--xeno", "combiBool"},
                {"-s", "",  " <shortOnlyField>", "shortOnly"},
                {"",   "",  "--long <longOnlyField>", "longOnly"},
                {"-b", ",", "--beta <combiField>", "combi"},
        };
        int i = -1;
        for (Map.Entry<Option, Field> entry : help.option2Field.entrySet()) {
            String[][] row = renderer.render(entry.getKey(), entry.getValue(), parameterRenderer);
            assertEquals(1, row.length);
            assertArrayEquals(Arrays.toString(row[0]), expected[++i], row[0]);
        }
    }

    @Test
    public void testCreateDefaultParameterRenderer_ReturnsDefaultParameterRenderer() {
        assertEquals(Help.DefaultParameterRenderer.class, Help.createDefaultParameterRenderer("=").getClass());
    }

    @Test
    public void testDefaultParameterRenderer_showsParamLabelIfPresentOrFieldNameOtherwise() {
        class Example {
            @Option(names = "--without" ) String longField;
            @Option(names = "--with", paramLabel = "LABEL") String otherField;
        }
        Help.IParameterRenderer spaceSeparatedParameterRenderer = Help.createDefaultParameterRenderer(" ");
        Help.IParameterRenderer equalSeparatedParameterRenderer = Help.createDefaultParameterRenderer("=");
        Help help = new Help(Example.class);

        String[] expected = new String[] {
                "<longField>",
                "LABEL",
        };
        int i = -1;
        for (Map.Entry<Option, Field> entry : help.option2Field.entrySet()) {
            i++;
            String withSpace = spaceSeparatedParameterRenderer.renderParameter(entry.getValue());
            assertEquals(withSpace, " " + expected[i], withSpace);
            String withEquals = equalSeparatedParameterRenderer.renderParameter(entry.getValue());
            assertEquals(withEquals, "=" + expected[i], withEquals);
        }
    }

    @Test
    public void testDefaultParameterRenderer_appliesToPositionalArgumentsIgnoresSeparator() {
        class WithLabel    { @Parameters(paramLabel = "POSITIONAL_ARGS") String positional; }
        class WithoutLabel { @Parameters()                               String positional; }
        Help.IParameterRenderer spaced = Help.createDefaultParameterRenderer(" ");
        Help.IParameterRenderer equals = Help.createDefaultParameterRenderer("=");

        Help withLabel = new Help(WithLabel.class);
        String withSpace = spaced.renderParameter(withLabel.positionalParametersField);
        assertEquals(withSpace, "POSITIONAL_ARGS", withSpace);
        String withEquals = equals.renderParameter(withLabel.positionalParametersField);
        assertEquals(withEquals, "POSITIONAL_ARGS", withEquals);

        Help withoutLabel = new Help(WithoutLabel.class);
        withSpace = spaced.renderParameter(withoutLabel.positionalParametersField);
        assertEquals(withSpace, "<positional>", withSpace);
        withEquals = equals.renderParameter(withoutLabel.positionalParametersField);
        assertEquals(withEquals, "<positional>", withEquals);
    }

    @Test
    public void testCreateDefaultLayout_returnsDefaultLayout() {
        assertEquals(Help.DefaultLayout.class, Help.createDefaultLayout().getClass());
    }

    @Test
    public void testDefaultLayout_addsEachRowToTable() {
        Help.ILayout layout = Help.createDefaultLayout();
        final String[][] values = { {"a", "b", "c", "d" }, {"1", "2", "3", "4"} };
        layout.layout(null, null, values, new TextTable() {
            int count = 0;
            @Override public void addRow(String[] columnValues) {
                assertArrayEquals(values[count], columnValues);
                count++;
            }
        });
    }

    @Test
    public void testTextTable() {
        TextTable table = new TextTable();
        table.addRow("-v", ",", "--verbose", "show what you're doing while you are doing it");
        table.addRow("-p", null, null, "the quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog.");
        assertEquals(String.format(
                "  -v, --verbose               show what you're doing while you are doing it     %n" +
                        "  -p                          the quick brown fox jumped over the lazy dog. The %n" +
                        "                                quick brown fox jumped over the lazy dog.       %n"
                ,""), table.toString(new StringBuilder()).toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTextTableAddsNewRowWhenTooManyValuesSpecified() {
        TextTable table = new TextTable();
        table.addRow("-c", ",", "--create", "description", "INVALID", "Row 3");
//        assertEquals(String.format("" +
//                        "  -c, --create                description                                       %n" +
//                        "                                INVALID                                         %n" +
//                        "                                Row 3                                           %n"
//                ,""), table.toString(new StringBuilder()).toString());
    }

    @Test
    public void testTextTableAddsNewRowWhenAnyColumnTooLong() {
        TextTable table = new TextTable();
        table.addRow("-c", ",",
                "--create, --create2, --create3, --create4, --create5, --create6, --create7, --create8",
                "description");
        assertEquals(String.format("" +
                        "  -c, --create, --create2, --create3, --create4, --create5, --create6, --       %n" +
                        "        create7, --create8                                                      %n" +
                        "                              description                                       %n"
                ,""), table.toString(new StringBuilder()).toString());

        table = new TextTable();
        table.addRow("-c", ",",
                "--create, --create2, --create3, --create4, --create5, --create6, --createAA7, --create8",
                "description");
        assertEquals(String.format("" +
                        "  -c, --create, --create2, --create3, --create4, --create5, --create6, --       %n" +
                        "        createAA7, --create8                                                    %n" +
                        "                              description                                       %n"
                ,""), table.toString(new StringBuilder()).toString());
    }

    @Test
    public void testCatUsageFormat() {
        @Usage(programName = "cat",
                summary = "Concatenate FILE(s), or standard input, to standard output.",
                footer = "Copyright(c) 2017")
        class Cat {
            @Parameters(paramLabel = "FILE",              description = "Files whose contents to display") List<File> files;
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
        CommandLine.usage(Cat.class, new PrintStream(baos));
        String expected = String.format(
                "Usage: cat [OPTIONS] [FILE...]%n" +
                        "Concatenate FILE(s), or standard input, to standard output.%n" +
                        "  -A, --show-all              equivalent to -vET                                %n" +
                        "  -b, --number-nonblank       number nonempty output lines, overrides -n        %n" +
                        "  -e                          equivalent to -vET                                %n" +
                        "  -E, --show-ends             display $ at end of each line                     %n" +
                        "  -n, --number                number all output lines                           %n" +
                        "  -s, --squeeze-blank         suppress repeated empty output lines              %n" +
                        "  -t                          equivalent to -vT                                 %n" +
                        "  -T, --show-tabs             display TAB characters as ^I                      %n" +
                        "  -u                          (ignored)                                         %n" +
                        "  -v, --show-nonprinting      use ^ and M- notation, except for LDF and TAB     %n" +
                        "      --help                  display this help and exit                        %n" +
                        "      --version               output version information and exit               %n" +
                        "Copyright(c) 2017%n", "");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testZipUsageFormat() {
        @Usage(summary = {
                "Copyright (c) 1990-2008 Info-ZIP - Type 'zip \"-L\"' for software license.",
                "Zip 3.0 (July 5th 2008). Usage:",
                "zip [-options] [-b path] [-t mmddyyyy] [-n suffixes] [zipfile list] [-xi list]",
                "  The default action is to add or replace zipfile entries from list, which",
                "  can include the special name - to compress standard input.",
                "  If zipfile and list are omitted, zip compresses stdin to stdout."}
        )
        class Zip {
            @Option(names = "-f", description = "freshen: only changed files") boolean freshen;
            @Option(names = "-u", description = "update: only changed or new files") boolean update;
            @Option(names = "-d", description = "delete entries in zipfile") boolean delete;
            @Option(names = "-m", description = "move into zipfile (delete OS files)") boolean move;
            @Option(names = "-r", description = "recurse into directories") boolean recurse;
            @Option(names = "-j", description = "junk (don't record) directory names") boolean junk;
            @Option(names = "-0", description = "store only") boolean store;
            @Option(names = "-l", description = "convert LF to CR LF (-ll CR LF to LF)") boolean lf2crlf;
            @Option(names = "-1", description = "compress faster") boolean faster;
            @Option(names = "-9", description = "compress better") boolean better;
            @Option(names = "-q", description = "quiet operation") boolean quiet;
            @Option(names = "-v", description = "verbose operation/print version info") boolean verbose;
            @Option(names = "-c", description = "add one-line comments") boolean comments;
            @Option(names = "-z", description = "add zipfile comment") boolean zipComment;
            @Option(names = "-@", description = "read names from stdin") boolean readFileList;
            @Option(names = "-o", description = "make zipfile as old as latest entry") boolean old;
            @Option(names = "-x", description = "exclude the following names") boolean exclude;
            @Option(names = "-i", description = "include only the following names") boolean include;
            @Option(names = "-F", description = "fix zipfile (-FF try harder)") boolean fix;
            @Option(names = "-D", description = "do not add directory entries") boolean directories;
            @Option(names = "-A", description = "adjust self-extracting exe") boolean adjust;
            @Option(names = "-J", description = "junk zipfile prefix (unzipsfx)") boolean junkPrefix;
            @Option(names = "-T", description = "test zipfile integrity") boolean test;
            @Option(names = "-X", description = "eXclude eXtra file attributes") boolean excludeAttribs;
            @Option(names = "-y", description = "store symbolic links as the link instead of the referenced file") boolean symbolic;
            @Option(names = "-e", description = "encrypt") boolean encrypt;
            @Option(names = "-n", description = "don't compress these suffixes") boolean dontCompress;
            @Option(names = "-h2", description = "show more help") boolean moreHelp;
        }
        String expected  = String.format("" +
                "Copyright (c) 1990-2008 Info-ZIP - Type 'zip \"-L\"' for software license.%n" +
                "Zip 3.0 (July 5th 2008). Usage:%n" +
                "zip [-options] [-b path] [-t mmddyyyy] [-n suffixes] [zipfile list] [-xi list]%n" +
                "  The default action is to add or replace zipfile entries from list, which%n" +
                "  can include the special name - to compress standard input.%n" +
                "  If zipfile and list are omitted, zip compresses stdin to stdout.%n" +
                "  -f   freshen: only changed files  -u   update: only changed or new files    %n" +
                "  -d   delete entries in zipfile    -m   move into zipfile (delete OS files)  %n" +
                "  -r   recurse into directories     -j   junk (don't record) directory names  %n" +
                "  -0   store only                   -l   convert LF to CR LF (-ll CR LF to LF)%n" +
                "  -1   compress faster              -9   compress better                      %n" +
                "  -q   quiet operation              -v   verbose operation/print version info %n" +
                "  -c   add one-line comments        -z   add zipfile comment                  %n" +
                "  -@   read names from stdin        -o   make zipfile as old as latest entry  %n" +
                "  -x   exclude the following names  -i   include only the following names     %n" +
                "  -F   fix zipfile (-FF try harder) -D   do not add directory entries         %n" +
                "  -A   adjust self-extracting exe   -J   junk zipfile prefix (unzipsfx)       %n" +
                "  -T   test zipfile integrity       -X   eXclude eXtra file attributes        %n" +
                "  -y   store symbolic links as the link instead of the referenced file        %n" +
                "  -e   encrypt                      -n   don't compress these suffixes        %n" +
                "  -h2  show more help                                                         %n", "");
        Help help = new Help(Zip.class);
        StringBuilder sb = new StringBuilder();
        help.appendSummaryTo(sb); // show the first 6 lines, including copyright, description and usage
        TextTable textTable = new TextTable(new Column(5, 2, TRUNCATE), // values should fit
                                            new Column(30, 2, SPAN), // overflow into adjacent columns
                                            new Column(4,  1, TRUNCATE), // values should fit again
                                            new Column(39, 2, WRAP)); // overflow into next row (same column)
        textTable.optionRenderer = Help.createMinimalOptionRenderer(); // define and install a custom renderer
        textTable.layout = new Help.ILayout() { // define and install a custom layout
            Point previous = new Point(0, 0);
            public void layout(Option option, Field field, String[][] values, TextTable table) {
                String[] columnValues = values[0]; // we know renderer creates a single row with two values

                // We want to show two options on one row, next to each other,
                // unless the first option spanned multiple columns (in which case there are not enough columns left)
                int col = previous.x + 1;
                if (col == 1 || col + columnValues.length > table.columns.length) { // if true, write into next row

                    // table also adds an empty row if a text value spanned multiple columns
                    if (table.rowCount() == 0 || table.rowCount() == previous.y + 1) { // avoid adding 2 empty rows
                        table.addEmptyRow(); // create the slots to write the text values into
                    }
                    col = 0; // we are starting a new row, reset the column to write into
                }
                for (int i = 0; i < columnValues.length; i++) {
                    // always write to the last row, column depends on what happened previously
                    previous = table.putValue(table.rowCount() - 1, col + i, columnValues[i]);
                }
            }
        };
        // Now that the textTable has a renderer and layout installed,
        // we add Options to the textTable to build up the option details help text.
        // Note that we don't sort the options, so they appear in the order the fields are declared in the Zip class.
        for (Option option : help.option2Field.keySet()) {
            if (!option.hidden()) {
                textTable.addOption(option, help.option2Field.get(option));
            }
        }
        textTable.toString(sb); // finally, copy the options details help text into the StringBuilder
        assertEquals(expected, sb.toString());
    }

    /** for Netstat test */
    private enum Protocol {IP, IPv6, ICMP, ICMPv6, TCP, TCPv6, UDP, UDPv6}
    @Test
    public void testNetstatUsageFormat() {
        @Usage(programName = "NETSTAT",
                separator = " ",
                detailedUsageHeader = true,
                summary = {"Displays protocol statistics and current TCP/IP network connections.", ""})
        class Netstat {
            @Option(names="-a", description="Displays all connections and listening ports.")
            boolean displayAll;
            @Option(names="-b", description="Displays the executable involved in creating each connection or "
                    + "listening port. In some cases well-known executables host "
                    + "multiple independent components, and in these cases the "
                    + "sequence of components involved in creating the connection "
                    + "or listening port is displayed. In this case the executable "
                    + "name is in [] at the bottom, on top is the component it called, "
                    + "and so forth until TCP/IP was reached. Note that this option "
                    + "can be time-consuming and will fail unless you have sufficient "
                    + "permissions.")
            boolean displayExecutable;
            @Option(names="-e", description="Displays Ethernet statistics. This may be combined with the -s option.")
            boolean displayEthernetStats;
            @Option(names="-f", description="Displays Fully Qualified Domain Names (FQDN) for foreign addresses.")
            boolean displayFQCN;
            @Option(names="-n", description="Displays addresses and port numbers in numerical form.")
            boolean displayNumerical;
            @Option(names="-o", description="Displays the owning process ID associated with each connection.")
            boolean displayOwningProcess;
            @Option(names="-p", paramLabel = "proto",
                    description="Shows connections for the protocol specified by proto; proto "
                    + "may be any of: TCP, UDP, TCPv6, or UDPv6.  If used with the -s "
                    + "option to display per-protocol statistics, proto may be any of: "
                    + "IP, IPv6, ICMP, ICMPv6, TCP, TCPv6, UDP, or UDPv6.")
            Protocol proto;
            @Option(names="-q", description="Displays all connections, listening ports, and bound "
                    + "nonlistening TCP ports. Bound nonlistening ports may or may not "
                    + "be associated with an active connection.")
            boolean query;
            @Option(names="-r", description="Displays the routing table.")
            boolean displayRoutingTable;
            @Option(names="-s", description="Displays per-protocol statistics.  By default, statistics are "
                    + "shown for IP, IPv6, ICMP, ICMPv6, TCP, TCPv6, UDP, and UDPv6; "
                    + "the -p option may be used to specify a subset of the default.")
            boolean displayStatistics;
            @Option(names="-t", description="Displays the current connection offload state.")
            boolean displayOffloadState;
            @Option(names="-x", description="Displays NetworkDirect connections, listeners, and shared endpoints.")
            boolean displayNetDirect;
            @Option(names="-y", description="Displays the TCP connection template for all connections. "
                    + "Cannot be combined with the other options.")
            boolean displayTcpConnectionTemplate;
            @Parameters(arity = "0..1", paramLabel = "interval", description = ""
                    + "Redisplays selected statistics, pausing interval seconds "
                    + "between each display.  Press CTRL+C to stop redisplaying "
                    + "statistics.  If omitted, netstat will print the current "
                    + "configuration information once.")
            int interval;
        }
        StringBuilder sb = new StringBuilder();
        Help help = new Help(Netstat.class);
        help.appendSummaryTo(sb).appendDetailedUsagePatternsTo("", null, sb);
        sb.append(System.getProperty("line.separator"));

        TextTable textTable = new TextTable(
                new Column(15, 2, TRUNCATE),
                new Column(65, 1, WRAP));
        textTable.optionRenderer = Help.createMinimalOptionRenderer();
        textTable.parameterRenderer = help.parameterRenderer;
        textTable.indentWrappedLines = 0;
        for (Option option : help.option2Field.keySet()) {
            textTable.addOption(option, help.option2Field.get(option));
        }
        // FIXME needs Show positional parameters details in TextTable similar to option details #48
        // textTable.addOption(help.positionalParametersField.getAnnotation(CommandLine.Parameters.class), help.positionalParametersField);
        textTable.toString(sb);
        String expected = String.format("" +
                "Displays protocol statistics and current TCP/IP network connections.%n" +
                "%n" +
                "NETSTAT [-a] [-b] [-e] [-f] [-n] [-o] [-p proto] [-q] [-r] [-s] [-t] [-x] [-y] [interval]%n" +
                // FIXME needs Show multiple detailed usage header lines for mutually exclusive options #46
                // "NETSTAT [-a] [-b] [-e] [-f] [-n] [-o] [-p proto] [-q] [-r] [-s] [-t] [-x] [interval]%n" +
                // "NETSTAT [-y] [interval]%n" +
                "%n" +
                "  -a            Displays all connections and listening ports.                   %n" +
                "  -b            Displays the executable involved in creating each connection or %n" +
                "                listening port. In some cases well-known executables host       %n" +
                "                multiple independent components, and in these cases the         %n" +
                "                sequence of components involved in creating the connection or   %n" +
                "                listening port is displayed. In this case the executable name   %n" +
                "                is in [] at the bottom, on top is the component it called, and  %n" +
                "                so forth until TCP/IP was reached. Note that this option can be %n" +
                "                time-consuming and will fail unless you have sufficient         %n" +
                "                permissions.                                                    %n" +
                "  -e            Displays Ethernet statistics. This may be combined with the -s  %n" +
                "                option.                                                         %n" +
                "  -f            Displays Fully Qualified Domain Names (FQDN) for foreign        %n" +
                "                addresses.                                                      %n" +
                "  -n            Displays addresses and port numbers in numerical form.          %n" +
                "  -o            Displays the owning process ID associated with each connection. %n" +
                "  -p proto      Shows connections for the protocol specified by proto; proto    %n" +
                "                may be any of: TCP, UDP, TCPv6, or UDPv6.  If used with the -s  %n" +
                "                option to display per-protocol statistics, proto may be any of: %n" +
                "                IP, IPv6, ICMP, ICMPv6, TCP, TCPv6, UDP, or UDPv6.              %n" +
                "  -q            Displays all connections, listening ports, and bound            %n" +
                "                nonlistening TCP ports. Bound nonlistening ports may or may not %n" +
                "                be associated with an active connection.                        %n" +
                "  -r            Displays the routing table.                                     %n" +
                "  -s            Displays per-protocol statistics.  By default, statistics are   %n" +
                "                shown for IP, IPv6, ICMP, ICMPv6, TCP, TCPv6, UDP, and UDPv6;   %n" +
                "                the -p option may be used to specify a subset of the default.   %n" +
                "  -t            Displays the current connection offload state.                  %n" +
                "  -x            Displays NetworkDirect connections, listeners, and shared       %n" +
                "                endpoints.                                                      %n" +
                "  -y            Displays the TCP connection template for all connections.       %n" +
                "                Cannot be combined with the other options.                      %n"
// FIXME needs Show positional parameters details in TextTable similar to option details #48
//                "  interval      Redisplays selected statistics, pausing interval seconds        %n" +
//                "                between each display.  Press CTRL+C to stop redisplaying        %n" +
//                "                statistics.  If omitted, netstat will print the current         %n" +
//                "                configuration information once.                                 %n"
        , "");
        assertEquals(expected, sb.toString());
    }
}
