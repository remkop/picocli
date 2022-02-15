package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.MissingParameterException;
import picocli.CommandLine.Option;

import static org.junit.Assert.*;

public class InterpolatedModelTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void testVariablesInUsageHelp() {
        @Command(name = "${sys:commandName}",
                descriptionHeading = "${sys:descriptionHeading}",
                description = {"${sys:description1}", "and ${sys:description2}"},
                headerHeading = "${sys:headerHeading}",
                header = {"${sys:header1}", "and ${sys:header2}"},
                footer = {"${sys:footer1}", "and ${sys:footer2}"},
                footerHeading = "${sys:footerHeading}",
//                resourceBundle = "${sys:resourceBundle}", // TODO separate test?
                aliases = {"${sys:alias1}", "${sys:alias2}"},
                commandListHeading = "${sys:commandListHeading}", subcommands = CommandLine.HelpCommand.class,
                separator = "${sys:separator}",
                parameterListHeading = "${sys:parameterListHeading}",
                optionListHeading = "${sys:optionListHeading}"
        )
        class App {
            @Option(names = "-x", description = "${sys:xdesc}") int x;
        }

        System.setProperty("commandName", "THISCOMMAND");
        System.setProperty("descriptionHeading", "DESCRIPTION HEADING%n");
        System.setProperty("description1", "DESCRIPTION 1");
        System.setProperty("description2", "DESCRIPTION 2");
        System.setProperty("headerHeading", "HEADER HEADING%n");
        System.setProperty("header1", "HEADER 1");
        System.setProperty("header2", "HEADER 2");
        System.setProperty("footerHeading", "FOOTER HEADING%n");
        System.setProperty("footer1", "FOOTER 1");
        System.setProperty("footer2", "FOOTER 2");
        System.setProperty("resourceBundle", "RESOURCEBUNDLE"); // TODO
        System.setProperty("alias1", "ALIAS 1");
        System.setProperty("alias2", "ALIAS 2");
        System.setProperty("commandListHeading", "SUBCOMMANDS HEADING%n");
        System.setProperty("parameterListHeading", "PARAM HEADING%n");
        System.setProperty("optionListHeading", "OPTION HEADING%n");
        System.setProperty("separator", "+++");

        System.setProperty("xdesc", "option description");

        String actual = new CommandLine(new App()).getUsageMessage();
        String expected = String.format("" +
                "HEADER HEADING%n" +
                "HEADER 1%n" +
                "and HEADER 2%n" +
                "Usage: THISCOMMAND [-x+++<x>] [COMMAND]%n" +
                "DESCRIPTION HEADING%n" +
                "DESCRIPTION 1%n" +
                "and DESCRIPTION 2%n" +
                "OPTION HEADING%n" +
                "  -x+ <x>   option description%n" +
                "SUBCOMMANDS HEADING%n" +
                "  help  Display help information about the specified command.%n" +
                "FOOTER HEADING%n" +
                "FOOTER 1%n" +
                "and FOOTER 2%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testVariablesInUsageHelpCustomSynopsis() {
        @Command(name = "${sys:commandName}",
                descriptionHeading = "${sys:descriptionHeading}",
                description = {"${sys:description1}", "and ${sys:description2}"},
                headerHeading = "${sys:headerHeading}",
                header = {"${sys:header1}", "and ${sys:header2}"},
                footer = {"${sys:footer1}", "and ${sys:footer2}"},
                footerHeading = "${sys:footerHeading}",
//                resourceBundle = "${sys:resourceBundle}", // TODO separate test?
                aliases = {"${sys:alias1}", "${sys:alias2}"},
                commandListHeading = "${sys:commandListHeading}", subcommands = CommandLine.HelpCommand.class,
                separator = "${sys:separator}",
                parameterListHeading = "${sys:parameterListHeading}",
                optionListHeading = "${sys:optionListHeading}",
                synopsisHeading = "${sys:synopsisHeading}",
                customSynopsis = {"${sys:customSynopsis1}", "${sys:customSynopsis2}"}
        )
        class App {
            @Option(names = "-x", description = "${sys:xdesc}") int x;
        }

        System.setProperty("commandName", "THISCOMMAND");
        System.setProperty("descriptionHeading", "DESCRIPTION HEADING%n");
        System.setProperty("description1", "I'm a description (and I feel fine)");
        System.setProperty("description2", "line 2");
        System.setProperty("headerHeading", "HEADER HEADING%n");
        System.setProperty("header1", "HEADER 1");
        System.setProperty("header2", "HEADER 2");
        System.setProperty("footerHeading", "FOOTER HEADING%n");
        System.setProperty("footer1", "FOOTER 1");
        System.setProperty("footer2", "FOOTER 2");
        System.setProperty("resourceBundle", "RESOURCEBUNDLE"); // TODO
        System.setProperty("alias1", "ALIAS 1");
        System.setProperty("alias2", "ALIAS 2");
        System.setProperty("commandListHeading", "SUBCOMMANDS HEADING%n");
        System.setProperty("parameterListHeading", "PARAM HEADING%n");
        System.setProperty("optionListHeading", "OPTION HEADING%n");
        System.setProperty("synopsisHeading", "SYNOPSIS HEADING%n");
        System.setProperty("customSynopsis1", "SYNOPSIS 1");
        System.setProperty("customSynopsis2", "SYNOPSIS 2");
        System.setProperty("separator", "+++");

        System.setProperty("xdesc", "option description");

        String actual = new CommandLine(new App()).getUsageMessage();
        String expected = String.format("" +
                "HEADER HEADING%n" +
                "HEADER 1%n" +
                "and HEADER 2%n" +
                "SYNOPSIS HEADING%n" +
                "SYNOPSIS 1%n" +
                "SYNOPSIS 2%n" +
                "DESCRIPTION HEADING%n" +
                "I'm a description (and I feel fine)%n" +
                "and line 2%n" +
                "OPTION HEADING%n" +
                "  -x+ <x>   option description%n" +
                "SUBCOMMANDS HEADING%n" +
                "  help  Display help information about the specified command.%n" +
                "FOOTER HEADING%n" +
                "FOOTER 1%n" +
                "and FOOTER 2%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testVariablesInOptionAttributes() {
        @Command(name = "cmd")
        class App {
            @Option(names = {"${sys:xname1}", "${sys:xname2}"},
                    description = "${sys:xdesc}",
                    arity = "${sys:xarity}",
                    defaultValue = "${sys:xdefault}",
                    split = "${sys:xsplit}",
                    descriptionKey = "${sys:xdescriptionKey}",
                    paramLabel = "${sys:paramLabel}"
            ) int[] x;
        }

        System.setProperty("xname1", "-NAME1");
        System.setProperty("xname2", "--NAME2");
        System.setProperty("xdesc", "OPT DESCR");
        System.setProperty("xarity", "4..6");
        System.setProperty("xdefault", "12345");
        System.setProperty("xsplit", ";;;");
        System.setProperty("xdescriptionKey", "DESCR-KEY");
        System.setProperty("paramLabel", "PARAMLABEL");

        CommandLine commandLine = new CommandLine(new App());
        String actual = commandLine.getUsageMessage();
        String expected = String.format("" +
                "Usage: cmd [-NAME1=PARAMLABEL[;;;PARAMLABEL...] PARAMLABEL[;;;PARAMLABEL...]%n" +
                "           PARAMLABEL[;;;PARAMLABEL...] PARAMLABEL[;;;PARAMLABEL...]%n" +
                "           [PARAMLABEL [PARAMLABEL]]]...%n" +
                "      -NAME1, --NAME2=PARAMLABEL[;;;PARAMLABEL...] PARAMLABEL[;;;PARAMLABEL...]%n" +
                "        PARAMLABEL[;;;PARAMLABEL...] PARAMLABEL[;;;PARAMLABEL...] [PARAMLABEL%n" +
                "        [PARAMLABEL]]%n" +
                "         OPT DESCR%n");
        assertEquals(expected, actual);

        CommandLine.Model.OptionSpec spec = commandLine.getCommandSpec().findOption("NAME1");
        assertNotNull(spec);
        assertSame(spec, commandLine.getCommandSpec().findOption("NAME2"));
//        assertEquals();
    }

    @Test
    public void testVersionShortName() {
        @Command(name = "abc", mixinStandardHelpOptions = true)
        class App {}

        System.setProperty("picocli.version.name.0", "-v");
        System.setProperty("picocli.version.name.1", "-VERSION");
        System.setProperty("picocli.help.name.0", "-H");
        System.setProperty("picocli.help.name.1", "-HELP");

        String actual = new CommandLine(new App()).getUsageMessage();
        String expected = String.format("" +
                "Usage: abc [-Hv]%n" +
                "  -H, -HELP      Show this help message and exit.%n" +
                "  -v, -VERSION   Print version information and exit.%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testReleaseNotesExample_WithoutPropertiesSet() {
        @Command(name = "app")
        class App {
            @Command(name = "status", description = "This command logs the ${COMMAND-NAME} for ${PARENT-COMMAND-NAME}.")
            void status(
                @Option(names = {"${dirOptionName1:--d}", "${dirOptionName2:---directories}"}, // -d or --directories
                        description = {"Specify one or more directories, separated by '${sys:path.separator}'.",
                                "The default is the user home directory (${DEFAULT-VALUE})."},
                        arity = "${sys:dirOptionArity:-1..*}",
                        defaultValue = "${sys:user.home}",
                        split = "${sys:path.separator}")
                String[] directories) { }
        }

        CommandLine status = new CommandLine(new App()).getSubcommands().get("status");
        String actual = status.getUsageMessage();
        String expected = String.format("" +
                "Usage: app status [-d=<arg0>[%1$s<arg0>...]...]...%n" +
                "This command logs the status for app.%n" +
                "  -d, --directories=<arg0>[%1$s<arg0>...]...%n" +
                "         Specify one or more directories, separated by '%1$s'.%n" +
                "         The default is the user home directory (%2$s).%n",
                System.getProperty("path.separator"), System.getProperty("user.home"));
        assertEquals(expected, actual);

        assertEquals(CommandLine.Range.valueOf("1..*"), status.getCommandSpec().findOption("d").arity());
    }

    @Test
    public void testReleaseNotesExample_WithPropertiesSet() {
        @Command(name = "app")
        class App {
            @Command(name = "status", description = "This command logs the ${COMMAND-NAME} for ${PARENT-COMMAND-NAME}.")
            void status(
                    @Option(names = {"${dirOptionName1:--d}", "${dirOptionName2:---directories}"}, // -d or --directories
                            description = {"Specify one or more directories, separated by '${sys:path.separator}'.",
                                    "The default is the user home directory (${DEFAULT-VALUE})."},
                            arity = "${sys:dirOptionArity:-1..*}",
                            defaultValue = "${sys:user.home}",
                            split = "${sys:path.separator}")
                            String[] directories) { }
        }

        System.setProperty("dirOptionName1", "-x");
        System.setProperty("dirOptionName2", "--extended");
        System.setProperty("dirOptionArity", "2..3");

        CommandLine status = new CommandLine(new App()).getSubcommands().get("status");
        String actual = status.getUsageMessage();
        String expected = String.format("" +
                        "Usage: app status [-x=<arg0>[%1$s<arg0>...] <arg0>[%1$s<arg0>...] [<arg0>]]...%n" +
                        "This command logs the status for app.%n" +
                        "  -x, --extended=<arg0>[%1$s<arg0>...] <arg0>[%1$s<arg0>...] [<arg0>]%n" +
                        "         Specify one or more directories, separated by '%1$s'.%n" +
                        "         The default is the user home directory (%2$s).%n",
                System.getProperty("path.separator"), System.getProperty("user.home"));
        assertEquals(expected, actual);

        assertEquals(CommandLine.Range.valueOf("2..3"), status.getCommandSpec().findOption("x").arity());
    }

    // https://github.com/remkop/picocli/issues/676
    @Test
    public void testIssue676() {
        class Issue676 {
            @Option(names="--mypath", defaultValue = "${sys:MYPATH}", required = true)
            private String path;
        }
        System.clearProperty("MYPATH");
        Issue676 bean = new Issue676();
        CommandLine cmd = new CommandLine(bean);
        try {
            cmd.parseArgs();
            fail("Expected exception");
        } catch (MissingParameterException ex) {
            assertEquals("Missing required option: '--mypath=<path>'", ex.getMessage());
        }

        System.setProperty("MYPATH", "abc");
        cmd.parseArgs();
        assertEquals("abc", bean.path);
    }

    @Test
    public void testInterpolateFallbackValue() {
        @Command(mixinStandardHelpOptions = false)
        class AppWithFallback {
            @Option(names="--mypath", fallbackValue = "${sys:user.home}",
                    description = "Path. Fallback=${FALLBACK-VALUE}.")
            private String path;
        }
        String expected = String.format("" +
                        "Usage: <main class> [--mypath=<path>]%n" +
                        "      --mypath=<path>   Path. Fallback=%1$s.%n",
                System.getProperty("user.home"));

        String actual = new CommandLine(new AppWithFallback()).getUsageMessage(CommandLine.Help.Ansi.OFF);
        assertEquals(expected, actual);
    }

    @Test
    public void testIssue723() {
        @Command(mixinStandardHelpOptions = false, showDefaultValues = true)
        class Issue723 {
            @Option(names="--mypath", defaultValue = "${sys:user.home}",
                    description = "Path. Default=${DEFAULT-VALUE}.")
            private String path;
        }
        String expected = String.format("" +
                "Usage: <main class> [--mypath=<path>]%n" +
                "      --mypath=<path>   Path. Default=%1$s.%n" +
                "                          Default: %1$s%n",
                System.getProperty("user.home"));

        String actual = new CommandLine(new Issue723()).getUsageMessage(CommandLine.Help.Ansi.OFF);
        assertEquals(expected, actual);
    }

    @Test
    public void testIssue723_withStandardHelpOptions() {
        @Command(mixinStandardHelpOptions = true, showDefaultValues = true)
        class Issue723 {
            @Option(names="--mypath", defaultValue = "${sys:user.home}",
                    description = "Path. Default=${DEFAULT-VALUE}.")
            private String path;
        }
        String expected = String.format("" +
                        "Usage: <main class> [-hV] [--mypath=<path>]%n" +
                        "  -h, --help            Show this help message and exit.%n" +
                        "      --mypath=<path>   Path. Default=%1$s.%n" +
                        "                          Default: %1$s%n" +
                        "  -V, --version         Print version information and exit.%n",
                System.getProperty("user.home"));

        String actual = new CommandLine(new Issue723()).getUsageMessage(CommandLine.Help.Ansi.OFF);
        assertEquals(expected, actual);
    }
}
