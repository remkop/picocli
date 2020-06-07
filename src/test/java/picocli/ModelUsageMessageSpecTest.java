package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Model.UsageMessageSpec;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;
import static picocli.TestUtil.usageString;

public class ModelUsageMessageSpecTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Test
    public void testEmptyModelUsageHelp() {
        CommandSpec spec = CommandSpec.create();
        CommandLine commandLine = new CommandLine(spec);
        String actual = usageString(commandLine, CommandLine.Help.Ansi.OFF);
        assertEquals(String.format("Usage: <main class>%n"), actual);
    }

    @Test
    public void testModelUsageHelp() {
        CommandSpec spec = CommandSpec.create();
        spec.addOption(OptionSpec.builder("-h", "--help").usageHelp(true).description("show help and exit").build());
        spec.addOption(OptionSpec.builder("-V", "--version").versionHelp(true).description("show help and exit").build());
        spec.addOption(OptionSpec.builder("-c", "--count").paramLabel("COUNT").arity("1").type(int.class).description("number of times to execute").build());
        spec.addOption(OptionSpec.builder("-f", "--fix").paramLabel("FIXED(BOOLEAN)").arity("1").hideParamSyntax(true).required(true).description("run with fixed option").build());
        CommandLine commandLine = new CommandLine(spec);
        String actual = usageString(commandLine, CommandLine.Help.Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> [-hV] [-c=COUNT] -f=FIXED(BOOLEAN)%n" +
                "  -c, --count=COUNT          number of times to execute%n" +
                "  -f, --fix=FIXED(BOOLEAN)   run with fixed option%n" +
                "  -h, --help                 show help and exit%n" +
                "  -V, --version              show help and exit%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testModelUsageHelpWithCustomSeparator() {
        CommandSpec spec = CommandSpec.create();
        spec.addOption(OptionSpec.builder("-h", "--help").usageHelp(true).description("show help and exit").build());
        spec.addOption(OptionSpec.builder("-V", "--version").versionHelp(true).description("show help and exit").build());
        spec.addOption(OptionSpec.builder("-c", "--count").paramLabel("COUNT").arity("1").type(int.class).description("number of times to execute").build());
        spec.addOption(OptionSpec.builder("-f", "--fix").paramLabel("FIXED(=BOOLEAN)").arity("1").hideParamSyntax(true).required(true).description("run with fixed option").build());
        CommandLine commandLine = new CommandLine(spec).setSeparator(" ");
        String actual = usageString(commandLine, CommandLine.Help.Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> [-hV] [-c COUNT] -f FIXED(=BOOLEAN)%n" +
                "  -c, --count COUNT   number of times to execute%n" +
                "  -f, --fix FIXED(=BOOLEAN)%n" +
                "                      run with fixed option%n" +
                "  -h, --help          show help and exit%n" +
                "  -V, --version       show help and exit%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testUsageHelpPositional_empty() {
        CommandSpec spec = CommandSpec.create();
        spec.addPositional(PositionalParamSpec.builder().build());
        CommandLine commandLine = new CommandLine(spec);
        String actual = usageString(commandLine, CommandLine.Help.Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> PARAM%n" +
                "      PARAM%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testUsageHelpPositional_withDescription() {
        CommandSpec spec = CommandSpec.create();
        spec.addPositional(PositionalParamSpec.builder().description("positional param").build());
        CommandLine commandLine = new CommandLine(spec);
        String actual = usageString(commandLine, CommandLine.Help.Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> PARAM%n" +
                "      PARAM   positional param%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testUsageHelp_emptyWithAutoHelpMixin() {
        CommandSpec spec = CommandSpec.create().addMixin("auto", CommandSpec.forAnnotatedObject(new CommandLine.AutoHelpMixin()));
        CommandLine commandLine = new CommandLine(spec);
        String actual = usageString(commandLine, CommandLine.Help.Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> [-hV]%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testUsageHelp_CustomizedUsageMessage() {
        CommandSpec spec = CommandSpec.create().addMixin("auto", CommandSpec.forAnnotatedObject(new CommandLine.AutoHelpMixin()));
        spec.name("the awesome util");
        spec.usageMessage()
                .descriptionHeading("Description heading%n")
                .description("description line 1", "description line 2")
                .footerHeading("Footer heading%n")
                .footer("footer line 1", "footer line 2")
                .headerHeading("Header heading%n")
                .header("header line 1", "header line 2")
                .optionListHeading("Options%n")
                .parameterListHeading("Positional Parameters%n");
        spec.addPositional(PositionalParamSpec.builder().description("positional param").build());
        CommandLine commandLine = new CommandLine(spec);
        String actual = usageString(commandLine, CommandLine.Help.Ansi.OFF);
        String expected = String.format("" +
                "Header heading%n" +
                "header line 1%n" +
                "header line 2%n" +
                "Usage: the awesome util [-hV] PARAM%n" +
                "Description heading%n" +
                "description line 1%n" +
                "description line 2%n" +
                "Positional Parameters%n" +
                "      PARAM       positional param%n" +
                "Options%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n" +
                "Footer heading%n" +
                "footer line 1%n" +
                "footer line 2%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testUsageHelp_abbreviateSynopsisWithoutPositional() throws UnsupportedEncodingException {
        CommandSpec spec = CommandSpec.create();
        spec.usageMessage().abbreviateSynopsis(true).requiredOptionMarker('!').sortOptions(false);
        spec.addOption(OptionSpec.builder("-x").required(true).description("required").build());
        CommandLine commandLine = new CommandLine(spec);
        String actual = usageString(commandLine, CommandLine.Help.Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> [OPTIONS]%n" +
                "! -x     required%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testUsageHelp_abbreviateSynopsisWithPositional() throws UnsupportedEncodingException {
        CommandSpec spec = CommandSpec.create();
        spec.usageMessage().abbreviateSynopsis(true).requiredOptionMarker('!').sortOptions(false);
        spec.addOption(OptionSpec.builder("-x").required(true).description("required").build());
        spec.addPositional(PositionalParamSpec.builder().arity("1").paramLabel("POSITIONAL").description("positional").build());
        CommandLine commandLine = new CommandLine(spec);
        String actual = usageString(commandLine, CommandLine.Help.Ansi.OFF);
        String expected = String.format("" +
                "Usage: <main class> [OPTIONS] POSITIONAL%n" +
                "!     POSITIONAL   positional%n" +
                "! -x               required%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testUsageHelp_width_default80() {
        assertEquals(80, UsageMessageSpec.DEFAULT_USAGE_WIDTH);
        assertEquals(UsageMessageSpec.DEFAULT_USAGE_WIDTH, new UsageMessageSpec().width());
    }

    @Test
    public void testUsageHelp_width_configurableWithSystemProperty() {
        System.setProperty("picocli.usage.width", "67");
        try {
            assertEquals(67, new UsageMessageSpec().width());
        } finally {
            System.clearProperty("picocli.usage.width");
        }
    }

    @Test
    public void testUsageHelp_width_SystemPropertyOverrulesSetValue() {
        System.setProperty("picocli.usage.width", "67");
        try {
            assertEquals(67, new UsageMessageSpec().width(123).width());
        } finally {
            System.clearProperty("picocli.usage.width");
        }
    }

    @Test
    public void testUsageHelp_width_setter() {
        UsageMessageSpec spec = new UsageMessageSpec();
        spec.width(67);
        assertEquals(67, spec.width());
    }

    @Test(expected = CommandLine.InitializationException.class)
    public void testUsageHelp_width_setterDisallowsValuesBelow55() {
        new UsageMessageSpec().width(54);
    }

    @Test
    public void testUsageHelp_width_setterAllowsValuesAt55OrHigher() {
        assertEquals(55, new UsageMessageSpec().width(55).width());
        assertEquals(Integer.MAX_VALUE, new UsageMessageSpec().width(Integer.MAX_VALUE).width());
    }
    @Test
    public void testUsageMessageSpec_synopsisSubcommandLabelSetter() {
        @Command(name = "blah")
        class MyApp {
            @Command void sub() {}
        }
        CommandLine cmd = new CommandLine(new MyApp());
        String expected = String.format("" +
                "Usage: blah [COMMAND]%n" +
                "Commands:%n" +
                "  sub%n");
        assertEquals(expected, cmd.getUsageMessage());

        UsageMessageSpec spec = cmd.getCommandSpec().usageMessage();
        assertEquals("[COMMAND]", spec.synopsisSubcommandLabel());

        spec.synopsisSubcommandLabel("xxx");
        assertEquals("xxx", spec.synopsisSubcommandLabel());
        String expectedAfter = String.format("" +
                "Usage: blah xxx%n" +
                "Commands:%n" +
                "  sub%n");
        assertEquals(expectedAfter, cmd.getUsageMessage());
    }

    @Test
    public void testUsageMessageSpec_showAtFileInUsageHelp() {
        @Command(name = "blah") class MyApp {}

        CommandLine cmd = new CommandLine(new MyApp());
        String expected = String.format("" +
                "Usage: blah%n");
        assertEquals(expected, cmd.getUsageMessage());

        UsageMessageSpec spec = cmd.getCommandSpec().usageMessage();
        assertFalse(spec.showAtFileInUsageHelp());

        spec.showAtFileInUsageHelp(true);
        assertTrue(spec.showAtFileInUsageHelp());
        String expectedAfter = String.format("" +
                "Usage: blah [@<filename>...]%n" +
                "      [@<filename>...]   One or more argument files containing options.%n");
        assertEquals(expectedAfter, cmd.getUsageMessage());
    }

    @Test
    public void testUsageMessageSpec_showEndOfOptionsDelimiterInUsageHelp() {
        @Command(name = "blah") class MyApp {}

        CommandLine cmd = new CommandLine(new MyApp());
        String expected = String.format("" +
                "Usage: blah%n");
        assertEquals(expected, cmd.getUsageMessage());

        UsageMessageSpec spec = cmd.getCommandSpec().usageMessage();
        assertFalse(spec.showEndOfOptionsDelimiterInUsageHelp());

        spec.showEndOfOptionsDelimiterInUsageHelp(true);
        assertTrue(spec.showEndOfOptionsDelimiterInUsageHelp());
        String expectedAfter = String.format("" +
                "Usage: blah [--]%n" +
                "  --     This option can be used to separate command-line options from the list%n" +
                "           of positional parameters.%n");
        assertEquals(expectedAfter, cmd.getUsageMessage());
    }

    @Test
    public void testUsageMessageSpec_updateFromCommand() {
        @Command(name = "blah", resourceBundle = "picocli.SharedMessages") class MyApp {}

        UsageMessageSpec spec = new UsageMessageSpec();
        assertNull(spec.messages());

        Command annotation = MyApp.class.getAnnotation(Command.class);
        CommandSpec commandSpec = CommandSpec.create();
        spec.updateFromCommand(annotation, commandSpec, false);

        CommandLine.Model.Messages messages = spec.messages();
        assertNotNull(messages);
        assertSame(commandSpec, messages.commandSpec());
        assertEquals("picocli.SharedMessages", messages.resourceBundleBaseName());
    }
}
