package picocli;

import org.junit.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.IDefaultValueProvider;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.MissingParameterException;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ScopeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class InheritedDefaultProviderTest {
    @Command(name = "root", subcommands = Child.class)
    static class App {
        String value;
        int assignments;

        @Option(names = "--required", required = true, scope = ScopeType.INHERIT)
        void setValue(String value) {
            this.value = value;
            assignments++;
        }
    }

    @Command(name = "child", subcommands = Leaf.class)
    static class Child { }

    @Command(name = "leaf")
    static class Leaf { }

    static class RecordingProvider implements IDefaultValueProvider {
        final List<ArgSpec> requests = new ArrayList<ArgSpec>();
        final String value;

        RecordingProvider(String value) {
            this.value = value;
        }

        public String defaultValue(ArgSpec arg) {
            requests.add(arg);
            return value;
        }
    }

    @Test
    public void requiredInheritedOptionUsesProviderAtEveryDepth() {
        for (String[] args : new String[][] {{}, {"child"}, {"child", "leaf"}}) {
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            RecordingProvider provider = new RecordingProvider("default");
            cmd.setDefaultValueProvider(provider);

            cmd.parseArgs(args);

            assertEquals("default", app.value);
            assertEquals(1, app.assignments);
            assertEquals(1, provider.requests.size());
            assertSame(cmd.getCommandSpec().findOption("--required"), provider.requests.get(0));
        }
    }

    @Test
    public void explicitValueBeforeSubcommandSkipsProvider() {
        App app = new App();
        CommandLine cmd = new CommandLine(app);
        RecordingProvider provider = new RecordingProvider("default");
        cmd.setDefaultValueProvider(provider);

        cmd.parseArgs("--required", "explicit", "child", "leaf");

        assertEquals("explicit", app.value);
        assertTrue(provider.requests.isEmpty());
        assertEquals(1, app.assignments);
    }

    @Test
    public void explicitValueAfterSubcommandSkipsProvider() {
        App app = new App();
        CommandLine cmd = new CommandLine(app);
        RecordingProvider provider = new RecordingProvider("default");
        cmd.setDefaultValueProvider(provider);

        cmd.parseArgs("child", "leaf", "--required", "explicit");

        assertEquals("explicit", app.value);
        assertTrue(provider.requests.isEmpty());
        assertEquals(1, app.assignments);
    }

    @Test
    public void explicitValueBetweenSubcommandsSkipsProvider() {
        App app = new App();
        CommandLine cmd = new CommandLine(app);
        RecordingProvider provider = new RecordingProvider("default");
        cmd.setDefaultValueProvider(provider);

        cmd.parseArgs("child", "--required", "explicit", "leaf");

        assertEquals("explicit", app.value);
        assertTrue(provider.requests.isEmpty());
    }

    @Test(expected = MissingParameterException.class)
    public void nullProviderResultLeavesOptionRequired() {
        new CommandLine(new App()).setDefaultValueProvider(new RecordingProvider(null))
                .parseArgs("child", "leaf");
    }

    @Test(expected = MissingParameterException.class)
    public void optionWithoutProviderRemainsRequired() {
        new CommandLine(new App()).parseArgs("child", "leaf");
    }

    @Test
    public void inheritedDefaultUsesDefiningCommandsProvider() {
        App app = new App();
        CommandLine cmd = new CommandLine(app);
        RecordingProvider rootProvider = new RecordingProvider("root-default");
        RecordingProvider leafProvider = new RecordingProvider("leaf-default");
        cmd.setDefaultValueProvider(rootProvider);
        cmd.getSubcommands().get("child").getSubcommands().get("leaf")
                .setDefaultValueProvider(leafProvider);

        cmd.parseArgs("child", "leaf");

        assertEquals("root-default", app.value);
        assertEquals(1, rootProvider.requests.size());
        assertTrue(leafProvider.requests.isEmpty());
    }

    @Test
    public void inheritedDefaultUsesDefiningCommandsConverter() {
        OptionSpec option = OptionSpec.builder("--required").type(Value.class)
                .required(true).scopeType(ScopeType.INHERIT).build();
        CommandLine cmd = new CommandLine(CommandSpec.create().name("root").addOption(option));
        CommandLine child = new CommandLine(CommandSpec.create().name("child"));
        cmd.addSubcommand(child);
        cmd.setDefaultValueProvider(new RecordingProvider("default"));
        cmd.registerConverter(Value.class, new ITypeConverter<Value>() {
            public Value convert(String value) { return new Value("root:" + value); }
        });
        child.registerConverter(Value.class, new ITypeConverter<Value>() {
            public Value convert(String value) { return new Value("child:" + value); }
        });

        cmd.parseArgs("child");

        assertEquals("root:default", ((Value) option.getValue()).value);
    }

    static class Value {
        final String value;
        Value(String value) { this.value = value; }
    }

    @Test
    public void defaultsAreAppliedOnceOnEachParse() {
        App app = new App();
        CommandLine cmd = new CommandLine(app);
        RecordingProvider provider = new RecordingProvider("default");
        cmd.setDefaultValueProvider(provider);

        cmd.parseArgs("child", "leaf");
        cmd.parseArgs("child", "leaf");

        assertEquals("default", app.value);
        assertEquals(2, app.assignments);
        assertEquals(2, provider.requests.size());
    }

    @Test
    public void defaultCanBeDefinedOnAnIntermediateCommand() {
        CommandLine root = new CommandLine(CommandSpec.create().name("root"));
        OptionSpec option = OptionSpec.builder("--required").type(String.class)
                .required(true).scopeType(ScopeType.INHERIT).build();
        CommandLine middle = new CommandLine(CommandSpec.create().name("middle").addOption(option));
        middle.addSubcommand("leaf", CommandSpec.create().name("leaf"));
        root.addSubcommand(middle);
        RecordingProvider rootProvider = new RecordingProvider("root-default");
        RecordingProvider middleProvider = new RecordingProvider("middle-default");
        root.setDefaultValueProvider(rootProvider);
        middle.setDefaultValueProvider(middleProvider);

        root.parseArgs("middle", "leaf");

        assertEquals("middle-default", option.getValue());
        assertTrue(rootProvider.requests.isEmpty());
        assertEquals(1, middleProvider.requests.size());
        assertSame(option, middleProvider.requests.get(0));
    }

    @Test
    public void collectionDefaultIsNotAppendedTwice() {
        OptionSpec option = OptionSpec.builder("--required").type(List.class).auxiliaryTypes(String.class)
                .splitRegex(",").required(true).scopeType(ScopeType.INHERIT).build();
        CommandLine cmd = new CommandLine(CommandSpec.create().name("root").addOption(option));
        cmd.addSubcommand("child", CommandSpec.create().name("child"));
        RecordingProvider provider = new RecordingProvider("one,two");
        cmd.setDefaultValueProvider(provider);

        cmd.parseArgs("child");

        assertEquals(Arrays.asList("one", "two"), option.getValue());
        assertEquals(1, provider.requests.size());
    }

    @Command(name = "positional", subcommands = Child.class)
    static class PositionalApp {
        @Parameters(index = "0", scope = ScopeType.INHERIT)
        String value;
    }

    @Test
    public void requiredInheritedPositionalUsesProvider() {
        PositionalApp app = new PositionalApp();
        CommandLine cmd = new CommandLine(app);
        RecordingProvider provider = new RecordingProvider("default");
        cmd.setDefaultValueProvider(provider);

        cmd.parseArgs("child", "leaf");

        assertEquals("default", app.value);
        assertTrue(provider.requests.contains(cmd.getCommandSpec().positionalParameters().get(0)));
    }
}
