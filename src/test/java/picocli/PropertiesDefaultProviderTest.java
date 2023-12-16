package picocli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.rules.TestRule;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.PropertiesDefaultProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.*;

public class PropertiesDefaultProviderTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

    @Command(name = "classpathLoadedProvidertest",
        defaultValueProvider = PropertiesDefaultProvider.class)
    static class ClasspathLoadedDefaultsApp {
        @Option(names = "--aaa") int aaa;
        @Option(names = "-b", descriptionKey = "bbb") int bbb;
        @Parameters(index = "1", paramLabel = "ppp") int ppp;
        @Parameters(index = "0", paramLabel = "qqq", descriptionKey = "xxx") int xxx;
    }

    @Test
    public void testLoadFromResourceClasspathIfPropertySpecified() throws IOException {

        ClasspathLoadedDefaultsApp myApp = new ClasspathLoadedDefaultsApp();
        assertEquals(myApp.aaa, 0);
        assertEquals(myApp.bbb, 0);
        assertEquals(myApp.ppp, 0);
        assertEquals(myApp.xxx, 0);
        new CommandLine(myApp).parseArgs();

        assertEquals(myApp.aaa, 1001);
        assertEquals(myApp.bbb, 2002);
        assertEquals(myApp.ppp, 3003);
        assertEquals(myApp.xxx, 4004);
    }

    @Command(name = "providertest", subcommands = Subcommand.class,
        defaultValueProvider = PropertiesDefaultProvider.class)
    static class MyApp {
        @Option(names = "--aaa") int aaa;
        @Option(names = "-b", descriptionKey = "bbb") int bbb;
        @Parameters(index = "1", paramLabel = "ppp") int ppp;
        @Parameters(index = "0", paramLabel = "qqq", descriptionKey = "xxx") int xxx;
    }

    @Command(name = "providersub",
        defaultValueProvider = PropertiesDefaultProvider.class)
    static class Subcommand {
        @Option(names = "--aaa") int aaa;
        @Option(names = "-b", descriptionKey = "bbb") int bbb;
        @Parameters(index = "1", paramLabel = "ppp") int ppp;
        @Parameters(index = "0", paramLabel = "qqq", descriptionKey = "xxx") int xxx;
    }

    @Test
    public void testLoadFromUserHomeCommandNameByDefault() throws IOException {
        File f = new File(System.getProperty("user.home"), ".providertest.properties");
        if (f.exists()) {
            f.delete();
        }
        Properties expected = new Properties();
        expected.setProperty("aaa", "111");
        expected.setProperty("bbb", "222");
        expected.setProperty("ppp", "333");
        expected.setProperty("xxx", "444");
        expected.store(new FileOutputStream(f), "exported from test");

        MyApp myApp = new MyApp();
        assertEquals(myApp.aaa, 0);
        assertEquals(myApp.bbb, 0);
        assertEquals(myApp.ppp, 0);
        assertEquals(myApp.xxx, 0);
        new CommandLine(myApp).parseArgs();
        f.delete();

        assertEquals(myApp.aaa, 111);
        assertEquals(myApp.bbb, 222);
        assertEquals(myApp.ppp, 333);
        assertEquals(myApp.xxx, 444);
    }

    @Test
    public void testLoadFromDifferentLocationIfPropertySpecified() throws IOException {
        File tempFile = File.createTempFile("providertest", "properties");

        System.setProperty("picocli.defaults.providertest.path", tempFile.getAbsolutePath());

        if (tempFile.exists()) {
            tempFile.delete();
        }
        Properties expected = new Properties();
        expected.setProperty("aaa", "123");
        expected.setProperty("bbb", "234");
        expected.setProperty("ppp", "345");
        expected.setProperty("xxx", "456");
        expected.store(new FileOutputStream(tempFile), "exported from test to specific location");

        MyApp myApp = new MyApp();
        assertEquals(myApp.aaa, 0);
        assertEquals(myApp.bbb, 0);
        assertEquals(myApp.ppp, 0);
        assertEquals(myApp.xxx, 0);
        new CommandLine(myApp).parseArgs();
        tempFile.delete();
        assertEquals(myApp.aaa, 123);
        assertEquals(myApp.bbb, 234);
        assertEquals(myApp.ppp, 345);
        assertEquals(myApp.xxx, 456);
    }

    @Test
    public void testDefaultsForNestedSubcommandsCanBeLoadedFromTheirOwnFile() throws IOException {
        File parent = new File(System.getProperty("user.home"), ".providertest.properties");
        parent.delete();
        File f = new File(System.getProperty("user.home"), ".providersub.properties");
        if (f.exists()) {
            f.delete();
        }
        Properties expected = new Properties();
        expected.setProperty("aaa", "111");
        expected.setProperty("bbb", "222");
        expected.setProperty("ppp", "333");
        expected.setProperty("xxx", "444");
        expected.store(new FileOutputStream(f), "exported from test");

        MyApp myApp = new MyApp();
        //TestUtil.setTraceLevel(CommandLine.TraceLevel.DEBUG);
        CommandLine.ParseResult parseResult = new CommandLine(myApp).parseArgs("999", "888", "providersub");
        f.delete();

        assertEquals(myApp.aaa, 0);
        assertEquals(myApp.bbb, 0);
        assertEquals(myApp.ppp, 888);
        assertEquals(myApp.xxx, 999);

        Subcommand sub = (Subcommand) parseResult.subcommand().commandSpec().userObject();
        assertEquals(sub.aaa, 111);
        assertEquals(sub.bbb, 222);
        assertEquals(sub.ppp, 333);
        assertEquals(sub.xxx, 444);
    }

    @Test
    public void testDefaultsForNestedSubcommandsCanBeLoadedFromParentFile() throws IOException {
        File parent = new File(System.getProperty("user.home"), ".providertest.properties");
        parent.delete();
        File f = new File(System.getProperty("user.home"), ".providersub.properties");
        if (f.exists()) {
            f.delete();
        }
        Properties expected = new Properties();
        expected.setProperty("providertest.providersub.aaa", "111");
        expected.setProperty("providertest.providersub.bbb", "222");
        expected.setProperty("providertest.providersub.ppp", "333");
        expected.setProperty("providertest.providersub.xxx", "444");
        expected.store(new FileOutputStream(parent), "exported from test");

        MyApp myApp = new MyApp();
        //TestUtil.setTraceLevel(CommandLine.TraceLevel.DEBUG);
        CommandLine.ParseResult parseResult = null;
        try {
            parseResult = new CommandLine(myApp).parseArgs("999", "888", "providersub");
        } finally {
            f.delete();
        }

        assertEquals(myApp.aaa, 0);
        assertEquals(myApp.bbb, 0);
        assertEquals(myApp.ppp, 888);
        assertEquals(myApp.xxx, 999);

        Subcommand sub = (Subcommand) parseResult.subcommand().commandSpec().userObject();
        assertEquals(sub.aaa, 111);
        assertEquals(sub.bbb, 222);
        assertEquals(sub.ppp, 333);
        assertEquals(sub.xxx, 444);
    }

    @Command( name="myCommand")
    static class CommandIssue876 implements Runnable {

        @Option(names = "-x")
        int x;

        @ArgGroup()
        MyArgGroup anArgGroup;
        //MyArgGroup anArgGroup = new MyArgGroup(); // this does work!

        static class MyArgGroup {
            @Option(names = { "-y" }, descriptionKey= "myOption")
            int y;
        }

        public void run() {
            System.out.println("Option x is picked up: " + x);
            System.out.println("Option y inside arg group is not picked up (static class!): " + anArgGroup.y);
        }
    }

    @Ignore
    @Test
    public void testArgGroups() throws IOException {
        File temp = File.createTempFile("MyCommand", ".properties");
        FileWriter fw = new FileWriter(temp);
        fw.write("myCommand.x=6\n" +
                "myCommand.y=9\n" +
                "myCommand.myOption=9\n");
        fw.flush();
        fw.close();

        CommandLine cmd = new CommandLine(new CommandIssue876());
        cmd.setDefaultValueProvider(new PropertiesDefaultProvider(temp));
        cmd.execute();
    }

    @Test(expected = NullPointerException.class)
    public void testNullFile() {
        new PropertiesDefaultProvider((File) null);
    }

    @Test
    public void testNonExistingFile() {
        TestUtil.setTraceLevel(CommandLine.TraceLevel.DEBUG);
        new PropertiesDefaultProvider(new File("nosuchfile"));
        assertTrue(systemErrRule.getLog().startsWith("[picocli WARN] PropertiesDefaultProvider: defaults configuration file "));
        assertTrue(systemErrRule.getLog().endsWith(String.format("nosuchfile does not exist or is not readable%n")));
    }

    @Test
    public void testEmptyFile() throws Exception {
        File temp = File.createTempFile("MyCommand", ".properties");
        FileWriter fw = new FileWriter(temp);
        fw.flush();
        fw.close();

        PropertiesDefaultProvider provider = new PropertiesDefaultProvider(temp);
        String actual = provider.defaultValue(OptionSpec.builder("-x").build());
        assertNull(actual);
    }

    @Test
    public void testExistingFile() throws Exception {
        File temp = File.createTempFile("MyCommand", ".properties");
        FileWriter fw = new FileWriter(temp);
        fw.write("---=9\n");
        fw.flush();
        fw.close();

        PropertiesDefaultProvider provider = new PropertiesDefaultProvider(temp);
        String actual = provider.defaultValue(OptionSpec.builder("---").build());
        assertEquals("9", actual);

    }
}
