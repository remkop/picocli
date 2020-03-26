package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.DuplicateOptionAnnotationsException;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Option;

import java.util.ListResourceBundle;

import static org.junit.Assert.*;
import static picocli.CommandLine.ScopeType.INHERIT;
import static picocli.CommandLine.ScopeType.LOCAL;

public class InheritedOptionTest {
    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Rule
    // allows tests to set any kind of properties they like, without having to individually roll them back
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Command(subcommands = Sub.class)
    static class Top {
        @Option(names = "--verbose", scope = INHERIT)
        boolean verbose;
    }
    @Command(name = "sub", subcommands = SubSub.class)
    static class Sub { }

    @Command(name = "subsub")
    static class SubSub { }

    @Test
    public void testGlobalOptionIsAddedToSubcommand() {
        Top top = new Top();
        CommandLine cmd = new CommandLine(top);
        cmd.parseArgs("sub", "--verbose");
        assertTrue(top.verbose);
    }

    @Test
    public void testGlobalOptionIsAddedToSubSubcommand() {
        Top top = new Top();
        CommandLine cmd = new CommandLine(top);
        cmd.parseArgs("sub", "subsub", "--verbose");
        assertTrue(top.verbose);
    }

    @Test
    public void testGlobalOptionDisallowedIfSubcommandAlreadyHasGlobalOptionWithSameName() {
        Top top = new Top();
        CommandLine cmd = new CommandLine(top);

        class Other {
            @Option(names = "--verbose", scope = INHERIT)
            boolean verbose;
        }
        Other other = new Other();
        try {
            cmd.addSubcommand("other", other);
            fail("Expected exception");
        } catch (DuplicateOptionAnnotationsException ex) {
            String msg = String.format("Option name '--verbose' is used by both field boolean %s.verbose and field boolean %s.verbose",
                    top.getClass().getName(), other.getClass().getName());
            assertEquals(msg, ex.getMessage());
        }
        //cmd.parseArgs("other", "--verbose");
        //assertTrue(top.verbose);
        //assertFalse(other.verbose);
    }

    @Test
    public void testGlobalOptionDisallowedIfSubcommandAlreadyHasNonGlobalOptionWithSameName() {
        Top top = new Top();
        CommandLine cmd = new CommandLine(top);

        class Other {
            @Option(names = "--verbose") // local
            boolean verbose;
        }
        Other other = new Other();
        try {
            cmd.addSubcommand("other", other);
            fail("Expected exception");
        } catch (DuplicateOptionAnnotationsException ex) {
            String msg = String.format("Option name '--verbose' is used by both field boolean %s.verbose and field boolean %s.verbose",
                    top.getClass().getName(), other.getClass().getName());
            assertEquals(msg, ex.getMessage());
        }
    }

    static class Base {
        @Option(names = "--verbose", scope = INHERIT)
        boolean verbose;
    }

    @Command(name = "ext",
            subcommands = ExtSub.class,
            resourceBundle = "picocli.InheritedOptionTest$MyBundle") //MyBundle.class.getName()
    static class Ext extends Base{
    }
    
    @Command(name = "sub")
    static class ExtSub {
        @Command void subsub() {}
    }

    @Test
    public void testGlobalOptionInBaseClass() {
        //TestUtil.setTraceLevel("DEBUG");
        // both top-level command and subcommand extend from a base class where global option is defined
        Ext ext = new Ext();
        CommandLine cmd = new CommandLine(ext);
        cmd.parseArgs("sub", "subsub", "--verbose");
        assertTrue(ext.verbose);
    }

    public static class MyBundle extends ListResourceBundle { // used in Ext
        protected Object[][] getContents() {
            return new Object[][] {
                    {"verbose", "VERBOSE DESCRIPTION"},
                    {"sub.verbose", "SUB CUSTOM VERBOSE DESCRIPTION IS IGNORED"}
            };
        }
    }

    @Test
    public void testGlobalOptionDescriptionFromResourceBundle() {
        CommandLine cmd = new CommandLine(new Ext());
        String top = cmd.getUsageMessage();
        String expected = String.format("" +
                "Usage: ext [--verbose] [COMMAND]%n" +
                "      --verbose   VERBOSE DESCRIPTION%n" +
                "Commands:%n" +
                "  sub%n");
        assertEquals(expected, top);

        CommandLine subCmd = cmd.getSubcommands().get("sub");
        String sub = subCmd.getUsageMessage();
        String expectedSub = String.format("" +
                "Usage: ext sub [--verbose] [COMMAND]%n" +
                "      --verbose   VERBOSE DESCRIPTION%n" +
                "Commands:%n" +
                "  subsub%n");
        assertEquals(expectedSub, sub);

        CommandLine subsubCmd = subCmd.getSubcommands().get("subsub");
        String subsub = subsubCmd.getUsageMessage();
        String expectedSubSub = String.format("" +
                "Usage: ext sub subsub [--verbose]%n" +
                "      --verbose   VERBOSE DESCRIPTION%n");
        assertEquals(expectedSubSub, subsub);
    }

    @Test
    public void testProgrammaticOptionBuilderScopeLocalByDefault() {
        assertEquals(LOCAL, OptionSpec.builder("-a").scopeType());
    }

    @Test
    public void testProgrammaticOptionBuilderScopeMutable() {
        assertEquals(INHERIT, OptionSpec.builder("-a").scopeType(INHERIT).scopeType());
        assertEquals(INHERIT, OptionSpec.builder("-a").scopeType(INHERIT).build().scopeType());
    }

    @Test
    public void testProgrammaticOptionLocalByDefault() {
        assertEquals(LOCAL, OptionSpec.builder("-a").build().scopeType());
    }

    @Test
    public void testProgrammaticAddOptionBeforeSub() {
        OptionSpec optA = OptionSpec.builder("-a").scopeType(INHERIT).build();
        CommandSpec spec = CommandSpec.create();
        spec.add(optA);
        CommandSpec sub = CommandSpec.create();
        spec.addSubcommand("sub", sub);
        assertNotNull(spec.findOption("-a"));
        assertNotNull(sub.findOption("-a"));
    }

    @Test
    public void testProgrammaticAddOptionAfterSub() {
        OptionSpec optA = OptionSpec.builder("-a").scopeType(INHERIT).build();
        CommandSpec spec = CommandSpec.create();
        CommandSpec sub = CommandSpec.create();
        spec.addSubcommand("sub", sub);
        spec.add(optA);
        assertNotNull(spec.findOption("-a"));
        assertNotNull(sub.findOption("-a"));
    }

    @Test
    public void testProgrammaticPositionalParamBuilderScopeLocalByDefault() {
        assertEquals(LOCAL, PositionalParamSpec.builder().scopeType());
    }

    @Test
    public void testProgrammaticPositionalParamBuilderScopeMutable() {
        assertEquals(INHERIT, PositionalParamSpec.builder().scopeType(INHERIT).scopeType());
        assertEquals(INHERIT, PositionalParamSpec.builder().scopeType(INHERIT).build().scopeType());
    }

    @Test
    public void testProgrammaticPositionalParamLocalByDefault() {
        assertEquals(LOCAL, PositionalParamSpec.builder().build().scopeType());
    }

    @Test
    public void testProgrammaticAddPositionalParamBeforeSub() {
        PositionalParamSpec optA = PositionalParamSpec.builder().scopeType(INHERIT).build();
        CommandSpec spec = CommandSpec.create();
        spec.add(optA);
        CommandSpec sub = CommandSpec.create();
        spec.addSubcommand("sub", sub);
        assertFalse(spec.positionalParameters().isEmpty());
        assertFalse(sub.positionalParameters().isEmpty());
    }

    @Test
    public void testProgrammaticAddPositionalParamAfterSub() {
        PositionalParamSpec positional = PositionalParamSpec.builder().scopeType(INHERIT).build();
        CommandSpec spec = CommandSpec.create();
        CommandSpec sub = CommandSpec.create();
        spec.addSubcommand("sub", sub);
        spec.add(positional);
        assertFalse(spec.positionalParameters().isEmpty());
        assertFalse(sub.positionalParameters().isEmpty());
    }
}
