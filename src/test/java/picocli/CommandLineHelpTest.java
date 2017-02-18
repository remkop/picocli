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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import static java.lang.String.format;
import static org.junit.Assert.*;

/**
 * Tests for picoCLI's "Usage" help functionality.
 */
public class CommandLineHelpTest {

    @Test
    @Ignore("requires support for detailedUsage")
    public void testUsageAnnotationDetailedUsage() throws Exception {
        @CommandLine.Usage(detailedUsage = true)
        class Params {
            @CommandLine.Option(names = {"-f", "--file"}, required = true, description = "the file to use")
            File file;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.usage(Params.class, new PrintStream(baos, true, "UTF8"));
        String result = baos.toString("UTF8");
        String programName = "<main class>"; //Params.class.getName();
        assertEquals(format("" +
                        "Usage: java %s -f <file>%n" +
                        "  -f, --file                  the file to use                                   %n",
                programName), result);
    }

    @Test
    public void testTextTable() {
        CommandLine.Help.TextTable table = new CommandLine.Help.TextTable();
        table.addRow("-v", ",", "--verbose", "show what you're doing while you are doing it");
        table.addRow("-p", null, null, "the quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog.");
        assertEquals(String.format(
                "  -v, --verbose               show what you're doing while you are doing it     %n" +
                        "  -p                          the quick brown fox jumped over the lazy dog. The %n" +
                        "                                quick brown fox jumped over the lazy dog.       %n"
                ,""), table.toString(new StringBuilder()).toString());
    }

    @Test
    public void testTextTableAddsNewRowWhenTooManyValuesSpecified() {
        CommandLine.Help.TextTable table = new CommandLine.Help.TextTable();
        table.addRow("-c", ",", "--create", "description", "INVALID");
        assertEquals(String.format("" +
                        "  -c, --create                description                                       %n" +
                        "                                INVALID                                         %n"
                ,""), table.toString(new StringBuilder()).toString());
    }

    @Test
    public void testCatUsageFormat() {
        @CommandLine.Usage(programName = "cat",
                summary = "Concatenate FILE(s), or standard input, to standard output.",
                footer = "Copyright(c) 2017")
        class Cat {
            @CommandLine.Parameters(description = "Files whose contents to display")
            @CommandLine.Option(names = "--help",    help = true,     description = "display this help and exit") boolean help;
            @CommandLine.Option(names = "--version", help = true,     description = "output version information and exit") boolean version;
            @CommandLine.Option(names = "-u",                         description = "(ignored)") boolean u;
            @CommandLine.Option(names = "-t",                         description = "equivalent to -vT") boolean t;
            @CommandLine.Option(names = "-e",                         description = "equivalent to -vET") boolean e;
            @CommandLine.Option(names = {"-A", "--show-all"},         description = "equivalent to -vET") boolean showAll;
            @CommandLine.Option(names = {"-s", "--squeeze-blank"},    description = "suppress repeated empty output lines") boolean squeeze;
            @CommandLine.Option(names = {"-v", "--show-nonprinting"}, description = "use ^ and M- notation, except for LDF and TAB") boolean v;
            @CommandLine.Option(names = {"-b", "--number-nonblank"},  description = "number nonempty output lines, overrides -n") boolean b;
            @CommandLine.Option(names = {"-T", "--show-tabs"},        description = "display TAB characters as ^I") boolean T;
            @CommandLine.Option(names = {"-E", "--show-ends"},        description = "display $ at end of each line") boolean E;
            @CommandLine.Option(names = {"-n", "--number"},           description = "number all output lines") boolean n;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.usage(Cat.class, new PrintStream(baos));
        String expected = String.format(
                "Usage: cat [OPTIONS] [PARAMETERS]%n" +
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
        String expected  = "" +
                "Copyright (c) 1990-2008 Info-ZIP - Type 'zip \"-L\"' for software license.\n" +
                "Zip 3.0 (July 5th 2008). Usage:\n" +
                "zip [-options] [-b path] [-t mmddyyyy] [-n suffixes] [zipfile list] [-xi list]\n" +
                "  The default action is to add or replace zipfile entries from list, which\n" +
                "  can include the special name - to compress standard input.\n" +
                "  If zipfile and list are omitted, zip compresses stdin to stdout.\n" +
                "  -f   freshen: only changed files  -u   update: only changed or new files\n" +
                "  -d   delete entries in zipfile    -m   move into zipfile (delete OS files)\n" +
                "  -r   recurse into directories     -j   junk (don't record) directory names\n" +
                "  -0   store only                   -l   convert LF to CR LF (-ll CR LF to LF)\n" +
                "  -1   compress faster              -9   compress better\n" +
                "  -q   quiet operation              -v   verbose operation/print version info\n" +
                "  -c   add one-line comments        -z   add zipfile comment\n" +
                "  -@   read names from stdin        -o   make zipfile as old as latest entry\n" +
                "  -x   exclude the following names  -i   include only the following names\n" +
                "  -F   fix zipfile (-FF try harder) -D   do not add directory entries\n" +
                "  -A   adjust self-extracting exe   -J   junk zipfile prefix (unzipsfx)\n" +
                "  -T   test zipfile integrity       -X   eXclude eXtra file attributes\n" +
                "  -y   store symbolic links as the link instead of the referenced file\n" +
                "  -e   encrypt                      -n   don't compress these suffixes\n" +
                "  -h2  show more help\n";
    }
}
