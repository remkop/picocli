package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.IDefaultValueProvider;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.PropertiesDefaultProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class DefaultProviderTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    static class TestDefaultProvider implements IDefaultValueProvider {
        public String defaultValue(ArgSpec argSpec) {
            return "Default provider string value";
        }
    }

    static class TestNullDefaultProvider implements IDefaultValueProvider {
        public String defaultValue(ArgSpec argSpec) {
            return null;
        }
    }

    @Command(defaultValueProvider = TestDefaultProvider.class,
            abbreviateSynopsis = true)
    static class App {
        @Option(names = "-a", description = "Default: ${DEFAULT-VALUE}")
        private String optionStringFieldWithoutDefaultNorInitialValue;
        @Option(names = "-b", description = "Default: ${DEFAULT-VALUE}", defaultValue = "Annotated default value")
        private String optionStringFieldWithAnnotatedDefault;
        @Option(names = "-c", description = "Default: ${DEFAULT-VALUE}", showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
        private String optionStringFieldWithInitDefault = "Initial default value";

        @Parameters(arity = "0..1", description = "Default: ${DEFAULT-VALUE}", showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
        private String paramStringFieldWithoutDefaultNorInitialValue;
        @Parameters(arity = "0..1", description = "Default: ${DEFAULT-VALUE}", defaultValue = "Annotated default value")
        private String paramStringFieldWithAnnotatedDefault;
        @Parameters(arity = "0..1", description = "Default: ${DEFAULT-VALUE}")
        private String paramStringFieldWithInitDefault = "Initial default value";

        private String stringForSetterDefault;

        @Option(names = "-d", description = "Default: ${DEFAULT-VALUE}", defaultValue = "Annotated setter default value")
        void setString(String val) {
            stringForSetterDefault = val;
        }
    }

    @Command(name = "sub")
    static class Sub {
        @Option(names = "-a")
        private String optionStringFieldWithoutDefaultNorInitialValue;
        @Option(names = "-b", defaultValue = "Annotated default value")
        private String optionStringFieldWithAnnotatedDefault;
        @Option(names = "-c")
        private String optionStringFieldWithInitDefault = "Initial default value";

        @Parameters(arity = "0..1")
        private String paramStringFieldWithoutDefaultNorInitialValue;
        @Parameters(arity = "0..1", defaultValue = "Annotated default value")
        private String paramStringFieldWithAnnotatedDefault;
        @Parameters(arity = "0..1")
        private String paramStringFieldWithInitDefault = "Initial default value";

        private String stringForSetterDefault;

        @Option(names = "-d", defaultValue = "Annotated setter default value")
        void setString(String val) {
            stringForSetterDefault = val;
        }
    }


    @Test
    public void testCommandDefaultProviderByAnnotationOverridesValues() {
        CommandLine cmd = new CommandLine(App.class);
        cmd.parseArgs();

        App app = cmd.getCommand();
        // if no default defined on the option, command default provider should be used
        assertEquals("Default provider string value", app.optionStringFieldWithoutDefaultNorInitialValue);
        assertEquals("Default provider string value", app.paramStringFieldWithoutDefaultNorInitialValue);
        // if a default is defined on the option either by annotation or by initial value, it must
        // override the default provider.
        assertEquals("Default provider string value", app.optionStringFieldWithAnnotatedDefault);
        assertEquals("Default provider string value", app.paramStringFieldWithAnnotatedDefault);

        assertEquals("Default provider string value", app.optionStringFieldWithInitDefault);
        assertEquals("Default provider string value", app.paramStringFieldWithInitDefault);

        assertEquals("Default provider string value", app.stringForSetterDefault);
    }

    @Test
    public void testCommandDefaultProviderDoesntOverridesDefaultsIfValueIsNull() {
        CommandLine cmd = new CommandLine(App.class);

        cmd.setDefaultValueProvider(new TestNullDefaultProvider());

        cmd.parseArgs();

        App app = cmd.getCommand();
        // if no default defined on the option, command default provider should be used
        assertNull(app.optionStringFieldWithoutDefaultNorInitialValue);
        assertNull(app.paramStringFieldWithoutDefaultNorInitialValue);
        // if a default is defined on the option either by annotation or by initial value, it must
        // override the default provider.
        assertEquals("Annotated default value", app.optionStringFieldWithAnnotatedDefault);
        assertEquals("Annotated default value", app.paramStringFieldWithAnnotatedDefault);

        assertEquals("Initial default value", app.optionStringFieldWithInitDefault);
        assertEquals("Initial default value", app.paramStringFieldWithInitDefault);

        assertEquals("Annotated setter default value", app.stringForSetterDefault);
    }

    @Test
    public void testDefaultProviderNullByDefault() {
        CommandLine cmd = new CommandLine(Sub.class);
        assertNull(cmd.getDefaultValueProvider());
    }

    @SuppressWarnings("unchecked")
    @Test(expected = UnsupportedOperationException.class)
    public void testNoDefaultProviderThrowsUnsupportedOperation() throws Exception {
        Class<IDefaultValueProvider> c = (Class<IDefaultValueProvider>) Class.forName("picocli.CommandLine$NoDefaultProvider");

        IDefaultValueProvider provider = CommandLine.defaultFactory().create(c);
        assertNotNull(provider);
        provider.defaultValue(CommandLine.Model.PositionalParamSpec.builder().build());
    }

    @Test
    public void testDefaultProviderReturnsSetValue() {
        CommandLine cmd = new CommandLine(Sub.class);
        TestDefaultProvider provider = new TestDefaultProvider();
        cmd.setDefaultValueProvider(provider);
        assertSame(provider, cmd.getDefaultValueProvider());
    }

    @Test
    public void testDefaultProviderPropagatedToSubCommand() {
        CommandLine cmd = new CommandLine(App.class);

        cmd.addSubcommand("sub", new CommandLine(Sub.class));

        CommandLine subCommandLine = cmd.getSubcommands().get("sub");
        cmd.setDefaultValueProvider(new TestDefaultProvider());

        assertNotNull(subCommandLine.getCommandSpec().defaultValueProvider());
        assertEquals(TestDefaultProvider.class, subCommandLine.getCommandSpec().defaultValueProvider().getClass());
    }

    @Test
    public void testCommandDefaultProviderSetting() {

        CommandLine cmd = new CommandLine(App.class);
        cmd.setDefaultValueProvider(new TestDefaultProvider());
        cmd.parseArgs();

        App app = cmd.getCommand();
        // if no default defined on the option, command default provider should be used
        assertEquals("Default provider string value", app.optionStringFieldWithoutDefaultNorInitialValue);
    }

    @Test
    public void testDefaultValueInDescription() {
        String expected = String.format("" +
                "Usage: <main class> [OPTIONS] [<paramStringFieldWithoutDefaultNorInitialValue>] [<paramStringFieldWithAnnotatedDefault>] [<paramStringFieldWithInitDefault>]%n" +
                "      [<paramStringFieldWithoutDefaultNorInitialValue>]%n" +
                "                 Default: Default provider string value%n" +
                "                   Default: Default provider string value%n" +
                "      [<paramStringFieldWithAnnotatedDefault>]%n" +
                "                 Default: Default provider string value%n" +
                "      [<paramStringFieldWithInitDefault>]%n" +
                "                 Default: Default provider string value%n" +
                "  -a=<optionStringFieldWithoutDefaultNorInitialValue>%n" +
                "                 Default: Default provider string value%n" +
                "  -b=<optionStringFieldWithAnnotatedDefault>%n" +
                "                 Default: Default provider string value%n" +
                "  -c=<optionStringFieldWithInitDefault>%n" +
                "                 Default: Default provider string value%n" +
                "                   Default: Default provider string value%n" +
                "  -d=<string>    Default: Default provider string value%n");
        CommandLine cmd = new CommandLine(App.class);
        assertEquals(expected, cmd.getUsageMessage(CommandLine.Help.Ansi.OFF));
    }

    @Test
    public void testDefaultValueInDescriptionAfterSetProvider() {
        String expected2 = String.format("" +
                "Usage: <main class> [OPTIONS] [<paramStringFieldWithoutDefaultNorInitialValue>] [<paramStringFieldWithAnnotatedDefault>] [<paramStringFieldWithInitDefault>]%n" +
                "      [<paramStringFieldWithoutDefaultNorInitialValue>]%n" +
                "                 Default: XYZ%n" +
                "                   Default: XYZ%n" +
                "      [<paramStringFieldWithAnnotatedDefault>]%n" +
                "                 Default: XYZ%n" +
                "      [<paramStringFieldWithInitDefault>]%n" +
                "                 Default: XYZ%n" +
                "  -a=<optionStringFieldWithoutDefaultNorInitialValue>%n" +
                "                 Default: XYZ%n" +
                "  -b=<optionStringFieldWithAnnotatedDefault>%n" +
                "                 Default: XYZ%n" +
                "  -c=<optionStringFieldWithInitDefault>%n" +
                "                 Default: XYZ%n" +
                "                   Default: XYZ%n" +
                "  -d=<string>    Default: XYZ%n");
        CommandLine cmd = new CommandLine(App.class);
        cmd.setDefaultValueProvider(new IDefaultValueProvider() {
            public String defaultValue(ArgSpec argSpec) throws
                    Exception {
                return "XYZ";
            }
        });
        assertEquals(expected2, cmd.getUsageMessage(CommandLine.Help.Ansi.OFF));
    }

    @Test
    public void testDefaultValueInDescriptionWithErrorProvider() {
        String expected2 = String.format("" +
                "Usage: <main class> [OPTIONS] [<paramStringFieldWithoutDefaultNorInitialValue>] [<paramStringFieldWithAnnotatedDefault>] [<paramStringFieldWithInitDefault>]%n" +
                "      [<paramStringFieldWithoutDefaultNorInitialValue>]%n" +
                "                 Default: null%n" +
                "                   Default: null%n" +
                "      [<paramStringFieldWithAnnotatedDefault>]%n" +
                "                 Default: Annotated default value%n" +
                "      [<paramStringFieldWithInitDefault>]%n" +
                "                 Default: Initial default value%n" +
                "  -a=<optionStringFieldWithoutDefaultNorInitialValue>%n" +
                "                 Default: null%n" +
                "  -b=<optionStringFieldWithAnnotatedDefault>%n" +
                "                 Default: Annotated default value%n" +
                "  -c=<optionStringFieldWithInitDefault>%n" +
                "                 Default: Initial default value%n" +
                "                   Default: Initial default value%n" +
                "  -d=<string>    Default: Annotated setter default value%n");
        CommandLine cmd = new CommandLine(App.class);
        cmd.setDefaultValueProvider(new IDefaultValueProvider() {
            public String defaultValue(ArgSpec argSpec) throws Exception {
                throw new IllegalStateException("abc");
            }
        });
        assertEquals(expected2, cmd.getUsageMessage(CommandLine.Help.Ansi.OFF));
    }
    static class FooDefaultProvider implements IDefaultValueProvider {
        public String defaultValue(ArgSpec argSpec) throws Exception {
            return "DURATION".equals(argSpec.paramLabel()) ? "1200" : null;
        }
    }

    @Test
    public void testIssue616DefaultProviderWithShowDefaultValues() {
        @Command(name = "foo", mixinStandardHelpOptions = true,
        defaultValueProvider = FooDefaultProvider.class,
        showDefaultValues = true)
        class FooCommand implements Runnable {

            @Option(names = {"-d", "--duration"}, paramLabel = "DURATION", arity = "1",
            description = "The duration, in seconds.")
            Integer duration;

            public void run() {
                System.out.printf("duration=%s%n", duration);
            }
        }

        String expected = String.format("" +
                "Usage: foo [-hV] [-d=DURATION]%n" +
                "  -d, --duration=DURATION   The duration, in seconds.%n" +
                "                              Default: 1200%n" +
                "  -h, --help                Show this help message and exit.%n" +
                "  -V, --version             Print version information and exit.%n");
        String actual = new CommandLine(new FooCommand()).getUsageMessage();
        assertEquals(expected, actual);
    }

    @Test
    public void testDefaultProvider_PropertiesConstructor() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("x", "xvalue");
        PropertiesDefaultProvider defaultProvider = new PropertiesDefaultProvider(properties);
        String value = defaultProvider.defaultValue(CommandLine.Model.OptionSpec.builder("-x").build());
        assertEquals("xvalue", value);
    }

    @Test
    public void testIssue962DefaultNotUsedIfArgumentSpecifiedOnCommandLine() {
        class App {
            List<Integer> specified = new ArrayList<Integer>();

            @Option(names = "--port", defaultValue = "${sys:TEST_PORT_962}", required = true)
            void setPort(Integer port) {
                specified.add(port);
            }

            @Option(names = "--field", defaultValue = "${sys:TEST_A_962}", required = true)
            Integer a;
        }
        System.setProperty("TEST_PORT_962", "xxx");
        System.setProperty("TEST_A_962", "xxx");

        App app1 = CommandLine.populateCommand(new App(), "--port=123", "--field=987");
        assertEquals((Integer) 987, app1.a);
        assertEquals(Arrays.asList(123), app1.specified);
    }

    @Test
    public void testIssue961DefaultNotUsedIfArgumentSpecifiedOnCommandLine() {
        class App {
            List<String> specified = new ArrayList<String>();

            @Option(names = "--option", defaultValue = "DEFAULT", required = true)
            void setValue(String value) {
                specified.add(value);
            }

            @Option(names = "--num", defaultValue = "INVALID", required = true)
            Integer a;
        }
        App app1 = CommandLine.populateCommand(new App(), "--option=123", "--num=987");
        assertEquals((Integer) 987, app1.a);
        assertEquals(Arrays.asList("123"), app1.specified);
    }

    static class NullDefaultProvider implements IDefaultValueProvider {
        public String defaultValue(ArgSpec argSpec) throws Exception {
            return ArgSpec.NULL_VALUE;
        }
    }

    @Test
    public void testNullDefault() {
        @Command(defaultValueProvider = NullDefaultProvider.class)
        class App {
            @Option(names = "-a")
            Integer a;
        }
        App app1 = CommandLine.populateCommand(new App());
        assertEquals(null, app1.a);
    }

    @Test
    public void testDefaultValueWithVariable() {
        @Command
        class App {
            @Option(names = "-a", defaultValue = "${VARIABLE:-555}")
            int a;
        }
        System.setProperty("VARIABLE", "123");
        App app1 = CommandLine.populateCommand(new App());
        assertEquals(123, app1.a);
    }

    @Test
    public void testDefaultValueWithVariableFallback() {
        @Command
        class App {
            @Option(names = "-a", defaultValue = "${VARIABLE:-555}")
            int a;
        }
        App app1 = CommandLine.populateCommand(new App());
        assertEquals(555, app1.a);
    }

    static class DefaultProviderWithVariables implements IDefaultValueProvider {
        static String value = "${VARIABLE:-555}";
        public String defaultValue(ArgSpec argSpec) throws Exception {
            return value;
        }
    }

    @Test
    public void testDefaultValueProviderWithVariablesUsesFallbackIfNoSystemPropEnvVarOrResourceBundle() {
        @Command(defaultValueProvider = DefaultProviderWithVariables.class)
        class App {
            @Option(names = "-a")
            int a;
        }
        App app1 = CommandLine.populateCommand(new App());
        assertEquals(555, app1.a);
    }

    @Test
    public void testDefaultValueProviderWithVariablesResolvesSystemProperty() {
        @Command(defaultValueProvider = DefaultProviderWithVariables.class)
        class App {
            @Option(names = "-a")
            int a;
        }
        System.setProperty("VARIABLE", "123");
        App app1 = CommandLine.populateCommand(new App());
        assertEquals(123, app1.a);
    }

    @Test
    public void testDefaultValueProviderWithVariablesResolvesResourceBundle() {
        @Command(defaultValueProvider = DefaultProviderWithVariables.class,
                resourceBundle = "picocli.DefaultProviderTestBundle")
        class App {
            @Option(names = "-a")
            int a;
        }
        App app1 = CommandLine.populateCommand(new App());
        assertEquals(789, app1.a);
    }

    @Test
    public void testDefaultValueProviderWithVariablesPrefersSystemPropertyOverResourceBundle() {
        @Command(defaultValueProvider = DefaultProviderWithVariables.class,
            resourceBundle = "picocli.DefaultProviderTestBundle")
        class App {
            @Option(names = "-a")
            int a;
        }
        System.setProperty("VARIABLE", "123");
        App app1 = CommandLine.populateCommand(new App());
        assertEquals(123, app1.a);
    }

    /**
     * Tests issue 1848 https://github.com/remkop/picocli/issues/1848
     * Test to ensure that ArgGroups with a multiplicity of 1, with a required option, and with a default value
     * provider, will properly show that required option as required, rather than optional.
     * */
    @Test
    public void testIssue1848ArgGroupWithRequiredOptionWithDefaultValueProvider() {

        @Command(name = "issue1848Command", defaultValueProvider = Issue1848CommandDefaultProvider.class)
        class App {
            @ArgGroup(exclusive = false, multiplicity = "1", order = 1)
            public Issue1848CommandConfigOptions issue1848CommandConfigOptions;

            @Option(names = {"--opt1"})
            private String opt1;
        }
        String helpOutput = new CommandLine(new App()).getHelp().fullSynopsis();
        assertTrue(helpOutput.contains("--opt2=<opt2>"));       // Check that "--opt2=<opt2>" exists.
        assertFalse(helpOutput.contains("[--opt2=<opt2>]"));    // But make sure it's not surrounded by square brackets.
    }

    class Issue1848CommandConfigOptions {
        @Option(names = {"--opt2"}, required = true, order = 1)
        private String opt2;

        @Option(names = {"--opt3"}, required = false, order = 2)
        private String opt3;
    }

    static class Issue1848CommandDefaultProvider implements CommandLine.IDefaultValueProvider {
        @Override
        public String defaultValue(CommandLine.Model.ArgSpec argSpec) throws Exception {
            // Commenting out for now as I'm unsure if it's expected behavior for default values supplied to a required
            // option, should result in that option's help/usage information indicating that the option is not required.
            /*
            if (argSpec.isOption()) {
                CommandLine.Model.OptionSpec option = (CommandLine.Model.OptionSpec) argSpec;
                if ("--url".equals(option.longestName())) {
                    return "https://localhost:8080";
                }
            }
            */
            return null;
        }
    }

}
