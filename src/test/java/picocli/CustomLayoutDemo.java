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

import picocli.CommandLine.Help.Ansi.Text;
import picocli.CommandLine.Help.Column;
import picocli.CommandLine.Help.IOptionRenderer;
import picocli.CommandLine.Help.IParameterRenderer;
import picocli.CommandLine.Help.Layout;
import picocli.CommandLine.Help.TextTable;

import static picocli.CommandLine.*;
import static picocli.CommandLine.Model.*;
import static picocli.CommandLine.Help.Column.Overflow.*;

/**
 * Demonstrates how the CommandLine.Help API can be used to create custom layouts for usage help messages.
 */
@Command(name = "picocli.CustomLayoutDemo", description = "Demonstrates picocli custom layouts.",
        footer = {
        "Run with -Dpicocli.ansi=true  to force picocli to use ansi codes,",
        "or  with -Dpicocli.ansi=false to force picocli to NOT use ansi codes.",
        "By default picocli will use ansi codes if the platform supports it."
        }
)
public class CustomLayoutDemo implements Runnable {
    public static void main(String[] args) {
        new CommandLine(new CustomLayoutDemo()).execute(args);
    }

    @Option(names = {"-z", "--zip"}, description = "Show usage help for a layout with 2 options per row.")
    private boolean showZip;

    @Option(names = {"-n", "--netstat"}, description = "Show usage help for a layout with a narrow options column and a wide description column. Descriptions that wrap to the next row are not indented.")
    private boolean showNetstat;

    public void run() {
        if (!showZip && !showNetstat) {
            CommandLine.usage(this, System.err);
            return;
        }
        if (showZip)     { System.out.println(createZipUsageFormat(Help.Ansi.AUTO)); }
        if (showNetstat) { System.out.println(createNetstatUsageFormat(Help.Ansi.AUTO)); }
    }


    public static String createZipUsageFormat(Help.Ansi ansi) {
        @Command(description = {
                "Copyright (c) 1990-2008 Info-ZIP - Type 'zip \"-L\"' for software license.",
                "Zip 3.0 (July 5th 2008). Command:",
                "@|bold zip|@ [@|yellow -options|@] [@|yellow -b|@ @|underline path|@] [@|yellow -t|@ @|underline mmddyyyy|@] [@|yellow -n|@ @|underline suffixes|@] [@|yellow zipfile|@ @|underline list|@] [@|yellow -xi|@ @|underline list|@]",
                "  The default action is to add or replace zipfile entries from list, which",
                "  can include the special name - to compress standard input.",
                "  If @|yellow zipfile|@ and @|yellow list|@ are omitted, zip compresses stdin to stdout."}
        )
        class Zip {
            @Option(names = "-f", description = "freshen: only changed files")
            boolean freshen;
            @Option(names = "-u", description = "update: only changed or new files")
            boolean update;
            @Option(names = "-d", description = "delete entries in zipfile")
            boolean delete;
            @Option(names = "-m", description = "move into zipfile (delete OS files)")
            boolean move;
            @Option(names = "-r", description = "recurse into directories")
            boolean recurse;
            @Option(names = "-j", description = "junk (don't record) directory names")
            boolean junk;
            @Option(names = "-0", description = "store only")
            boolean store;
            @Option(names = "-l", description = "convert LF to CR LF (@|yellow -ll|@ CR LF to LF)")
            boolean lf2crlf;
            @Option(names = "-1", description = "compress faster")
            boolean faster;
            @Option(names = "-9", description = "compress better")
            boolean better;
            @Option(names = "-q", description = "quiet operation")
            boolean quiet;
            @Option(names = "-v", description = "verbose operation/print version info")
            boolean verbose;
            @Option(names = "-c", description = "add one-line comments")
            boolean comments;
            @Option(names = "-z", description = "add zipfile comment")
            boolean zipComment;
            @Option(names = "-@", description = "read names from stdin")
            boolean readFileList;
            @Option(names = "-o", description = "make zipfile as old as latest entry")
            boolean old;
            @Option(names = "-x", description = "exclude the following names")
            boolean exclude;
            @Option(names = "-i", description = "include only the following names")
            boolean include;
            @Option(names = "-F", description = "fix zipfile (@|yellow -FF|@ try harder)")
            boolean fix;
            @Option(names = "-D", description = "do not add directory entries")
            boolean directories;
            @Option(names = "-A", description = "adjust self-extracting exe")
            boolean adjust;
            @Option(names = "-J", description = "junk zipfile prefix (unzipsfx)")
            boolean junkPrefix;
            @Option(names = "-T", description = "test zipfile integrity")
            boolean test;
            @Option(names = "-X", description = "eXclude eXtra file attributes")
            boolean excludeAttribs;
            @Option(names = "-y", description = "store symbolic links as the link instead of the referenced file")
            boolean symbolic;
            @Option(names = "-e", description = "encrypt")
            boolean encrypt;
            @Option(names = "-n", description = "don't compress these suffixes")
            boolean dontCompress;
            @Option(names = "-h2", description = "show more help")
            boolean moreHelp;
        }

        class TwoOptionsPerRowLayout extends Layout { // define a custom layout
            TextTable.Cell previous = new TextTable.Cell(0, 0);

            private TwoOptionsPerRowLayout(Help.ColorScheme colorScheme, TextTable textTable,
                                           IOptionRenderer optionRenderer,
                                           IParameterRenderer parameterRenderer) {
                super(colorScheme, textTable, optionRenderer, parameterRenderer);
            }

            @Override
            public void layout(ArgSpec arg, Text[][] values) {
                Text[] columnValues = values[0]; // we know renderer creates a single row with two values

                // We want to show two options on one row, next to each other,
                // unless the first option spanned multiple columns (in which case there are not enough columns left)
                int col = previous.column + 1;
                if (col == 1 || col + columnValues.length > table.columns().length) { // if true, write into next row

                    // table also adds an empty row if a text value spanned multiple columns
                    if (table.rowCount() == 0 || table.rowCount() == previous.row + 1) { // avoid adding 2 empty rows
                        table.addEmptyRow(); // create the slots to write the text values into
                    }
                    col = 0; // we are starting a new row, reset the column to write into
                }
                for (int i = 0; i < columnValues.length; i++) {
                    // always write to the last row, column depends on what happened previously
                    previous = table.putValue(table.rowCount() - 1, col + i, columnValues[i]);
                }
            }
        }
        @SuppressWarnings("deprecation")
        TextTable textTable = TextTable.forColumns(ansi,
                new Column(5, 2, TRUNCATE), // values should fit
                new Column(30, 2, SPAN), // overflow into adjacent columns
                new Column(4, 1, TRUNCATE), // values should fit again
                new Column(39, 2, WRAP));
        TwoOptionsPerRowLayout layout = new TwoOptionsPerRowLayout(
                Help.defaultColorScheme(ansi),
                textTable,
                Help.createMinimalOptionRenderer(),
                Help.createMinimalParameterRenderer());

        Help help = new Help(new Zip(), ansi);
        StringBuilder sb = new StringBuilder();
        sb.append(help.description()); // show the first 6 lines, including copyright, description and usage

        // Note that we don't sort the options, so they appear in the order the fields are declared in the Zip class.
        layout.addOptions(help.options(), help.parameterLabelRenderer());
        sb.append(layout); // finally, copy the options details help text into the StringBuilder

        return sb.toString();
    }

    /** for Netstat test */
    private enum Protocol {IP, IPv6, ICMP, ICMPv6, TCP, TCPv6, UDP, UDPv6}

    public static String createNetstatUsageFormat(Help.Ansi ansi) {
        @Command(name = "NETSTAT",
                separator = " ",
                abbreviateSynopsis = true,
                header = "Displays protocol statistics and current TCP/IP network connections.%n")
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
        Help help = new Help(new Netstat(), ansi);
        help.synopsisHeading("");
        sb.append(help.header()).append(help.detailedSynopsis(0, null, false));
        sb.append(System.getProperty("line.separator"));

        @SuppressWarnings("deprecation")
        TextTable textTable = TextTable.forColumns(ansi,
                new Column(15, 2, TRUNCATE),
                new Column(65, 1, WRAP));
        textTable.indentWrappedLines = 0;
        Layout layout = new Layout(
                Help.defaultColorScheme(ansi),
                textTable,
                Help.createMinimalOptionRenderer(),
                Help.createMinimalParameterRenderer());
        layout.addOptions(help.options(), help.parameterLabelRenderer());
        layout.addPositionalParameters(help.positionalParameters(), Help.createMinimalParamLabelRenderer());
        sb.append(layout);
        return sb.toString();
    }
}
