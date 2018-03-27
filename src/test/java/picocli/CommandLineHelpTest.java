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
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.*;
import picocli.CommandLine.Help.Ansi.IStyle;
import picocli.CommandLine.Help.Ansi.Style;
import picocli.CommandLine.Help.Ansi.Text;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.Help.TextTable;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.junit.Assert.*;
import static picocli.CommandLine.Help.Visibility.*;
import static picocli.HelpTestUtil.textArray;
import static picocli.HelpTestUtil.usageString;
import static picocli.ModelTestUtil.options;

/**
 * Tests for picocli's "Usage" help functionality.
 */
public class CommandLineHelpTest {
    private static final String LINESEP = System.getProperty("line.separator");

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @After
    public void after() {
        System.getProperties().remove("picocli.color.commands");
        System.getProperties().remove("picocli.color.options");
        System.getProperties().remove("picocli.color.parameters");
        System.getProperties().remove("picocli.color.optionParams");
    }

    @Test
    public void testShowDefaultValuesDemo() throws Exception {
        @Command(showDefaultValues = true)
        class FineGrainedDefaults {
            @Option(names = "-a", description = "ALWAYS shown even if null", showDefaultValue = ALWAYS)
            String optionA;

            @Option(names = "-b", description = "NEVER shown", showDefaultValue = NEVER)
            String optionB = "xyz";

            @Option(names = "-c", description = "ON_DEMAND hides null", showDefaultValue = ON_DEMAND)
            String optionC;

            @Option(names = "-d", description = "ON_DEMAND shows non-null", showDefaultValue = ON_DEMAND)
            String optionD = "abc";
        }
        String result = usageString(new FineGrainedDefaults(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> [-a=<optionA>] [-b=<optionB>] [-c=<optionC>] [-d=<optionD>]%n" +
                "  -a= <optionA>               ALWAYS shown even if null%n" +
                "                                Default: null%n" +
                "  -b= <optionB>               NEVER shown%n" +
                "  -c= <optionC>               ON_DEMAND hides null%n" +
                "  -d= <optionD>               ON_DEMAND shows non-null%n" +
                "                                Default: abc%n"), result);
    }

    @Test
    public void testCommandWithoutShowDefaultValuesOptionOnDemandNullValue_hidesDefault() throws Exception {
        @Command()
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use") File file;
            @Option(names = {"-o", "--opt"},  required = true, description = "another option", showDefaultValue = Help.Visibility.ON_DEMAND) File other;
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> -f=<file> -o=<other>%n" +
                "  -f, --file=<file>           the file to use%n" +
                "  -o, --opt=<other>           another option%n"), result);
    }

    @Test
    public void testCommandWithoutShowDefaultValuesOptionOnDemandNonNullValue_hidesDefault() throws Exception {
        @Command()
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use") File file = new File("/tmp/file");
            @Option(names = {"-o", "--opt"},  required = true, description = "another option", showDefaultValue = Help.Visibility.ON_DEMAND) File other = new File("/tmp/other");
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> -f=<file> -o=<other>%n" +
                "  -f, --file=<file>           the file to use%n" +
                "  -o, --opt=<other>           another option%n"), result);
    }

    @Test
    public void testCommandWithoutShowDefaultValuesOptionAlwaysNullValue_showsNullDefault() throws Exception {
        @Command()
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use", showDefaultValue = Help.Visibility.ALWAYS)
            File file;
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> -f=<file>%n" +
                "  -f, --file=<file>           the file to use%n" +
                "                                Default: null%n"), result);
    }

    @Test
    public void testCommandWithoutShowDefaultValuesOptionAlwaysNonNullValue_showsDefault() throws Exception {
        @Command()
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use", showDefaultValue = Help.Visibility.ALWAYS)
            File file = new File("/tmp/file");
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> -f=<file>%n" +
                "  -f, --file=<file>           the file to use%n" +
                "                                Default: %s%n", new File("/tmp/file")), result);
    }

    @Test
    public void testCommandShowDefaultValuesOptionOnDemandNullValue_hidesDefault() throws Exception {
        @Command(showDefaultValues = true)
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use") File file;
            @Option(names = {"-o", "--opt"},  required = true, description = "another option", showDefaultValue = Help.Visibility.ON_DEMAND) File other;
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> -f=<file> -o=<other>%n" +
                "  -f, --file=<file>           the file to use%n" +
                "  -o, --opt=<other>           another option%n"), result);
    }

    @Test
    public void testCommandShowDefaultValuesOptionNeverNullValue_hidesDefault() throws Exception {
        @Command(showDefaultValues = true)
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use") File file;
            @Option(names = {"-o", "--opt"},  required = true, description = "another option", showDefaultValue = Help.Visibility.NEVER) File other;
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> -f=<file> -o=<other>%n" +
                "  -f, --file=<file>           the file to use%n" +
                "  -o, --opt=<other>           another option%n"), result);
    }

    @Test
    public void testCommandShowDefaultValuesOptionAlwaysNullValue_showsNullDefault() throws Exception {
        @Command(showDefaultValues = true)
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use") File file;
            @Option(names = {"-o", "--opt"},  required = true, description = "another option", showDefaultValue = Help.Visibility.ALWAYS) File other;
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> -f=<file> -o=<other>%n" +
                "  -f, --file=<file>           the file to use%n" +
                "  -o, --opt=<other>           another option%n" +
                "                                Default: null%n"), result);
    }

    @Test
    public void testCommandShowDefaultValuesOptionOnDemandNonNullValue_showsDefault() throws Exception {
        @Command(showDefaultValues = true)
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use")
            File file = new File("theDefault.txt");
            @Option(names = {"-o", "--opt"},  required = true, description = "another option", showDefaultValue = Help.Visibility.ON_DEMAND)
            File other = new File("other.txt");
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> -f=<file> -o=<other>%n" +
                "  -f, --file=<file>           the file to use%n" +
                "                                Default: theDefault.txt%n" +
                "  -o, --opt=<other>           another option%n" +
                "                                Default: other.txt%n"), result);
    }

    @Test
    public void testCommandShowDefaultValuesOptionNeverNonNullValue_hidesDefault() throws Exception {
        @Command(showDefaultValues = true)
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use")
            File file = new File("theDefault.txt");
            @Option(names = {"-o", "--opt"},  required = true, description = "another option", showDefaultValue = Help.Visibility.NEVER)
            File other = new File("other.txt");
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> -f=<file> -o=<other>%n" +
                "  -f, --file=<file>           the file to use%n" +
                "                                Default: theDefault.txt%n" +
                "  -o, --opt=<other>           another option%n"), result);
    }

    @Test
    public void testCommandShowDefaultValuesOptionAlwaysNonNullValue_hidesDefault() throws Exception {
        @Command(showDefaultValues = true)
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use")
            File file = new File("theDefault.txt");
            @Option(names = {"-o", "--opt"},  required = true, description = "another option", showDefaultValue = Help.Visibility.ALWAYS)
            File other = new File("other.txt");
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> -f=<file> -o=<other>%n" +
                "  -f, --file=<file>           the file to use%n" +
                "                                Default: theDefault.txt%n" +
                "  -o, --opt=<other>           another option%n" +
                "                                Default: other.txt%n"), result);
    }

    @Test
    public void testCommandShowDefaultValuesOptionOnDemandArrayField() throws Exception {
        @Command(showDefaultValues = true)
        class Params {
            @Option(names = {"-x", "--array"}, required = true, description = "the array")
            int[] array = {1, 5, 11, 23};
            @Option(names = {"-y", "--other"}, required = true, description = "the other", showDefaultValue = Help.Visibility.ON_DEMAND)
            int[] other = {1, 5, 11, 23};
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> -x=<array> [-x=<array>]... -y=<other> [-y=<other>]...%n" +
                "  -x, --array=<array>         the array%n" +
                "                                Default: [1, 5, 11, 23]%n" +
                "  -y, --other=<other>         the other%n" +
                "                                Default: [1, 5, 11, 23]%n"), result);
    }

    @Test
    public void testCommandShowDefaultValuesOptionNeverArrayField_hidesDefault() throws Exception {
        @Command(showDefaultValues = true)
        class Params {
            @Option(names = {"-x", "--array"}, required = true, description = "the array")
            int[] array = {1, 5, 11, 23};
            @Option(names = {"-y", "--other"}, required = true, description = "the other", showDefaultValue = Help.Visibility.NEVER)
            int[] other = {1, 5, 11, 23};
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> -x=<array> [-x=<array>]... -y=<other> [-y=<other>]...%n" +
                "  -x, --array=<array>         the array%n" +
                "                                Default: [1, 5, 11, 23]%n" +
                "  -y, --other=<other>         the other%n"), result);
    }

    @Test
    public void testCommandShowDefaultValuesOptionAlwaysNullArrayField_showsNull() throws Exception {
        @Command(showDefaultValues = true)
        class Params {
            @Option(names = {"-x", "--array"}, required = true, description = "the array")
            int[] array;
            @Option(names = {"-y", "--other"}, required = true, description = "the other", showDefaultValue = Help.Visibility.ALWAYS)
            int[] other;
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> -x=<array> [-x=<array>]... -y=<other> [-y=<other>]...%n" +
                "  -x, --array=<array>         the array%n" +
                "  -y, --other=<other>         the other%n" +
                "                                Default: null%n"), result);
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
                        "  -f, --file=<file>           the file to use%n"), result);
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
                        "                                Default: def.txt%n"), result);
    }

    @Test
    public void testUsageParamLabels() throws Exception {
        @Command()
        class ParamLabels {
            @Option(names = "-P",    paramLabel = "KEY=VALUE", type  = {String.class, String.class},
                    description = "Project properties (key-value pairs)")              Map<String, String> props;
            @Option(names = "-f",    paramLabel = "FILE", description = "files")      File[] f;
            @Option(names = "-n",    description = "a number option")                  int number;
            @Parameters(index = "0", paramLabel = "NUM", description = "number param") int n;
            @Parameters(index = "1", description = "the host parameter")               InetAddress host;
        }
        String result = usageString(new ParamLabels(), Help.Ansi.OFF);
        assertEquals(format("" +
                        "Usage: <main class> [-n=<number>] [-f=FILE]... [-P=KEY=VALUE]... NUM <host>%n" +
                        "      NUM                     number param%n" +
                        "      <host>                  the host parameter%n" +
                        "  -f= FILE                    files%n" +
                        "  -n= <number>                a number option%n" +
                        "  -P= KEY=VALUE               Project properties (key-value pairs)%n"), result);
    }

    @Test
    public void testUsageParamLabelsWithLongMapOptionName() throws Exception {
        @Command()
        class ParamLabels {
            @Option(names = {"-P", "--properties"},
                    paramLabel  = "KEY=VALUE", type  = {String.class, String.class},
                    description = "Project properties (key-value pairs)")              Map<String, String> props;
            @Option(names = "-f",    paramLabel = "FILE", description = "a file")      File f;
            @Option(names = "-n",    description = "a number option")                  int number;
            @Parameters(index = "0", paramLabel = "NUM", description = "number param") int n;
            @Parameters(index = "1", description = "the host parameter")               InetAddress host;
        }
        String result = usageString(new ParamLabels(), Help.Ansi.OFF);
        assertEquals(format("" +
                        "Usage: <main class> [-f=FILE] [-n=<number>] [-P=KEY=VALUE]... NUM <host>%n" +
                        "      NUM                     number param%n" +
                        "      <host>                  the host parameter%n" +
                        "  -f= FILE                    a file%n" +
                        "  -n= <number>                a number option%n" +
                        "  -P, --properties=KEY=VALUE  Project properties (key-value pairs)%n"), result);
    }

    // ---------------
    @Test
    public void testUsageVariableArityRequiredShortOptionArray() throws UnsupportedEncodingException {
        // if option is required at least once and can be specified multiple times:
        // -f=ARG [-f=ARG]...
        class Args {
            @Option(names = "-a", required = true, paramLabel = "ARG") // default
            String[] a;
            @Option(names = "-b", required = true, paramLabel = "ARG", arity = "0..*")
            List<String> b;
            @Option(names = "-c", required = true, paramLabel = "ARG", arity = "1..*")
            String[] c;
            @Option(names = "-d", required = true, paramLabel = "ARG", arity = "2..*")
            List<String> d;
        }
        String expected = String.format("" +
                "Usage: <main class> -a=ARG [-a=ARG]... -b=[ARG]... [-b=[ARG]...]... -c=ARG...%n" +
                "                    [-c=ARG...]... -d=ARG ARG... [-d=ARG ARG...]...%n" +
                "  -a= ARG%n" +
                "  -b= [ARG]...%n" +
                "  -c= ARG...%n" +
                "  -d= ARG ARG...%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }

    @Test
    public void testUsageVariableArityShortOptionArray() throws UnsupportedEncodingException {
        class Args {
            @Option(names = "-a", paramLabel = "ARG") // default
            List<String> a;
            @Option(names = "-b", paramLabel = "ARG", arity = "0..*")
            String[] b;
            @Option(names = "-c", paramLabel = "ARG", arity = "1..*")
            List<String> c;
            @Option(names = "-d", paramLabel = "ARG", arity = "2..*")
            String[] d;
        }
        String expected = String.format("" +
                "Usage: <main class> [-a=ARG]... [-b=[ARG]...]... [-c=ARG...]... [-d=ARG%n" +
                "                    ARG...]...%n" +
                "  -a= ARG%n" +
                "  -b= [ARG]...%n" +
                "  -c= ARG...%n" +
                "  -d= ARG ARG...%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }

    @Test
    public void testUsageRangeArityRequiredShortOptionArray() throws UnsupportedEncodingException {
        // if option is required at least once and can be specified multiple times:
        // -f=ARG [-f=ARG]...
        class Args {
            @Option(names = "-a", required = true, paramLabel = "ARG", arity = "0..1")
            List<String> a;
            @Option(names = "-b", required = true, paramLabel = "ARG", arity = "1..2")
            String[] b;
            @Option(names = "-c", required = true, paramLabel = "ARG", arity = "1..3")
            String[] c;
            @Option(names = "-d", required = true, paramLabel = "ARG", arity = "2..4")
            String[] d;
        }
        String expected = String.format("" +
                "Usage: <main class> -a[=ARG] [-a[=ARG]]... -b=ARG [ARG] [-b=ARG [ARG]]...%n" +
                "                    -c=ARG [ARG [ARG]] [-c=ARG [ARG [ARG]]]... -d=ARG ARG [ARG%n" +
                "                    [ARG]] [-d=ARG ARG [ARG [ARG]]]...%n" +
                "  -a= [ARG]%n" +
                "  -b= ARG [ARG]%n" +
                "  -c= ARG [ARG [ARG]]%n" +
                "  -d= ARG ARG [ARG [ARG]]%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }

    @Test
    public void testUsageRangeArityShortOptionArray() throws UnsupportedEncodingException {
        class Args {
            @Option(names = "-a", paramLabel = "ARG", arity = "0..1")
            List<String> a;
            @Option(names = "-b", paramLabel = "ARG", arity = "1..2")
            String[] b;
            @Option(names = "-c", paramLabel = "ARG", arity = "1..3")
            String[] c;
            @Option(names = "-d", paramLabel = "ARG", arity = "2..4")
            String[] d;
        }
        String expected = String.format("" +
                "Usage: <main class> [-a[=ARG]]... [-b=ARG [ARG]]... [-c=ARG [ARG [ARG]]]...%n" +
                "                    [-d=ARG ARG [ARG [ARG]]]...%n" +
                "  -a= [ARG]%n" +
                "  -b= ARG [ARG]%n" +
                "  -c= ARG [ARG [ARG]]%n" +
                "  -d= ARG ARG [ARG [ARG]]%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }

    @Test
    public void testUsageFixedArityRequiredShortOptionArray() throws UnsupportedEncodingException {
        // if option is required at least once and can be specified multiple times:
        // -f=ARG [-f=ARG]...
        class Args {
            @Option(names = "-a", required = true, paramLabel = "ARG") // default
                    String[] a;
            @Option(names = "-b", required = true, paramLabel = "ARG", arity = "0")
            String[] b;
            @Option(names = "-c", required = true, paramLabel = "ARG", arity = "1")
            String[] c;
            @Option(names = "-d", required = true, paramLabel = "ARG", arity = "2")
            String[] d;
        }
        String expected = String.format("" +
                "Usage: <main class> -b [-b]... -a=ARG [-a=ARG]... -c=ARG [-c=ARG]... -d=ARG ARG%n" +
                "                    [-d=ARG ARG]...%n" +
                "  -a= ARG%n" +
                "  -b%n" +
                "  -c= ARG%n" +
                "  -d= ARG ARG%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }

    @Test
    public void testUsageFixedArityShortOptionArray() throws UnsupportedEncodingException {
        class Args {
            @Option(names = "-a", paramLabel = "ARG") // default
                    String[] a;
            @Option(names = "-b", paramLabel = "ARG", arity = "0")
            String[] b;
            @Option(names = "-c", paramLabel = "ARG", arity = "1")
            String[] c;
            @Option(names = "-d", paramLabel = "ARG", arity = "2")
            String[] d;
        }
        String expected = String.format("" +
                "Usage: <main class> [-b]... [-a=ARG]... [-c=ARG]... [-d=ARG ARG]...%n" +
                "  -a= ARG%n" +
                "  -b%n" +
                "  -c= ARG%n" +
                "  -d= ARG ARG%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }
    //--------------
    @Test
    public void testUsageVariableArityRequiredLongOptionArray() throws UnsupportedEncodingException {
        // if option is required at least once and can be specified multiple times:
        // -f=ARG [-f=ARG]...
        class Args {
            @Option(names = "--aa", required = true, paramLabel = "ARG") // default
            String[] a;
            @Option(names = "--bb", required = true, paramLabel = "ARG", arity = "0..*")
            List<String> b;
            @Option(names = "--cc", required = true, paramLabel = "ARG", arity = "1..*")
            String[] c;
            @Option(names = "--dd", required = true, paramLabel = "ARG", arity = "2..*")
            List<String> d;
        }
        String expected = String.format("" +
                "Usage: <main class> --aa=ARG [--aa=ARG]... --bb=[ARG]... [--bb=[ARG]...]...%n" +
                "                    --cc=ARG... [--cc=ARG...]... --dd=ARG ARG... [--dd=ARG%n" +
                "                    ARG...]...%n" +
                "      --aa=ARG%n" +
                "      --bb=[ARG]...%n" +
                "      --cc=ARG...%n" +
                "      --dd=ARG ARG...%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }

    @Test
    public void testUsageVariableArityLongOptionArray() throws UnsupportedEncodingException {
        class Args {
            @Option(names = "--aa", paramLabel = "ARG") // default
            List<String> a;
            @Option(names = "--bb", paramLabel = "ARG", arity = "0..*")
            String[] b;
            @Option(names = "--cc", paramLabel = "ARG", arity = "1..*")
            List<String> c;
            @Option(names = "--dd", paramLabel = "ARG", arity = "2..*")
            String[] d;
        }
        String expected = String.format("" +
                "Usage: <main class> [--aa=ARG]... [--bb=[ARG]...]... [--cc=ARG...]... [--dd=ARG%n" +
                "                    ARG...]...%n" +
                "      --aa=ARG%n" +
                "      --bb=[ARG]...%n" +
                "      --cc=ARG...%n" +
                "      --dd=ARG ARG...%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }

    @Test
    public void testUsageRangeArityRequiredLongOptionArray() throws UnsupportedEncodingException {
        // if option is required at least once and can be specified multiple times:
        // -f=ARG [-f=ARG]...
        class Args {
            @Option(names = "--aa", required = true, paramLabel = "ARG", arity = "0..1")
            List<String> a;
            @Option(names = "--bb", required = true, paramLabel = "ARG", arity = "1..2")
            String[] b;
            @Option(names = "--cc", required = true, paramLabel = "ARG", arity = "1..3")
            String[] c;
            @Option(names = "--dd", required = true, paramLabel = "ARG", arity = "2..4", description = "foobar")
            String[] d;
        }
        String expected = String.format("" +
                "Usage: <main class> --aa[=ARG] [--aa[=ARG]]... --bb=ARG [ARG] [--bb=ARG%n" +
                "                    [ARG]]... --cc=ARG [ARG [ARG]] [--cc=ARG [ARG [ARG]]]...%n" +
                "                    --dd=ARG ARG [ARG [ARG]] [--dd=ARG ARG [ARG [ARG]]]...%n" +
                "      --aa[=ARG]%n" +
                "      --bb=ARG [ARG]%n" +
                "      --cc=ARG [ARG [ARG]]%n" +
                "      --dd=ARG ARG [ARG [ARG]]%n" +
                "                              foobar%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }

    @Test
    public void testUsageRangeArityLongOptionArray() throws UnsupportedEncodingException {
        class Args {
            @Option(names = "--aa", paramLabel = "ARG", arity = "0..1")
            List<String> a;
            @Option(names = "--bb", paramLabel = "ARG", arity = "1..2")
            String[] b;
            @Option(names = "--cc", paramLabel = "ARG", arity = "1..3")
            String[] c;
            @Option(names = "--dd", paramLabel = "ARG", arity = "2..4", description = "foobar")
            String[] d;
        }
        String expected = String.format("" +
                "Usage: <main class> [--aa[=ARG]]... [--bb=ARG [ARG]]... [--cc=ARG [ARG%n" +
                "                    [ARG]]]... [--dd=ARG ARG [ARG [ARG]]]...%n" +
                "      --aa[=ARG]%n" +
                "      --bb=ARG [ARG]%n" +
                "      --cc=ARG [ARG [ARG]]%n" +
                "      --dd=ARG ARG [ARG [ARG]]%n" +
                "                              foobar%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }

    @Test
    public void testUsageFixedArityRequiredLongOptionArray() throws UnsupportedEncodingException {
        // if option is required at least once and can be specified multiple times:
        // -f=ARG [-f=ARG]...
        class Args {
            @Option(names = "--aa", required = true, paramLabel = "ARG") // default
            String[] a;
            @Option(names = "--bb", required = true, paramLabel = "ARG", arity = "0")
            String[] b;
            @Option(names = "--cc", required = true, paramLabel = "ARG", arity = "1")
            String[] c;
            @Option(names = "--dd", required = true, paramLabel = "ARG", arity = "2")
            String[] d;
        }
        String expected = String.format("" +
                "Usage: <main class> --bb [--bb]... --aa=ARG [--aa=ARG]... --cc=ARG%n" +
                "                    [--cc=ARG]... --dd=ARG ARG [--dd=ARG ARG]...%n" +
                "      --aa=ARG%n" +
                "      --bb%n" +
                "      --cc=ARG%n" +
                "      --dd=ARG ARG%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }

    @Test
    public void testUsageFixedArityLongOptionArray() throws UnsupportedEncodingException {
        class Args {
            @Option(names = "--aa", paramLabel = "ARG") // default
            String[] a;
            @Option(names = "--bb", paramLabel = "ARG", arity = "0")
            String[] b;
            @Option(names = "--cc", paramLabel = "ARG", arity = "1")
            String[] c;
            @Option(names = "--dd", paramLabel = "ARG", arity = "2")
            String[] d;
        }
        String expected = String.format("" +
                "Usage: <main class> [--bb]... [--aa=ARG]... [--cc=ARG]... [--dd=ARG ARG]...%n" +
                "      --aa=ARG%n" +
                "      --bb%n" +
                "      --cc=ARG%n" +
                "      --dd=ARG ARG%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }

    //------------------
    @Test
    public void testUsageVariableArityRequiredShortOptionMap() throws UnsupportedEncodingException {
        // if option is required at least once and can be specified multiple times:
        // -f=ARG [-f=ARG]...
        class Args {
            @Option(names = "-a", required = true, paramLabel = "KEY=VAL") // default
            Map<String, String> a;
            @Option(names = "-b", required = true, arity = "0..*")
            @SuppressWarnings("unchecked")
            Map b;
            @Option(names = "-c", required = true, arity = "1..*", type = {String.class, TimeUnit.class})
            Map<String, TimeUnit> c;
            @Option(names = "-d", required = true, arity = "2..*", type = {Integer.class, URL.class}, description = "description")
            Map<Integer, URL> d;
        }
        String expected = String.format("" +
                "Usage: <main class> -a=KEY=VAL [-a=KEY=VAL]... -b=[<String=String>]... [-b=%n" +
                "                    [<String=String>]...]... -c=<String=TimeUnit>...%n" +
                "                    [-c=<String=TimeUnit>...]... -d=<Integer=URL>%n" +
                "                    <Integer=URL>... [-d=<Integer=URL> <Integer=URL>...]...%n" +
                "  -a= KEY=VAL%n" +
                "  -b= [<String=String>]...%n" +
                "  -c= <String=TimeUnit>...%n" +
                "  -d= <Integer=URL> <Integer=URL>...%n" +
                "                              description%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }

    @Test
    public void testUsageVariableArityOptionMap() throws UnsupportedEncodingException {
        class Args {
            @Option(names = "-a") // default
            Map<String, String> a;
            @Option(names = "-b", arity = "0..*", type = {Integer.class, Integer.class})
            Map<Integer, Integer> b;
            @Option(names = "-c", paramLabel = "KEY=VALUE", arity = "1..*", type = {String.class, TimeUnit.class})
            Map<String, TimeUnit> c;
            @Option(names = "-d", arity = "2..*", type = {String.class, URL.class}, description = "description")
            Map<String, URL> d;
        }
        String expected = String.format("" +
                "Usage: <main class> [-a=<String=String>]... [-b=[<Integer=Integer>]...]...%n" +
                "                    [-c=KEY=VALUE...]... [-d=<String=URL> <String=URL>...]...%n" +
                "  -a= <String=String>%n" +
                "  -b= [<Integer=Integer>]...%n" +
                "  -c= KEY=VALUE...%n" +
                "  -d= <String=URL> <String=URL>...%n" +
                "                              description%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }

    @Test
    public void testUsageRangeArityRequiredOptionMap() throws UnsupportedEncodingException {
        // if option is required at least once and can be specified multiple times:
        // -f=ARG [-f=ARG]...
        class Args {
            @Option(names = "-a", required = true, arity = "0..1", description = "a description")
            Map<String, String> a;
            @Option(names = "-b", required = true, arity = "1..2", type = {Integer.class, Integer.class}, description = "b description")
            Map<Integer, Integer> b;
            @Option(names = "-c", required = true, arity = "1..3", type = {String.class, URL.class}, description = "c description")
            Map<String, URL> c;
            @Option(names = "-d", required = true, paramLabel = "K=URL", arity = "2..4", description = "d description")
            Map<String, URL> d;
        }
        String expected = String.format("" +
                "Usage: <main class> -a[=<String=String>] [-a[=<String=String>]]...%n" +
                "                    -b=<Integer=Integer> [<Integer=Integer>]%n" +
                "                    [-b=<Integer=Integer> [<Integer=Integer>]]...%n" +
                "                    -c=<String=URL> [<String=URL> [<String=URL>]]%n" +
                "                    [-c=<String=URL> [<String=URL> [<String=URL>]]]... -d=K=URL%n" +
                "                    K=URL [K=URL [K=URL]] [-d=K=URL K=URL [K=URL [K=URL]]]...%n" +
                "  -a= [<String=String>]       a description%n" +
                "  -b= <Integer=Integer> [<Integer=Integer>]%n" +
                "                              b description%n" +
                "  -c= <String=URL> [<String=URL> [<String=URL>]]%n" +
                "                              c description%n" +
                "  -d= K=URL K=URL [K=URL [K=URL]]%n" +
                "                              d description%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }

    @Test
    public void testUsageRangeArityOptionMap() throws UnsupportedEncodingException {
        class Args {
            @Option(names = "-a", arity = "0..1"/*, type = {UUID.class, URL.class}*/, description = "a description")
            Map<UUID, URL> a;
            @Option(names = "-b", arity = "1..2", type = {Long.class, UUID.class}, description = "b description")
            Map<?, ?> b;
            @Option(names = "-c", arity = "1..3", type = {Long.class}, description = "c description")
            Map<?, ?> c;
            @Option(names = "-d", paramLabel = "K=V", arity = "2..4", description = "d description")
            Map<?, ?> d;
        }
        String expected = String.format("" +
                "Usage: <main class> [-a[=<UUID=URL>]]... [-b=<Long=UUID> [<Long=UUID>]]...%n" +
                "                    [-c=<String=String> [<String=String> [<String=String>]]]...%n" +
                "                    [-d=K=V K=V [K=V [K=V]]]...%n" +
                "  -a= [<UUID=URL>]            a description%n" +
                "  -b= <Long=UUID> [<Long=UUID>]%n" +
                "                              b description%n" +
                "  -c= <String=String> [<String=String> [<String=String>]]%n" +
                "                              c description%n" +
                "  -d= K=V K=V [K=V [K=V]]     d description%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }

    @Test
    public void testUsageFixedArityRequiredOptionMap() throws UnsupportedEncodingException {
        // if option is required at least once and can be specified multiple times:
        // -f=ARG [-f=ARG]...
        class Args {
            @Option(names = "-a", required = true, description = "a description")
            Map<Short, Field> a;
            @Option(names = "-b", required = true, paramLabel = "KEY=VAL", arity = "0", description = "b description")
            @SuppressWarnings("unchecked")
            Map<?, ?> b;
            @Option(names = "-c", required = true, arity = "1", type = {Long.class, File.class}, description = "c description")
            Map<Long, File> c;
            @Option(names = "-d", required = true, arity = "2", type = {URI.class, URL.class}, description = "d description")
            Map<URI, URL> d;
        }
        String expected = String.format("" +
                "Usage: <main class> -b [-b]... -a=<Short=Field> [-a=<Short=Field>]...%n" +
                "                    -c=<Long=File> [-c=<Long=File>]... -d=<URI=URL> <URI=URL>%n" +
                "                    [-d=<URI=URL> <URI=URL>]...%n" +
                "  -a= <Short=Field>           a description%n" +
                "  -b                          b description%n" +
                "  -c= <Long=File>             c description%n" +
                "  -d= <URI=URL> <URI=URL>     d description%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }

    @Test
    public void testUsageFixedArityOptionMap() throws UnsupportedEncodingException {
        class Args {
            @Option(names = "-a", type = {Short.class, Field.class}, description = "a description")
            Map<Short, Field> a;
            @Option(names = "-b", arity = "0", type = {UUID.class, Long.class}, description = "b description")
            @SuppressWarnings("unchecked")
            Map<?, ?> b;
            @Option(names = "-c", arity = "1", description = "c description")
            Map<Long, File> c;
            @Option(names = "-d", arity = "2", type = {URI.class, URL.class}, description = "d description")
            Map<URI, URL> d;
        }
        String expected = String.format("" +
                "Usage: <main class> [-b]... [-a=<Short=Field>]... [-c=<Long=File>]...%n" +
                "                    [-d=<URI=URL> <URI=URL>]...%n" +
                "  -a= <Short=Field>           a description%n" +
                "  -b                          b description%n" +
                "  -c= <Long=File>             c description%n" +
                "  -d= <URI=URL> <URI=URL>     d description%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }
    //--------------
    @Test
    public void testUsageVariableArityParametersArray() throws UnsupportedEncodingException {
        // if option is required at least once and can be specified multiple times:
        // -f=ARG [-f=ARG]...
        class Args {
            @Parameters(paramLabel = "APARAM", description = "APARAM description")
            String[] a;
            @Parameters(arity = "0..*", description = "b description")
            List<String> b;
            @Parameters(arity = "1..*", description = "c description")
            String[] c;
            @Parameters(arity = "2..*", description = "d description")
            List<String> d;
        }
        String expected = String.format("" +
                "Usage: <main class> [APARAM]... [<b>]... <c>... <d> <d>...%n" +
                "      [APARAM]...             APARAM description%n" +
                "      [<b>]...                b description%n" +
                "      <c>...                  c description%n" +
                "      <d> <d>...              d description%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }

    @Test
    public void testUsageRangeArityParameterArray() throws UnsupportedEncodingException {
        class Args {
            @Parameters(index = "0", paramLabel = "PARAMA", arity = "0..1", description = "PARAMA description")
            List<String> a;
            @Parameters(index = "0", paramLabel = "PARAMB", arity = "1..2", description = "PARAMB description")
            String[] b;
            @Parameters(index = "0", paramLabel = "PARAMC", arity = "1..3", description = "PARAMC description")
            String[] c;
            @Parameters(index = "0", paramLabel = "PARAMD", arity = "2..4", description = "PARAMD description")
            String[] d;
        }
        String expected = String.format("" +
                "Usage: <main class> [PARAMA] PARAMB [PARAMB] PARAMC [PARAMC [PARAMC]] PARAMD%n" +
                "                    PARAMD [PARAMD [PARAMD]]%n" +
                "      [PARAMA]                PARAMA description%n" +
                "      PARAMB [PARAMB]         PARAMB description%n" +
                "      PARAMC [PARAMC [PARAMC]]%n" +
                "                              PARAMC description%n" +
                "      PARAMD PARAMD [PARAMD [PARAMD]]%n" +
                "                              PARAMD description%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }

    @Test
    public void testUsageFixedArityParametersArray() throws UnsupportedEncodingException {
        class Args {
            @Parameters(description = "a description (default arity)")
            String[] a;
            @Parameters(index = "0", arity = "0", description = "b description (arity=0)")
            String[] b;
            @Parameters(index = "1", arity = "1", description = "b description (arity=1)")
            String[] c;
            @Parameters(index = "2", arity = "2", description = "b description (arity=2)")
            String[] d;
        }
        String expected = String.format("" +
                "Usage: <main class>  [<a>]... <c> <d> <d>%n" +
                "                              b description (arity=0)%n" +
                "      [<a>]...                a description (default arity)%n" +
                "      <c>                     b description (arity=1)%n" +
                "      <d> <d>                 b description (arity=2)%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }

    @Test
    public void testUsageVariableArityParametersMap() throws UnsupportedEncodingException {
        class Args {
            @Parameters()
            Map<String, String> a;
            @Parameters(arity = "0..*", description = "a description (arity=0..*)")
            Map<Integer, Integer> b;
            @Parameters(paramLabel = "KEY=VALUE", arity = "1..*", type = {String.class, TimeUnit.class})
            Map<String, TimeUnit> c;
            @Parameters(arity = "2..*", type = {String.class, URL.class}, description = "description")
            Map<String, URL> d;
        }
        String expected = String.format("" +
                "Usage: <main class> [<String=String>]... [<Integer=Integer>]... KEY=VALUE...%n" +
                "                    <String=URL> <String=URL>...%n" +
                "      [<String=String>]...%n" +
                "      [<Integer=Integer>]...  a description (arity=0..*)%n" +
                "      KEY=VALUE...%n" +
                "      <String=URL> <String=URL>...%n" +
                "                              description%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }

    @Test
    public void testUsageRangeArityParametersMap() throws UnsupportedEncodingException {
        class Args {
            @Parameters(index = "0", arity = "0..1"/*, type = {UUID.class, URL.class}*/, description = "a description")
            Map<UUID, URL> a;
            @Parameters(index = "1", arity = "1..2", type = {Long.class, UUID.class}, description = "b description")
            Map<?, ?> b;
            @Parameters(index = "2", arity = "1..3", type = {Long.class}, description = "c description")
            Map<?, ?> c;
            @Parameters(index = "3", paramLabel = "K=V", arity = "2..4", description = "d description")
            Map<?, ?> d;
        }
        String expected = String.format("" +
                "Usage: <main class> [<UUID=URL>] <Long=UUID> [<Long=UUID>] <String=String>%n" +
                "                    [<String=String> [<String=String>]] K=V K=V [K=V [K=V]]%n" +
                "      [<UUID=URL>]            a description%n" +
                "      <Long=UUID> [<Long=UUID>]%n" +
                "                              b description%n" +
                "      <String=String> [<String=String> [<String=String>]]%n" +
                "                              c description%n" +
                "      K=V K=V [K=V [K=V]]     d description%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }

    @Test
    public void testUsageFixedArityParametersMap() throws UnsupportedEncodingException {
        class Args {
            @Parameters(type = {Short.class, Field.class}, description = "a description")
            Map<Short, Field> a;
            @Parameters(index = "0", arity = "0", type = {UUID.class, Long.class}, description = "b description (arity=0)")
            @SuppressWarnings("unchecked")
            Map<?, ?> b;
            @Parameters(index = "1", arity = "1", description = "c description")
            Map<Long, File> c;
            @Parameters(index = "2", arity = "2", type = {URI.class, URL.class}, description = "d description")
            Map<URI, URL> d;
        }
        String expected = String.format("" +
                "Usage: <main class>  [<Short=Field>]... <Long=File> <URI=URL> <URI=URL>%n" +
                "                              b description (arity=0)%n" +
                "      [<Short=Field>]...      a description%n" +
                "      <Long=File>             c description%n" +
                "      <URI=URL> <URI=URL>     d description%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }
    @Test
    public void testUsageWithCustomColorScheme() throws UnsupportedEncodingException {
        Help.ColorScheme scheme = new Help.ColorScheme(Help.Ansi.ON)
                .options(Style.bg_magenta).parameters(Style.bg_cyan).optionParams(Style.bg_yellow).commands(Style.reverse);
        class Args {
            @Parameters(description = "param desc") String[] params;
            @Option(names = "-x", description = "option desc") String[] options;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.usage(new Args(), new PrintStream(baos, true, "UTF8"), scheme);
        String actual = baos.toString("UTF8");

        String expected = String.format("" +
                "Usage: @|reverse <main class>|@ [@|bg_magenta -x|@=@|bg_yellow <options>|@]... [@|bg_cyan <params>|@]...%n" +
                "      [@|bg_cyan <params>|@]...           param desc%n" +
                "  @|bg_magenta -x|@= @|bg_yellow <|@@|bg_yellow options>|@               option desc%n");
        assertEquals(Help.Ansi.ON.new Text(expected).toString(), actual);
    }
    //----------
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
        OptionSpec[] fields = options(new App(), "aaaa", "bbbb", "cccc"); // -tkc
        Arrays.sort(fields, new Help.SortByShortestOptionNameAlphabetically());
        OptionSpec[] expected = options(new App(), "cccc", "bbbb", "aaaa"); // -ckt
        assertEquals(expected[0], fields[0]);
        assertEquals(expected[1], fields[1]);
        assertEquals(expected[2], fields[2]);
        assertArrayEquals(expected, fields);
    }

    @Test
    public void testSortByOptionArityAndNameComparator_sortsByMaxThenMinThenName() throws Exception {
        class App {
            @Option(names = {"-t", "--aaaa"}                ) boolean tImplicitArity0;
            @Option(names = {"-e", "--EEE"}, arity = "1"    ) boolean explicitArity1;
            @Option(names = {"--bbbb", "-k"}                ) boolean kImplicitArity0;
            @Option(names = {"--AAAA", "-a"}                ) int aImplicitArity1;
            @Option(names = {"--BBBB", "-z"}                ) String[] zImplicitArity1;
            @Option(names = {"--ZZZZ", "-b"}, arity = "1..3") String[] bExplicitArity1_3;
            @Option(names = {"-f", "--ffff"}                ) boolean fImplicitArity0;
        }
        OptionSpec[] fields = options(new App(), "tImplicitArity0", "explicitArity1", "kImplicitArity0",
                "aImplicitArity1", "zImplicitArity1", "bExplicitArity1_3", "fImplicitArity0");
        Arrays.sort(fields, new Help.SortByOptionArityAndNameAlphabetically());
        OptionSpec[] expected = options(new App(),
                "fImplicitArity0",
                "kImplicitArity0",
                "tImplicitArity0",
                "aImplicitArity1",
                "explicitArity1",
                "zImplicitArity1",
                "bExplicitArity1_3");
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
        OptionSpec option = help.options().get(0);
        Text[][] row1 = renderer.render(option, parameterRenderer, Help.defaultColorScheme(
                help.ansi()));
        assertEquals(1, row1.length);
        //assertArrayEquals(new String[]{"---long=<longField>", "long description"}, row1[0]);
        assertArrayEquals(new Text[]{
                help.ansi().new Text(format("%s---long%s=%s<longField>%s", "@|fg(yellow) ", "|@", "@|italic ", "|@")),
                help.ansi().new Text("long description")}, row1[0]);

        OptionSpec option2 = help.options().get(1);
        Text[][] row2 = renderer.render(option2, parameterRenderer, Help.defaultColorScheme(
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
        OptionSpec option = help.options().get(0);
        Text[][] row1 = renderer.render(option, parameterRenderer, help.colorScheme());
        assertEquals(1, row1.length);
        assertArrayEquals(Arrays.toString(row1[0]), textArray(help, "", "-L", ",", "---long=<longField>", "long description"), row1[0]);
        //assertArrayEquals(Arrays.toString(row1[1]), textArray(help, "", "", "", "", "  Default: null"), row1[1]); // #201 don't show null defaults

        option = help.options().get(1);
        Text[][] row2 = renderer.render(option, parameterRenderer, help.colorScheme());
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
        OptionSpec option = help.options().get(0);
        Text[][] row = renderer.render(option, parameterRenderer, help.colorScheme());
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
        OptionSpec option = help.options().get(0);
        Text[][] row = renderer.render(option, parameterRenderer, help.colorScheme());
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
        OptionSpec option = help.options().get(0);
        Text[][] row = renderer.render(option, parameterRenderer, help.colorScheme());
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
        help.commandSpec().usageMessage().showDefaultValues(true);
        Help.IOptionRenderer renderer = help.createDefaultOptionRenderer();
        Help.IParamLabelRenderer parameterRenderer = help.createDefaultParamLabelRenderer();
        OptionSpec option = help.options().get(0);
        Text[][] row = renderer.render(option, parameterRenderer, help.colorScheme());
        assertEquals(1, row.length);
        assertArrayEquals(Arrays.toString(row[0]), textArray(help, " ", "-b", ",", "-a, --alpha=<otherField>", "other"), row[0]);
        // assertArrayEquals(Arrays.toString(row[1]), textArray(help, "",    "", "",  "", "  Default: null"), row[1]); // #201 don't show null defaults
    }

    @Test
    public void testDefaultParameterRenderer_rendersSpacePrefixByDefaultForParametersWithPositiveArity() {
        class Required {
            @Parameters(description = "required") String required;
        }
        Help help = new Help(new Required());
        Help.IParameterRenderer renderer = help.createDefaultParameterRenderer();
        Help.IParamLabelRenderer parameterRenderer = Help.createMinimalParamLabelRenderer();
        PositionalParamSpec param = help.positionalParameters().get(0);
        Text[][] row1 = renderer.render(param, parameterRenderer, help.colorScheme());
        assertEquals(1, row1.length);
        assertArrayEquals(Arrays.toString(row1[0]), textArray(help, " ", "", "", "<required>", "required"), row1[0]);
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
        PositionalParamSpec param = help.positionalParameters().get(0);
        Text[][] row1 = renderer.render(param, parameterRenderer, help.colorScheme());
        assertEquals(1, row1.length);
        assertArrayEquals(Arrays.toString(row1[0]), textArray(help, "*", "", "", "<required>", "required"), row1[0]);
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
        PositionalParamSpec param = help.positionalParameters().get(0);
        Text[][] row1 = renderer.render(param, parameterRenderer, help.colorScheme());
        assertEquals(1, row1.length);
        assertArrayEquals(Arrays.toString(row1[0]), textArray(help, "", "", "", "<optional>", "optional"), row1[0]);
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
        help.commandSpec().usageMessage().showDefaultValues(false); // omit default values from description column
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
        for (OptionSpec option : help.options()) {
            Text[][] row = renderer.render(option, parameterRenderer, help.colorScheme());
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
        for (OptionSpec option : help.options()) {
            Text[][] row = renderer.render(option, parameterRenderer, help.colorScheme());
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

        Help help2 = new Help(new Example());
        help2.commandSpec().parser().separator(" ");
        Help.IParamLabelRenderer spaceSeparatedParameterRenderer = help2.createDefaultParamLabelRenderer();

        String[] expected = new String[] {
                "<longField>",
                "LABEL",
        };
        int i = -1;
        for (OptionSpec option : help.options()) {
            i++;
            Text withSpace = spaceSeparatedParameterRenderer.renderParameterLabel(option, help.ansi(), Collections.<IStyle>emptyList());
            assertEquals(withSpace.toString(), " " + expected[i], withSpace.toString());
            Text withEquals = equalSeparatedParameterRenderer.renderParameterLabel(option, help.ansi(), Collections.<IStyle>emptyList());
            assertEquals(withEquals.toString(), "=" + expected[i], withEquals.toString());
        }
    }

    @Test
    public void testDefaultParameterRenderer_appliesToPositionalArgumentsIgnoresSeparator() {
        class WithLabel    { @Parameters(paramLabel = "POSITIONAL_ARGS") String positional; }
        class WithoutLabel { @Parameters()                               String positional; }

        Help withLabel = new Help(new WithLabel());
        Help.IParamLabelRenderer equals = withLabel.createDefaultParamLabelRenderer();
        withLabel.commandSpec().parser().separator("=");
        Help.IParamLabelRenderer spaced = withLabel.createDefaultParamLabelRenderer();

        Text withSpace = spaced.renderParameterLabel(withLabel.positionalParameters().get(0), withLabel.ansi(), Collections.<IStyle>emptyList());
        assertEquals(withSpace.toString(), "POSITIONAL_ARGS", withSpace.toString());
        Text withEquals = equals.renderParameterLabel(withLabel.positionalParameters().get(0), withLabel.ansi(), Collections.<IStyle>emptyList());
        assertEquals(withEquals.toString(), "POSITIONAL_ARGS", withEquals.toString());

        Help withoutLabel = new Help(new WithoutLabel());
        withSpace = spaced.renderParameterLabel(withoutLabel.positionalParameters().get(0), withoutLabel.ansi(), Collections.<IStyle>emptyList());
        assertEquals(withSpace.toString(), "<positional>", withSpace.toString());
        withEquals = equals.renderParameterLabel(withoutLabel.positionalParameters().get(0), withoutLabel.ansi(), Collections.<IStyle>emptyList());
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
        @Command(abbreviateSynopsis = true)
        class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [OPTIONS]" + LINESEP, help.synopsis(0));
    }

    @Test
    public void testAbreviatedSynopsis_withoutParameters_ANSI() {
        @Command(abbreviateSynopsis = true)
        class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.defaultColorScheme(Help.Ansi.ON));
        assertEquals(Help.Ansi.ON.new Text("@|bold <main class>|@ [OPTIONS]" + LINESEP).toString(), help.synopsis(0));
    }

    @Test
    public void testAbreviatedSynopsis_withParameters() {
        @Command(abbreviateSynopsis = true)
        class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters File[] files;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [OPTIONS] [<files>]..." + LINESEP, help.synopsis(0));
    }

    @Test
    public void testAbreviatedSynopsis_withParameters_ANSI() {
        @Command(abbreviateSynopsis = true)
        class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters File[] files;
        }
        Help help = new Help(new App(), Help.defaultColorScheme(Help.Ansi.ON));
        assertEquals(Help.Ansi.ON.new Text("@|bold <main class>|@ [OPTIONS] [@|yellow <files>|@]..." + LINESEP).toString(), help.synopsis(0));
    }

    @Test
    public void testAbreviatedSynopsis_commandNameCustomizableDeclaratively() throws UnsupportedEncodingException {
        @Command(abbreviateSynopsis = true, name = "aprogram")
        class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters File[] files;
        }
        String expected = "" +
                "Usage: aprogram [OPTIONS] [<files>]...%n" +
                "      [<files>]...%n" +
                "  -c, --count=<count>%n" +
                "  -v, --verbose%n";
        String actual = usageString(new CommandLine(new App()), Help.Ansi.OFF);
        assertEquals(String.format(expected), actual);
    }

    @Test
    public void testAbreviatedSynopsis_commandNameCustomizableProgrammatically() throws UnsupportedEncodingException {
        @Command(abbreviateSynopsis = true)
        class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters File[] files;
        }
        String expected = "" +
                "Usage: anotherProgram [OPTIONS] [<files>]...%n" +
                "      [<files>]...%n" +
                "  -c, --count=<count>%n" +
                "  -v, --verbose%n";
        String actual = usageString(new CommandLine(new App()).setCommandName("anotherProgram"), Help.Ansi.OFF);
        assertEquals(String.format(expected), actual);
    }

    @Test
    public void testSynopsis_commandNameCustomizableDeclaratively() throws UnsupportedEncodingException {
        @Command(name = "aprogram")
        class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters File[] files;
        }
        String expected = "" +
                "Usage: aprogram [-v] [-c=<count>] [<files>]...%n" +
                "      [<files>]...%n" +
                "  -c, --count=<count>%n" +
                "  -v, --verbose%n";
        String actual = usageString(new CommandLine(new App()), Help.Ansi.OFF);
        assertEquals(String.format(expected), actual);
    }

    @Test
    public void testSynopsis_commandNameCustomizableProgrammatically() throws UnsupportedEncodingException {
        class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters File[] files;
        }
        String expected = "" +
                "Usage: anotherProgram [-v] [-c=<count>] [<files>]...%n" +
                "      [<files>]...%n" +
                "  -c, --count=<count>%n" +
                "  -v, --verbose%n";
        String actual = usageString(new CommandLine(new App()).setCommandName("anotherProgram"), Help.Ansi.OFF);
        assertEquals(String.format(expected), actual);
    }

    @Test
    public void testSynopsis_optionalOptionArity1_n_withDefaultSeparator() {
        @Command() class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, arity = "1..*") int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [-v] [-c=<count>...]" + LINESEP, help.synopsis(0));
    }

    @Test
    public void testSynopsis_optionalOptionArity1_n_withDefaultSeparator_ANSI() {
        @Command() class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, arity = "1..*") int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.defaultColorScheme(Help.Ansi.ON));
        assertEquals(Help.Ansi.ON.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@=@|italic <count>|@...]" + LINESEP),
                help.synopsis(0));
    }

    @Test
    public void testSynopsis_optionalOptionArity0_1_withSpaceSeparator() {
        @Command(separator = " ") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, arity = "0..1") int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [-v] [-c [<count>]]" + LINESEP, help.synopsis(0));
    }

    @Test
    public void testSynopsis_optionalOptionArity0_1_withSpaceSeparator_ANSI() {
        @Command(separator = " ") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, arity = "0..1") int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.defaultColorScheme(Help.Ansi.ON));
        assertEquals(Help.Ansi.ON.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@ [@|italic <count>|@]]" + LINESEP), help.synopsis(0));
    }

    @Test
    public void testSynopsis_requiredOptionWithSeparator() {
        @Command() class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, required = true) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [-v] -c=<count>" + LINESEP, help.synopsis(0));
    }

    @Test
    public void testSynopsis_requiredOptionWithSeparator_ANSI() {
        @Command() class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, required = true) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.defaultColorScheme(Help.Ansi.ON));
        assertEquals(Help.Ansi.ON.new Text("@|bold <main class>|@ [@|yellow -v|@] @|yellow -c|@=@|italic <count>|@" + LINESEP), help.synopsis(0));
    }

    @Test
    public void testSynopsis_optionalOption_withSpaceSeparator() {
        @Command(separator = " ") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [-v] [-c <count>]" + LINESEP, help.synopsis(0));
    }

    @Test
    public void testSynopsis_optionalOptionArity0_1__withSeparator() {
        class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, arity = "0..1") int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [-v] [-c[=<count>]]" + LINESEP, help.synopsis(0));
    }

    @Test
    public void testSynopsis_optionalOptionArity0_n__withSeparator() {
        @Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, arity = "0..*") int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        // NOTE Expected :<main class> [-v] [-c[=<count>]...] but arity=0 for int field is weird anyway...
        assertEquals("<main class> [-v] [-c=[<count>]...]" + LINESEP, help.synopsis(0));
    }

    @Test
    public void testSynopsis_optionalOptionArity1_n__withSeparator() {
        @Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}, arity = "1..*") int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [-v] [-c=<count>...]" + LINESEP, help.synopsis(0));
    }

    @Test
    public void testSynopsis_withProgrammaticallySetSeparator_withParameters() throws UnsupportedEncodingException {
        class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters File[] files;
        }
        CommandLine commandLine = new CommandLine(new App()).setSeparator(":");
        String actual = usageString(commandLine, Help.Ansi.OFF);
        String expected = "" +
                "Usage: <main class> [-v] [-c:<count>] [<files>]...%n" +
                "      [<files>]...%n" +
                "  -c, --count:<count>%n" +
                "  -v, --verbose%n";
        assertEquals(String.format(expected), actual);
    }

    @Test
    public void testSynopsis_withSeparator_withParameters() {
        @Command(separator = ":") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters File[] files;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [-v] [-c:<count>] [<files>]..." + LINESEP, help.synopsis(0));
    }

    @Test
    public void testSynopsis_withSeparator_withParameters_ANSI() {
        @Command(separator = ":") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters File[] files;
        }
        Help help = new Help(new App(), Help.defaultColorScheme(Help.Ansi.ON));
        assertEquals(Help.Ansi.ON.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@:@|italic <count>|@] [@|yellow <files>|@]..." + LINESEP),
                help.synopsis(0));
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
        assertEquals("<main class> [-v] [-c=<count>] [FILE]..." + LINESEP, help.synopsis(0));
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
        assertEquals(Help.Ansi.ON.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@=@|italic <count>|@] [@|yellow FILE|@]..." + LINESEP),
                help.synopsis(0));
    }

    @Test
    public void testSynopsis_withSeparator_withLabeledRequiredParameters() {
        @Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters(paramLabel = "FILE", arity = "1..*") File[] files;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> [-v] [-c=<count>] FILE..." + LINESEP, help.synopsis(0));
    }

    @Test
    public void testSynopsis_withSeparator_withLabeledRequiredParameters_ANSI() {
        @Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters(paramLabel = "FILE", arity = "1..*") File[] files;
        }
        Help help = new Help(new App(), Help.Ansi.ON);
        assertEquals(Help.Ansi.ON.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@=@|italic <count>|@] @|yellow FILE|@..." + LINESEP),
                help.synopsis(0));
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
        assertEquals("<main class> [-avx] [-c=COUNT]" + LINESEP, help.synopsis(0));
    }

    @Test
    public void testSynopsis_clustersRequiredBooleanOptions() {
        @Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}, required = true) boolean verbose;
            @Option(names = {"--aaaa", "-a"}, required = true) boolean aBoolean;
            @Option(names = {"--xxxx", "-x"}, required = true) Boolean xBoolean;
            @Option(names = {"--count", "-c"}, paramLabel = "COUNT") int count;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> -avx [-c=COUNT]" + LINESEP, help.synopsis(0));
    }

    @Test
    public void testSynopsis_clustersRequiredBooleanOptionsSeparately() {
        @Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--aaaa", "-a"}) boolean aBoolean;
            @Option(names = {"--xxxx", "-x"}) Boolean xBoolean;
            @Option(names = {"--Verbose", "-V"}, required = true) boolean requiredVerbose;
            @Option(names = {"--Aaaa", "-A"}, required = true) boolean requiredABoolean;
            @Option(names = {"--Xxxx", "-X"}, required = true) Boolean requiredXBoolean;
            @Option(names = {"--count", "-c"}, paramLabel = "COUNT") int count;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        assertEquals("<main class> -AVX [-avx] [-c=COUNT]" + LINESEP, help.synopsis(0));
    }

    @Test
    public void testSynopsis_clustersRequiredBooleanOptionsSeparately_ANSI() {
        @Command(separator = "=") class App {
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
                help.synopsis(0));
    }

    @Test
    public void testSynopsis_firstLineLengthAdjustedForSynopsisHeading() throws Exception {
        //Usage: small-test-program [-acorv!?] [--version] [-h <number>] [-p <file>|<folder>] [-d
//                 <folder> [<folder>]] [-i <includePattern>
//                 [<includePattern>...]]
        @Command(name="small-test-program", sortOptions = false, separator = " ")
        class App {
            @Option(names = "-a") boolean a;
            @Option(names = "-c") boolean c;
            @Option(names = "-o") boolean o;
            @Option(names = "-r") boolean r;
            @Option(names = "-v") boolean v;
            @Option(names = "-!") boolean exclamation;
            @Option(names = "-?") boolean question;
            @Option(names = {"--version"}) boolean version;
            @Option(names = {"--handle", "-h"}) int number;
            @Option(names = {"--ppp", "-p"}, paramLabel = "<file>|<folder>") File f;
            @Option(names = {"--ddd", "-d"}, paramLabel = "<folder>", arity="1..2") File[] d;
            @Option(names = {"--include", "-i"}, paramLabel = "<includePattern>") String pattern;
        }
        Help help = new Help(new App(), Help.Ansi.OFF);
        String expected = "" +
                "Usage: small-test-program [-!?acorv] [--version] [-h <number>] [-i" + LINESEP +
                "                          <includePattern>] [-p <file>|<folder>] [-d <folder>" + LINESEP +
                "                          [<folder>]]..." + LINESEP;
        assertEquals(expected, help.synopsisHeading() + help.synopsis(help.synopsisHeadingLength()));

        help.commandSpec().usageMessage().synopsisHeading("Usage:%n");
        expected = "" +
                "Usage:" + LINESEP +
                "small-test-program [-!?acorv] [--version] [-h <number>] [-i <includePattern>]" + LINESEP +
                "                   [-p <file>|<folder>] [-d <folder> [<folder>]]..." + LINESEP;
        assertEquals(expected, help.synopsisHeading() + help.synopsis(help.synopsisHeadingLength()));
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
                help.synopsis(0));
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
                help.synopsis(0));
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
                help.synopsis(0));
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
                help.synopsis(0));
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
            @Parameters(index = "3", arity = "1..2", description = "destination port range") int[] port2range;
            @Parameters(index = "4..*", description = "files to transfer") String[] files;
            @Parameters(hidden = true) String[] all;
        }
        String actual = usageString(new App(), Help.Ansi.OFF);
        String expected = String.format(
                "Usage: <main class> <host1> <port1> <host2> <port2range> [<port2range>]%n" +
                        "                    [<files>]...%n" +
                        "      <host1>                 source host%n" +
                        "      <port1>                 source port%n" +
                        "      <host2>                 destination host%n" +
                        "      <port2range> [<port2range>]%n" +
                        "                              destination port range%n" +
                        "      [<files>]...            files to transfer%n"
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
        assertEquals("base", help.commandName());
        assertEquals(String.format("cust%n"), help.synopsis(0));
        assertEquals(String.format("cust%n"), help.customSynopsis());
        assertEquals(String.format("base%n"), help.abbreviatedSynopsis());
        assertEquals(String.format("base%n"), help.detailedSynopsis(0,null, true));
        assertEquals("abcd", help.synopsisHeading());
        assertEquals("", help.commandList());
        assertEquals("", help.commandListHeading());
        assertEquals("c o m m a n d s", help.commandSpec().usageMessage().commandListHeading());
        assertEquals(String.format("base description%n"), help.description());
        assertEquals("base descr heading", help.descriptionHeading());
        assertEquals(String.format("base footer%n"), help.footer());
        assertEquals("base footer heading", help.footerHeading());
        assertEquals(String.format("base header%n"), help.header());
        assertEquals("base header heading", help.headerHeading());
        assertEquals("", help.optionList());
        assertEquals("", help.optionListHeading());
        assertEquals("base option heading", help.commandSpec().usageMessage().optionListHeading());
        assertEquals("", help.parameterList());
        assertEquals("", help.parameterListHeading());
        assertEquals("base param heading", help.commandSpec().usageMessage().parameterListHeading());

        assertEquals(";", help.commandSpec().parser().separator());
        assertEquals('&', help.commandSpec().usageMessage().requiredOptionMarker());
        assertTrue(help.commandSpec().usageMessage().abbreviateSynopsis());
        assertTrue(help.commandSpec().usageMessage().showDefaultValues());
        assertFalse(help.commandSpec().usageMessage().sortOptions());
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
                sortOptions = false, synopsisHeading = "abcd", subcommands = Sub.class)
        class FullBase extends EmptyBase{ }
        Help help = new Help(new FullBase());
        assertEquals("base", help.commandName());
        assertEquals(String.format("cust%n"), help.synopsis(0));
        assertEquals(String.format("cust%n"), help.customSynopsis());
        assertEquals(String.format("base%n"), help.abbreviatedSynopsis());
        assertEquals(String.format("base%n"), help.detailedSynopsis(0, null, true));
        assertEquals("abcd", help.synopsisHeading());
        assertEquals(String.format("  sub  This is a subcommand%n"), help.commandList());
        assertEquals("c o m m a n d s", help.commandListHeading());
        assertEquals(String.format("base description%n"), help.description());
        assertEquals("base descr heading", help.descriptionHeading());
        assertEquals(String.format("base footer%n"), help.footer());
        assertEquals("base footer heading", help.footerHeading());
        assertEquals(String.format("base header%n"), help.header());
        assertEquals("base header heading", help.headerHeading());
        assertEquals("", help.optionList());
        assertEquals("base option heading", help.commandSpec().usageMessage().optionListHeading());
        assertEquals("", help.optionListHeading()); // because no options
        assertEquals("", help.parameterList());
        assertEquals("base param heading", help.commandSpec().usageMessage().parameterListHeading());
        assertEquals("", help.parameterListHeading()); // because no parameters
        assertTrue(help.commandSpec().usageMessage().abbreviateSynopsis());
        assertTrue(help.commandSpec().usageMessage().showDefaultValues());
        assertFalse(help.commandSpec().usageMessage().sortOptions());
        assertEquals(";", help.commandSpec().parser().separator());
        assertEquals('&', help.commandSpec().usageMessage().requiredOptionMarker());
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
        assertEquals("sub", help.commandName());
        assertEquals(String.format("subcust%n"), help.synopsis(0));
        assertEquals(String.format("subcust%n"), help.customSynopsis());
        assertEquals(String.format("sub%n"), help.abbreviatedSynopsis());
        assertEquals(String.format("sub%n"), help.detailedSynopsis(0,null, true));
        assertEquals("xyz", help.synopsisHeading());
        assertEquals("", help.commandList());
        assertEquals("", help.commandListHeading()); // empty: no commands
        assertEquals("subc o m m a n d s", help.commandSpec().usageMessage().commandListHeading());
        assertEquals(String.format("sub description%n"), help.description());
        assertEquals("sub descr heading", help.descriptionHeading());
        assertEquals(String.format("sub footer%n"), help.footer());
        assertEquals("sub footer heading", help.footerHeading());
        assertEquals(String.format("sub header%n"), help.header());
        assertEquals("sub header heading", help.headerHeading());
        assertEquals("", help.optionList());
        assertEquals("", help.optionListHeading());
        assertEquals("sub option heading", help.commandSpec().usageMessage().optionListHeading());
        assertEquals("", help.parameterList());
        assertEquals("", help.parameterListHeading());
        assertEquals("sub param heading", help.commandSpec().usageMessage().parameterListHeading());
        assertTrue(help.commandSpec().usageMessage().abbreviateSynopsis());
        assertTrue(help.commandSpec().usageMessage().showDefaultValues());
        assertFalse(help.commandSpec().usageMessage().sortOptions());
        assertEquals(":", help.commandSpec().parser().separator());
        assertEquals('%', help.commandSpec().usageMessage().requiredOptionMarker());
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
        assertEquals(format("<main class> <param0> <param1>%n"), help.synopsis(0));
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
        assertEquals(format("<main class> <param0> <param1> <param2> <param3>%n"), help.synopsis(0));
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
    public void testUsageNestedSubcommand() throws IOException {
        @Command(name = "main") class MainCommand { @Option(names = "-a") boolean a; @Option(names = "-h", help = true) boolean h;}
        @Command(name = "cmd1") class ChildCommand1 { @Option(names = "-b") boolean b; }
        @Command(name = "cmd2") class ChildCommand2 { @Option(names = "-c") boolean c; @Option(names = "-h", help = true) boolean h;}
        @Command(name = "sub11") class GrandChild1Command1 { @Option(names = "-d") boolean d; }
        @Command(name = "sub12") class GrandChild1Command2 { @Option(names = "-e") int e; }
        @Command(name = "sub21") class GrandChild2Command1 { @Option(names = "-h", help = true) boolean h; }
        @Command(name = "sub22") class GrandChild2Command2 { @Option(names = "-g") boolean g; }
        @Command(name = "sub22sub1") class GreatGrandChild2Command2_1 {
            @Option(names = "-h", help = true) boolean h;
            @Option(names = {"-t", "--type"}) String customType;
        }
        CommandLine commandLine = new CommandLine(new MainCommand());
        commandLine
                .addSubcommand("cmd1", new CommandLine(new ChildCommand1())
                        .addSubcommand("sub11", new GrandChild1Command1())
                        .addSubcommand("sub12", new GrandChild1Command2())
                )
                .addSubcommand("cmd2", new CommandLine(new ChildCommand2())
                        .addSubcommand("sub21", new GrandChild2Command1())
                        .addSubcommand("sub22", new CommandLine(new GrandChild2Command2())
                                .addSubcommand("sub22sub1", new GreatGrandChild2Command2_1())
                        )
                );
        String main = usageString(commandLine, Help.Ansi.OFF);
        assertEquals(String.format("" +
                "Usage: main [-ah]%n" +
                "  -a%n" +
                "  -h%n" +
                "Commands:%n" +
                "  cmd1%n" +
                "  cmd2%n"), main);

        String cmd2 = usageString(commandLine.getSubcommands().get("cmd2"), Help.Ansi.OFF);
        assertEquals(String.format("" +
                "Usage: cmd2 [-ch]%n" +
                "  -c%n" +
                "  -h%n" +
                "Commands:%n" +
                "  sub21%n" +
                "  sub22%n"), cmd2);

        String sub22 = usageString(commandLine.getSubcommands().get("cmd2").getSubcommands().get("sub22"), Help.Ansi.OFF);
        assertEquals(String.format("" +
                "Usage: sub22 [-g]%n" +
                "  -g%n" +
                "Commands:%n" +
                "  sub22sub1%n"), sub22);
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
    public void testTextSplitLines() {
        Help.Ansi ansi = Help.Ansi.ON;
        Text[] all = {
                ansi.new Text("@|bold 012\n34|@").append("5\nAA\n6").append("@|underline 78\n90|@"),
                ansi.new Text("@|bold 012\r34|@").append("5\rAA\r6").append("@|underline 78\r90|@"),
                ansi.new Text("@|bold 012\r\n34|@").append("5\r\nAA\r\n6").append("@|underline 78\r\n90|@"),
        };
        for (Text text : all) {
            Text[] lines = text.splitLines();
            int i = 0;
            assertEquals(ansi.new Text("@|bold 012|@"), lines[i++]);
            assertEquals(ansi.new Text("@|bold 34|@5"), lines[i++]);
            assertEquals(ansi.new Text("AA"), lines[i++]);
            assertEquals(ansi.new Text("6@|underline 78|@"), lines[i++]);
            assertEquals(ansi.new Text("@|underline 90|@"), lines[i++]);
        }
    }
    @Test
    public void testTextSplitLinesStartEnd() {
        Help.Ansi ansi = Help.Ansi.ON;
        Text[] all = {
                ansi.new Text("\n@|bold 012\n34|@").append("5\nAA\n6").append("@|underline 78\n90|@\n"),
                ansi.new Text("\r@|bold 012\r34|@").append("5\rAA\r6").append("@|underline 78\r90|@\r"),
                ansi.new Text("\r\n@|bold 012\r\n34|@").append("5\r\nAA\r\n6").append("@|underline 78\r\n90|@\r\n"),
        };
        for (Text text : all) {
            Text[] lines = text.splitLines();
            int i = 0;
            assertEquals(ansi.new Text(""), lines[i++]);
            assertEquals(ansi.new Text("@|bold 012|@"), lines[i++]);
            assertEquals(ansi.new Text("@|bold 34|@5"), lines[i++]);
            assertEquals(ansi.new Text("AA"), lines[i++]);
            assertEquals(ansi.new Text("6@|underline 78|@"), lines[i++]);
            assertEquals(ansi.new Text("@|underline 90|@"), lines[i++]);
            assertEquals(ansi.new Text(""), lines[i++]);
        }
    }
    @Test
    public void testTextSplitLinesStartEndIntermediate() {
        Help.Ansi ansi = Help.Ansi.ON;
        Text[] all = {
                ansi.new Text("\n@|bold 012\n\n\n34|@").append("5\n\n\nAA\n\n\n6").append("@|underline 78\n90|@\n"),
                ansi.new Text("\r@|bold 012\r\r\r34|@").append("5\r\r\rAA\r\r\r6").append("@|underline 78\r90|@\r"),
                ansi.new Text("\r\n@|bold 012\r\n\r\n\r\n34|@").append("5\r\n\r\n\r\nAA\r\n\r\n\r\n6").append("@|underline 78\r\n90|@\r\n"),
        };
        for (Text text : all) {
            Text[] lines = text.splitLines();
            int i = 0;
            assertEquals(ansi.new Text(""), lines[i++]);
            assertEquals(ansi.new Text("@|bold 012|@"), lines[i++]);
            assertEquals(ansi.new Text(""), lines[i++]);
            assertEquals(ansi.new Text(""), lines[i++]);
            assertEquals(ansi.new Text("@|bold 34|@5"), lines[i++]);
            assertEquals(ansi.new Text(""), lines[i++]);
            assertEquals(ansi.new Text(""), lines[i++]);
            assertEquals(ansi.new Text("AA"), lines[i++]);
            assertEquals(ansi.new Text(""), lines[i++]);
            assertEquals(ansi.new Text(""), lines[i++]);
            assertEquals(ansi.new Text("6@|underline 78|@"), lines[i++]);
            assertEquals(ansi.new Text("@|underline 90|@"), lines[i++]);
            assertEquals(ansi.new Text(""), lines[i++]);
        }
    }
    @Test
    public void testEmbeddedNewLinesInUsageSections() throws UnsupportedEncodingException {
        @Command(description = "first line\nsecond line\nthird line", headerHeading = "headerHeading1\nheaderHeading2",
                header = "header1\nheader2", descriptionHeading = "descriptionHeading1\ndescriptionHeading2",
                footerHeading = "footerHeading1\nfooterHeading2", footer = "footer1\nfooter2")
        class App {
            @Option(names = {"-v", "--verbose"}, description = "optionDescription1\noptionDescription2") boolean v;
            @Parameters(description = "paramDescription1\nparamDescription2") String file;
        }
        String actual = usageString(new App(), Help.Ansi.OFF);
        String expected = String.format("" +
                "headerHeading1%n" +
                "headerHeading2header1%n" +
                "header2%n" +
                "Usage: <main class> [-v] <file>%n" +
                "descriptionHeading1%n" +
                "descriptionHeading2first line%n" +
                "second line%n" +
                "third line%n" +
                "      <file>                  paramDescription1%n" +
                "                              paramDescription2%n" +
                "  -v, --verbose               optionDescription1%n" +
                "                              optionDescription2%n" +
                "footerHeading1%n" +
                "footerHeading2footer1%n" +
                "footer2%n");
        assertEquals(expected, actual);
    }
    @Test
    public void testTextWithMultipleStyledSections() {
        assertEquals("\u001B[1m<main class>\u001B[21m\u001B[0m [\u001B[33m-v\u001B[39m\u001B[0m] [\u001B[33m-c\u001B[39m\u001B[0m [\u001B[3m<count>\u001B[23m\u001B[0m]]",
                Help.Ansi.ON.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@ [@|italic <count>|@]]").toString());
    }

    @Test
    public void testTextAdjacentStyles() {
        assertEquals("\u001B[3m<commit\u001B[23m\u001B[0m\u001B[3m>\u001B[23m\u001B[0m%n",
                Help.Ansi.ON.new Text("@|italic <commit|@@|italic >|@%n").toString());
    }

    @Test
    public void testTextNoConversionWithoutClosingTag() {
        assertEquals("\u001B[3mabc\u001B[23m\u001B[0m", Help.Ansi.ON.new Text("@|italic abc|@").toString());
        assertEquals("@|italic abc",                    Help.Ansi.ON.new Text("@|italic abc").toString());
    }

    @Test
    public void testTextNoConversionWithoutSpaceSeparator() {
        assertEquals("\u001B[3ma\u001B[23m\u001B[0m", Help.Ansi.ON.new Text("@|italic a|@").toString());
        assertEquals("@|italic|@",                    Help.Ansi.ON.new Text("@|italic|@").toString());
        assertEquals("",                              Help.Ansi.ON.new Text("@|italic |@").toString());
    }

    @Test
    public void testPalette236ColorForegroundIndex() {
        assertEquals("\u001B[38;5;45mabc\u001B[39m\u001B[0m", Help.Ansi.ON.new Text("@|fg(45) abc|@").toString());
    }

    @Test
    public void testPalette236ColorForegroundRgb() {
        int num = 16 + 36 * 5 + 6 * 5 + 5;
        assertEquals("\u001B[38;5;" + num + "mabc\u001B[39m\u001B[0m", Help.Ansi.ON.new Text("@|fg(5;5;5) abc|@").toString());
    }

    @Test
    public void testPalette236ColorBackgroundIndex() {
        assertEquals("\u001B[48;5;77mabc\u001B[49m\u001B[0m", Help.Ansi.ON.new Text("@|bg(77) abc|@").toString());
    }

    @Test
    public void testPalette236ColorBackgroundRgb() {
        int num = 16 + 36 * 3 + 6 * 3 + 3;
        assertEquals("\u001B[48;5;" + num + "mabc\u001B[49m\u001B[0m", Help.Ansi.ON.new Text("@|bg(3;3;3) abc|@").toString());
    }

    @Test
    public void testAnsiEnabled() {
        assertTrue(Help.Ansi.ON.enabled());
        assertFalse(Help.Ansi.OFF.enabled());

        System.setProperty("picocli.ansi", "true");
        assertEquals(true, Help.Ansi.AUTO.enabled());

        System.setProperty("picocli.ansi", "false");
        assertEquals(false, Help.Ansi.AUTO.enabled());

        System.clearProperty("picocli.ansi");
        boolean isWindows = System.getProperty("os.name").startsWith("Windows");
        boolean isXterm   = System.getenv("TERM") != null && System.getenv("TERM").startsWith("xterm");
        boolean isAtty    = (isWindows && isXterm) // cygwin pseudo-tty
                          || hasConsole();
        assertEquals(isAtty && (!isWindows || isXterm), Help.Ansi.AUTO.enabled());
    }

    private boolean hasConsole() {
        try { return System.class.getDeclaredMethod("console").invoke(null) != null; }
        catch (Throwable reflectionFailed) { return true; }
    }

    @Test
    public void testSystemPropertiesOverrideDefaultColorScheme() {
        @Command(separator = "=") class App {
            @Option(names = {"--verbose", "-v"}) boolean verbose;
            @Option(names = {"--count", "-c"}) int count;
            @Option(names = {"--help", "-h"}, hidden = true) boolean helpRequested;
            @Parameters(paramLabel = "FILE", arity = "1..*") File[] files;
        }
        Help.Ansi ansi = Help.Ansi.ON;
        // default color scheme
        assertEquals(ansi.new Text("@|bold <main class>|@ [@|yellow -v|@] [@|yellow -c|@=@|italic <count>|@] @|yellow FILE|@..." + LINESEP),
                new Help(new App(), ansi).synopsis(0));

        System.setProperty("picocli.color.commands", "blue");
        System.setProperty("picocli.color.options", "green");
        System.setProperty("picocli.color.parameters", "cyan");
        System.setProperty("picocli.color.optionParams", "magenta");
        assertEquals(ansi.new Text("@|blue <main class>|@ [@|green -v|@] [@|green -c|@=@|magenta <count>|@] @|cyan FILE|@..." + LINESEP),
                new Help(new App(), ansi).synopsis(0));
    }

    @Test
    public void testSystemPropertiesOverrideExplicitColorScheme() {
        @Command(separator = "=") class App {
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
        assertEquals(ansi.new Text("@|faint,bg(magenta) <main class>|@ [@|bg(red) -v|@] [@|bg(red) -c|@=@|bg(green) <count>|@] @|reverse FILE|@..." + LINESEP),
                new Help(new App(), explicit).synopsis(0));

        System.setProperty("picocli.color.commands", "blue");
        System.setProperty("picocli.color.options", "blink");
        System.setProperty("picocli.color.parameters", "red");
        System.setProperty("picocli.color.optionParams", "magenta");
        assertEquals(ansi.new Text("@|blue <main class>|@ [@|blink -v|@] [@|blink -c|@=@|magenta <count>|@] @|red FILE|@..." + LINESEP),
                new Help(new App(), explicit).synopsis(0));
    }

    @Test
    public void testCommandLine_printVersionInfo_printsSinglePlainTextString() throws Exception {
        @Command(version = "1.0") class Versioned {}
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CommandLine(new Versioned()).printVersionHelp(new PrintStream(baos, true, "UTF8"), Help.Ansi.OFF);
        String result = baos.toString("UTF8");
        assertEquals(String.format("1.0%n"), result);
    }

    @Test
    public void testCommandLine_printVersionInfo_printsArrayOfPlainTextStrings() throws Exception {
        @Command(version = {"Versioned Command 1.0", "512-bit superdeluxe", "(c) 2017"}) class Versioned {}
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CommandLine(new Versioned()).printVersionHelp(new PrintStream(baos, true, "UTF8"), Help.Ansi.OFF);
        String result = baos.toString("UTF8");
        assertEquals(String.format("Versioned Command 1.0%n512-bit superdeluxe%n(c) 2017%n"), result);
    }

    @Test
    public void testCommandLine_printVersionInfo_printsSingleStringWithMarkup() throws Exception {
        @Command(version = "@|red 1.0|@") class Versioned {}
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CommandLine(new Versioned()).printVersionHelp(new PrintStream(baos, true, "UTF8"), Help.Ansi.ON);
        String result = baos.toString("UTF8");
        assertEquals(String.format("\u001B[31m1.0\u001B[39m\u001B[0m%n"), result);
    }

    @Test
    public void testCommandLine_printVersionInfo_printsArrayOfStringsWithMarkup() throws Exception {
        @Command(version = {
                "@|yellow Versioned Command 1.0|@",
                "@|blue Build 12345|@",
                "@|red,bg(white) (c) 2017|@" })
        class Versioned {}
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CommandLine(new Versioned()).printVersionHelp(new PrintStream(baos, true, "UTF8"), Help.Ansi.ON);
        String result = baos.toString("UTF8");
        assertEquals(String.format("" +
                "\u001B[33mVersioned Command 1.0\u001B[39m\u001B[0m%n" +
                "\u001B[34mBuild 12345\u001B[39m\u001B[0m%n" +
                "\u001B[31m\u001B[47m(c) 2017\u001B[49m\u001B[39m\u001B[0m%n"), result);
    }
    @Test
    public void testCommandLine_printVersionInfo_formatsArguments() throws Exception {
        @Command(version = {"First line %1$s", "Second line %2$s", "Third line %s %s"}) class Versioned {}
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, "UTF8");
        new CommandLine(new Versioned()).printVersionHelp(ps, Help.Ansi.OFF, "VALUE1", "VALUE2", "VALUE3");
        String result = baos.toString("UTF8");
        assertEquals(String.format("First line VALUE1%nSecond line VALUE2%nThird line VALUE1 VALUE2%n"), result);
    }

    @Test
    public void testCommandLine_printVersionInfo_withMarkupAndParameterContainingMarkup() throws Exception {
        @Command(version = {
                "@|yellow Versioned Command 1.0|@",
                "@|blue Build 12345|@%1$s",
                "@|red,bg(white) (c) 2017|@%2$s" })
        class Versioned {}

        CommandLine commandLine = new CommandLine(new Versioned());
        verifyVersionWithMarkup(commandLine);
    }

    static class MarkupVersionProvider implements IVersionProvider {
        public String[] getVersion() {
            return new String[] {
                    "@|yellow Versioned Command 1.0|@",
                    "@|blue Build 12345|@%1$s",
                    "@|red,bg(white) (c) 2017|@%2$s" };
        }
    }

    @Test
    public void testCommandLine_printVersionInfo_fromAnnotation_withMarkupAndParameterContainingMarkup() throws Exception {
        @Command(versionProvider = MarkupVersionProvider.class)
        class Versioned {}

        CommandLine commandLine = new CommandLine(new Versioned());
        verifyVersionWithMarkup(commandLine);
    }

    @Test
    public void testCommandLine_printVersionInfo_usesProviderIfBothProviderAndStaticVersionInfoExist() throws Exception {
        @Command(versionProvider = MarkupVersionProvider.class, version = "static version is ignored")
        class Versioned {}

        CommandLine commandLine = new CommandLine(new Versioned());
        verifyVersionWithMarkup(commandLine);
    }

    private void verifyVersionWithMarkup(CommandLine commandLine) throws UnsupportedEncodingException {
        String[] args = {"@|bold VALUE1|@", "@|underline VALUE2|@", "VALUE3"};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, "UTF8");
        commandLine.printVersionHelp(ps, Help.Ansi.ON, (Object[]) args);
        String result = baos.toString("UTF8");
        assertEquals(String.format("" +
                "\u001B[33mVersioned Command 1.0\u001B[39m\u001B[0m%n" +
                "\u001B[34mBuild 12345\u001B[39m\u001B[0m\u001B[1mVALUE1\u001B[21m\u001B[0m%n" +
                "\u001B[31m\u001B[47m(c) 2017\u001B[49m\u001B[39m\u001B[0m\u001B[4mVALUE2\u001B[24m\u001B[0m%n"), result);
    }

    static class FailingVersionProvider implements IVersionProvider {
        public String[] getVersion() throws Exception {
            throw new IllegalStateException("sorry can't give you a version");
        }
    }
    @Test
    public void testFailingVersionProvider() {
        @Command(versionProvider = FailingVersionProvider.class)
        class App {}
        CommandLine cmd = new CommandLine(new App());
        try {
            cmd.printVersionHelp(System.out);
            fail("Expected exception");
        } catch (ExecutionException ex) {
            assertEquals("Could not get version info from " + cmd.getCommandSpec().versionProvider() + ": java.lang.IllegalStateException: sorry can't give you a version", ex.getMessage());
        }
    }

    @Test
    public void testNoVersionProvider_errorWhenInvoked() {
        try {
            Class<?> cls = Class.forName("picocli.CommandLine$NoVersionProvider");
            try {
                Constructor<?> constructor = cls.getDeclaredConstructor(new Class[0]);
                constructor.setAccessible(true);
                IVersionProvider provider = (IVersionProvider) constructor.newInstance();
                try {
                    provider.getVersion();
                    fail("expected an exception to be thrown here");
                } catch (UnsupportedOperationException ex) {
                    // expected
                }
            } catch (Exception e) {
                fail(e.getMessage());
            }
        } catch (ClassNotFoundException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testMapFieldHelp() throws Exception {
        class App {
            @Parameters(arity = "2", split = "\\|",
                    paramLabel = "FIXTAG=VALUE",
                    description = "Exactly two lists of vertical bar '|'-separated FIXTAG=VALUE pairs.")
            Map<Integer,String> message;

            @Option(names = {"-P", "-map"}, split = ",",
                    paramLabel = "TIMEUNIT=VALUE",
                    description = "Any number of TIMEUNIT=VALUE pairs. These may be specified separately (-PTIMEUNIT=VALUE) or as a comma-separated list.")
            Map<TimeUnit, String> map;
        }
        String actual = usageString(new App(), Help.Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> [-P=TIMEUNIT=VALUE[,TIMEUNIT=VALUE]...]... FIXTAG=VALUE%n" +
                "                    [\\|FIXTAG=VALUE]... FIXTAG=VALUE[\\|FIXTAG=VALUE]...%n" +
                "      FIXTAG=VALUE[\\|FIXTAG=VALUE]... FIXTAG=VALUE[\\|FIXTAG=VALUE]...%n" +
                "                              Exactly two lists of vertical bar '|'-separated%n" +
                "                                FIXTAG=VALUE pairs.%n" +
                "  -P, -map=TIMEUNIT=VALUE[,TIMEUNIT=VALUE]...%n" +
                "                              Any number of TIMEUNIT=VALUE pairs. These may be%n" +
                "                                specified separately (-PTIMEUNIT=VALUE) or as a%n" +
                "                                comma-separated list.%n");
        assertEquals(expected, actual);
    }
    @Test
    public void testMapFieldTypeInference() throws UnsupportedEncodingException {
        class App {
            @Option(names = "-a") Map<Integer, URI> a;
            @Option(names = "-b") Map<TimeUnit, StringBuilder> b;
            @SuppressWarnings("unchecked")
            @Option(names = "-c") Map c;
            @Option(names = "-d") List<File> d;
            @Option(names = "-e") Map<? extends Integer, ? super Long> e;
            @Option(names = "-f", type = {Long.class, Float.class}) Map<? extends Number, ? super Number> f;
            @SuppressWarnings("unchecked")
            @Option(names = "-g", type = {TimeUnit.class, Float.class}) Map<?, ?> g;
        }
        String actual = usageString(new App(), Help.Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> [-a=<Integer=URI>]... [-b=<TimeUnit=StringBuilder>]...%n" +
                "                    [-c=<String=String>]... [-d=<d>]... [-e=<Integer=Long>]...%n" +
                "                    [-f=<Long=Float>]... [-g=<TimeUnit=Float>]...%n" +
                "  -a= <Integer=URI>%n" +
                "  -b= <TimeUnit=StringBuilder>%n" +
                "%n" +
                "  -c= <String=String>%n" +
                "  -d= <d>%n" +
                "  -e= <Integer=Long>%n" +
                "  -f= <Long=Float>%n" +
                "  -g= <TimeUnit=Float>%n");
        assertEquals(expected, actual);
    }
    @Test
    public void test200NPEWithEmptyCommandName() throws UnsupportedEncodingException {
        @Command(name = "") class Args {}
        String actual = usageString(new Args(), Help.Ansi.OFF);
        String expected = String.format("" +
                "Usage: %n" +
                "");
        assertEquals(expected, actual);
    }

    @Test
    public void testPrintHelpIfRequestedReturnsTrueForUsageHelp() throws IOException {
        class App {
            @Option(names = "-h", usageHelp = true) boolean usageRequested;
        }
        List<CommandLine> list = new CommandLine(new App()).parse("-h");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertTrue(CommandLine.printHelpIfRequested(list, new PrintStream(baos), Help.Ansi.OFF));

        String expected = String.format("" +
                "Usage: <main class> [-h]%n" +
                "  -h%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testPrintHelpIfRequestedReturnsTrueForVersionHelp() throws IOException {
        @Command(version = "abc 1.2.3 myversion")
        class App {
            @Option(names = "-V", versionHelp = true) boolean versionRequested;
        }
        List<CommandLine> list = new CommandLine(new App()).parse("-V");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertTrue(CommandLine.printHelpIfRequested(list, new PrintStream(baos), Help.Ansi.OFF));

        String expected = String.format("abc 1.2.3 myversion%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testPrintHelpIfRequestedReturnsFalseForNoHelp() throws IOException {
        class App {
            @Option(names = "-v") boolean verbose;
        }
        List<CommandLine> list = new CommandLine(new App()).parse("-v");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertFalse(CommandLine.printHelpIfRequested(list, new PrintStream(baos), Help.Ansi.OFF));

        String expected = "";
        assertEquals(expected, baos.toString());
    }

    @Command(name = "top", subcommands = {Sub.class})
    static class Top {
        @Option(names = "-o", required = true) String mandatory;
        @Option(names = "-h", usageHelp = true) boolean isUsageHelpRequested;
    }
    @Command(name = "sub", description = "This is a subcommand") static class Sub {}

    @Test
    public void test244SubcommandsNotParsed() {
        List<CommandLine> list = new CommandLine(new Top()).parse("-h", "sub");
        assertEquals(2, list.size());
        assertTrue(list.get(0).getCommand() instanceof Top);
        assertTrue(list.get(1).getCommand() instanceof Sub);
        assertTrue(((Top) list.get(0).getCommand()).isUsageHelpRequested);
    }

    @Test
    public void testDemoUsage() throws Exception {
        String expected = String.format("" +
                "       .__                    .__  .__%n" +
                "______ |__| ____  ____   ____ |  | |__|%n" +
                "\\____ \\|  |/ ___\\/  _ \\_/ ___\\|  | |  |%n" +
                "|  |_> >  \\  \\__(  <_> )  \\___|  |_|  |%n" +
                "|   __/|__|\\___  >____/ \\___  >____/__|%n" +
                "|__|           \\/           \\/%n" +
                "%n" +
                "Usage: picocli.Demo [-123airtV] [--simple]%n" +
                "%n" +
                "Demonstrates picocli subcommands parsing and usage help.%n" +
                "%n" +
                "Options:%n" +
                "  -a, --autocomplete          Generate sample autocomplete script for git%n" +
                "  -1, --showUsageForSubcommandGitCommit%n" +
                "                              Shows usage help for the git-commit subcommand%n" +
                "  -2, --showUsageForMainCommand%n" +
                "                              Shows usage help for a command with subcommands%n" +
                "  -3, --showUsageForSubcommandGitStatus%n" +
                "                              Shows usage help for the git-status subcommand%n" +
                "      --simple                Show help for the first simple Example in the%n" +
                "                                manual%n" +
                "  -i, --index                 Show 256 color palette index values%n" +
                "  -r, --rgb                   Show 256 color palette RGB component values%n" +
                "  -t, --tests                 Runs all tests in this class%n" +
                "  -V, --version               Show version information and exit%n" +
                "%n" +
                "VM Options:%n" +
                "Run with -ea to enable assertions used in the tests.%n" +
                "Run with -Dpicocli.ansi=true to force picocli to use ansi codes,%n" +
                " or with -Dpicocli.ansi=false to force picocli to NOT use ansi codes.%n" +
                "(By default picocli will use ansi codes if the platform supports it.)%n" +
                "%n" +
                "If you would like to contribute or report an issue%n" +
                "go to github: https://github.com/remkop/picocli%n" +
                "%n" +
                "If you like the project star it on github and follow me on twitter!%n" +
                "This project is created and maintained by Remko Popma (@remkopopma)%n" +
                "%n");
        assertEquals(expected, usageString(new Demo(), Help.Ansi.OFF));
    }

    @Test
    public void testHelpCannotBeAddedAsSubcommand() {
        @Command(subcommands = Help.class) class App{}
        try {
            new CommandLine(new App(), new InnerClassFactory(this));
        } catch (InitializationException ex) {
            assertEquals("picocli.CommandLine$Help is not a valid subcommand. Did you mean picocli.CommandLine$HelpCommand?", ex.getMessage());
        }
    }
    @Test
    public void testAutoHelpMixinUsageHelpOption() throws Exception {
        @Command(mixinStandardHelpOptions = true) class App {}

        String[] helpOptions = {"-h", "--help"};
        for (String option : helpOptions) {
            List<CommandLine> list = new CommandLine(new App()).parse(option);
            assertTrue(list.get(0).isUsageHelpRequested());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            assertTrue(CommandLine.printHelpIfRequested(list, new PrintStream(baos), Help.Ansi.OFF));

            String expected = String.format("" +
                    "Usage: <main class> [-hV]%n" +
                    "  -h, --help                  Show this help message and exit.%n" +
                    "  -V, --version               Print version information and exit.%n");
            assertEquals(expected, baos.toString());
        }
    }

    @Test
    public void testAutoHelpMixinVersionHelpOption() throws Exception {
        @Command(mixinStandardHelpOptions = true, version = "1.2.3") class App {}

        String[] versionOptions = {"-V", "--version"};
        for (String option : versionOptions) {
            List<CommandLine> list = new CommandLine(new App()).parse(option);
            assertTrue(list.get(0).isVersionHelpRequested());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            assertTrue(CommandLine.printHelpIfRequested(list, new PrintStream(baos), Help.Ansi.OFF));

            String expected = String.format("1.2.3%n");
            assertEquals(expected, baos.toString());
        }
    }

    @Test
    public void testAutoHelpMixinUsageHelpSubcommandOnAppWithoutSubcommands() throws Exception {
        @Command(mixinStandardHelpOptions = true, subcommands = HelpCommand.class) class App {}

        List<CommandLine> list = new CommandLine(new App()).parse("help");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertTrue(CommandLine.printHelpIfRequested(list, new PrintStream(baos), Help.Ansi.OFF));

        String expected = String.format("" +
                "Usage: <main class> [-hV]%n" +
                "  -h, --help                  Show this help message and exit.%n" +
                "  -V, --version               Print version information and exit.%n" +
                "Commands:%n" +
                "  help  Displays help information about the specified command%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testAutoHelpMixinRunHelpSubcommandOnAppWithoutSubcommands() throws Exception {
        @Command(mixinStandardHelpOptions = true, subcommands = HelpCommand.class)
        class App implements Runnable{ public void run(){}}

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.run(new App(), new PrintStream(baos), Help.Ansi.OFF, "help");

        String expected = String.format("" +
                "Usage: <main class> [-hV]%n" +
                "  -h, --help                  Show this help message and exit.%n" +
                "  -V, --version               Print version information and exit.%n" +
                "Commands:%n" +
                "  help  Displays help information about the specified command%n");
        assertEquals(expected, baos.toString());
    }
    @Test
    public void testHelpSubcommandWithValidCommand() throws Exception {
        @Command(subcommands = {Sub.class, HelpCommand.class}) class App implements Runnable{ public void run(){}}

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.run(new App(), new PrintStream(baos), Help.Ansi.OFF, "help", "sub");

        String expected = String.format("" +
                "Usage: sub%n" +
                "This is a subcommand%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testHelpSubcommandWithInvalidCommand() throws Exception {
        @Command(mixinStandardHelpOptions = true, subcommands = {Sub.class, HelpCommand.class})
        class App implements Runnable{ public void run(){}}

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.run(new App(), System.out, new PrintStream(baos), Help.Ansi.OFF, "help", "abcd");

        String expected = String.format("" +
                "Unknown subcommand 'abcd'.%n" +
                "Usage: <main class> [-hV]%n" +
                "  -h, --help                  Show this help message and exit.%n" +
                "  -V, --version               Print version information and exit.%n" +
                "Commands:%n" +
                "  sub   This is a subcommand%n" +
                "  help  Displays help information about the specified command%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testHelpSubcommandWithHelpOption() throws Exception {
        @Command(subcommands = {Sub.class, HelpCommand.class})
        class App implements Runnable{ public void run(){}}

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.run(new App(), new PrintStream(baos), Help.Ansi.OFF, "help", "-h");

        String expected = String.format("" +
                "Displays help information about the specified command%n" +
                "%n" +
                "Usage: help [-h] [COMMAND]...%n" +
                "%n" +
                "When no COMMAND is given, the usage help for the main command is displayed.%n" +
                "If a COMMAND is specified, the help for that command is shown.%n" +
                "%n" +
                "      [COMMAND]...            The COMMAND to display the usage help message for.%n" +
                "  -h, --help                  Show usage help for the help command and exit.%n");
        assertEquals(expected, baos.toString());

        StringWriter sw = new StringWriter();
        new CommandLine(new App()).getSubcommands().get("help").usage(new PrintWriter(sw));
        assertEquals(expected, sw.toString());
    }

    @Test
    public void testHelpSubcommandWithoutCommand() throws Exception {
        @Command(mixinStandardHelpOptions = true, subcommands = {Sub.class, HelpCommand.class})
        class App implements Runnable{ public void run(){}}

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.run(new App(), new PrintStream(baos), Help.Ansi.OFF, "help");

        String expected = String.format("" +
                "Usage: <main class> [-hV]%n" +
                "  -h, --help                  Show this help message and exit.%n" +
                "  -V, --version               Print version information and exit.%n" +
                "Commands:%n" +
                "  sub   This is a subcommand%n" +
                "  help  Displays help information about the specified command%n");
        assertEquals(expected, baos.toString());

        StringWriter sw = new StringWriter();
        new CommandLine(new App()).usage(new PrintWriter(sw));
        assertEquals(expected, sw.toString());
    }

    @Test
    public void testUsageTextWithHiddenSubcommand() throws Exception {
        @Command(name = "foo", description = "This is a visible subcommand") class Foo { }
        @Command(name = "bar", description = "This is a hidden subcommand", hidden = true) class Bar { }
        @Command(name = "app", subcommands = {Foo.class, Bar.class}) class App { }

        CommandLine app = new CommandLine(new App(), new InnerClassFactory(this));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        app.usage(new PrintStream(baos));

        String expected = format("" +
                "Usage: app%n" +
                "Commands:%n" +
                "  foo  This is a visible subcommand%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testUsage_NoHeaderIfAllSubcommandHidden() throws Exception {
        @Command(name = "foo", description = "This is a foo sub-command", hidden = true) class Foo { }
        @Command(name = "bar", description = "This is a foo sub-command", hidden = true) class Bar { }
        @Command(name = "app", abbreviateSynopsis = true) class App { }

        CommandLine app = new CommandLine(new App())
                .addSubcommand("foo", new Foo())
                .addSubcommand("bar", new Bar());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        app.usage(new PrintStream(baos));

        String expected = format("" +
                "Usage: app%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void test282BrokenValidationWhenNoOptionsToCompareWith() throws Exception {
        class App implements Runnable {
            @Parameters(paramLabel = "FILES", arity = "1..*", description = "List of files")
            private List<File> files = new ArrayList<File>();

            public void run() { }
        }
        String[] args = new String[] {"-unknown"};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.run(new App(), System.out, new PrintStream(baos), Help.Ansi.OFF, args);

        String expected = format("" +
                "Missing required parameters at positions 0..*: FILES%n" +
                "Usage: <main class> FILES...%n" +
                "      FILES...                List of files%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void test282ValidationWorksWhenOptionToCompareWithExists() throws Exception {
        class App implements Runnable {
            @Parameters(paramLabel = "FILES", arity = "1..*", description = "List of files")
            private List<File> files = new ArrayList<File>();

            @CommandLine.Option(names = {"-v"}, description = "Print output")
            private boolean verbose;

            public void run() { }
        }
        String[] args = new String[] {"-unknown"};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.run(new App(), System.out, new PrintStream(baos), Help.Ansi.OFF, args);

        String expected = format("" +
                "Missing required parameters at positions 0..*: FILES%n" +
                "Usage: <main class> [-v] FILES...%n" +
                "      FILES...                List of files%n" +
                "  -v                          Print output%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testShouldGetUsageWidthFromSystemProperties() {
        int defaultWidth = Help.getUsageHelpWidth();
        assertEquals(80, defaultWidth);
        try {
            System.setProperty("picocli.usage.width", "123");
            int width = Help.getUsageHelpWidth();
            assertEquals(123, width);
        } finally {
            System.setProperty("picocli.usage.width", String.valueOf(defaultWidth));
        }
    }

    @Test
    public void testInvalidUsageWidthPropertyValue() throws UnsupportedEncodingException {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2500);
        System.setErr(new PrintStream(baos));

        System.clearProperty("picocli.trace");
        System.setProperty("picocli.usage.width", "INVALID");
        int actual = Help.getUsageHelpWidth();
        System.setErr(originalErr);
        System.clearProperty("picocli.usage.width");

        assertEquals(80, actual);
        assertEquals(format("[picocli WARN] Invalid picocli.usage.width value 'INVALID'. Using default usage width 80.%n"), baos.toString("UTF-8"));
    }

    @Test
    public void testTooSmallUsageWidthPropertyValue() throws UnsupportedEncodingException {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2500);
        System.setErr(new PrintStream(baos));

        System.clearProperty("picocli.trace");
        System.setProperty("picocli.usage.width", "54");
        int actual = Help.getUsageHelpWidth();
        System.setErr(originalErr);
        System.clearProperty("picocli.usage.width");

        assertEquals(55, actual);
        assertEquals(format("[picocli WARN] Invalid picocli.usage.width value 54. Using minimum usage width 55.%n"), baos.toString("UTF-8"));
    }

    @Test
    public void testTextTableWithLargeWidth() {
        int defWidth = Help.getUsageHelpWidth();
        System.setProperty("picocli.usage.width", "200");

        try {
            TextTable table = new TextTable(Help.Ansi.OFF);
            table.addRowValues(textArray(Help.Ansi.OFF, "", "-v", ",", "--verbose", "show what you're doing while you are doing it"));
            table.addRowValues(textArray(Help.Ansi.OFF, "", "-p", null, null, "the quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy doooooooooooooooog."));

            assertEquals(String.format(
                    "  -v, --verbose               show what you're doing while you are doing it%n" +
                            "  -p                          the quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy doooooooooooooooog.%n"
            ), table.toString(new StringBuilder()).toString());
        }  finally {
            System.setProperty("picocli.usage.width", String.valueOf(defWidth));
        }
    }

    @Test
    public void testLongMultiLineSynopsisIndentedWithLargeWidth() {
        int defWidth = Help.getUsageHelpWidth();
        System.setProperty("picocli.usage.width", "200");

        try {
            @Command(name = "<best-app-ever>")
            class App {
                @Option(names = "--long-option-name", paramLabel = "<long-option-value>") int a;
                @Option(names = "--another-long-option-name", paramLabel = "<another-long-option-value>") int b;
                @Option(names = "--third-long-option-name", paramLabel = "<third-long-option-value>") int c;
                @Option(names = "--fourth-long-option-name", paramLabel = "<fourth-long-option-value>") int d;
            }
            Help help = new Help(new App(), Help.Ansi.OFF);
            assertEquals(String.format(
                    "<best-app-ever> [--another-long-option-name=<another-long-option-value>] [--fourth-long-option-name=<fourth-long-option-value>] [--long-option-name=<long-option-value>]%n" +
                            "                [--third-long-option-name=<third-long-option-value>]%n"),
                    help.synopsis(0));
        } finally {
            System.setProperty("picocli.usage.width", String.valueOf(defWidth));
        }
    }

    @Test
    public void testWideUsage() throws Exception {
        @Command(description = "The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog.")
        class App {
            @Option(names = "-s", description = "The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog.")
            String shortOption;

            @Option(names = "--very-very-very-looooooooooooooooong-option-name", description = "The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog.")
            String lengthyOption;
        }
        System.setProperty("picocli.usage.width", String.valueOf(120));
        try {
            String actual = usageString(new App(), Help.Ansi.OFF);
            String expected = format("Usage: <main class> [--very-very-very-looooooooooooooooong-option-name=<lengthyOption>] [-s=<shortOption>]%n" +
                    "The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped%n" +
                    "over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The%n" +
                    "quick brown fox jumped over the lazy dog.%n" +
                    "      --very-very-very-looooooooooooooooong-option-name=<lengthyOption>%n" +
                    "                              The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy%n" +
                    "                                dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the%n" +
                    "                                lazy dog.%n" +
                    "  -s= <shortOption>           The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy%n" +
                    "                                dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the%n" +
                    "                                lazy dog.%n");
            assertEquals(expected, actual);
        } finally {
            System.setProperty("picocli.usage.width", String.valueOf(80));
        }
    }
}
