package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;

import static org.junit.Assert.*;
import static picocli.TestUtil.versionString;

public class VersionProviderTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    static class FailingVersionProvider implements CommandLine.IVersionProvider {
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
        } catch (CommandLine.ExecutionException ex) {
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
                CommandLine.IVersionProvider provider = (CommandLine.IVersionProvider) constructor.newInstance();
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

    static class MarkupVersionProvider implements CommandLine.IVersionProvider {
        public String[] getVersion() {
            return new String[] {
                    "@|yellow Versioned Command 1.0|@",
                    "@|blue Build 12345|@%1$s",
                    "@|red,bg(white) (c) 2017|@%2$s" };
        }
    }

    @Test
    public void testCommandLine_printVersionInfo_fromAnnotation_withMarkupAndParameterContainingMarkup() {
        @Command(versionProvider = MarkupVersionProvider.class)
        class Versioned {}

        CommandLine commandLine = new CommandLine(new Versioned());
        verifyVersionWithMarkup(commandLine);
    }

    @Test
    public void testCommandLine_printVersionInfo_usesProviderIfBothProviderAndStaticVersionInfoExist() {
        @Command(versionProvider = MarkupVersionProvider.class, version = "static version is ignored")
        class Versioned {}

        CommandLine commandLine = new CommandLine(new Versioned());
        verifyVersionWithMarkup(commandLine);
    }

    private void verifyVersionWithMarkup(CommandLine commandLine) {
        String[] args = {"@|bold VALUE1|@", "@|underline VALUE2|@", "VALUE3"};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true);
        commandLine.printVersionHelp(ps, CommandLine.Help.Ansi.ON, (Object[]) args);
        String result = baos.toString();
        assertEquals(String.format("" +
                "\u001B[33mVersioned Command 1.0\u001B[39m\u001B[0m%n" +
                "\u001B[34mBuild 12345\u001B[39m\u001B[0m\u001B[1mVALUE1\u001B[21m\u001B[0m%n" +
                "\u001B[31m\u001B[47m(c) 2017\u001B[49m\u001B[39m\u001B[0m\u001B[4mVALUE2\u001B[24m\u001B[0m%n"), result);
    }

    @Test
    public void testCommandLine_printVersionInfo_withMarkupAndParameterContainingMarkup() {
        @Command(version = {
                "@|yellow Versioned Command 1.0|@",
                "@|blue Build 12345|@%1$s",
                "@|red,bg(white) (c) 2017|@%2$s" })
        class Versioned {}

        CommandLine commandLine = new CommandLine(new Versioned());
        verifyVersionWithMarkup(commandLine);
    }

    @Test
    public void testMixinAnnotationWithVersionProvider() {
        class MyVersionProvider implements CommandLine.IVersionProvider {
            public String[] getVersion() {
                return new String[] {"line 1", "line 2"} ;
            }
        }
        @Command(version = "Mixin 1.0", versionProvider = MyVersionProvider.class)
        class MixMeIn {}

        class Receiver {
            @CommandLine.Mixin
            MixMeIn mixMeIn;
        }

        CommandLine commandLine = new CommandLine(new Receiver(), new InnerClassFactory(this));
        CommandSpec commandSpec = commandLine.getCommandSpec();
        assertTrue(commandSpec.versionProvider() instanceof MyVersionProvider);
        assertArrayEquals(new String[] {"line 1", "line 2"}, commandSpec.version());
    }
    static class BadVersionProvider implements CommandLine.IVersionProvider {
        public BadVersionProvider() {
            throw new IllegalStateException("bad class");
        }
        public String[] getVersion() throws Exception { return new String[0]; }
    }
    @Test
    public void testFailingVersionProviderWithDefaultFactory() {
        @Command(versionProvider = BadVersionProvider.class)
        class App { }
        try {
            new CommandLine(new App());
        } catch (CommandLine.InitializationException ex) {
            assertEquals("Could not instantiate class " +
                    "picocli.VersionProviderTest$BadVersionProvider: java.lang.reflect.InvocationTargetException", ex.getMessage());
        }
    }

    @Test
    public void testVersionHelp_versionProvider() {
        CommandLine.IVersionProvider provider = new CommandLine.IVersionProvider() {
            public String[] getVersion() {
                return new String[] {"2.0", "by provider"};
            }
        };
        CommandSpec spec = CommandSpec.create().versionProvider(provider);
        CommandLine commandLine = new CommandLine(spec);
        String actual = versionString(commandLine, CommandLine.Help.Ansi.OFF);
        String expected = String.format("" +
                "2.0%n" +
                "by provider%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testInitVersionProvider() {
        CommandLine.IVersionProvider versionProvider1 = new CommandLine.IVersionProvider() {
            public String[] getVersion() { return new String[0]; }
        };
        CommandLine.IVersionProvider versionProvider2 = new CommandLine.IVersionProvider() {
            public String[] getVersion() { return new String[0];  }
        };

        CommandSpec spec = CommandSpec.wrapWithoutInspection(null);
        spec.versionProvider(versionProvider1);

        CommandSpec mixin = CommandSpec.wrapWithoutInspection(null);
        mixin.versionProvider(versionProvider2);

        spec.addMixin("helper", mixin);
        assertSame(versionProvider1, spec.versionProvider());
    }

    @Test
    public void testDefaultValueProvider() {
        CommandLine.IDefaultValueProvider provider1 = new CommandLine.IDefaultValueProvider() {
            public String defaultValue(CommandLine.Model.ArgSpec argSpec) { return null; }
        };
        CommandLine.IDefaultValueProvider provider2 = new CommandLine.IDefaultValueProvider() {
            public String defaultValue(CommandLine.Model.ArgSpec argSpec) { return null; }
        };

        CommandSpec spec = CommandSpec.wrapWithoutInspection(null);
        spec.defaultValueProvider(provider1);

        CommandSpec mixin = CommandSpec.wrapWithoutInspection(null);
        mixin.defaultValueProvider(provider2);

        spec.addMixin("helper", mixin);
        assertSame(provider1, spec.defaultValueProvider());
    }

    @Test
    public void testCommandLine_printVersionInfo_printsArrayOfPlainTextStrings() {
        @Command(version = {"Versioned Command 1.0", "512-bit superdeluxe", "(c) 2017"}) class Versioned {}
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CommandLine(new Versioned()).printVersionHelp(new PrintStream(baos, true), CommandLine.Help.Ansi.OFF);
        String result = baos.toString();
        assertEquals(String.format("Versioned Command 1.0%n512-bit superdeluxe%n(c) 2017%n"), result);
    }

    @Test
    public void testCommandLine_printVersionInfo_printsSingleStringWithMarkup() {
        @Command(version = "@|red 1.0|@") class Versioned {}
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CommandLine(new Versioned()).printVersionHelp(new PrintStream(baos, true), CommandLine.Help.Ansi.ON);
        String result = baos.toString();
        assertEquals(String.format("\u001B[31m1.0\u001B[39m\u001B[0m%n"), result);
    }

    @Test
    public void testCommandLine_printVersionInfo_printsArrayOfStringsWithMarkup() {
        @Command(version = {
                "@|yellow Versioned Command 1.0|@",
                "@|blue Build 12345|@",
                "@|red,bg(white) (c) 2017|@" })
        class Versioned {}
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CommandLine(new Versioned()).printVersionHelp(new PrintStream(baos, true), CommandLine.Help.Ansi.ON);
        String result = baos.toString();
        assertEquals(String.format("" +
                "\u001B[33mVersioned Command 1.0\u001B[39m\u001B[0m%n" +
                "\u001B[34mBuild 12345\u001B[39m\u001B[0m%n" +
                "\u001B[31m\u001B[47m(c) 2017\u001B[49m\u001B[39m\u001B[0m%n"), result);
    }
    @Test
    public void testCommandLine_printVersionInfo_formatsArguments() {
        @Command(version = {"First line %1$s", "Second line %2$s", "Third line %s %s"}) class Versioned {}
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true);
        new CommandLine(new Versioned()).printVersionHelp(ps, CommandLine.Help.Ansi.OFF, "VALUE1", "VALUE2", "VALUE3");
        String result = baos.toString();
        assertEquals(String.format("First line VALUE1%nSecond line VALUE2%nThird line VALUE1 VALUE2%n"), result);
    }

    @Test
    public void testCommandLine_printVersionInfo_printsSinglePlainTextString() {
        @Command(version = "1.0") class Versioned {}
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CommandLine(new Versioned()).printVersionHelp(new PrintStream(baos, true), CommandLine.Help.Ansi.OFF);
        String result = baos.toString();
        assertEquals(String.format("1.0%n"), result);
    }

    @Test
    public void testPrintVersionHelp() {
        @Command(version = "abc 1.2.3 myversion")
        class App {
            @Option(names = "-V", versionHelp = true) boolean versionRequested;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CommandLine(new App()).printVersionHelp(new PrintStream(baos));
        assertEquals(String.format("abc 1.2.3 myversion%n"), baos.toString());
    }

    @Test
    public void testPrintVersionHelpPrintWriter() {
        @Command(version = "abc 1.2.3 myversion")
        class App {
            @Option(names = "-V", versionHelp = true) boolean versionRequested;
        }
        StringWriter sw = new StringWriter();
        new CommandLine(new App()).printVersionHelp(new PrintWriter(sw));
        assertEquals(String.format("abc 1.2.3 myversion%n"), sw.toString());
    }
    static class SpecInjectedVersionProvider implements CommandLine.IVersionProvider {
        @Spec CommandSpec spec;

        public String[] getVersion() {
            return new String[] { "Version info for " + spec.qualifiedName() };
        }
    }
    @Test
    public void testCommandLine_SpecInjectedVersionProvider() {
        @Command(name = "versioned", versionProvider = SpecInjectedVersionProvider.class, version = "ignored")
        class Versioned {
            @Command(versionProvider = SpecInjectedVersionProvider.class, version = "sub ignored")
            void sub() {}
        }

        CommandLine commandLine = new CommandLine(new Versioned());
        StringWriter sw = new StringWriter();
        commandLine.printVersionHelp(new PrintWriter(sw), CommandLine.Help.Ansi.OFF);
        assertEquals(String.format("Version info for versioned%n"), sw.toString());

        sw = new StringWriter();
        commandLine.getSubcommands().get("sub").printVersionHelp(new PrintWriter(sw), CommandLine.Help.Ansi.OFF);
        assertEquals(String.format("Version info for versioned sub%n"), sw.toString());
    }
}
