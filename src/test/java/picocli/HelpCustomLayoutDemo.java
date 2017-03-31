/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package picocli;

import org.junit.Test;
import picocli.CommandLine.Help.Column;
import picocli.CommandLine.Help.IOptionRenderer;
import picocli.CommandLine.Help.IParameterRenderer;
import picocli.CommandLine.Help.Layout;
import picocli.CommandLine.Help.TextTable;

import java.awt.Point;
import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static picocli.CommandLine.*;
import static picocli.CommandLine.Help.Column.Overflow.*;

/**
 * Demonstrates how the CommandLine.Help API can be used to create custom layouts for usage help messages.
 */
public class HelpCustomLayoutDemo {

    @Test
    public void testZipUsageFormat() {
        @Command(description = {
                "Copyright (c) 1990-2008 Info-ZIP - Type 'zip \"-L\"' for software license.",
                "Zip 3.0 (July 5th 2008). Command:",
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

        class TwoOptionsPerRowLayout extends Layout { // define a custom layout
            Point previous = new Point(0, 0);

            private TwoOptionsPerRowLayout(TextTable textTable,
                                           IOptionRenderer optionRenderer,
                                           IParameterRenderer parameterRenderer) {
                super(textTable, optionRenderer, parameterRenderer);
            }

            @Override public void layout(Field field, String[][] values) {
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
        }
        TextTable textTable = new TextTable(
                new Column( 5, 2, TRUNCATE), // values should fit
                new Column(30, 2, SPAN), // overflow into adjacent columns
                new Column( 4, 1, TRUNCATE), // values should fit again
                new Column(39, 2, WRAP));
        TwoOptionsPerRowLayout layout = new TwoOptionsPerRowLayout(textTable, Help.createMinimalOptionRenderer(),
                Help.createMinimalParameterRenderer());

        Help help = new Help(new Zip());
        StringBuilder sb = new StringBuilder();
        sb.append(help.description()); // show the first 6 lines, including copyright, description and usage

        // Note that we don't sort the options, so they appear in the order the fields are declared in the Zip class.
        layout.addOptions(help.optionFields, help.parameterLabelRenderer);
        sb.append(layout); // finally, copy the options details help text into the StringBuilder

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
        assertEquals(expected, sb.toString());
    }

    /** for Netstat test */
    private enum Protocol {IP, IPv6, ICMP, ICMPv6, TCP, TCPv6, UDP, UDPv6}
    @Test
    public void testNetstatUsageFormat() {
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
        Help help = new Help(new Netstat());
        sb.append(help.header()).append(help.detailedSynopsis(null, false));
        sb.append(System.getProperty("line.separator"));

        TextTable textTable = new TextTable(
                new Column(15, 2, TRUNCATE),
                new Column(65, 1, WRAP));
        textTable.indentWrappedLines = 0;
        Layout layout = new Layout(textTable, Help.createMinimalOptionRenderer(), Help
                .createMinimalParameterRenderer());
        layout.addOptions(help.optionFields, help.parameterLabelRenderer);
        layout.addPositionalParameters(help.positionalParametersFields, Help.createMinimalParamLabelRenderer());
        sb.append(layout);

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
        assertEquals(expected, sb.toString());
    }
}
