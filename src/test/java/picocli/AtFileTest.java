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
import picocli.CommandLine.ParseResult;

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
import java.util.ListResourceBundle;
import java.util.Set;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_AT_FILE_PARAMETER;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_COMMAND_LIST_HEADING;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_OPTION_LIST_HEADING;
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
            @Option(names = "-v")
            private boolean verbose;

            @Parameters
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
            @Parameters
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
        @Option(names = "--simpleArg")
        private boolean simple;

        @Option(names = "--argWithSpaces")
        private String withSpaces;

        @Option(names = "--quotedArg")
        private String quoted;

        @Option(names = "--multiArg", arity = "1..*")
        private List<String> strings;

        @Option(names = "--urlArg")
        private URL url;

        @Option(names = "--unescapedBackslashArg")
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
            @Option(names = "-v")
            private boolean verbose;

            @Parameters
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
        @Command
        class App { }
        assertTrue(new CommandLine(new App()).isExpandAtFiles());
    }

    @Test
    public void testAtFileExpandedRelative() {
        class App {
            @Option(names = "-v")
            private boolean verbose;

            @Parameters
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
            @Option(names = "-x")
            private boolean xxx;

            @Option(names = "-f")
            private String[] fff;

            @Option(names = "-v")
            private boolean verbose;

            @Parameters
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
            @Option(names = "-x")
            private boolean xxx;

            @Option(names = "-f")
            private String[] fff;

            @Option(names = "-v")
            private boolean verbose;

            @Parameters
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
            @Option(names = "-x")
            private boolean xxx;

            @Option(names = "-f")
            private String[] fff;

            @Option(names = "-v")
            private boolean verbose;

            @Parameters
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
            @Option(names = "-x")
            private boolean xxx;

            @Option(names = "-f")
            private String[] fff;

            @Option(names = "-v")
            private boolean verbose;

            @Parameters
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
            @Option(names = "-x")
            private boolean xxx;

            @Option(names = "-f")
            private String[] fff;

            @Option(names = "-v")
            private boolean verbose;

            @Parameters
            private List<String> files;
        }
        setTraceLevel(CommandLine.TraceLevel.OFF);
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
            @Parameters
            private List<String> files;
        }
        setTraceLevel(CommandLine.TraceLevel.INFO);
        File file = findFile("/argfile5-escapedAtValues.txt");
        App app = CommandLine.populateCommand(new App(), "aa", "@" + file.getAbsolutePath(), "bb");
        assertEquals(Arrays.asList("aa", "@val1", "@argfile5-escapedAtValues.txt", "bb"), app.files);
        assertTrue(systemErrRule.getLog(), systemErrRule.getLog().contains("Not expanding @-escaped argument"));
    }

    @Test
    public void testEscapedAtFileIsUnescapedButNotExpanded() {
        class App {
            @Parameters
            private List<String> files;
        }
        setTraceLevel(CommandLine.TraceLevel.OFF);
        File file = findFile("/argfile1.txt");
        App app = CommandLine.populateCommand(new App(), "aa", "@@" + file.getAbsolutePath(), "bb");
        assertEquals(Arrays.asList("aa", "@" + file.getAbsolutePath(), "bb"), app.files);
    }

    @Test
    public void testMultipleAtFilesExpandedMixedWithOtherParams() {
        class App {
            @Option(names = "-x")
            private boolean xxx;

            @Option(names = "-f")
            private String[] fff;

            @Option(names = "-v")
            private boolean verbose;

            @Parameters
            private List<String> files;
        }
        File file = findFile("/argfile1.txt");
        File file2 = findFile("/argfile2.txt");

        setTraceLevel(CommandLine.TraceLevel.OFF);
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
            @Option(names = "-x")
            private boolean xxx;

            @Option(names = "-f")
            private String[] fff;

            @Option(names = "-v")
            private boolean verbose;

            @Parameters
            private List<String> files;
        }
        File file = findFile("/argfile-with-nested-at-file.txt");
        File file2 = findFile("/argfile2.txt");
        File nested = new File("argfile2.txt");
        nested.delete();
        assertFalse("does not exist yet", nested.exists());
        copyFile(file2, nested);

        setTraceLevel(CommandLine.TraceLevel.OFF);
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
            @Option(names = "-x")
            private boolean xxx;

            @Option(names = "-f")
            private String[] fff;

            @Option(names = "-v")
            private boolean verbose;

            @Parameters
            private List<String> files;
        }
        File file = findFile("/argfile-with-recursive-at-file.txt");
        File localCopy = new File("argfile-with-recursive-at-file.txt");
        localCopy.delete();
        assertFalse("does not exist yet", localCopy.exists());
        copyFile(file, localCopy);

        setTraceLevel(CommandLine.TraceLevel.INFO);
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
            @Option(names = "-x")
            private boolean xxx;

            @Option(names = "-f")
            private String[] fff;

            @Option(names = "-v")
            private boolean verbose;

            @Parameters
            private List<String> files;
        }
        File file = findFile("/argfile-with-nested-at-file.txt");
        File nested = new File("argfile2.txt");
        nested.delete();
        assertFalse(nested + " does not exist", nested.exists());

        setTraceLevel(CommandLine.TraceLevel.INFO);
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
        @Command
        class A {}
        assertEquals((Character) '#', new CommandLine(new A()).getAtFileCommentChar());
    }

    @Test
    public void testAtFileExpansionExceptionHandling() throws Exception {
        Class<?> interpreterClass = Class.forName("picocli.CommandLine$Interpreter");
        Method m = interpreterClass.getDeclaredMethod("expandValidArgumentFile", String.class, File.class, List.class, Set.class);
        m.setAccessible(true);

        class App {
            @Parameters
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

    @Test
    public void testShowAtFileInUsageHelpBasic() {
        @Command(name = "myapp", mixinStandardHelpOptions = true,
                showAtFileInUsageHelp = true, description = "Example command.")
        class MyApp {
            @Parameters(description = "A file.") File file;
        }

        String actual = new CommandLine(new MyApp()).getUsageMessage();
        String expected = String.format("" +
                "Usage: myapp [-hV] [@<filename>...] <file>%n" +
                "Example command.%n" +
                "      [@<filename>...]   One or more argument files containing options.%n" +
                "      <file>             A file.%n" +
                "  -h, --help             Show this help message and exit.%n" +
                "  -V, --version          Print version information and exit.%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testShowAtFileInUsageHelpSystemProperties() {
        @Command(name = "myapp", mixinStandardHelpOptions = true,
                showAtFileInUsageHelp = true, description = "Example command.")
        class MyApp {
            @Parameters(description = "A file.") File file;
        }

        System.setProperty("picocli.atfile.label", "my@@@@file");
        System.setProperty("picocli.atfile.description", "@files rock!");

        String actual = new CommandLine(new MyApp()).getUsageMessage();
        String expected = String.format("" +
                "Usage: myapp [-hV] [my@@@@file...] <file>%n" +
                "Example command.%n" +
                "      [my@@@@file...]   @files rock!%n" +
                "      <file>            A file.%n" +
                "  -h, --help            Show this help message and exit.%n" +
                "  -V, --version         Print version information and exit.%n");
        assertEquals(expected, actual);
    }

    public static class MyResourceBundle extends ListResourceBundle {
        protected Object[][] getContents() {
            return new Object[][] {
                    {"picocli.atfile", "hi! I amd the @file description from a file"},
                    {"picocli.atfile.label", "BUNDLE@FILE"},
                    {"picocli.atfile.description", "BUNDLE @FILE DESCRIPTION"},
            };
        }
    }

    @Test
    public void testShowAtFileInUsageHelpResourceBundleWithSystemProps() {
        @Command(name = "A", mixinStandardHelpOptions = true, resourceBundle = "picocli.AtFileTest$MyResourceBundle",
                showAtFileInUsageHelp = true, description = "... description ...")
        class A { }

        System.setProperty("picocli.atfile.label", "my@@@@file");
        System.setProperty("picocli.atfile.description", "@files rock!");

        String actual = new CommandLine(new A()).setResourceBundle(new MyResourceBundle()).getUsageMessage();
        String expected = String.format("" +
                "Usage: A [-hV] [my@@@@file...]%n" +
                "... description ...%n" +
                "      [my@@@@file...]   hi! I amd the @file description from a file%n" +
                "  -h, --help            Show this help message and exit.%n" +
                "  -V, --version         Print version information and exit.%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testShowAtFileInUsageHelpResourceBundleWithoutSystemProps() {
        @Command(name = "A", mixinStandardHelpOptions = true, resourceBundle = "picocli.AtFileTest$MyResourceBundle",
                showAtFileInUsageHelp = true, description = "... description ...")
        class A { }

        String actual = new CommandLine(new A()).getUsageMessage();
        String expected = String.format("" +
                "Usage: A [-hV] [BUNDLE@FILE...]%n" +
                "... description ...%n" +
                "      [BUNDLE@FILE...]   hi! I amd the @file description from a file%n" +
                "  -h, --help             Show this help message and exit.%n" +
                "  -V, --version          Print version information and exit.%n");
        assertEquals(expected, actual);
    }


    @Test
    public void testAtFileParameterListSectionLast() {
        @Command(name = "A", mixinStandardHelpOptions = true,
                showAtFileInUsageHelp = true, description = "... description ...")
        class A { }

        CommandLine commandLine = new CommandLine(new A());
        List<String> helpSectionKeys = commandLine.getHelpSectionKeys();
        List<String> copy = new ArrayList<String>(helpSectionKeys);
        copy.remove(SECTION_KEY_AT_FILE_PARAMETER);
        copy.add(copy.indexOf(SECTION_KEY_COMMAND_LIST_HEADING), SECTION_KEY_AT_FILE_PARAMETER);
        commandLine.setHelpSectionKeys(copy);

        String actual = commandLine.getUsageMessage();
        String expected = String.format("" +
                "Usage: A [-hV] [@<filename>...]%n" +
                "... description ...%n" +
                "  -h, --help             Show this help message and exit.%n" +
                "  -V, --version          Print version information and exit.%n" +
                "      [@<filename>...]   One or more argument files containing options.%n" +
                "");
        assertEquals(expected, actual);
    }

    @Test
    public void testAtFileParameterListSectionBeforeOptions() {
        @Command(name = "A", mixinStandardHelpOptions = true,
                showAtFileInUsageHelp = true, description = "... description ...")
        class A {
            @Parameters(index = "0", arity = "1", description = "Some file.") File file;
            @Parameters(index = "1", description = "Some other file.") File anotherFile;
        }

        CommandLine commandLine = new CommandLine(new A());
        List<String> helpSectionKeys = commandLine.getHelpSectionKeys();
        List<String> copy = new ArrayList<String>(helpSectionKeys);
        copy.remove(SECTION_KEY_AT_FILE_PARAMETER);
        copy.add(helpSectionKeys.indexOf(SECTION_KEY_OPTION_LIST_HEADING), SECTION_KEY_AT_FILE_PARAMETER);
        commandLine.setHelpSectionKeys(copy);

        String actual = commandLine.getUsageMessage();
        String expected = String.format("" +
                "Usage: A [-hV] [@<filename>...] <file> <anotherFile>%n" +
                "... description ...%n" +
                "      <file>             Some file.%n" +
                "      <anotherFile>      Some other file.%n" +
                "      [@<filename>...]   One or more argument files containing options.%n" +
                "  -h, --help             Show this help message and exit.%n" +
                "  -V, --version          Print version information and exit.%n" +
                "");
        assertEquals(expected, actual);
    }

    @Test
    public void testAtFileParameterListHeadingShownIfNoOtherPositionalParameters() {
        @Command(name = "A", mixinStandardHelpOptions = true,
                showAtFileInUsageHelp = true,
                parameterListHeading = "Parameters:%n",
                optionListHeading = "Options:%n",
                description = "... description ...")
        class A { }

        String actual = new CommandLine(new A()).getUsageMessage();
        String expected = String.format("" +
                "Usage: A [-hV] [@<filename>...]%n" +
                "... description ...%n" +
                "Parameters:%n" +
                "      [@<filename>...]   One or more argument files containing options.%n" +
                "Options:%n" +
                "  -h, --help             Show this help message and exit.%n" +
                "  -V, --version          Print version information and exit.%n");
        assertEquals(expected, actual);
    }

    @Test
    public void testAtFileExpandedArgsParsed() {
        class App {
            @Option(names = "-v")
            private boolean verbose;

            @Parameters
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
        CommandLine commandLine = new CommandLine(new App());
        ParseResult parseResult = commandLine.parseArgs(new String[] {"@" + relative});

        assertEquals(Arrays.asList("1111", "-v", "2222", ";3333"), parseResult.expandedArgs());
    }

    @Ignore
    @Test
    public void testIssue1457() {
        class Issue1457 {
            @Option(names = { "-p" })
            String prefix = "";
        }
        File atFile = findFile("/argfile-issue1457.txt");
        Issue1457 obj = new Issue1457();
        CommandLine cmd = new CommandLine(obj);
        cmd.parseArgs("@" + atFile.getAbsolutePath());
        assertEquals("PREFIX", obj.prefix);
    }
}
