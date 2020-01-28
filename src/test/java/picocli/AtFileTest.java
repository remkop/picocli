package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.rules.TestRule;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static picocli.TestUtil.setTraceLevel;

public class AtFileTest {
    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");
    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();
    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

    private File findFile(String resource) {
        URL url = this.getClass().getResource(resource);
        assertNotNull(resource, url);
        String str = url.toString();
        return new File(str.substring(str.indexOf("file:") + 5));
    }

    @Test
    public void testAtFileExpandedAbsolute() {
        class App {
            @CommandLine.Option(names = "-v")
            private boolean verbose;

            @CommandLine.Parameters
            private List<String> files;
        }
        File file = findFile("/argfile1.txt");
        App app = CommandLine.populateCommand(new App(), "@" + file.getAbsolutePath());
        assertTrue(app.verbose);
        assertEquals(Arrays.asList("1111", "2222", ";3333"), app.files);
    }

    @Test
    public void testAtFileExpansionIgnoresSingleAtCharacter() {
        class App {
            @CommandLine.Parameters
            private List<String> files;
        }
        App app = CommandLine.populateCommand(new App(), "@", "abc");
        assertEquals(Arrays.asList("@", "abc"), app.files);
    }

    /**
     *
     *
     * @param source
     * @return Will have a newline at the end, no matter if the source file had one.
     * @throws IOException
     */
    static String readFile(File source) throws IOException {
        String newLine = System.getProperty("line.separator", "\n");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(source));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(newLine);
            }
            sb.append(newLine); // enforce a newline at the end
            return sb.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    static void writeFile(File target, String contents) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(target));
            writer.append(contents);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static class AtFileTestingApp {
        @CommandLine.Option(names = "--simpleArg")
        private boolean simple;

        @CommandLine.Option(names = "--argWithSpaces")
        private String withSpaces;

        @CommandLine.Option(names = "--quotedArg")
        private String quoted;

        @CommandLine.Option(names = "--multiArg", arity = "1..*")
        private List<String> strings;

        @CommandLine.Option(names = "--urlArg")
        private URL url;

        @CommandLine.Option(names = "--unescapedBackslashArg")
        private String unescaped;
    }

    @Test
    public void testUseSimplifiedAtFilesCanBeSetProgrammatically() {
        CommandLine.Model.ParserSpec parser = new CommandLine.Model.ParserSpec();
        assertFalse(parser.useSimplifiedAtFiles());

        parser.useSimplifiedAtFiles(true);
        assertTrue(parser.useSimplifiedAtFiles());
    }

    @Test
    public void testUseSimplifiedAtFilesFromSystemProperty() {
        CommandLine.Model.ParserSpec parser = new CommandLine.Model.ParserSpec();
        assertFalse(parser.useSimplifiedAtFiles());

        System.setProperty("picocli.useSimplifiedAtFiles", "true");
        assertTrue(parser.useSimplifiedAtFiles());
    }

    @Test
    public void testUseSimplifiedAtFilesFromSystemPropertyCaseInsensitive() {
        CommandLine.Model.ParserSpec parser = new CommandLine.Model.ParserSpec();
        assertFalse(parser.useSimplifiedAtFiles());

        System.setProperty("picocli.useSimplifiedAtFiles", "TRUE");
        assertTrue(parser.useSimplifiedAtFiles());
    }

    @Test
    public void testUseSimplifiedAtFilesFromEmptySystemProperty() {
        CommandLine.Model.ParserSpec parser = new CommandLine.Model.ParserSpec();
        assertFalse(parser.useSimplifiedAtFiles());

        System.setProperty("picocli.useSimplifiedAtFiles", "");
        assertTrue(parser.useSimplifiedAtFiles());
    }

    @Test
    public void testUseSimplifiedAtFilesIsOverriddenBySystemProperty() {
        CommandLine.Model.ParserSpec parser = new CommandLine.Model.ParserSpec();
        assertFalse(parser.useSimplifiedAtFiles());

        parser.useSimplifiedAtFiles(true);
        System.setProperty("picocli.useSimplifiedAtFiles", "false");
        assertFalse(parser.useSimplifiedAtFiles());
    }

    @Test
    public void testAtFileSimplified() throws IOException {
        System.setProperty("picocli.useSimplifiedAtFiles", "true");
        /*
         * first copy the old file and ensure it has a newline at the end. we do it this way to ensure that editors
         * can not mess up the file by removing the newline, therefore invalidating this test.
         */
        File oldFile = findFile("/argfile-simplified.txt");
        String contents = readFile(oldFile); // this is where we ensure the newline is there
        File newFile = File.createTempFile("picocli","atfile");
        writeFile(newFile, contents);
        AtFileTestingApp app = CommandLine.populateCommand(new AtFileTestingApp(), "@" + newFile.getAbsolutePath());
        assertTrue(app.simple);
        assertEquals("something with spaces", app.withSpaces);
        assertEquals("\"something else\"", app.quoted);
        assertEquals(Arrays.asList("something else", "yet something else"), app.strings);
        assertEquals("https://picocli.info/", app.url.toString());
        assertEquals("C:\\Program Files\\picocli.txt", app.unescaped);
    }

    @Test
    public void testAtFileEndingWithoutNewline() throws IOException {
        System.setProperty("picocli.useSimplifiedAtFiles", "true");
        /*
         * first copy the old file and ensure it has no newline at the end. we do it this way to ensure that editors
         * can not mess up the file by adding the newline, therefore invalidating this test.
         */
        File oldFile = findFile("/argfile-simplified.txt");
        String contents = readFile(oldFile).trim(); // this is where we remove the newline
        File newFile = File.createTempFile("picocli","atfile");
        writeFile(newFile, contents);
        // then use the new file as the CLI at-file
        AtFileTestingApp app = CommandLine.populateCommand(new AtFileTestingApp(), "@" + newFile.getAbsolutePath());
        assertTrue(app.simple);
        assertEquals("something with spaces", app.withSpaces);
        assertEquals("\"something else\"", app.quoted);
        assertEquals(Arrays.asList("something else", "yet something else"), app.strings);
        assertEquals("https://picocli.info/", app.url.toString());
        assertEquals("C:\\Program Files\\picocli.txt", app.unescaped);
    }

    @Test
    public void testAtFileSimplifiedWithQuotesTrimmed() {
        System.setProperty("picocli.useSimplifiedAtFiles", "");
        System.setProperty("picocli.trimQuotes", "true");
        File file = findFile("/argfile-simplified-quoted.txt");
        AtFileTestingApp app = CommandLine.populateCommand(new AtFileTestingApp(), "@" + file.getAbsolutePath());
        assertEquals("something else", app.quoted);
        assertEquals("https://picocli.info/", app.url.toString());
        assertEquals("C:\\Program Files\\picocli.txt", app.unescaped);
    }

    @Test
    public void testAtFileNotExpandedIfDisabled() {
        class App {
            @CommandLine.Option(names = "-v")
            private boolean verbose;

            @CommandLine.Parameters
            private List<String> files;
        }
        File file = findFile("/argfile1.txt");
        assertTrue(file.getAbsoluteFile().exists());
        App app = new App();
        new CommandLine(app).setExpandAtFiles(false).parseArgs("@" + file.getAbsolutePath());
        assertFalse(app.verbose);
        assertEquals(Arrays.asList("@" + file.getAbsolutePath()), app.files);
    }

    @Test
    public void testAtFileExpansionEnabledByDefault() {
        @CommandLine.Command
        class App { }
        assertTrue(new CommandLine(new App()).isExpandAtFiles());
    }

    @Test
    public void testAtFileExpandedRelative() {
        class App {
            @CommandLine.Option(names = "-v")
            private boolean verbose;

            @CommandLine.Parameters
            private List<String> files;
        }
        File file = findFile("/argfile1.txt");
        if (!file.getAbsolutePath().startsWith(System.getProperty("user.dir"))) {
            return;
        }
        String relative = file.getAbsolutePath().substring(System.getProperty("user.dir").length());
        if (relative.startsWith(File.separator)) {
            relative = relative.substring(File.separator.length());
        }
        App app = CommandLine.populateCommand(new App(), "@" + relative);
        assertTrue(app.verbose);
        assertEquals(Arrays.asList("1111", "2222", ";3333"), app.files);
    }

    @Test
    public void testAtFileExpandedMixedWithOtherParams() {
        class App {
            @CommandLine.Option(names = "-x")
            private boolean xxx;

            @CommandLine.Option(names = "-f")
            private String[] fff;

            @CommandLine.Option(names = "-v")
            private boolean verbose;

            @CommandLine.Parameters
            private List<String> files;
        }
        File file = findFile("/argfile1.txt");
        App app = CommandLine.populateCommand(new App(), "-f", "fVal1", "@" + file.getAbsolutePath(), "-x", "-f", "fVal2");
        assertTrue(app.verbose);
        assertEquals(Arrays.asList("1111", "2222", ";3333"), app.files);
        assertTrue(app.xxx);
        assertArrayEquals(new String[]{"fVal1", "fVal2"}, app.fff);
    }

    @Test
    public void testAtFileExpandedWithCommentsOff() {
        class App {
            @CommandLine.Option(names = "-x")
            private boolean xxx;

            @CommandLine.Option(names = "-f")
            private String[] fff;

            @CommandLine.Option(names = "-v")
            private boolean verbose;

            @CommandLine.Parameters
            private List<String> files;
        }
        File file = findFile("/argfile1.txt");
        App app = new App();
        CommandLine cmd = new CommandLine(app);
        cmd.setAtFileCommentChar(null);
        cmd.parseArgs("-f", "fVal1", "@" + file.getAbsolutePath(), "-x", "-f", "fVal2");
        assertTrue(app.verbose);
        assertEquals(Arrays.asList("#", "first", "comment", "1111", "2222", "#another", "comment", ";3333"), app.files);
        assertTrue(app.xxx);
        assertArrayEquals(new String[]{"fVal1", "fVal2"}, app.fff);
    }

    @Test
    public void testAtFileExpandedWithNonDefaultCommentChar() {
        class App {
            @CommandLine.Option(names = "-x")
            private boolean xxx;

            @CommandLine.Option(names = "-f")
            private String[] fff;

            @CommandLine.Option(names = "-v")
            private boolean verbose;

            @CommandLine.Parameters
            private List<String> files;
        }
        File file = findFile("/argfile1.txt");
        App app = new App();
        CommandLine cmd = new CommandLine(app);
        cmd.setAtFileCommentChar(';');
        cmd.parseArgs("-f", "fVal1", "@" + file.getAbsolutePath(), "-x", "-f", "fVal2");
        assertTrue(app.verbose);
        assertEquals(Arrays.asList("#", "first", "comment", "1111", "2222", "#another", "comment"), app.files);
        assertTrue(app.xxx);
        assertArrayEquals(new String[]{"fVal1", "fVal2"}, app.fff);
    }

    @Test
    public void testAtFileWithMultipleValuesPerLine() {
        class App {
            @CommandLine.Option(names = "-x")
            private boolean xxx;

            @CommandLine.Option(names = "-f")
            private String[] fff;

            @CommandLine.Option(names = "-v")
            private boolean verbose;

            @CommandLine.Parameters
            private List<String> files;
        }
        File file = findFile("/argfile3-multipleValuesPerLine.txt");
        App app = CommandLine.populateCommand(new App(), "-f", "fVal1", "@" + file.getAbsolutePath(), "-f", "fVal2");
        assertTrue(app.verbose);
        assertEquals(Arrays.asList("1111", "2222", "3333"), app.files);
        assertTrue(app.xxx);
        assertArrayEquals(new String[]{"fVal1", "FFFF", "F2F2F2", "fVal2"}, app.fff);
    }

    @Test
    public void testAtFileWithQuotedValuesContainingWhitespace() {
        class App {
            @CommandLine.Option(names = "-x")
            private boolean xxx;

            @CommandLine.Option(names = "-f")
            private String[] fff;

            @CommandLine.Option(names = "-v")
            private boolean verbose;

            @CommandLine.Parameters
            private List<String> files;
        }
        setTraceLevel("OFF");
        File file = findFile("/argfile4-quotedValuesContainingWhitespace.txt");
        App app = CommandLine.populateCommand(new App(), "-f", "fVal1", "@" + file.getAbsolutePath(), "-f", "fVal2");
        assertTrue(app.verbose);
        assertEquals(Arrays.asList("11 11", "22\n22", "3333"), app.files);
        assertTrue(app.xxx);
        assertArrayEquals(new String[]{"fVal1", "F F F F", "F2 F2 F2", "fVal2"}, app.fff);
    }

    @Test
    public void testAtFileWithExcapedAtValues() {
        class App {
            @CommandLine.Parameters
            private List<String> files;
        }
        setTraceLevel("INFO");
        File file = findFile("/argfile5-escapedAtValues.txt");
        App app = CommandLine.populateCommand(new App(), "aa", "@" + file.getAbsolutePath(), "bb");
        assertEquals(Arrays.asList("aa", "@val1", "@argfile5-escapedAtValues.txt", "bb"), app.files);
        assertTrue(this.systemErrRule.getLog().contains("Not expanding @-escaped argument"));
    }

    @Test
    public void testEscapedAtFileIsUnescapedButNotExpanded() {
        class App {
            @CommandLine.Parameters
            private List<String> files;
        }
        setTraceLevel("OFF");
        File file = findFile("/argfile1.txt");
        App app = CommandLine.populateCommand(new App(), "aa", "@@" + file.getAbsolutePath(), "bb");
        assertEquals(Arrays.asList("aa", "@" + file.getAbsolutePath(), "bb"), app.files);
    }

    @Test
    public void testMultipleAtFilesExpandedMixedWithOtherParams() {
        class App {
            @CommandLine.Option(names = "-x")
            private boolean xxx;

            @CommandLine.Option(names = "-f")
            private String[] fff;

            @CommandLine.Option(names = "-v")
            private boolean verbose;

            @CommandLine.Parameters
            private List<String> files;
        }
        File file = findFile("/argfile1.txt");
        File file2 = findFile("/argfile2.txt");

        setTraceLevel("OFF");
        App app = new App();
        CommandLine commandLine = new CommandLine(app).setOverwrittenOptionsAllowed(true);
        commandLine.setToggleBooleanFlags(true);
        commandLine.parseArgs("-f", "fVal1", "@" + file.getAbsolutePath(), "-x", "@" + file2.getAbsolutePath(),  "-f", "fVal2");
        assertFalse("invoked twice", app.verbose);
        assertEquals(Arrays.asList("1111", "2222", ";3333", "1111", "2222", "3333"), app.files);
        assertFalse("invoked twice", app.xxx);
        assertArrayEquals(new String[]{"fVal1", "FFFF", "F2F2F2", "fVal2"}, app.fff);
    }

    @Test
    public void testNestedAtFile() throws IOException {
        class App {
            @CommandLine.Option(names = "-x")
            private boolean xxx;

            @CommandLine.Option(names = "-f")
            private String[] fff;

            @CommandLine.Option(names = "-v")
            private boolean verbose;

            @CommandLine.Parameters
            private List<String> files;
        }
        File file = findFile("/argfile-with-nested-at-file.txt");
        File file2 = findFile("/argfile2.txt");
        File nested = new File("argfile2.txt");
        nested.delete();
        assertFalse("does not exist yet", nested.exists());
        copyFile(file2, nested);

        setTraceLevel("OFF");
        App app = new App();
        CommandLine commandLine = new CommandLine(app).setOverwrittenOptionsAllowed(true);
        commandLine.parseArgs("-f", "fVal1", "@" + file.getAbsolutePath(),  "-f", "fVal2");
        assertTrue("invoked in argFile2", app.verbose);
        assertEquals(Arrays.asList("abcdefg", "1111", "2222", "3333"), app.files);
        assertTrue("invoked in argFile2", app.xxx);
        assertArrayEquals(new String[]{"fVal1", "FFFF", "F2F2F2", "fVal2"}, app.fff);
        assertTrue("Deleted " + nested, nested.delete());
    }

    @Test
    public void testRecursiveNestedAtFileIgnored() throws IOException {
        class App {
            @CommandLine.Option(names = "-x")
            private boolean xxx;

            @CommandLine.Option(names = "-f")
            private String[] fff;

            @CommandLine.Option(names = "-v")
            private boolean verbose;

            @CommandLine.Parameters
            private List<String> files;
        }
        File file = findFile("/argfile-with-recursive-at-file.txt");
        File localCopy = new File("argfile-with-recursive-at-file.txt");
        localCopy.delete();
        assertFalse("does not exist yet", localCopy.exists());
        copyFile(file, localCopy);

        setTraceLevel("INFO");
        App app = new App();
        CommandLine commandLine = new CommandLine(app).setOverwrittenOptionsAllowed(true);
        commandLine.parseArgs("-f", "fVal1", "@" + localCopy.getAbsolutePath(),  "-f", "fVal2");
        assertEquals(Arrays.asList("abc defg", "xyz"), app.files);
        assertArrayEquals(new String[]{"fVal1", "fVal2"}, app.fff);
        assertFalse("not invoked", app.verbose);
        assertFalse("not invoked", app.xxx);
        assertTrue("Deleted " + localCopy, localCopy.delete());

        assertThat(systemErrRule.getLog(), containsString("[picocli INFO] Parsing 5 command line args [-f, fVal1, @"));
        assertThat(systemErrRule.getLog(), containsString("[picocli INFO] Expanding argument file @"));
        assertThat(systemErrRule.getLog(), containsString("[picocli INFO] Expanding argument file @argfile-with-recursive-at-file.txt"));
        assertThat(systemErrRule.getLog(), containsString("[picocli INFO] Already visited file "));
        assertThat(systemErrRule.getLog(), containsString("; ignoring..."));
    }

    @Test
    public void testNestedAtFileNotFound() throws IOException {
        class App {
            @CommandLine.Option(names = "-x")
            private boolean xxx;

            @CommandLine.Option(names = "-f")
            private String[] fff;

            @CommandLine.Option(names = "-v")
            private boolean verbose;

            @CommandLine.Parameters
            private List<String> files;
        }
        File file = findFile("/argfile-with-nested-at-file.txt");
        File nested = new File("argfile2.txt");
        nested.delete();
        assertFalse(nested + " does not exist", nested.exists());

        setTraceLevel("INFO");
        App app = new App();
        CommandLine commandLine = new CommandLine(app).setOverwrittenOptionsAllowed(true);
        commandLine.parseArgs("-f", "fVal1", "@" + file.getAbsolutePath(),  "-f", "fVal2");
        assertEquals(Arrays.asList("abcdefg", "@" + nested.getName()), app.files);
        assertArrayEquals(new String[]{"fVal1", "fVal2"}, app.fff);
        assertFalse("never invoked", app.verbose);
        assertFalse("never invoked", app.xxx);

        assertThat(systemErrRule.getLog(), containsString("[picocli INFO] Parsing 5 command line args [-f, fVal1, @"));
        assertThat(systemErrRule.getLog(), containsString("[picocli INFO] Expanding argument file @"));
        assertThat(systemErrRule.getLog(), containsString("[picocli INFO] Expanding argument file @argfile2.txt"));
        assertThat(systemErrRule.getLog(), containsString("[picocli INFO] File argfile2.txt does not exist or cannot be read; treating argument literally"));
    }

    private void copyFile(File source, File destination) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(destination);
            byte[] buff = new byte[(int) source.length()];
            int read = 0;
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
        } finally {
            if (in != null) { try { in.close(); } catch (Exception ignored) {} }
            if (out != null) { try { out.close(); } catch (Exception ignored) {} }
        }
    }

    @Test
    public void testGetAtFileCommentChar_SharpByDefault() {
        @CommandLine.Command
        class A {}
        assertEquals((Character) '#', new CommandLine(new A()).getAtFileCommentChar());
    }

    @Test
    public void testAtFileExpansionExceptionHandling() throws Exception {
        Class<?> interpreterClass = Class.forName("picocli.CommandLine$Interpreter");
        Method m = interpreterClass.getDeclaredMethod("expandValidArgumentFile", String.class, File.class, List.class, Set.class);
        m.setAccessible(true);

        class App {
            @CommandLine.Parameters
            private List<String> files;
        }
        App app = new App();
        CommandLine commandLine = new CommandLine(app);

        Field f = CommandLine.class.getDeclaredField("interpreter");
        f.setAccessible(true);
        Object interpreter = f.get(commandLine);
        try {
            m.invoke(interpreter, "fileName", null, new ArrayList<String>(), new HashSet<String>());
            fail("Expected exception");
        } catch (InvocationTargetException ex) {
            CommandLine.InitializationException actual = (CommandLine.InitializationException) ex.getCause();
            assertEquals("Could not read argument file @fileName", actual.getMessage());
            assertTrue(String.valueOf(actual.getCause()), actual.getCause() instanceof NullPointerException);
        }
    }
}
