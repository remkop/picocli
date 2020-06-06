package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListResourceBundle;

import static org.junit.Assert.*;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_AT_FILE_PARAMETER;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_COMMAND_LIST_HEADING;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_END_OF_OPTIONS;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_OPTION_LIST;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_OPTION_LIST_HEADING;

public class EndOfOptionsDelimiterTest {
    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");
    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();
    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();


    @Test
    public void testEndOfOptionsDelimiter() {
        @Command(name = "A", mixinStandardHelpOptions = true,
                showEndOfOptionsDelimiterInUsageHelp = true,
                description = "... description ...")
        class A {
            @Parameters(arity = "1", description = "The file.")
            private File file;

            @Option(names = {"-x", "--long"}, arity="0..*", description = "Option with multiple params.")
            private String params;
        }

        String actual = new CommandLine(new A()).getUsageMessage();
        String expected = String.format("" +
                "Usage: A [-hV] [-x[=<params>...]] [--] <file>%n" +
                "... description ...%n" +
                "      <file>                 The file.%n" +
                "  -h, --help                 Show this help message and exit.%n" +
                "  -V, --version              Print version information and exit.%n" +
                "  -x, --long[=<params>...]   Option with multiple params.%n" +
                "  --                         This option can be used to separate command-line%n" +
                "                               options from the list of positional parameters.%n" +
                "");
        assertEquals(expected, actual);
    }

    @Test
    public void testShowEndOfOptionsDelimiterInUsageHelpBasic() {
        @Command(name = "myapp", mixinStandardHelpOptions = true,
                showEndOfOptionsDelimiterInUsageHelp = true, description = "Example command.")
        class MyApp {
            @Parameters(description = "A file.")
            File file;
        }

        String actual = new CommandLine(new MyApp()).getUsageMessage();
        String expected = String.format("" +
                "Usage: myapp [-hV] [--] <file>%n" +
                "Example command.%n" +
                "      <file>      A file.%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "  --              This option can be used to separate command-line options from%n" +
                "                    the list of positional parameters.%n" +
                "");
        assertEquals(expected, actual);
    }

    @Test
    public void testShowEndOfOptionsDelimiterInUsageHelpSystemProperties() {
        @Command(name = "myapp", mixinStandardHelpOptions = true,
                showEndOfOptionsDelimiterInUsageHelp = true, description = "Example command.")
        class MyApp {
            @Parameters(description = "A file.") File file;
        }

        System.setProperty("picocli.endofoptions.description", "End of options -- rock!");

        String actual = new CommandLine(new MyApp()).getUsageMessage();
        String expected = String.format("" +
                "Usage: myapp [-hV] [--] <file>%n" +
                "Example command.%n" +
                "      <file>      A file.%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "  --              End of options -- rock!%n" +
                "");
        assertEquals(expected, actual);
    }

    public static class MyResourceBundle extends ListResourceBundle {
        protected Object[][] getContents() {
            /** See {@link picocli.CommandLine.Help#END_OF_OPTIONS_OPTION} for the keys. */
            return new Object[][] {
                    {"picocli.endofoptions", "hi! I am the -- end-of-options description from a file"},
                    //{"picocli.endofoptions.description", "BUNDLE -- DESCRIPTION"},
            };
        }
    }

    @Test
    public void testShowEndOfOptionsDelimiterInUsageHelpResourceBundleWithoutSystemProps() {
        @Command(name = "A", mixinStandardHelpOptions = true, resourceBundle = "picocli.EndOfOptionsDelimiterTest$MyResourceBundle",
                showEndOfOptionsDelimiterInUsageHelp = true, description = "... description ...")
        class A { }

        String actual = new CommandLine(new A()).getUsageMessage();
        String expected = String.format("" +
                "Usage: A [-hV] [--]%n" +
                "... description ...%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "  --              hi! I am the -- end-of-options description from a file%n" +
                "");
        assertEquals(expected, actual);
    }

    @Test
    public void testShowEndOfOptionsDelimiterInUsageHelpResourceBundleWithSystemProps() {
        @Command(name = "A", mixinStandardHelpOptions = true, resourceBundle = "picocli.EndOfOptionsDelimiterTest$MyResourceBundle",
                showEndOfOptionsDelimiterInUsageHelp = true, description = "... description ...")
        class A { }

        System.setProperty("picocli.endofoptions.description", "EndOfOptions rock!");

        String actual = new CommandLine(new A()).setResourceBundle(new AtFileTest.MyResourceBundle()).getUsageMessage();
        String expected = String.format("" +
                "Usage: A [-hV] [--]%n" +
                "... description ...%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "  --              EndOfOptions rock!%n" +
                "");
        assertEquals(expected, actual);
    }

    @Test
    public void testShowEndOfOptionsDelimiterInUsageHelpResourceBundleWithDescriptionKey() {
        @Command(name = "A", mixinStandardHelpOptions = true, resourceBundle = "picocli.EndOfOptionsDelimiterTest$MyResourceBundle",
                showEndOfOptionsDelimiterInUsageHelp = true, description = "... description ...")
        class A { }

        System.setProperty("picocli.endofoptions", "EndOfOptions SHORT KEY!");

        String actual = new CommandLine(new A()).setResourceBundle(new AtFileTest.MyResourceBundle()).getUsageMessage();
        String expected = String.format("" +
                "Usage: A [-hV] [--]%n" +
                "... description ...%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "  --              This option can be used to separate command-line options from%n" +
                "                    the list of positional parameters.%n" +
                "");
        assertEquals(expected, actual);
    }

    public static class MyResourceBundleWithLongKey extends ListResourceBundle {
        protected Object[][] getContents() {
            return new Object[][] {
                    //{"picocli.endofoptions", "hi! I am the -- end-of-options description from a file"},
                    {"picocli.endofoptions.description", "BUNDLE -- DESCRIPTION"},
            };
        }
    }

    @Test
    public void testShowEndOfOptionsDelimiterInUsageHelpResourceBundle2WithoutSystemProps() {
        @Command(name = "A", mixinStandardHelpOptions = true, resourceBundle = "picocli.EndOfOptionsDelimiterTest$MyResourceBundleWithLongKey",
                showEndOfOptionsDelimiterInUsageHelp = true, description = "... description ...")
        class A { }

        String actual = new CommandLine(new A()).getUsageMessage();
        String expected = String.format("" +
                "Usage: A [-hV] [--]%n" +
                "... description ...%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "  --              BUNDLE -- DESCRIPTION%n" +
                "");
        assertEquals(expected, actual);
    }

    @Test
    public void testShowEndOfOptionsDelimiterInUsageHelpResourceBundle2WithSystemPropDescriptionKey() {
        @Command(name = "A", mixinStandardHelpOptions = true, resourceBundle = "picocli.EndOfOptionsDelimiterTest$MyResourceBundleWithLongKey",
                showEndOfOptionsDelimiterInUsageHelp = true, description = "... description ...")
        class A { }

        System.setProperty("picocli.endofoptions", "EndOfOptions2 DESCRIPTION KEY!"); //ignored

        String actual = new CommandLine(new A()).getUsageMessage();
        String expected = String.format("" +
                "Usage: A [-hV] [--]%n" +
                "... description ...%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "  --              BUNDLE -- DESCRIPTION%n" +
                "");
        assertEquals(expected, actual);
    }

    @Test
    public void testShowEndOfOptionsDelimiterInUsageHelpResourceBundle2WithSystemPropLongKey() {
        @Command(name = "A", mixinStandardHelpOptions = true, resourceBundle = "picocli.EndOfOptionsDelimiterTest$MyResourceBundleWithLongKey",
                showEndOfOptionsDelimiterInUsageHelp = true, description = "... description ...")
        class A { }

        System.setProperty("picocli.endofoptions.description", "EndOfOptions2 LONG KEY!");

        String actual = new CommandLine(new A()).getUsageMessage();
        String expected = String.format("" +
                "Usage: A [-hV] [--]%n" +
                "... description ...%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "  --              EndOfOptions2 LONG KEY!%n" +
                "");
        assertEquals(expected, actual);
    }

    @Test
    public void testShowEndOfOptionsDelimiterInUsageHelpWithCustomEndOfOptionsDelimiter() {
        @Command(name = "A", mixinStandardHelpOptions = true,
                showEndOfOptionsDelimiterInUsageHelp = true, description = "... description ...")
        class A { }

        String actual = new CommandLine(new A())
                .setEndOfOptionsDelimiter("@+@")
                .getUsageMessage();

        String expected = String.format("" +
                "Usage: A [-hV] [@+@]%n" +
                "... description ...%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "      @+@         This option can be used to separate command-line options from%n" +
                "                    the list of positional parameters.%n" +
                "");
        assertEquals(expected, actual);
    }

    @Test
    public void testEndOfOptionsListSectionAsFirstOption() {
        @Command(name = "A", mixinStandardHelpOptions = true,
                optionListHeading = "Options:%n",
                showEndOfOptionsDelimiterInUsageHelp = true, description = "... description ...")
        class A { }

        CommandLine commandLine = new CommandLine(new A());
        List<String> helpSectionKeys = commandLine.getHelpSectionKeys();
        List<String> copy = new ArrayList<String>(helpSectionKeys);
        copy.remove(SECTION_KEY_END_OF_OPTIONS);
        copy.add(copy.indexOf(SECTION_KEY_OPTION_LIST), SECTION_KEY_END_OF_OPTIONS);
        commandLine.setHelpSectionKeys(copy);

        String actual = commandLine.getUsageMessage();
        String expected = String.format("" +
                "Usage: A [-hV] [--]%n" +
                "... description ...%n" +
                "Options:%n" +
                "  --              This option can be used to separate command-line options from%n" +
                "                    the list of positional parameters.%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "");
        assertEquals(expected, actual);
        //commandLine.usage(System.out);
    }

    @Test
    public void testEndOfOptionsListSectionBeforeOptions() {
        @Command(name = "A", mixinStandardHelpOptions = true,
                optionListHeading = "Options:%n",
                showEndOfOptionsDelimiterInUsageHelp = true, description = "... description ...")
        class A {
            @Parameters(index = "0", arity = "1", description = "Some file.") File file;
            @Parameters(index = "1", description = "Some other file.") File anotherFile;
        }

        CommandLine commandLine = new CommandLine(new A());
        List<String> helpSectionKeys = commandLine.getHelpSectionKeys();
        List<String> copy = new ArrayList<String>(helpSectionKeys);
        copy.remove(SECTION_KEY_END_OF_OPTIONS);
        copy.add(helpSectionKeys.indexOf(SECTION_KEY_OPTION_LIST_HEADING), SECTION_KEY_END_OF_OPTIONS);
        commandLine.setHelpSectionKeys(copy);

        String actual = commandLine.getUsageMessage();
        String expected = String.format("" +
                "Usage: A [-hV] [--] <file> <anotherFile>%n" +
                "... description ...%n" +
                "      <file>          Some file.%n" +
                "      <anotherFile>   Some other file.%n" +
                "  --                  This option can be used to separate command-line options%n" +
                "                        from the list of positional parameters.%n" +
                "Options:%n" +
                "  -h, --help          Show this help message and exit.%n" +
                "  -V, --version       Print version information and exit.%n" +
                "");
        assertEquals(expected, actual);
    }

    @Test
    public void testEndOfOptionsListHeadingShownIfPositionalParamAndNoOptions() {
        @Command(name = "A",
                showEndOfOptionsDelimiterInUsageHelp = true,
                parameterListHeading = "Parameters:%n",
                optionListHeading = "Options:%n",
                description = "... description ...")
        class A {
            @Parameters(description = "Some positional parameter")
            String positional;
        }

        String actual = new CommandLine(new A()).getUsageMessage();
        String expected = String.format("" +
                "Usage: A [--] <positional>%n" +
                "... description ...%n" +
                "Parameters:%n" +
                "      <positional>   Some positional parameter%n" +
                "Options:%n" +
                "  --                 This option can be used to separate command-line options%n" +
                "                       from the list of positional parameters.%n"
        );
        assertEquals(expected, actual);
    }

    @Test
    public void testEndOfOptionsAndAtFileUsageWithoutParamsOrOptions() {
        @Command(name = "A",
                showAtFileInUsageHelp = true,
                showEndOfOptionsDelimiterInUsageHelp = true,
                parameterListHeading = "Parameters:%n",
                optionListHeading = "Options:%n",
                description = "... description ...")
        class A { }

        String actual = new CommandLine(new A()).getUsageMessage();
        String expected = String.format("" +
                "Usage: A [--] [@<filename>...]%n" +
                "... description ...%n" +
                "Parameters:%n" +
                "      [@<filename>...]   One or more argument files containing options.%n" +
                "Options:%n" +
                "  --                     This option can be used to separate command-line%n" +
                "                           options from the list of positional parameters.%n" +
                "");
        assertEquals(expected, actual);
    }

}
