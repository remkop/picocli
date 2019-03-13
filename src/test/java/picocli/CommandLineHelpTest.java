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
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
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
import java.lang.reflect.Method;
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
    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();
    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

    @After
    public void after() {
        System.getProperties().remove("picocli.color.commands");
        System.getProperties().remove("picocli.color.options");
        System.getProperties().remove("picocli.color.parameters");
        System.getProperties().remove("picocli.color.optionParams");
    }

    @Test
    public void testShowDefaultValuesDemo() {
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
                "  -a=<optionA>    ALWAYS shown even if null%n" +
                "                    Default: null%n" +
                "  -b=<optionB>    NEVER shown%n" +
                "  -c=<optionC>    ON_DEMAND hides null%n" +
                "  -d=<optionD>    ON_DEMAND shows non-null%n" +
                "                    Default: abc%n"), result);
    }

    @Test
    public void testCommandWithoutShowDefaultValuesOptionOnDemandNullValue_hidesDefault() {
        @Command()
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use") File file;
            @Option(names = {"-o", "--opt"},  required = true, description = "another option", showDefaultValue = Help.Visibility.ON_DEMAND) File other;
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> -f=<file> -o=<other>%n" +
                "  -f, --file=<file>   the file to use%n" +
                "  -o, --opt=<other>   another option%n"), result);
    }

    @Test
    public void testCommandWithoutShowDefaultValuesOptionOnDemandNonNullValue_hidesDefault() {
        @Command()
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use") File file = new File("/tmp/file");
            @Option(names = {"-o", "--opt"},  required = true, description = "another option", showDefaultValue = Help.Visibility.ON_DEMAND) File other = new File("/tmp/other");
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> -f=<file> -o=<other>%n" +
                "  -f, --file=<file>   the file to use%n" +
                "  -o, --opt=<other>   another option%n"), result);
    }

    @Test
    public void testCommandWithoutShowDefaultValuesOptionAlwaysNullValue_showsNullDefault() {
        @Command()
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use", showDefaultValue = Help.Visibility.ALWAYS)
            File file;
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> -f=<file>%n" +
                "  -f, --file=<file>   the file to use%n" +
                "                        Default: null%n"), result);
    }

    @Test
    public void testCommandWithoutShowDefaultValuesOptionAlwaysNonNullValue_showsDefault() {
        @Command()
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use", showDefaultValue = Help.Visibility.ALWAYS)
            File file = new File("/tmp/file");
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> -f=<file>%n" +
                "  -f, --file=<file>   the file to use%n" +
                "                        Default: %s%n", new File("/tmp/file")), result);
    }

    @Test
    public void testCommandShowDefaultValuesOptionOnDemandNullValue_hidesDefault() {
        @Command(showDefaultValues = true)
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use") File file;
            @Option(names = {"-o", "--opt"},  required = true, description = "another option", showDefaultValue = Help.Visibility.ON_DEMAND) File other;
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> -f=<file> -o=<other>%n" +
                "  -f, --file=<file>   the file to use%n" +
                "  -o, --opt=<other>   another option%n"), result);
    }

    @Test
    public void testCommandShowDefaultValuesOptionNeverNullValue_hidesDefault() {
        @Command(showDefaultValues = true)
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use") File file;
            @Option(names = {"-o", "--opt"},  required = true, description = "another option", showDefaultValue = Help.Visibility.NEVER) File other;
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> -f=<file> -o=<other>%n" +
                "  -f, --file=<file>   the file to use%n" +
                "  -o, --opt=<other>   another option%n"), result);
    }

    @Test
    public void testCommandShowDefaultValuesOptionAlwaysNullValue_showsNullDefault() {
        @Command(showDefaultValues = true)
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use") File file;
            @Option(names = {"-o", "--opt"},  required = true, description = "another option", showDefaultValue = Help.Visibility.ALWAYS) File other;
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> -f=<file> -o=<other>%n" +
                "  -f, --file=<file>   the file to use%n" +
                "  -o, --opt=<other>   another option%n" +
                "                        Default: null%n"), result);
    }

    @Test
    public void testCommandShowDefaultValuesOptionOnDemandNonNullValue_showsDefault() {
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
                "  -f, --file=<file>   the file to use%n" +
                "                        Default: theDefault.txt%n" +
                "  -o, --opt=<other>   another option%n" +
                "                        Default: other.txt%n"), result);
    }

    @Test
    public void testCommandShowDefaultValuesOptionNeverNonNullValue_hidesDefault() {
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
                "  -f, --file=<file>   the file to use%n" +
                "                        Default: theDefault.txt%n" +
                "  -o, --opt=<other>   another option%n"), result);
    }

    @Test
    public void testCommandShowDefaultValuesOptionAlwaysNonNullValue_hidesDefault() {
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
                "  -f, --file=<file>   the file to use%n" +
                "                        Default: theDefault.txt%n" +
                "  -o, --opt=<other>   another option%n" +
                "                        Default: other.txt%n"), result);
    }

    @Test
    public void testCommandShowDefaultValuesOptionOnDemandArrayField() {
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
                "  -x, --array=<array>   the array%n" +
                "                          Default: [1, 5, 11, 23]%n" +
                "  -y, --other=<other>   the other%n" +
                "                          Default: [1, 5, 11, 23]%n"), result);
    }

    @Test
    public void testCommandShowDefaultValuesOptionNeverArrayField_hidesDefault() {
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
                "  -x, --array=<array>   the array%n" +
                "                          Default: [1, 5, 11, 23]%n" +
                "  -y, --other=<other>   the other%n"), result);
    }

    @Test
    public void testCommandShowDefaultValuesOptionAlwaysNullArrayField_showsNull() {
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
                "  -x, --array=<array>   the array%n" +
                "  -y, --other=<other>   the other%n" +
                "                          Default: null%n"), result);
    }
    @Test
    public void testCommandShowDefaultValuesVariableForArrayField() {
        @Command
        class Params {
            @Option(names = {"-x", "--array"}, required = true, description = "null array: Default: ${DEFAULT-VALUE}")
            int[] nil;
            @Option(names = {"-y", "--other"}, required = true, description = "non-null: Default: ${DEFAULT-VALUE}")
            int[] other = {1, 5, 11, 23};
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> -x=<nil> [-x=<nil>]... -y=<other> [-y=<other>]...%n" +
                "  -x, --array=<nil>     null array: Default: null%n" +
                "  -y, --other=<other>   non-null: Default: [1, 5, 11, 23]%n"), result);
    }
    @Test
    public void testOptionSpec_defaultValue_overwritesInitialValue() {
        @Command(showDefaultValues = true)
        class Params {
            @Option(names = {"-x", "--array"}, required = true, paramLabel = "INT", description = "the array")
            int[] array = {1, 5, 11, 23};
        }
        CommandLine cmd = new CommandLine(new Params());
        OptionSpec x = cmd.getCommandSpec().posixOptionsMap().get('x').toBuilder().defaultValue("5,4,3,2,1").splitRegex(",").build();

        cmd = new CommandLine(CommandSpec.create().addOption(x));
        cmd.getCommandSpec().usageMessage().showDefaultValues(true);
        String result = usageString(cmd, Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> [-x=INT[,INT...]]...%n" +
                "  -x, --array=INT[,INT...]   the array%n" +
                "                               Default: 5,4,3,2,1%n"), result);
    }

    @Test
    public void testCommandWithoutShowDefaultValuesPositionalOnDemandNullValue_hidesDefault() {
        @Command()
        class Params {
            @Parameters(index = "0", description = "the file to use") File file;
            @Parameters(index = "1", description = "another option", showDefaultValue = Help.Visibility.ON_DEMAND) File other;
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> <file> <other>%n" +
                "      <file>    the file to use%n" +
                "      <other>   another option%n"), result);
    }

    @Test
    public void testCommandWithoutShowDefaultValuesPositionalOnDemandNonNullValue_hidesDefault() {
        @Command()
        class Params {
            @Parameters(index = "0", description = "the file to use") File file = new File("/tmp/file");
            @Parameters(index = "1", description = "another option", showDefaultValue = Help.Visibility.ON_DEMAND) File other = new File("/tmp/other");
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> <file> <other>%n" +
                "      <file>    the file to use%n" +
                "      <other>   another option%n"), result);
    }

    @Test
    public void testCommandWithoutShowDefaultValuesPositionalAlwaysNullValue_showsNullDefault() {
        @Command()
        class Params {
            @Parameters(index = "0", description = "the file to use", showDefaultValue = Help.Visibility.ALWAYS)
            File file;
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> <file>%n" +
                "      <file>   the file to use%n" +
                "                 Default: null%n"), result);
    }

    @Test
    public void testCommandWithoutShowDefaultValuesPositionalAlwaysNonNullValue_showsDefault() {
        @Command()
        class Params {
            @Parameters(index = "0", description = "the file to use", showDefaultValue = Help.Visibility.ALWAYS)
            File file = new File("/tmp/file");
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> <file>%n" +
                "      <file>   the file to use%n" +
                "                 Default: %s%n", new File("/tmp/file")), result);
    }

    @Test
    public void testCommandShowDefaultValuesPositionalOnDemandNullValue_hidesDefault() {
        @Command(showDefaultValues = true)
        class Params {
            @Parameters(index = "0", description = "the file to use") File file;
            @Parameters(index = "1", description = "another option", showDefaultValue = Help.Visibility.ON_DEMAND) File other;
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> <file> <other>%n" +
                "      <file>    the file to use%n" +
                "      <other>   another option%n"), result);
    }

    @Test
    public void testCommandShowDefaultValuesPositionalNeverNullValue_hidesDefault() {
        @Command(showDefaultValues = true)
        class Params {
            @Parameters(index = "0", description = "the file to use") File file;
            @Parameters(index = "1", description = "another option", showDefaultValue = Help.Visibility.NEVER) File other;
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> <file> <other>%n" +
                "      <file>    the file to use%n" +
                "      <other>   another option%n"), result);
    }

    @Test
    public void testCommandShowDefaultValuesPositionalAlwaysNullValue_showsNullDefault() {
        @Command(showDefaultValues = true)
        class Params {
            @Parameters(index = "0", description = "the file to use") File file;
            @Parameters(index = "1", description = "another option", showDefaultValue = Help.Visibility.ALWAYS) File other;
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> <file> <other>%n" +
                "      <file>    the file to use%n" +
                "      <other>   another option%n" +
                "                  Default: null%n"), result);
    }

    @Test
    public void testCommandShowDefaultValuesPositionalOnDemandNonNullValue_showsDefault() {
        @Command(showDefaultValues = true)
        class Params {
            @Parameters(index = "0", description = "the file to use")
            File file = new File("theDefault.txt");
            @Parameters(index = "1", description = "another option", showDefaultValue = Help.Visibility.ON_DEMAND)
            File other = new File("other.txt");
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> <file> <other>%n" +
                "      <file>    the file to use%n" +
                "                  Default: theDefault.txt%n" +
                "      <other>   another option%n" +
                "                  Default: other.txt%n"), result);
    }

    @Test
    public void testCommandShowDefaultValuesPositionalNeverNonNullValue_hidesDefault() {
        @Command(showDefaultValues = true)
        class Params {
            @Parameters(index = "0", description = "the file to use")
            File file = new File("theDefault.txt");
            @Parameters(index = "1", description = "another option", showDefaultValue = Help.Visibility.NEVER)
            File other = new File("other.txt");
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> <file> <other>%n" +
                "      <file>    the file to use%n" +
                "                  Default: theDefault.txt%n" +
                "      <other>   another option%n"), result);
    }

    @Test
    public void testCommandShowDefaultValuesPositionalAlwaysNonNullValue_hidesDefault() {
        @Command(showDefaultValues = true)
        class Params {
            @Parameters(index = "0", description = "the file to use")
            File file = new File("theDefault.txt");
            @Parameters(index = "1", description = "another option", showDefaultValue = Help.Visibility.ALWAYS)
            File other = new File("other.txt");
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> <file> <other>%n" +
                "      <file>    the file to use%n" +
                "                  Default: theDefault.txt%n" +
                "      <other>   another option%n" +
                "                  Default: other.txt%n"), result);
    }

    @Test
    public void testPositionalParamSpec_defaultValue_overwritesInitialValue() {
        @Command(showDefaultValues = true)
        class Params {
            @Parameters(paramLabel = "INT", description = "the array")
            int[] value = {1, 5, 11, 23};
        }
        CommandLine cmd = new CommandLine(new Params());
        PositionalParamSpec x = cmd.getCommandSpec().positionalParameters().get(0).toBuilder().defaultValue("5,4,3,2,1").splitRegex(",").build();

        cmd = new CommandLine(CommandSpec.create().add(x));
        cmd.getCommandSpec().usageMessage().showDefaultValues(true);
        String result = usageString(cmd, Help.Ansi.OFF);
        assertEquals(format("" +
                "Usage: <main class> [INT[,INT...]...]%n" +
                "      [INT[,INT...]...]   the array%n" +
                "                            Default: 5,4,3,2,1%n"), result);
    }

    @Test
    public void testUsageSeparatorWithoutDefault() {
        @Command()
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use") File file = new File("def.txt");
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                        "Usage: <main class> -f=<file>%n" +
                        "  -f, --file=<file>   the file to use%n"), result);
    }

    @Test
    public void testUsageSeparator() {
        @Command(showDefaultValues = true)
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use") File file = new File("def.txt");
        }
        String result = usageString(new Params(), Help.Ansi.OFF);
        assertEquals(format("" +
                        "Usage: <main class> -f=<file>%n" +
                        "  -f, --file=<file>   the file to use%n" +
                        "                        Default: def.txt%n"), result);
    }

    @Test
    public void testUsageParamLabels() {
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
                        "      NUM         number param%n" +
                        "      <host>      the host parameter%n" +
                        "  -f=FILE         files%n" +
                        "  -n=<number>     a number option%n" +
                        "  -P=KEY=VALUE    Project properties (key-value pairs)%n"), result);
    }

    @Test
    public void testUsageParamLabelsWithLongMapOptionName() {
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
                "      NUM        number param%n" +
                "      <host>     the host parameter%n" +
                "  -f=FILE        a file%n" +
                "  -n=<number>    a number option%n" +
                "  -P, --properties=KEY=VALUE%n" +
                "                 Project properties (key-value pairs)%n"), result);
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
                "Usage: <main class> -a=ARG [-a=ARG]... -b[=ARG...] [-b[=ARG...]]... -c=ARG...%n" +
                "                    [-c=ARG...]... -d=ARG ARG... [-d=ARG ARG...]...%n" +
                "  -a=ARG%n" +
                "  -b=[ARG...]%n" +
                "  -c=ARG...%n" +
                "  -d=ARG ARG...%n");
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
                "Usage: <main class> [-a=ARG]... [-b[=ARG...]]... [-c=ARG...]... [-d=ARG%n" +
                "                    ARG...]...%n" +
                "  -a=ARG%n" +
                "  -b=[ARG...]%n" +
                "  -c=ARG...%n" +
                "  -d=ARG ARG...%n");
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
                "  -a=[ARG]%n" +
                "  -b=ARG [ARG]%n" +
                "  -c=ARG [ARG [ARG]]%n" +
                "  -d=ARG ARG [ARG [ARG]]%n");
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
                "  -a=[ARG]%n" +
                "  -b=ARG [ARG]%n" +
                "  -c=ARG [ARG [ARG]]%n" +
                "  -d=ARG ARG [ARG [ARG]]%n");
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
                "  -a=ARG%n" +
                "  -b%n" +
                "  -c=ARG%n" +
                "  -d=ARG ARG%n");
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
                "  -a=ARG%n" +
                "  -b%n" +
                "  -c=ARG%n" +
                "  -d=ARG ARG%n");
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
                "Usage: <main class> --aa=ARG [--aa=ARG]... --bb[=ARG...] [--bb[=ARG...]]...%n" +
                "                    --cc=ARG... [--cc=ARG...]... --dd=ARG ARG... [--dd=ARG%n" +
                "                    ARG...]...%n" +
                "      --aa=ARG%n" +
                "      --bb[=ARG...]%n" +
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
                "Usage: <main class> [--aa=ARG]... [--bb[=ARG...]]... [--cc=ARG...]... [--dd=ARG%n" +
                "                    ARG...]...%n" +
                "      --aa=ARG%n" +
                "      --bb[=ARG...]%n" +
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
                "                             foobar%n");
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
                "                             foobar%n");
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
                "Usage: <main class> -a=KEY=VAL [-a=KEY=VAL]... -b[=<String=String>...] [-b%n" +
                "                    [=<String=String>...]]... -c=<String=TimeUnit>...%n" +
                "                    [-c=<String=TimeUnit>...]... -d=<Integer=URL>%n" +
                "                    <Integer=URL>... [-d=<Integer=URL> <Integer=URL>...]...%n" +
                "  -a=KEY=VAL%n" +
                "  -b=[<String=String>...]%n" +
                "  -c=<String=TimeUnit>...%n" +
                "  -d=<Integer=URL> <Integer=URL>...%n" +
                "                             description%n");
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
                "Usage: <main class> [-a=<String=String>]... [-b[=<Integer=Integer>...]]...%n" +
                "                    [-c=KEY=VALUE...]... [-d=<String=URL> <String=URL>...]...%n" +
                "  -a=<String=String>%n" +
                "  -b=[<Integer=Integer>...]%n" +
                "%n" + // TODO
                "  -c=KEY=VALUE...%n" +
                "  -d=<String=URL> <String=URL>...%n" +
                "                        description%n");
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
                "  -a=[<String=String>]    a description%n" +
                "  -b=<Integer=Integer> [<Integer=Integer>]%n" +
                "                          b description%n" +
                "  -c=<String=URL> [<String=URL> [<String=URL>]]%n" +
                "                          c description%n" +
                "  -d=K=URL K=URL [K=URL [K=URL]]%n" +
                "                          d description%n");
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
                "  -a=[<UUID=URL>]           a description%n" +
                "  -b=<Long=UUID> [<Long=UUID>]%n" +
                "                            b description%n" +
                "  -c=<String=String> [<String=String> [<String=String>]]%n" +
                "                            c description%n" +
                "  -d=K=V K=V [K=V [K=V]]    d description%n");
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
                "  -a=<Short=Field>          a description%n" +
                "  -b                        b description%n" +
                "  -c=<Long=File>            c description%n" +
                "  -d=<URI=URL> <URI=URL>    d description%n");
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
                "  -a=<Short=Field>          a description%n" +
                "  -b                        b description%n" +
                "  -c=<Long=File>            c description%n" +
                "  -d=<URI=URL> <URI=URL>    d description%n");
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
                "Usage: <main class> [APARAM...] [<b>...] <c>... <d> <d>...%n" +
                "      [APARAM...]   APARAM description%n" +
                "      [<b>...]      b description%n" +
                "      <c>...        c description%n" +
                "      <d> <d>...    d description%n");
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
                "      [PARAMA]          PARAMA description%n" +
                "      PARAMB [PARAMB]   PARAMB description%n" +
                "      PARAMC [PARAMC [PARAMC]]%n" +
                "                        PARAMC description%n" +
                "      PARAMD PARAMD [PARAMD [PARAMD]]%n" +
                "                        PARAMD description%n");
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
                "Usage: <main class>  [<a>...] <c> <d> <d>%n" +
                "                 b description (arity=0)%n" +
                "      [<a>...]   a description (default arity)%n" +
                "      <c>        b description (arity=1)%n" +
                "      <d> <d>    b description (arity=2)%n");
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
                "Usage: <main class> [<String=String>...] [<Integer=Integer>...] KEY=VALUE...%n" +
                "                    <String=URL> <String=URL>...%n" +
                "      [<String=String>...]%n" +
                "      [<Integer=Integer>...] a description (arity=0..*)%n" +
                "      KEY=VALUE...%n" +
                "      <String=URL> <String=URL>...%n" +
                "                             description%n");
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
                "      [<UUID=URL>]          a description%n" +
                "      <Long=UUID> [<Long=UUID>]%n" +
                "                            b description%n" +
                "      <String=String> [<String=String> [<String=String>]]%n" +
                "                            c description%n" +
                "      K=V K=V [K=V [K=V]]   d description%n");
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
                "Usage: <main class>  [<Short=Field>...] <Long=File> <URI=URL> <URI=URL>%n" +
                "                            b description (arity=0)%n" +
                "      [<Short=Field>...]    a description%n" +
                "      <Long=File>           c description%n" +
                "      <URI=URL> <URI=URL>   d description%n");
        //CommandLine.usage(new Args(), System.out);
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
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
    public void testSortByShortestOptionNameAlphabetically_handlesNulls() throws Exception {
        Help.SortByShortestOptionNameAlphabetically sort = new Help.SortByShortestOptionNameAlphabetically();
        OptionSpec a = OptionSpec.builder("-a").build();
        OptionSpec b = OptionSpec.builder("-b").build();
        assertEquals(1, sort.compare(null, a));
        assertEquals(-1, sort.compare(a, null));
        assertEquals(-1, sort.compare(a, b));
        assertEquals(0, sort.compare(a, a));
        assertEquals(1, sort.compare(b, a));
    }

    @Test
    public void testSortByOptionOrder() throws Exception {
        class App {
            @Option(names = {"-a"}, order = 9) boolean a;
            @Option(names = {"-b"}, order = 8) boolean b;
            @Option(names = {"-c"}, order = 7) boolean c;
            @Option(names = {"-d"}, order = 6) int d;
            @Option(names = {"-e"}, order = 5) String[] e;
            @Option(names = {"-f"}, order = 4) String[] f;
            @Option(names = {"-g"}, order = 3) boolean g;
        }
        OptionSpec[] fields = options(new App(), "a", "b", "c", "d", "e", "f", "g");
        Arrays.sort(fields, Help.createOrderComparator());
        OptionSpec[] expected = options(new App(), "g", "f", "e", "d", "c", "b", "a");
        assertArrayEquals(expected, fields);
    }

    @Test
    public void testSortByOptionOrderAllowsGaps() throws Exception {
        @Command(sortOptions = false)
        class App {
            @Option(names = {"-a"}, order = 9) boolean a;
            @Option(names = {"-b"}, order = 8) boolean b;
            @Option(names = {"-c"}, order = 7) boolean c;
            @Option(names = {"-d"}, order = 6) int d;
            @Option(names = {"-e"}, order = 3) String[] e;
            @Option(names = {"-f"}, order = 1) String[] f;
            @Option(names = {"-g"}, order = 0) boolean g;
        }
        OptionSpec[] fields = options(new App(), "a", "b", "c", "d", "e", "f", "g");
        Arrays.sort(fields, Help.createOrderComparator());
        OptionSpec[] expected = options(new App(), "g", "f", "e", "d", "c", "b", "a");
        assertArrayEquals(expected, fields);

        String expectedUsage = String.format("" +
                "Usage: <main class> [-abcg] [-d=<d>] [-e=<e>]... [-f=<f>]...%n" +
                "  -g%n" +
                "  -f=<f>%n" +
                "  -e=<e>%n" +
                "  -d=<d>%n" +
                "  -c%n" +
                "  -b%n" +
                "  -a%n");
        assertEquals(expectedUsage, new CommandLine(new App()).getUsageMessage());
    }

    @Test
    public void testSortByOptionOrderStableSortWhenEqualOrder() throws Exception {
        @Command(sortOptions = false)
        class App {
            @Option(names = {"-a"}, order = 9) boolean a;
            @Option(names = {"-b"}, order = 8) boolean b;
            @Option(names = {"-c"}, order = 7) boolean c;
            @Option(names = {"-d"}, order = 7) int d;
            @Option(names = {"-e"}, order = 7) String[] e;
            @Option(names = {"-f"}, order = 7) String[] f;
            @Option(names = {"-g"}, order = 0) boolean g;
        }
        OptionSpec[] fields = options(new App(), "a", "b", "c", "d", "e", "f", "g");
        Arrays.sort(fields, Help.createOrderComparator());
        OptionSpec[] expected = options(new App(), "g", "c", "d", "e", "f", "b", "a");
        assertArrayEquals(expected, fields);

        String expectedUsage = String.format("" +
                "Usage: <main class> [-abcg] [-d=<d>] [-e=<e>]... [-f=<f>]...%n" +
                "  -g%n" +
                "  -c%n" +
                "  -d=<d>%n" +
                "  -e=<e>%n" +
                "  -f=<f>%n" +
                "  -b%n" +
                "  -a%n");
        assertEquals(expectedUsage, new CommandLine(new App()).getUsageMessage());
    }

    @Test
    public void testSortDeclarationOrderWhenOrderAttributeOmitted() {
        @Command(sortOptions = false)
        class App {
            @Option(names = {"-a"}) boolean a;
            @Option(names = {"-b"}) boolean b;
            @Option(names = {"-c"}) boolean c;
            @Option(names = {"-d"}) int d;
            @Option(names = {"-e"}) String[] e;
            @Option(names = {"-f"}) String[] f;
            @Option(names = {"-g"}) boolean g;
        }

        String expectedUsage = String.format("" +
                "Usage: <main class> [-abcg] [-d=<d>] [-e=<e>]... [-f=<f>]...%n" +
                "  -a%n" +
                "  -b%n" +
                "  -c%n" +
                "  -d=<d>%n" +
                "  -e=<e>%n" +
                "  -f=<f>%n" +
                "  -g%n");
        assertEquals(expectedUsage, new CommandLine(new App()).getUsageMessage());
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
        Help help = new Help(new Example(), Help.Ansi.ON);
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
    public void testUsageOptions_hideParamSyntax_on() {
        class App {
            @Option(names = "-x1") String single;
            @Option(names = "-s1", arity = "2") String[] multi;
            @Option(names = "-x2", hideParamSyntax = true) String singleHide;
            @Option(names = "-s2", hideParamSyntax = true, arity = "2") String[] multiHide;
            @Option(names = "-o3", hideParamSyntax = false, split = ",") String[] multiSplit;
            @Option(names = "-s3", hideParamSyntax = true, split = ",") String[] multiHideSplit;
        }
        String actual = new CommandLine(new App()).getUsageMessage(Help.Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> [-x1=<single>] [-x2=<singleHide>] [-o3=<multiSplit>[,%n" +
                "                    <multiSplit>...]]... [-s3=<multiHideSplit>]... [-s1=<multi>%n" +
                "                    <multi>]... [-s2=<multiHide>]...%n" +
                "      -o3=<multiSplit>[,<multiSplit>...]%n" +
                "%n" +
                "      -s1=<multi> <multi>%n" +
                "      -s2=<multiHide>%n" +
                "      -s3=<multiHideSplit>%n" +
                "      -x1=<single>%n" +
                "      -x2=<singleHide>%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testUsageParameters_hideParamSyntax_on() {
        class App {
            @Parameters() String single;
            @Parameters(arity = "2") String[] multi;
            @Parameters(hideParamSyntax = true) String singleHide;
            @Parameters(hideParamSyntax = true, arity = "2") String[] multiHide;
            @Parameters(hideParamSyntax = false, split = ",") String[] multiSplit;
            @Parameters(hideParamSyntax = true, split = ",") String[] multiHideSplit;
        }
        String actual = new CommandLine(new App()).getUsageMessage(Help.Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> [<multiSplit>[,<multiSplit>...]...] <multiHideSplit>%n" +
                "                    <single> <singleHide> (<multi> <multi>)... <multiHide>%n" +
                "      [<multiSplit>[,<multiSplit>...]...]%n" +
                "%n" +
                "      <multiHideSplit>%n" +
                "      <single>%n" +
                "      <singleHide>%n" +
                "      (<multi> <multi>)...%n" +
                "      <multiHide>%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testDefaultParameterRenderer_hideParamSyntax_on() {
        class App {
            @Parameters(index = "0") String single;
            @Parameters(index = "1", arity = "2") String[] multi;
            @Parameters(index = "2", hideParamSyntax = true) String singleHide;
            @Parameters(index = "3", hideParamSyntax = true,  arity = "2") String[] multiHide;
            @Parameters(index = "4", hideParamSyntax = false, arity = "*", split = ",") String[] multiSplit;
            @Parameters(index = "5", hideParamSyntax = true,  arity = "*", split = ",") String[] multiHideSplit;
        }
        Help withLabel = new Help(new App(), Help.Ansi.OFF);
        withLabel.commandSpec().parser().separator("=");
        Help.IParamLabelRenderer equals = withLabel.createDefaultParamLabelRenderer();
        withLabel.commandSpec().parser().separator(" ");
        Help.IParamLabelRenderer spaced = withLabel.createDefaultParamLabelRenderer();

        String[] expected = new String[] {
                "<single>", //
                "<multi> <multi>", //
                "<singleHide>", //
                "<multiHide>", //
                "[<multiSplit>[,<multiSplit>...]...]", //
                "<multiHideSplit>", //
        };
        for (int i = 0; i < expected.length; i++) {
            Text withEquals = equals.renderParameterLabel(withLabel.positionalParameters().get(i), withLabel.ansi(), Collections.<IStyle>emptyList());
            Text withSpace = spaced.renderParameterLabel(withLabel.positionalParameters().get(i), withLabel.ansi(), Collections.<IStyle>emptyList());
            assertEquals(withEquals.toString(), expected[i], withEquals.toString());
            assertEquals(withSpace.toString(), expected[i], withSpace.toString());
        }
    }

    @Test
    public void testDefaultLayout_addsEachRowToTable() {
        final Text[][] values = {
                textArray(Help.Ansi.OFF, "a", "b", "c", "d"),
                textArray(Help.Ansi.OFF, "1", "2", "3", "4")
        };
        final int[] count = {0};
        TextTable tt = TextTable.forDefaultColumns(Help.Ansi.OFF, UsageMessageSpec.DEFAULT_USAGE_WIDTH);
        tt = new TextTable(Help.Ansi.OFF, tt.columns()) {
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
        Help help = new Help(new App(), Help.Ansi.ON);
        assertEquals(Help.Ansi.ON.new Text("@|bold <main class>|@ [OPTIONS]" + LINESEP).toString(), help.synopsis(0));
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
                "Usage: aprogram [OPTIONS] [<files>...]%n" +
                "      [<files>...]%n" +
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
                "Usage: anotherProgram [OPTIONS] [<files>...]%n" +
                "      [<files>...]%n" +
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
                "Usage: aprogram [-v] [-c=<count>] [<files>...]%n" +
                "      [<files>...]%n" +
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
                "Usage: anotherProgram [-v] [-c=<count>] [<files>...]%n" +
                "      [<files>...]%n" +
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
        Help help = new Help(new App(), Help.Ansi.ON);
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
        Help help = new Help(new App(), Help.Ansi.ON);
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
        Help help = new Help(new App(), Help.Ansi.ON);
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
        assertEquals("<main class> [-v] [-c[=<count>...]]" + LINESEP, help.synopsis(0));
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
                "Usage: <main class> [-v] [-c:<count>] [<files>...]%n" +
                "      [<files>...]%n" +
                "  -c, --count:<count>%n" +
                "  -v, --verbose%n";
        assertEquals(String.format(expected), actual);
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
        TextTable table = TextTable.forDefaultColumns(Help.Ansi.OFF, UsageMessageSpec.DEFAULT_USAGE_WIDTH);
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
        TextTable table = TextTable.forDefaultColumns(Help.Ansi.OFF, UsageMessageSpec.DEFAULT_USAGE_WIDTH);
        table.addRowValues(textArray(Help.Ansi.OFF, "", "-c", ",", "--create", "description", "INVALID", "Row 3"));
//        assertEquals(String.format("" +
//                        "  -c, --create                description                                       %n" +
//                        "                                INVALID                                         %n" +
//                        "                                Row 3                                           %n"
//                ,""), table.toString(new StringBuilder()).toString());
    }

    @Test
    public void testTextTableAddsNewRowWhenAnyColumnTooLong() {
        TextTable table = TextTable.forDefaultColumns(Help.Ansi.OFF, UsageMessageSpec.DEFAULT_USAGE_WIDTH);
        table.addRowValues("*", "-c", ",",
                "--create, --create2, --create3, --create4, --create5, --create6, --create7, --create8",
                "description");
        assertEquals(String.format("" +
                        "* -c, --create, --create2, --create3, --create4, --create5, --create6, --create7,%n" +
                        "        --create8%n" +
                        "                              description%n"
                ,""), table.toString(new StringBuilder()).toString());

        table = TextTable.forDefaultColumns(Help.Ansi.OFF, UsageMessageSpec.DEFAULT_USAGE_WIDTH);
        table.addRowValues("", "-c", ",",
                "--create, --create2, --create3, --create4, --create5, --create6, --createAA7, --create8",
                "description");
        assertEquals(String.format("" +
                        "  -c, --create, --create2, --create3, --create4, --create5, --create6, --createAA7,%n" +
                        "        --create8%n" +
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
                        "  -A, --show-all           equivalent to -vET%n" +
                        "  -b, --number-nonblank    number nonempty output lines, overrides -n%n" +
                        "  -e                       equivalent to -vET%n" +
                        "  -E, --show-ends          display $ at end of each line%n" +
                        "  -n, --number             number all output lines%n" +
                        "  -s, --squeeze-blank      suppress repeated empty output lines%n" +
                        "  -t                       equivalent to -vT%n" +
                        "  -T, --show-tabs          display TAB characters as ^I%n" +
                        "  -u                       (ignored)%n" +
                        "  -v, --show-nonprinting   use ^ and M- notation, except for LDF and TAB%n" +
                        "      --help               display this help and exit%n" +
                        "      --version            output version information and exit%n" +
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
                        "                    [<files>...]%n" +
                        "      <host1>        source host%n" +
                        "      <port1>        source port%n" +
                        "      <host2>        destination host%n" +
                        "      <port2range> [<port2range>]%n" +
                        "                     destination port range%n" +
                        "      [<files>...]   files to transfer%n"
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
        assertEquals(String.format("base [COMMAND]%n"), help.abbreviatedSynopsis());
        assertEquals(String.format("base [COMMAND]%n"), help.detailedSynopsis(0, null, true));
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
    public void testSubclassedCommandHelp() {
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
                "Usage: main [-ah] [COMMAND]%n" +
                "  -a%n" +
                "  -h%n" +
                "Commands:%n" +
                "  cmd1%n" +
                "  cmd2%n"), main);

        String cmd2 = usageString(commandLine.getSubcommands().get("cmd2"), Help.Ansi.OFF);
        assertEquals(String.format("" +
                "Usage: main cmd2 [-ch] [COMMAND]%n" +
                "  -c%n" +
                "  -h%n" +
                "Commands:%n" +
                "  sub21%n" +
                "  sub22%n"), cmd2);

        String sub22 = usageString(commandLine.getSubcommands().get("cmd2").getSubcommands().get("sub22"), Help.Ansi.OFF);
        assertEquals(String.format("" +
                "Usage: main cmd2 sub22 [-g] [COMMAND]%n" +
                "  -g%n" +
                "Commands:%n" +
                "  sub22sub1%n"), sub22);
    }

    @Test
    public void testLayoutConstructorCreatesDefaultColumns() {
        ColorScheme colorScheme = new ColorScheme();
        Help.Layout layout = new Help.Layout(colorScheme, 99);

        TextTable expected = TextTable.forDefaultColumns(Help.Ansi.OFF, 99);
        assertEquals(expected.columns().length, layout.table.columns().length);
        for (int i = 0; i < expected.columns().length; i++) {
            assertEquals(expected.columns()[i].indent, layout.table.columns()[i].indent);
            assertEquals(expected.columns()[i].width, layout.table.columns()[i].width);
            assertEquals(expected.columns()[i].overflow, layout.table.columns()[i].overflow);
        }
    }

    @Test
    public void testHelpCreateLayout_CreatesDefaultColumns() {
        Help help = new Help(CommandSpec.create(), new ColorScheme(Help.Ansi.OFF));
        Help.Layout layout = help.createDefaultLayout();

        TextTable expected = TextTable.forDefaultColumns(Help.Ansi.OFF, 80);
        assertEquals(expected.columns().length, layout.table.columns().length);
        for (int i = 0; i < expected.columns().length; i++) {
            assertEquals(expected.columns()[i].indent, layout.table.columns()[i].indent);
            assertEquals(expected.columns()[i].width, layout.table.columns()[i].width);
            assertEquals(expected.columns()[i].overflow, layout.table.columns()[i].overflow);
        }
    }

    @Test
    public void testMinimalParameterLabelRenderer() {
        Help.IParamLabelRenderer renderer = Help.createMinimalParamLabelRenderer();
        assertEquals("", renderer.separator());
    }

    @Test
    public void testMinimalOptionRenderer() {
        Help.MinimalOptionRenderer renderer = new Help.MinimalOptionRenderer();
        Text[][] texts = renderer.render(OptionSpec.builder("-x").build(),
                Help.createMinimalParamLabelRenderer(), new ColorScheme());
        assertEquals("", texts[0][1].plainString());
    }

    @Test
    public void testMinimalParameterRenderer() {
        Help.MinimalParameterRenderer renderer = new Help.MinimalParameterRenderer();
        Text[][] texts = renderer.render(PositionalParamSpec.builder().build(),
                Help.createMinimalParamLabelRenderer(), new ColorScheme());
        assertEquals("", texts[0][1].plainString());
    }

    @Test
    public void testTextTableConstructorRequiresAtLeastOneColumn() {
        try {
            new TextTable(Help.Ansi.OFF, new Help.Column[0]);
        } catch (IllegalArgumentException ex) {
            assertEquals("At least one column is required", ex.getMessage());
        }
    }

    @Test
    public void testTextTablePutValue_DisallowsInvalidRowIndex() {
        TextTable tt = new TextTable(Help.Ansi.OFF, new Help.Column[] {new Help.Column(30, 2, Help.Column.Overflow.SPAN)});
        try {
            tt.putValue(1, 0, Help.Ansi.OFF.text("abc"));
        } catch (IllegalArgumentException ex) {
            assertEquals("Cannot write to row 1: rowCount=0", ex.getMessage());
        }
    }

    @Test
    public void testTextTablePutValue_NullOrEmpty() {
        TextTable tt = new TextTable(Help.Ansi.OFF, new Help.Column[] {new Help.Column(30, 2, Help.Column.Overflow.SPAN)});
        tt.addEmptyRow();

        TextTable.Cell cell00 = tt.putValue(0, 0, null);
        assertEquals(0, cell00.column);
        assertEquals(0, cell00.row);

        TextTable.Cell other00 = tt.putValue(0, 0, Help.Ansi.EMPTY_TEXT);
        assertEquals(0, other00.column);
        assertEquals(0, other00.row);
    }

    @Test
    public void testTextTableAddRowValues() {
        TextTable tt = new TextTable(Help.Ansi.OFF, new Help.Column[] {new Help.Column(30, 2, Help.Column.Overflow.SPAN)});
        tt.addRowValues(new String[] {null});
        assertEquals(Help.Ansi.EMPTY_TEXT, tt.textAt(0, 0));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testTextTableCellAt() {
        TextTable tt = new TextTable(Help.Ansi.OFF, new Help.Column[] {new Help.Column(30, 2, Help.Column.Overflow.SPAN)});
        tt.addRowValues(new String[] {null});
        assertEquals(Help.Ansi.EMPTY_TEXT, tt.cellAt(0, 0));
    }

    @Test
    public void testJoin() throws Exception {
        Method m = Help.class.getDeclaredMethod("join", String[].class, int.class, int.class, String.class);
        m.setAccessible(true);

        String result = (String) m.invoke(null, (String[]) null, 0, 0, "abc");
        assertEquals("", result);
    }

    @Test
    public void testFormat() throws Exception {
        Method m = CommandLine.class.getDeclaredMethod("format", String.class, Object[].class);
        m.setAccessible(true);

        String result = (String) m.invoke(null, (String) null, new Object[]{"abc"});
        assertEquals("", result);
    }

    @Test
    public void testJoin2() {
        StringBuilder sb = Help.join(Help.Ansi.OFF, 80, null, new StringBuilder("abc"));
        assertEquals("abc", sb.toString());
    }

    @Test
    public void testCountTrailingSpaces() throws Exception {
        Method m = Help.class.getDeclaredMethod("countTrailingSpaces", String.class);
        m.setAccessible(true);

        int result = (Integer) m.invoke(null, (String) null);
        assertEquals(0, result);
    }

    @Test
    public void testHeading() throws Exception {
        Method m = Help.class.getDeclaredMethod("heading", Help.Ansi.class, int.class, String.class, Object[].class);
        m.setAccessible(true);

        String result = (String) m.invoke(null, Help.Ansi.OFF, 80, "\r\n", new Object[0]);
        assertEquals(String.format("%n"), result);

        String result2 = (String) m.invoke(null, Help.Ansi.OFF, 80, "boom", new Object[0]);
        assertEquals(String.format("boom"), result2);

        String result3 = (String) m.invoke(null, Help.Ansi.OFF, 80, null, new Object[0]);
        assertEquals("", result3);
    }
    @Test
    public void trimTrailingLineSeparator() {
        assertEquals("abc", Help.trimLineSeparator("abc"));
        String lineSep = System.getProperty("line.separator");
        assertEquals("abc", Help.trimLineSeparator("abc" + lineSep));
        assertEquals("abc" + lineSep, Help.trimLineSeparator("abc" + lineSep + lineSep));
    }

    @Test
    public void testHelpCreateDetailedSynopsisOptionsText() {
        Help help = new Help(CommandSpec.create().addOption(OptionSpec.builder("xx").build()),
                new ColorScheme(Help.Ansi.OFF));
        Text text = help.createDetailedSynopsisOptionsText(null, true);
        assertEquals(" [xx]", text.toString());
    }

    @Test
    public void testAddAllSubcommands() {
        Help help = new Help(CommandSpec.create(), new Help.ColorScheme(Help.Ansi.OFF));
        help.addAllSubcommands(null);
        assertTrue(help.subcommands().isEmpty());
    }
    @SuppressWarnings("deprecation")
    @Test
    public void testDetailedSynopsis() {
        Help help = new Help(CommandSpec.create(), new Help.ColorScheme(Help.Ansi.OFF));
        String str = help.detailedSynopsis(new Help.SortByShortestOptionNameAlphabetically(), true);
        assertEquals(String.format("<main class>%n"), str);
    }

    @Test
    public void testCreateDescriptionFirstLines() throws Exception {
        Method m = Help.class.getDeclaredMethod("createDescriptionFirstLines",
                Help.ColorScheme.class, Model.ArgSpec.class, String[].class, boolean[].class);
        m.setAccessible(true);

        String[][] input = new String[][] {
                new String[0],
                new String[] {""},
                new String[] {"a", "b", "c"}
        };
        Help.Ansi.Text[][] expectedOutput = new Help.Ansi.Text[][] {
                new Help.Ansi.Text[] {Help.Ansi.OFF.text("")},
                new Help.Ansi.Text[] {Help.Ansi.OFF.text("")},
                new Help.Ansi.Text[] {Help.Ansi.OFF.text("a"), Help.Ansi.OFF.text("b"), Help.Ansi.OFF.text("c")}
        };
        for (int i = 0; i < input.length; i++) {
            String[] description = input[i];
            Help.Ansi.Text[] result = (Help.Ansi.Text[]) m.invoke(null, new Help.ColorScheme(Help.Ansi.OFF), null, description, new boolean[3]);
            Help.Ansi.Text[] expected = expectedOutput[i];

            for (int j = 0; j < result.length; j++) {
                assertEquals(expected[j], result[j]);
            }
        }
    }

    @Test
    public void testAbbreviatedSynopsis() {
        CommandSpec spec = CommandSpec.create();
        spec.addPositional(PositionalParamSpec.builder().paramLabel("a").hidden(true).build());
        spec.addPositional(PositionalParamSpec.builder().paramLabel("b").build());
        Help help = new Help(spec, new Help.ColorScheme(Help.Ansi.OFF));
        String actual = help.abbreviatedSynopsis();
        assertEquals(String.format("<main class> b...%n"), actual);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testSynopsis() {
        Help help = new Help(CommandSpec.create(), new Help.ColorScheme(Help.Ansi.OFF));
        String actual = help.synopsis();
        assertEquals(String.format("<main class>%n"), actual);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testAddSubcommand() {
        @Command(name = "app", mixinStandardHelpOptions = true)
        class App { }
        Help help = new Help(new CommandLine(CommandSpec.create()).getCommandSpec(), new Help.ColorScheme(Help.Ansi.OFF));
        help.addSubcommand("boo", new App());
        assertEquals(1, help.subcommands().size());
        assertEquals("app", help.subcommands().get("boo").commandSpec().name());
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
                "      <file>      paramDescription1%n" +
                "                  paramDescription2%n" +
                "  -v, --verbose   optionDescription1%n" +
                "                  optionDescription2%n" +
                "footerHeading1%n" +
                "footerHeading2footer1%n" +
                "footer2%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testCommandLine_printVersionInfo_printsSinglePlainTextString() {
        @Command(version = "1.0") class Versioned {}
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CommandLine(new Versioned()).printVersionHelp(new PrintStream(baos, true), Help.Ansi.OFF);
        String result = baos.toString();
        assertEquals(String.format("1.0%n"), result);
    }

    static class FailingVersionProvider implements IVersionProvider {
        public String[] getVersion() {
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
    public void testRepeatingGroup() {
        class App {
            @Parameters(arity = "2", description = "description") String[] twoArgs;
        }
        String actual = usageString(new App(), Help.Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> (<twoArgs> <twoArgs>)...%n" +
                "      (<twoArgs> <twoArgs>)...%n" +
                "         description%n");
        assertEquals(expected, actual);
    }
    
    @Test
    public void testMapFieldHelp_with_unlimitedSplit() {
        class App {
            @Parameters(arity = "2", split = "\\|",
                    paramLabel = "FIXTAG=VALUE",
                    description = "Repeating group of two lists of vertical bar '|'-separated FIXTAG=VALUE pairs.")
            Map<Integer,String> message;

            @Option(names = {"-P", "-map"}, split = ",",
                    paramLabel = "TIMEUNIT=VALUE",
                    description = "Any number of TIMEUNIT=VALUE pairs. These may be specified separately (-PTIMEUNIT=VALUE) or as a comma-separated list.")
            Map<TimeUnit, String> map;
        }
        String actual = usageString(new App(), Help.Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> [-P=TIMEUNIT=VALUE[,TIMEUNIT=VALUE...]]... (FIXTAG=VALUE%n" +
                "                    [\\|FIXTAG=VALUE...] FIXTAG=VALUE[\\|FIXTAG=VALUE...])...%n" +
                "      (FIXTAG=VALUE[\\|FIXTAG=VALUE...] FIXTAG=VALUE[\\|FIXTAG=VALUE...])...%n" +
                "         Repeating group of two lists of vertical bar '|'-separated FIXTAG=VALUE%n" +
                "           pairs.%n" +
                "  -P, -map=TIMEUNIT=VALUE[,TIMEUNIT=VALUE...]%n" +
                "         Any number of TIMEUNIT=VALUE pairs. These may be specified separately%n" +
                "           (-PTIMEUNIT=VALUE) or as a comma-separated list.%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testMapFieldHelpSplit_with_limitSplit() {
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
        CommandSpec spec = CommandSpec.forAnnotatedObject(new App());
        spec.parser().limitSplit(true);
        String actual = usageString(new CommandLine(spec), Help.Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> [-P=TIMEUNIT=VALUE[,TIMEUNIT=VALUE]...]...%n" +
                "                    (FIXTAG=VALUE\\|FIXTAG=VALUE)...%n" +
                "      (FIXTAG=VALUE\\|FIXTAG=VALUE)...%n" +
                "         Exactly two lists of vertical bar '|'-separated FIXTAG=VALUE pairs.%n" +
                "  -P, -map=TIMEUNIT=VALUE[,TIMEUNIT=VALUE]...%n" +
                "         Any number of TIMEUNIT=VALUE pairs. These may be specified separately%n" +
                "           (-PTIMEUNIT=VALUE) or as a comma-separated list.%n");
        assertEquals(expected, actual);
    }

//    def cli = new CliBuilder(name:'ant',
//    header:'Options:')
//            cli.help('print this message')
//            cli.logfile(type:File, argName:'file', 'use given file for log')
//            cli.D(type:Map, argName:'property=value', args: '+', 'use value for given property')
//            cli.lib(argName:'path', valueSeparator:',', args: '3',
//            'comma-separated list of up to 3 paths to search for jars and classes')
    @Test
    public void testMultiValueCliBuilderCompatibility() {
        class App {
            @Option(names = "--help", description = "print this message")
            boolean help;
            @Option(names = "--logfile", description = "use given file for log")
            File file;
            @Option(names = "-P", arity = "0..*", paramLabel = "<key=ppp>", description = "use value for project key")
            Map projectMap;
            @Option(names = "-D", arity = "1..*", paramLabel = "<key=ddd>", description = "use value for given property")
            Map map;
            @Option(names = "-S", arity = "0..*", split = ",", paramLabel = "<key=sss>", description = "use value for project key")
            Map sss;
            @Option(names = "-T", arity = "1..*", split = ",", paramLabel = "<key=ttt>", description = "use value for given property")
            Map ttt;
            @Option(names = "--x", arity = "0..2", split = ",", description = "comma-separated list of up to 2 xxx's")
            String[] x;
            @Option(names = "--y", arity = "3", split = ",", description = "exactly 3 y's")
            String[] y;
            @Option(names = "--lib", arity = "1..3", split = ",", description = "comma-separated list of up to 3 paths to search for jars and classes")
            String[] path;
        }
        CommandSpec spec = CommandSpec.forAnnotatedObject(new App());
        spec.parser().limitSplit(true);
        String actual = usageString(new CommandLine(spec), Help.Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> [--help] [--logfile=<file>] [--x[=<x>[,<x>]]]...%n" +
                "                    [--lib=<path>[,<path>[,<path>]]]... [--y=<y>,<y>,<y>]... [-P%n" +
                "                    [=<key=ppp>...]]... [-S[=<key=sss>[,<key=sss>]...]]...%n" +
                "                    [-D=<key=ddd>...]... [-T=<key=ttt>[,<key=ttt>]...]...%n" +
                "      --help             print this message%n" +
                "      --lib=<path>[,<path>[,<path>]]%n" +
                "                         comma-separated list of up to 3 paths to search for jars%n" +
                "                           and classes%n" +
                "      --logfile=<file>   use given file for log%n" +
                "      --x[=<x>[,<x>]]    comma-separated list of up to 2 xxx's%n" +
                "      --y=<y>,<y>,<y>    exactly 3 y's%n" +
                "  -D=<key=ddd>...        use value for given property%n" +
                "  -P=[<key=ppp>...]      use value for project key%n" +
                "  -S=[<key=sss>[,<key=sss>]...]%n" +
                "                         use value for project key%n" +
                "  -T=<key=ttt>[,<key=ttt>]...%n" +
                "                         use value for given property%n");
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
                "  -a=<Integer=URI>%n" +
                "  -b=<TimeUnit=StringBuilder>%n" +
                "%n" +
                "  -c=<String=String>%n" +
                "  -d=<d>%n" +
                "  -e=<Integer=Long>%n" +
                "  -f=<Long=Float>%n" +
                "  -g=<TimeUnit=Float>%n");
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

    @SuppressWarnings("deprecation")
    @Test
    public void testPrintHelpIfRequested1ReturnsTrueForUsageHelp() throws IOException {
        class App {
            @Option(names = "-h", usageHelp = true) boolean usageRequested;
        }
        List<CommandLine> list = new CommandLine(new App()).parse("-h");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream out = new PrintStream(baos);
        assertTrue(CommandLine.printHelpIfRequested(list, out, Help.Ansi.OFF));
        
        String expected = String.format("" +
                "Usage: <main class> [-h]%n" +
                "  -h%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testPrintHelpIfRequestedWithParseResultReturnsTrueForUsageHelp() throws IOException {
        class App {
            @Option(names = "-h", usageHelp = true) boolean usageRequested;
        }
        ParseResult parseResult = new CommandLine(new App()).parseArgs("-h");
        assertTrue(CommandLine.printHelpIfRequested(parseResult));

        String expected = String.format("" +
                "Usage: <main class> [-h]%n" +
                "  -h%n");
        assertEquals(expected, systemOutRule.getLog());
    }

    @Test
    public void testPrintHelpIfRequested2ReturnsTrueForUsageHelp() throws IOException {
        class App {
            @Option(names = "-h", usageHelp = true) boolean usageRequested;
        }
        List<CommandLine> list = new CommandLine(new App()).parse("-h");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream out = new PrintStream(baos);
        assertTrue(CommandLine.printHelpIfRequested(list, out, out, Help.Ansi.OFF));

        String expected = String.format("" +
                "Usage: <main class> [-h]%n" +
                "  -h%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testPrintHelpIfRequestedWithCustomColorScheme() {
        ColorScheme customColorScheme = new Help.ColorScheme(Help.Ansi.ON)
                .optionParams(Style.fg_magenta)
                .commands(Style.bg_cyan)
                .options(Style.fg_green)
                .parameters(Style.bg_white);
    
        @Command(mixinStandardHelpOptions = true)
        class App {
            @Option(names = { "-f" }, paramLabel = "ARCHIVE", description = "the archive file") File archive;
            @Parameters(paramLabel = "POSITIONAL", description = "positional arg") String arg;
        }
        List<CommandLine> list = new CommandLine(new App()).parse("--help");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream out = new PrintStream(baos);
        assertTrue(CommandLine.printHelpIfRequested(list, out, out, customColorScheme));
    
        String expected = Help.Ansi.ON.string(String.format("" +
                "Usage: @|bg_cyan <main class>|@ [@|green -hV|@] [@|green -f|@=@|magenta ARCHIVE|@] @|bg_white POSITIONAL|@%n" +
                "@|bg_white  |@     @|bg_white POSITIONAL|@   positional arg%n" +
                "  @|green -f|@=@|magenta A|@@|magenta RCHIVE|@       the archive file%n" +
                "  @|green -h|@, @|green --help|@       Show this help message and exit.%n" +
                "  @|green -V|@, @|green --version|@    Print version information and exit.%n"));
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
        final PrintStream out = new PrintStream(baos);
        assertTrue(CommandLine.printHelpIfRequested(list, out, out, Help.Ansi.OFF));

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
        final PrintStream out = new PrintStream(baos);
        assertFalse(CommandLine.printHelpIfRequested(list, out, out, Help.Ansi.OFF));

        String expected = "";
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testPrintHelpIfRequestedForHelpCommandThatDoesNotImplementIHelpCommandInitializable() {
        @Command(name = "help", helpCommand = true)
        class MyHelp implements Runnable {
            public void run() {
            }
        }
        @Command(subcommands = MyHelp.class)
        class App implements Runnable {
            public void run() {
            }
        }
        CommandLine cmd = new CommandLine(new App(), new InnerClassFactory(this));
        ParseResult result = cmd.parseArgs("help");
        assertTrue(CommandLine.printHelpIfRequested(result));
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
    public void testDemoUsage() {
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
                "  -a, --autocomplete   Generate sample autocomplete script for git%n" +
                "  -1, --showUsageForSubcommandGitCommit%n" +
                "                       Shows usage help for the git-commit subcommand%n" +
                "  -2, --showUsageForMainCommand%n" +
                "                       Shows usage help for a command with subcommands%n" +
                "  -3, --showUsageForSubcommandGitStatus%n" +
                "                       Shows usage help for the git-status subcommand%n" +
                "      --simple         Show help for the first simple Example in the manual%n" +
                "  -i, --index          Show 256 color palette index values%n" +
                "  -r, --rgb            Show 256 color palette RGB component values%n" +
                "  -t, --tests          Runs all tests in this class%n" +
                "  -V, --version        Show version information and exit%n" +
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
    public void testAutoHelpMixinUsageHelpOption() {
        @Command(mixinStandardHelpOptions = true) class App {}

        String[] helpOptions = {"-h", "--help"};
        for (String option : helpOptions) {
            List<CommandLine> list = new CommandLine(new App()).parse(option);
            assertTrue(list.get(0).isUsageHelpRequested());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final PrintStream out = new PrintStream(baos);
            assertTrue(CommandLine.printHelpIfRequested(list, out, out, Help.Ansi.OFF));

            String expected = String.format("" +
                    "Usage: <main class> [-hV]%n" +
                    "  -h, --help      Show this help message and exit.%n" +
                    "  -V, --version   Print version information and exit.%n");
            assertEquals(expected, baos.toString());
        }
    }

    @Test
    public void testAutoHelpMixinVersionHelpOption() {
        @Command(mixinStandardHelpOptions = true, version = "1.2.3") class App {}

        String[] versionOptions = {"-V", "--version"};
        for (String option : versionOptions) {
            List<CommandLine> list = new CommandLine(new App()).parse(option);
            assertTrue(list.get(0).isVersionHelpRequested());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final PrintStream out = new PrintStream(baos);
            assertTrue(CommandLine.printHelpIfRequested(list, out, out, Help.Ansi.OFF));

            String expected = String.format("1.2.3%n");
            assertEquals(expected, baos.toString());
        }
    }

    @Test
    public void testAutoHelpMixinUsageHelpSubcommandOnAppWithoutSubcommands() {
        @Command(mixinStandardHelpOptions = true, subcommands = HelpCommand.class) class App {}

        List<CommandLine> list = new CommandLine(new App()).parse("help");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream out = new PrintStream(baos);
        assertTrue(CommandLine.printHelpIfRequested(list, out, out, Help.Ansi.OFF));

        String expected = String.format("" +
                "Usage: <main class> [-hV] [COMMAND]%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "Commands:%n" +
                "  help  Displays help information about the specified command%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testAutoHelpMixinRunHelpSubcommandOnAppWithoutSubcommands() {
        @Command(mixinStandardHelpOptions = true, subcommands = HelpCommand.class)
        class App implements Runnable{ public void run(){}}

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.run(new App(), new PrintStream(baos), Help.Ansi.OFF, "help");

        String expected = String.format("" +
                "Usage: <main class> [-hV] [COMMAND]%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "Commands:%n" +
                "  help  Displays help information about the specified command%n");
        assertEquals(expected, baos.toString());
    }
    @Test
    public void testHelpSubcommandWithValidCommand() {
        @Command(subcommands = {Sub.class, HelpCommand.class}) class App implements Runnable{ public void run(){}}

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.run(new App(), new PrintStream(baos), Help.Ansi.OFF, "help", "sub");

        String expected = String.format("" +
                "Usage: <main class> sub%n" +
                "This is a subcommand%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testHelpSubcommandWithInvalidCommand() {
        @Command(mixinStandardHelpOptions = true, subcommands = {Sub.class, HelpCommand.class})
        class App implements Runnable{ public void run(){}}

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.run(new App(), System.out, new PrintStream(baos), Help.Ansi.OFF, "help", "abcd");

        String expected = String.format("" +
                "Unknown subcommand 'abcd'.%n" +
                "Usage: <main class> [-hV] [COMMAND]%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "Commands:%n" +
                "  sub   This is a subcommand%n" +
                "  help  Displays help information about the specified command%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testHelpSubcommandWithHelpOption() {
        @Command(subcommands = {Sub.class, HelpCommand.class})
        class App implements Runnable{ public void run(){}}

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.run(new App(), new PrintStream(baos), Help.Ansi.OFF, "help", "-h");

        String expected = String.format("" +
                "Displays help information about the specified command%n" +
                "%n" +
                "Usage: <main class> help [-h] [COMMAND...]%n" +
                "%n" +
                "When no COMMAND is given, the usage help for the main command is displayed.%n" +
                "If a COMMAND is specified, the help for that command is shown.%n" +
                "%n" +
                "      [COMMAND...]   The COMMAND to display the usage help message for.%n" +
                "  -h, --help         Show usage help for the help command and exit.%n");
        assertEquals(expected, baos.toString());

        StringWriter sw = new StringWriter();
        new CommandLine(new App()).getSubcommands().get("help").usage(new PrintWriter(sw));
        assertEquals(expected, sw.toString());
    }

    @Test
    public void testHelpSubcommandWithoutCommand() {
        @Command(mixinStandardHelpOptions = true, subcommands = {Sub.class, HelpCommand.class})
        class App implements Runnable{ public void run(){}}

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.run(new App(), new PrintStream(baos), Help.Ansi.OFF, "help");

        String expected = String.format("" +
                "Usage: <main class> [-hV] [COMMAND]%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "Commands:%n" +
                "  sub   This is a subcommand%n" +
                "  help  Displays help information about the specified command%n");
        assertEquals(expected, baos.toString());

        StringWriter sw = new StringWriter();
        new CommandLine(new App()).usage(new PrintWriter(sw));
        assertEquals(expected, sw.toString());
    }

    @Test
    public void testHelpSubcommandRunDoesNothingIfParentNotSet() {
        HelpCommand cmd = new HelpCommand();
        cmd.run();
        assertEquals("", this.systemOutRule.getLog());
    }

    @Test
    public void testHelpSubcommandRunPrintsParentUsageIfParentSet() {
        HelpCommand cmd = new HelpCommand();
        CommandLine help = new CommandLine(cmd);
        CommandSpec spec = CommandSpec.create().name("parent");
        spec.usageMessage().description("the parent command");
        spec.addSubcommand("parent", help);
        new CommandLine(spec); // make sure parent spec has a CommandLine

        cmd.init(help, Help.Ansi.OFF, System.out, System.err);
        cmd.run();
        String expected = String.format("" +
                "Usage: parent [COMMAND]%n" +
                "the parent command%n" +
                "Commands:%n" +
                "  parent  Displays help information about the specified command%n");
        assertEquals(expected, this.systemOutRule.getLog());
    }

    @Test
    public void testUsageHelpForNestedSubcommands() {
        @Command(name = "subsub", mixinStandardHelpOptions = true) class SubSub { }
        @Command(name = "sub", subcommands = {SubSub.class}) class Sub { }
        @Command(name = "main", subcommands = {Sub.class}) class App { }

        CommandLine app = new CommandLine(new App(), new InnerClassFactory(this));
        //ParseResult result = app.parseArgs("sub", "subsub", "--help");
        //CommandLine.printHelpIfRequested(result);
        CommandLine subsub = app.getSubcommands().get("sub").getSubcommands().get("subsub");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        subsub.usage(new PrintStream(baos), Help.Ansi.OFF);

        String expected = String.format("" +
                "Usage: main sub subsub [-hV]%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testUsageTextWithHiddenSubcommand() {
        @Command(name = "foo", description = "This is a visible subcommand") class Foo { }
        @Command(name = "bar", description = "This is a hidden subcommand", hidden = true) class Bar { }
        @Command(name = "app", subcommands = {Foo.class, Bar.class}) class App { }

        CommandLine app = new CommandLine(new App(), new InnerClassFactory(this));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        app.usage(new PrintStream(baos));

        String expected = format("" +
                "Usage: app [COMMAND]%n" +
                "Commands:%n" +
                "  foo  This is a visible subcommand%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testUsage_NoHeaderIfAllSubcommandHidden() {
        @Command(name = "foo", description = "This is a foo sub-command", hidden = true) class Foo { }
        @Command(name = "bar", description = "This is a foo sub-command", hidden = true) class Bar { }
        @Command(name = "app", abbreviateSynopsis = true) class App { }

        CommandLine app = new CommandLine(new App())
                .addSubcommand("foo", new Foo())
                .addSubcommand("bar", new Bar());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        app.usage(new PrintStream(baos));

        String expected = format("" +
                "Usage: app [COMMAND]%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void test282BrokenValidationWhenNoOptionsToCompareWith() {
        class App implements Runnable {
            @Parameters(paramLabel = "FILES", arity = "1..*", description = "List of files")
            private List<File> files = new ArrayList<File>();

            public void run() { }
        }
        String[] args = new String[] {"-unknown"};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.run(new App(), System.out, new PrintStream(baos), Help.Ansi.OFF, args);

        String expected = format("" +
                "Missing required parameter: FILES%n" +
                "Usage: <main class> FILES...%n" +
                "      FILES...   List of files%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void test282ValidationWorksWhenOptionToCompareWithExists() {
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
                "Missing required parameter: FILES%n" +
                "Usage: <main class> [-v] FILES...%n" +
                "      FILES...   List of files%n" +
                "  -v             Print output%n");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testShouldGetUsageWidthFromSystemProperties() {
        int defaultWidth = new UsageMessageSpec().width();
        assertEquals(80, defaultWidth);
        try {
            System.setProperty("picocli.usage.width", "123");
            int width = new UsageMessageSpec().width();
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
        int actual = new UsageMessageSpec().width();
        System.setErr(originalErr);
        System.clearProperty("picocli.usage.width");

        assertEquals(80, actual);
        assertEquals(format("[picocli WARN] Invalid picocli.usage.width value 'INVALID'. Using usage width 80.%n"), baos.toString("UTF-8"));
    }

    @Test
    public void testUsageWidthFromCommandAttribute() {
        @Command(usageHelpWidth = 60,
        description = "0123456789012345678901234567890123456789012345678901234567890123456789")
        class App {}
        CommandLine cmd = new CommandLine(new App());

        assertEquals(60, cmd.getUsageHelpWidth());
        assertEquals(60, cmd.getCommandSpec().usageMessage().width());
    }

    @Test
    public void testUsageWidthFromSystemPropertyOverridesCommandAttribute() {
        @Command(usageHelpWidth = 60,
                description = "0123456789012345678901234567890123456789012345678901234567890123456789")
        class App {}
        System.setProperty("picocli.usage.width", "123");
        try {
            CommandLine cmd = new CommandLine(new App());

            assertEquals(123, cmd.getUsageHelpWidth());
            assertEquals(123, cmd.getCommandSpec().usageMessage().width());
        } finally {
            System.clearProperty("picocli.usage.width");
        }
    }

    @Test
    public void testInvalidUsageWidthCommandAttribute() throws UnsupportedEncodingException {
        @Command(usageHelpWidth = 40)
        class App {}
        try {
            new CommandLine(new App());
            fail("Expected exception");
        } catch (InitializationException ex) {
            assertEquals("Invalid usage message width 40. Minimum value is 55", ex.getMessage());
        };
    }

    @Test
    public void testTooSmallUsageWidthPropertyValue() throws UnsupportedEncodingException {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2500);
        System.setErr(new PrintStream(baos));

        System.clearProperty("picocli.trace");
        System.setProperty("picocli.usage.width", "54");
        int actual = new UsageMessageSpec().width();
        System.setErr(originalErr);
        System.clearProperty("picocli.usage.width");

        assertEquals(55, actual);
        assertEquals(format("[picocli WARN] Invalid picocli.usage.width value 54. Using minimum usage width 55.%n"), baos.toString("UTF-8"));
    }

    @Test
    public void testTextTableWithLargeWidth() {
        TextTable table = TextTable.forDefaultColumns(Help.Ansi.OFF, 200);
        table.addRowValues(textArray(Help.Ansi.OFF, "", "-v", ",", "--verbose", "show what you're doing while you are doing it"));
        table.addRowValues(textArray(Help.Ansi.OFF, "", "-p", null, null, "the quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy doooooooooooooooog."));

        assertEquals(String.format(
                "  -v, --verbose               show what you're doing while you are doing it%n" +
                        "  -p                          the quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy doooooooooooooooog.%n"
        ), table.toString(new StringBuilder()).toString());
    }

    @Test
    public void testLongMultiLineSynopsisIndentedWithLargeWidth() {
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
            System.setProperty("picocli.usage.width", String.valueOf(UsageMessageSpec.DEFAULT_USAGE_WIDTH));
        }
    }

    @Command(description = "The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog.")
    static class WideDescriptionApp {
        @Option(names = "-s", description = "The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog.")
        String shortOption;

        @Option(names = "--very-very-very-looooooooooooooooong-option-name", description = "The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog.")
        String lengthyOption;

        static final String expected = format("Usage: <main class> [--very-very-very-looooooooooooooooong-option-name=<lengthyOption>] [-s=<shortOption>]%n" +
                "The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped%n" +
                "over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The%n" +
                "quick brown fox jumped over the lazy dog.%n" +
                "      --very-very-very-looooooooooooooooong-option-name=<lengthyOption>%n" +
                "                      The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick%n" +
                "                        brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog.%n" +
                "  -s=<shortOption>    The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick%n" +
                "                        brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog.%n");
    }

    @Test
    public void testWideUsageViaSystemProperty() {
        System.setProperty("picocli.usage.width", String.valueOf(120));
        try {
            String actual = usageString(new WideDescriptionApp(), Help.Ansi.OFF);
            assertEquals(WideDescriptionApp.expected, actual);
        } finally {
            System.setProperty("picocli.usage.width", String.valueOf(80));
        }
    }

    @Test
    public void testWideUsage() {
        CommandLine cmd = new CommandLine(new WideDescriptionApp());
        cmd.setUsageHelpWidth(120);
        String actual = usageString(cmd, Help.Ansi.OFF);
        assertEquals(WideDescriptionApp.expected, actual);
    }

    @Test
    public void testCliBuilderLsExample() {
        @Command(name="ls")
        class App {
            @Option(names = "-a", description = "display all files") boolean a;
            @Option(names = "-l", description = "use a long listing format") boolean l;
            @Option(names = "-t", description = "sort by modification time") boolean t;
        }
        String actual = usageString(new App(), Help.Ansi.OFF);
        assertEquals(String.format("" +
                "Usage: ls [-alt]%n" +
                "  -a     display all files%n" +
                "  -l     use a long listing format%n" +
                "  -t     sort by modification time%n"), actual);
    }

    @Test
    public void testAnsiText() {
        String markup = "@|bg(red),white,underline some text|@";
        Help.Ansi.Text txt = Help.Ansi.ON.text(markup);
        Help.Ansi.Text txt2 = Help.Ansi.ON.new Text(markup);
        assertEquals(txt, txt2);
    }

    @Test
    public void testAnsiString() {
        String msg = "some text";
        String markup = "@|bg(red),white,underline " + msg + "|@";
        String ansiTxt = Help.Ansi.ON.string(markup);
        String ansiTxt2 = Help.Ansi.ON.new Text(markup).toString();
        assertEquals(ansiTxt, ansiTxt2);
    }

    @Test
    public void testAnsiValueOf() {
        assertEquals("true=ON", Help.Ansi.ON, Help.Ansi.valueOf(true));
        assertEquals("false=OFF", Help.Ansi.OFF, Help.Ansi.valueOf(false));
    }

    @Test
    public void testIssue430NewlineInSubcommandDescriptionList() { // courtesy [Benny Bottema](https://github.com/bbottema)
        CommandSpec rootCmd = createCmd("newlines", "Displays subcommands, one of which contains description newlines");

        rootCmd.addSubcommand("subA", createCmd("subA", "regular description for subA"));
        rootCmd.addSubcommand("subB", createCmd("subB", "very,\nspecial,\nChristopher Walken style,\ndescription."));
        rootCmd.addSubcommand("subC", createCmd("subC", "not so,%nspecial,%nJon Voight style,%ndescription."));
        rootCmd.addSubcommand("subD", createCmd("subD", "regular description for subD"));

        assertEquals(String.format("" +
                "Usage: newlines [-hV] [COMMAND]%n" +
                "Displays subcommands, one of which contains description newlines%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "Commands:%n" +
                "  subA  regular description for subA%n" +
                "  subB  very,%n" +
                "        special,%n" +
                "        Christopher Walken style,%n" +
                "        description.%n" +
                "  subC  not so,%n" +
                "        special,%n" +
                "        Jon Voight style,%n" +
                "        description.%n" +
                "  subD  regular description for subD%n"), new CommandLine(rootCmd).getUsageMessage());
    }

    @Test
    public void testMultiLineWrappedDescription() {
        CommandSpec rootCmd = createCmd("wrapDescriptions", "Displays sub commands, with extra long descriptions");

        CommandSpec oneLineCmd = createCmd("oneLine", "The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog.");
        CommandSpec multiLineCmd = createCmd("multiLine", "The quick brown fox jumped over the lazy dog.%nThe quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog.%n%nThe quick brown fox jumped over the lazy dog.");

        rootCmd.addSubcommand("oneLine", oneLineCmd);
        rootCmd.addSubcommand("multiLine", multiLineCmd);

        assertEquals(String.format("" +
                "Usage: wrapDescriptions [-hV] [COMMAND]%n" +
                "Displays sub commands, with extra long descriptions%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "Commands:%n" +
                "  oneLine    The quick brown fox jumped over the lazy dog. The quick brown fox%n" +
                "               jumped over the lazy dog. The quick brown fox jumped over the%n" +
                "               lazy dog. The quick brown fox jumped over the lazy dog. The%n" +
                "               quick brown fox jumped over the lazy dog. The quick brown fox%n" +
                "               jumped over the lazy dog.%n" +
                "  multiLine  The quick brown fox jumped over the lazy dog.%n" +
                "             The quick brown fox jumped over the lazy dog. The quick brown fox%n" +
                "               jumped over the lazy dog. The quick brown fox jumped over the%n" +
                "               lazy dog. The quick brown fox jumped over the lazy dog.%n%n" +
                "             The quick brown fox jumped over the lazy dog.%n"), new CommandLine(rootCmd).getUsageMessage());

        assertEquals(String.format(
                "Usage: wrapDescriptions oneLine [-hV]%n" +
                "The quick brown fox jumped over the lazy dog. The quick brown fox jumped over%n" +
                "the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox%n" +
                "jumped over the lazy dog. The quick brown fox jumped over the lazy dog. The%n" +
                "quick brown fox jumped over the lazy dog.%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n"), new CommandLine(oneLineCmd).getUsageMessage());

        assertEquals(String.format(
                "Usage: wrapDescriptions multiLine [-hV]%n" +
                "The quick brown fox jumped over the lazy dog.%n" +
                "The quick brown fox jumped over the lazy dog. The quick brown fox jumped over%n" +
                "the lazy dog. The quick brown fox jumped over the lazy dog. The quick brown fox%n" +
                "jumped over the lazy dog.%n%n" +
                "The quick brown fox jumped over the lazy dog.%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n"), new CommandLine(multiLineCmd).getUsageMessage());
    }

    @Test
    public void testMultiLineWrappedDefaultValueWontRunIntoInfiniteLoop(){
        class Args {
            @Parameters(arity = "1..*", description = "description", defaultValue = "/long/value/length/equals/columnValue/maxlength/and/non/null/offset/xxx", showDefaultValue = ALWAYS)
            String[] c;
        }
        String expected = String.format("" +
                "Usage: <main class> <c>...%n" +
                "      <c>...   description%n" +
                "                 Default:%n" +
                "                 /long/value/length/equals/columnValue/maxlength/and/non/null/offset/%n" +
                "                 xxx%n");
        assertEquals(expected, usageString(new Args(), Help.Ansi.OFF));
    }

    private static CommandSpec createCmd(String name, String description) {
        CommandSpec cmd = CommandSpec.create().name(name).mixinStandardHelpOptions(true);
        cmd.usageMessage().description(description);
        return cmd;
    }

    @Test
    public void testHelpFactoryIsUsedWhenSet() {
        @Command() class TestCommand { }

        IHelpFactory helpFactoryWithOverridenHelpMethod = new IHelpFactory() {
            public Help create(CommandSpec commandSpec, ColorScheme colorScheme) {
                return new Help(commandSpec, colorScheme) {
                    @Override
                    public String detailedSynopsis(int synopsisHeadingLength, Comparator<OptionSpec> optionSort, boolean clusterBooleanOptions) {
                        return "<custom detailed synopsis>";
                    }
                };
            }
        };
        CommandLine commandLineWithCustomHelpFactory = new CommandLine(new TestCommand()).setHelpFactory(helpFactoryWithOverridenHelpMethod);
        assertEquals("Usage: <custom detailed synopsis>", commandLineWithCustomHelpFactory.getUsageMessage(Help.Ansi.OFF));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        commandLineWithCustomHelpFactory.usage(new PrintStream(baos, true));
        assertEquals("Usage: <custom detailed synopsis>", baos.toString());
    }

    @Test
    public void testCustomizableHelpSections() {
        @Command(header="<header> (%s)", description="<description>") class TestCommand { }
        CommandLine commandLineWithCustomHelpSections = new CommandLine(new TestCommand());

        IHelpSectionRenderer renderer = new IHelpSectionRenderer() { public String render(Help help) {
            return help.header("<custom header param>");
        } };
        commandLineWithCustomHelpSections.getHelpSectionMap().put("customSectionExtendsHeader", renderer);

        commandLineWithCustomHelpSections.setHelpSectionKeys(Arrays.asList(
                UsageMessageSpec.SECTION_KEY_DESCRIPTION,
                UsageMessageSpec.SECTION_KEY_SYNOPSIS_HEADING,
                "customSectionExtendsHeader"));

        String expected = String.format("" +
                "<description>%n" +
                "Usage: <header> (<custom header param>)%n");
        assertEquals(expected, commandLineWithCustomHelpSections.getUsageMessage(Help.Ansi.OFF));
    }

    @Test
    public void testNullSectionRenderer() {
        CommandLine cmd = new CommandLine(new UsageDemo());

        cmd.getHelpSectionMap().clear();
        cmd.getHelpSectionMap().put(UsageMessageSpec.SECTION_KEY_HEADER, null);
        cmd.getHelpSectionMap().put(UsageMessageSpec.SECTION_KEY_DESCRIPTION, new IHelpSectionRenderer() {
            public String render(Help help) {
                return "abc";
            }
        });

        String actual = cmd.getUsageMessage();
        String expected = "abc";
        assertEquals(expected, actual);
    }

    @Test
    public void testBooleanOptionWithArity1() {
        @Command(mixinStandardHelpOptions = true)
        class ExampleCommand {
            @Option(names = { "-b" }, arity = "1")
            boolean booleanWithArity1;
        }
        String expected = String.format("" +
                "Usage: <main class> [-hV] [-b=<booleanWithArity1>]%n" +
                "  -b=<booleanWithArity1>%n" +
                "  -h, --help                Show this help message and exit.%n" +
                "  -V, --version             Print version information and exit.%n");
        assertEquals(expected, new CommandLine(new ExampleCommand()).getUsageMessage(Help.Ansi.OFF));
    }

    @Test
    public void testIssue615DescriptionContainingPercentChar() {
        class App {
            @Option(names = {"--excludebase"},
                    arity="1..*",
                    description = "exclude child files of cTree (only works with --ctree).%n"
                            + "Currently must be explicit or with trailing % for truncated glob."
            )
            public String[] excludeBase;
        }
        String expected = String.format("" +
                "Usage: <main class> [--excludebase=<excludeBase>...]...%n" +
                "      --excludebase=<excludeBase>...%n" +
                "         exclude child files of cTree (only works with --ctree).%%nCurrently must be%n" +
                "           explicit or with trailing %% for truncated glob.%n");
        String actual = new CommandLine(new App()).getUsageMessage();
        assertEquals(expected, actual);

        assertTrue(systemErrRule.getLog().contains(
                "[picocli WARN] Could not format 'exclude child files of cTree (only works with --ctree).%n" +
                        "Currently must be explicit or with trailing % for truncated glob.' " +
                        "(Underlying error:"));
        assertTrue(systemErrRule.getLog().contains(
                "). " +
                        "Using raw String: '%n' format strings have not been replaced with newlines. " +
                        "Please ensure to escape '%' characters with another '%'."));
    }

    @Test
    public void testDescriptionWithDefaultValueContainingPercentChar() {
        class App {
            @Option(names = {"-f"},
                    defaultValue = "%s - page %d of %d",
                    description = "format string. Default: ${DEFAULT-VALUE}")
            public String formatString;
        }
        String expected = String.format("" +
                "Usage: <main class> [-f=<formatString>]%n" +
                "  -f=<formatString>    format string. Default: %%s - page %%d of %%d%n");
        String actual = new CommandLine(new App()).getUsageMessage();
        assertEquals(expected, actual);

        assertFalse(systemErrRule.getLog().contains(
                "[picocli WARN] Could not format 'format string. Default: %s - page %d of %d' " +
                        "(Underlying error:"));
        assertFalse(systemErrRule.getLog().contains(
                "). " +
                        "Using raw String: '%n' format strings have not been replaced with newlines. " +
                        "Please ensure to escape '%' characters with another '%'."));
    }
}
