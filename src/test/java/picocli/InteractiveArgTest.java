package picocli;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.UnmatchedArgumentException;

import java.io.*;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class InteractiveArgTest {

    // allows tests to set any kind of properties they like, without having to individually roll them back
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    static class Streams {
        PrintStream out = System.out;
        PrintStream err = System.err;
        InputStream in = System.in;

        void reset() {
            System.setOut(out);
            System.setOut(err);
            System.setIn(in);
        }
    }

    static class Capture {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayOutputStream errBaos = new ByteArrayOutputStream();

        Capture() {
            this(null);
        }

        Capture(String input) {
            System.setOut(new PrintStream(baos));
            System.setErr(new PrintStream(errBaos));
            if (input != null) {
                System.setIn(new ByteArrayInputStream(input.getBytes()));
            }
        }

        static Capture multiInputValue(String input) {
            Capture result = new Capture(null);
            System.setIn(inputStream(input));
            return result;
        }

        private static ByteArrayInputStream inputStream(final String value) {
            return new ByteArrayInputStream(value.getBytes()) {
                int count;

                @Override
                public synchronized int read(byte[] b, int off, int len) {
                    System.arraycopy(value.getBytes(), 0, b, off, value.length());
                    return (count++ % 3) == 0 ? value.length() : -1;
                }
            };
        }

        String out() {
            return baos.toString();
        }

        String err() {
            return errBaos.toString();
        }
    }

    @After
    public void afterEachTest() {
//        CommandLine.tracer().setLevel(CommandLine.TraceLevel.WARN);
    }

    @Test
    public void testInteractiveOptionReadsFromStdIn() {
        class App {
            @Option(names = "-x", description = {"Pwd", "line2"}, interactive = true) int x;
            @Option(names = "-z") int z;
        }

        Streams streams = new Streams();
        System.setProperty("picocli.trace", "DEBUG");
        try {
            Capture capture = new Capture("1234567890");

            App app = new App();
            CommandLine cmd = new CommandLine(app);
            ParseResult result = cmd.parseArgs("-x");
            ArgSpec specX = result.matchedArgs().get(0);
            assertThat(specX.toString(), containsString("App.x"));

            assertEquals("Enter value for -x (Pwd): ", capture.out());
            assertEquals(1234567890, app.x);
            assertEquals(0, app.z);

            String trace = capture.err();
            assertThat(trace, containsString("User entered 10 characters"));
            assertThat(trace, containsString(
                "Setting " + specX.toString() + " to *****(masked) (interactive value)"));
            assertThat(trace, not(containsString("1234567890")));

            cmd.parseArgs("-z", "678");

            assertEquals(0, app.x);
            assertEquals(678, app.z);
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractiveOptionReadsFromStdInWithEchoing() {
        class App {
            @Option(names = "-x", description = {"Pwd", "line2"}, interactive = true, echo = true) int x;
        }

        Streams streams = new Streams();
        System.setProperty("picocli.trace", "DEBUG");
        try {
            Capture capture = new Capture("1234567890");

            App app = new App();
            CommandLine cmd = new CommandLine(app);
            ParseResult result = cmd.parseArgs("-x");
            ArgSpec specX = result.matchedArgs().get(0);
            assertThat(specX.toString(), containsString("App.x"));

            assertEquals("Enter value for -x (Pwd): ", capture.out());
            assertEquals(1234567890, app.x);

            String trace = capture.err();
            assertThat(trace, containsString("User entered 1234567890"));
            assertThat(trace, containsString(
                "Setting " + specX.toString() + " to 1234567890"));
            assertThat(trace, not(containsString("10 characters")));
            assertThat(trace, not(containsString("***")));
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractiveOptionWithoutDescriptionStandardPrompt() {
        class App {
            @Option(names = "-x", interactive = true) int x;
        }

        Streams streams = new Streams();
        try {
            Capture capture = new Capture("123");

            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parseArgs("-x");

            assertEquals("Enter value for -x: ", capture.out());
            assertEquals(123, app.x);
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractiveOptionReadsFromStdInWithCustomPrompt() {
        class App {
            @Option(names = "-x", description = {"Pwd", "line2"}, interactive = true, prompt = "[Customized]Enter your X: ") int x;
        }

        Streams streams = new Streams();
        try {
            Capture capture = new Capture("123");

            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parseArgs("-x");

            assertEquals("[Customized]Enter your X: ", capture.out());
            assertEquals(123, app.x);
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractiveOptionAsListOfIntegers() throws IOException {
        class App {
            @Option(names = "-x", description = {"Pwd", "line2"}, interactive = true)
            List<Integer> x;

            @Option(names = "-z")
            int z;
        }

        Streams streams = new Streams();
        System.setProperty("picocli.trace", "DEBUG");
        try {
            Capture capture = Capture.multiInputValue("1234567890");
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            ParseResult result = cmd.parseArgs("-x", "-x");
            ArgSpec specX = result.matchedArgs().get(0);
            assertThat(specX.toString(), containsString("App.x"));

            assertEquals("Enter value for -x (Pwd): Enter value for -x (Pwd): ", capture.out());
            assertEquals(Arrays.asList(1234567890, 1234567890), app.x);
            assertEquals(0, app.z);

            String trace = capture.err();
            assertThat(trace, containsString("User entered 10 characters"));
            assertThat(trace, containsString(
                "Adding *** (masked interactive value) to " + specX.toString()
                    + " for option -x on " + app.getClass().getSimpleName()));
            assertThat(trace, not(containsString("1234567890")));

            cmd.parseArgs("-z", "678");

            assertNull(app.x);
            assertEquals(678, app.z);
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractiveOptionAsListOfIntegersWithEchoing() {
        class App {
            @Option(names = "-x", description = {"Pwd", "line2"}, interactive = true, echo = true)
            List<Integer> x;

            @Option(names = "-z")
            int z;
        }

        Streams streams = new Streams();
        System.setProperty("picocli.trace", "DEBUG");
        try {
            Capture capture = Capture.multiInputValue("1234567890");
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            ParseResult result = cmd.parseArgs("-x", "-x");
            ArgSpec specX = result.matchedArgs().get(0);
            assertThat(specX.toString(), containsString("App.x"));

            assertEquals("Enter value for -x (Pwd): Enter value for -x (Pwd): ", capture.out());
            assertEquals(Arrays.asList(1234567890, 1234567890), app.x);
            assertEquals(0, app.z);

            String trace = capture.err();
            assertThat(trace, containsString("User entered 1234567890"));
            assertThat(trace, containsString(
                "Adding 1234567890 (interactive value) to "
                    + specX.toString() + " for option -x on " + app.getClass().getSimpleName()));
            assertThat(trace, not(containsString("10 characters")));
            assertThat(trace, not(containsString("***")));
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractiveOptionAsListOfIntegersWithCustomPrompt() {
        class App {
            @Option(names = "-x", description = {"Pwd", "line2"}, interactive = true, prompt = "[Customized]Enter your x: ")
            List<Integer> x;

            @Option(names = "-z")
            int z;
        }

        Streams streams = new Streams();
        try {
            Capture capture = Capture.multiInputValue("123");
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parseArgs("-x", "-x");

            assertEquals("[Customized]Enter your x: [Customized]Enter your x: ", capture.out());
            assertEquals(Arrays.asList(123, 123), app.x);
            assertEquals(0, app.z);
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractiveOptionAsListOfCharArrays() throws IOException {
        class App {
            @Option(names = "-x", description = {"Pwd", "line2"}, interactive = true)
            List<char[]> x;

            @Option(names = "-z")
            int z;
        }

        Streams streams = new Streams();
        try {
            Capture capture = Capture.multiInputValue("123");
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parseArgs("-x", "-x");

            assertEquals("Enter value for -x (Pwd): Enter value for -x (Pwd): ", capture.out());
            assertEquals(2, app.x.size());
            assertArrayEquals("123".toCharArray(), app.x.get(0));
            assertArrayEquals("123".toCharArray(), app.x.get(1));
            assertEquals(0, app.z);

            cmd.parseArgs("-z", "678");

            assertNull(app.x);
            assertEquals(678, app.z);
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractiveOptionAsCharArray() throws IOException {
        class App {
            @Option(names = "-x", description = {"Pwd", "line2"}, interactive = true)
            char[] x;

            @Option(names = "-z")
            int z;
        }

        Streams streams = new Streams();
        try {
            Capture capture = new Capture("123");
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parseArgs("-x");

            assertEquals("Enter value for -x (Pwd): ", capture.out());
            assertArrayEquals("123".toCharArray(), app.x);
            assertEquals(0, app.z);
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractiveOptionArity_0_1_ConsumesFromCommandLineIfPossible() throws IOException {
        class App {
            @Option(names = "-x", arity = "0..1", interactive = true)
            char[] x;

            @Parameters()
            String[] remainder;
        }

        Streams streams = new Streams();
        try {
            Capture capture = new Capture("123");
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parseArgs("-x", "456", "abc");

            assertArrayEquals("456".toCharArray(), app.x);
            assertArrayEquals(new String[]{"abc"}, app.remainder);
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractiveOptionAsListOfCharArraysArity_0_1_ConsumesFromCommandLineIfPossible() throws IOException {
        class App {
            @Option(names = "-x", arity = "0..1", interactive = true)
            List<char[]> x;

            @Parameters()
            String[] remainder;
        }

        Streams streams = new Streams();
        try {
            Capture capture = new Capture("123");
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parseArgs("-x", "456", "-x", "789", "abc");

            assertEquals(2, app.x.size());
            assertArrayEquals("456".toCharArray(), app.x.get(0));
            assertArrayEquals("789".toCharArray(), app.x.get(1));
            assertArrayEquals(new String[]{"abc"}, app.remainder);
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractiveOptionArity_0_1_AvoidsConsumingOption() throws IOException {
        class App {
            @Option(names = "-x", arity = "0..1", interactive = true)
            char[] x;

            @Option(names = "-z")
            int z;

            @Parameters()
            String[] remainder;
        }

        Streams streams = new Streams();
        try {
            Capture capture = new Capture("123");
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parseArgs("-x", "-z", "456", "abc");

            assertArrayEquals("123".toCharArray(), app.x);
            assertEquals(456, app.z);
            assertArrayEquals(new String[]{"abc"}, app.remainder);
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractiveOptionAsListOfCharArraysArity_0_1_AvoidsConsumingOption() throws IOException {
        class App {
            @Option(names = "-x", arity = "0..1", interactive = true)
            List<char[]> x;

            @Option(names = "-z")
            int z;

            @Parameters()
            String[] remainder;
        }

        Streams streams = new Streams();
        try {
            Capture capture = new Capture("123");
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parseArgs("-x", "-z", "456", "abc");

            assertEquals(1, app.x.size());
            assertArrayEquals("123".toCharArray(), app.x.get(0));
            assertEquals(456, app.z);
            assertArrayEquals(new String[]{"abc"}, app.remainder);
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractiveOptionArity_0_1_ConsumesUnknownOption() throws IOException {
        class App {
            @Option(names = "-x", arity = "0..1", interactive = true)
            char[] x;

            @Option(names = "-z")
            int z;

            @Parameters()
            String[] remainder;
        }

        Streams streams = new Streams();
        try {
            Capture capture = new Capture("123");
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parseArgs("-x", "-y", "456", "abc");

            assertArrayEquals("-y".toCharArray(), app.x);
            assertEquals(0, app.z);
            assertArrayEquals(new String[]{"456", "abc"}, app.remainder);
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractiveOptionAsListOfCharArraysArity_0_1_ConsumesUnknownOption() throws IOException {
        class App {
            @Option(names = "-x", arity = "0..1", interactive = true)
            List<char[]> x;

            @Option(names = "-z")
            int z;

            @Parameters()
            String[] remainder;
        }

        Streams streams = new Streams();
        try {
            Capture capture = new Capture("123");
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parseArgs("-x", "-y", "-x", "-w", "456", "abc");

            assertEquals(2, app.x.size());
            assertArrayEquals("-y".toCharArray(), app.x.get(0));
            assertArrayEquals("-w".toCharArray(), app.x.get(1));
            assertEquals(0, app.z);
            assertArrayEquals(new String[]{"456", "abc"}, app.remainder);
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractiveOptionReadsFromStdInMultiLinePrompt() {
        class App {
            @Option(names = "-x", description = {"Pwd%nline2", "ignored"}, interactive = true) int x;
            @Option(names = "-z") int z;
        }

        Streams streams = new Streams();
        try {
            Capture capture = new Capture("123");

            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parseArgs("-x", "-z", "987");

            String expectedPrompt = format("Enter value for -x (Pwd%nline2): ");
            assertEquals(expectedPrompt, capture.out());
            assertEquals(123, app.x);
            assertEquals(987, app.z);
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractivePositionalReadsFromStdIn() {
        class App {
            @Parameters(index = "0", description = {"Pwd%nline2", "ignored"}, interactive = true) int x;
            @Parameters(index = "1") int z;
        }

        Streams streams = new Streams();
        System.setProperty("picocli.trace", "DEBUG");
        try {
            Capture capture = new Capture("1234567890");

            App app = new App();
            CommandLine cmd = new CommandLine(app);
            ParseResult result = cmd.parseArgs("987");
            ArgSpec specX = result.matchedArgs().get(0);
            assertThat(specX.toString(), containsString("App.x"));

            String expectedPrompt = format("Enter value for position 0 (Pwd%nline2): ");
            assertEquals(expectedPrompt, capture.out());
            assertEquals(1234567890, app.x);
            assertEquals(987, app.z);

            String trace = capture.err();
            assertThat(trace, containsString("User entered 10 characters"));
            assertThat(trace, containsString(
                "Setting " + specX.toString() + " to *****(masked) (interactive value)"));
            assertThat(trace, not(containsString("1234567890")));
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractivePositionalReadsFromStdInWithEchoing() {
        class App {
            @Parameters(index = "0", description = {"Pwd%nline2", "ignored"}, interactive = true, echo = true) int x;
            @Parameters(index = "1") int z;
        }

        Streams streams = new Streams();
        System.setProperty("picocli.trace", "DEBUG");
        try {
            Capture capture = new Capture("1234567890");

            App app = new App();
            CommandLine cmd = new CommandLine(app);
            ParseResult result = cmd.parseArgs("987");
            ArgSpec specX = result.matchedArgs().get(0);
            assertThat(specX.toString(), containsString("App.x"));

            String expectedPrompt = format("Enter value for position 0 (Pwd%nline2): ");
            assertEquals(expectedPrompt, capture.out());
            assertEquals(1234567890, app.x);
            assertEquals(987, app.z);

            String trace = capture.err();
            assertThat(trace, containsString("User entered 1234567890"));
            assertThat(trace, containsString(
                "Setting " + specX.toString() + " to 123"));
            assertThat(trace, not(containsString("10 characters")));
            assertThat(trace, not(containsString("***")));
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractivePositionalReadsFromStdInWithCustomPrompt() {
        class App {
            @Parameters(index = "0", description = {"Pwd%nline2", "ignored"}, interactive = true, prompt = "[Customized]Enter your value: ") int x;
            @Parameters(index = "1") int z;
        }

        Streams streams = new Streams();
        try {
            Capture capture = new Capture("123");

            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parseArgs("987");

            String expectedPrompt = format("[Customized]Enter your value: ");
            assertEquals(expectedPrompt, capture.out());
            assertEquals(123, app.x);
            assertEquals(987, app.z);
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractivePositionalDoesntReadFromStdInIfNoFollowingPositionalParam() {
        class App {
            @Parameters(index = "0", interactive = true, description = {"Pwd"})
            int x;

            @Option(names = "-s") String str;
        }

        Streams streams = new Streams();
        try {
            Capture capture = new Capture("123");

            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parseArgs("-s", "abc");

            assertEquals("abc", app.str);
            //assertEquals(123, app.x);
            assertEquals("No value was read from console", 0, app.x);
            //String expectedPrompt = format("Enter value for position 0 (Pwd): ");
            String expectedPrompt = ""; // interactive arg was not prompted
            assertEquals(expectedPrompt, capture.out());
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractivePositional2ReadsFromStdIn() {
        class App {
            @Parameters(index = "0") int a;
            @Parameters(index = "1", description = {"Pwd%nline2", "ignored"}, interactive = true) int x;
            @Parameters(index = "2") int z;
        }

        Streams streams = new Streams();
        try {
            Capture capture = new Capture("123");

            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parseArgs("333", "987");

            String expectedPrompt = format("Enter value for position 1 (Pwd%nline2): ");
            assertEquals(expectedPrompt, capture.out());
            assertEquals(333, app.a);
            assertEquals(123, app.x);
            assertEquals(987, app.z);
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testLoginExample() {
        class Login implements Callable<Object> {
            @Option(names = {"-u", "--user"}, description = "User name")
            String user;

            @Option(names = {"-p", "--password"}, description = "Password or passphrase", interactive = true)
            char[] password;

            public Object call() throws Exception {
                byte[] bytes = new byte[password.length];
                for (int i = 0; i < bytes.length; i++) { bytes[i] = (byte) password[i]; }

                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(bytes);

                System.out.printf("Hi %s, your password is hashed to %s.%n", user, base64(md.digest()));

                // null out the arrays when done
                Arrays.fill(bytes, (byte) 0);
                Arrays.fill(password, ' ');

                return null;
            }

            private String base64(byte[] arr) throws Exception {
                //return javax.xml.bind.DatatypeConverter.printBase64Binary(arr);
                try {
                    Object enc = Class.forName("java.util.Base64").getDeclaredMethod("getEncoder").invoke(null, new Object[0]);
                    return (String) Class.forName("java.util.Base64$Encoder").getDeclaredMethod("encodeToString", new Class[]{byte[].class}).invoke(enc, new Object[] {arr});
                } catch (Exception beforeJava8) {
                    //return new sun.misc.BASE64Encoder().encode(arr);
                    return "75K3eLr+dx6JJFuJ7LwIpEpOFmwGZZkRiB84PURz6U8="; // :-)
                }
            }
        }

        Streams streams = new Streams();
        try {
            Capture capture = new Capture("password123");

            Login login = new Login();
            new CommandLine(login).execute("-u", "user123", "-p");

            String expectedPrompt = format("Enter value for --password (Password or passphrase): " +
                    "Hi user123, your password is hashed to 75K3eLr+dx6JJFuJ7LwIpEpOFmwGZZkRiB84PURz6U8=.%n");
            assertEquals(expectedPrompt, capture.out());
            assertEquals("user123", login.user);
            assertArrayEquals("           ".toCharArray(), login.password);
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractivePositionalAsListOfCharArrays() throws IOException {
        class App {
            @Parameters(index = "0..1", description = {"Pwd", "line2"}, interactive = true)
            List<char[]> x;
            @Parameters(index = "2") int z;
        }

        Streams streams = new Streams();
        try {
            Capture capture = Capture.multiInputValue("123");
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parseArgs("999");

            assertEquals("Enter value for position 0 (Pwd): Enter value for position 1 (Pwd): ", capture.out());
            assertEquals(2, app.x.size());
            assertArrayEquals("123".toCharArray(), app.x.get(0));
            assertArrayEquals("123".toCharArray(), app.x.get(1));
            assertEquals(999, app.z);
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractivePositionalAsCharArray() throws IOException {
        class App {
            @Parameters(index = "0", description = {"Pwd", "line2"}, interactive = true)
            char[] x;
            @Parameters(index = "1") int z;
        }

        Streams streams = new Streams();
        try {
            Capture capture = new Capture("123");
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parseArgs("9");

            assertEquals("Enter value for position 0 (Pwd): ", capture.out());
            assertArrayEquals("123".toCharArray(), app.x);
            assertEquals(9, app.z);
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractivePositionalArity_0_1_ConsumesFromCommandLineIfPossible() throws IOException {
        class App {
            @Parameters(index = "0", arity = "0..1", interactive = true)
            char[] x;

            @Parameters(index = "1")
            String[] remainder;
        }

        Streams streams = new Streams();
        try {
            Capture capture = new Capture("123");
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parseArgs("456", "abc");

            assertArrayEquals("456".toCharArray(), app.x);
            assertArrayEquals(new String[]{"abc"}, app.remainder);
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractivePositionalAsListOfCharArraysArity_0_1_ConsumesFromCommandLineIfPossible() throws IOException {
        class App {
            @Parameters(index = "0..1", arity = "0..1", interactive = true)
            List<char[]> x;

            @Parameters(index = "2")
            String[] remainder;
        }

        Streams streams = new Streams();
        try {
            Capture capture = new Capture("123");
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parseArgs("456", "789", "abc");

            assertEquals(2, app.x.size());
            assertArrayEquals("456".toCharArray(), app.x.get(0));
            assertArrayEquals("789".toCharArray(), app.x.get(1));
            assertArrayEquals(new String[]{"abc"}, app.remainder);
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractivePositionalArity_0_1_DoesNotConsumeUnknownOption() throws IOException {
        class App {
            @Parameters(index = "0", arity = "0..1", interactive = true)
            char[] x;

            @Option(names = "-z")
            int z;

            @Parameters(index = "1")
            String[] remainder;
        }

        Streams streams = new Streams();
        try {
            Capture capture = new Capture("123");
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            try {
                cmd.parseArgs("-y", "456", "abc");
                fail("Expect exception");
            } catch (UnmatchedArgumentException ex) {
                assertEquals("Unknown option: '-y'", ex.getMessage());
            }
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testInteractivePositionalAsListOfCharArraysArity_0_1_DoesNotConsumeUnknownOption() throws IOException {
        class App {
            @Parameters(index = "0..1", arity = "0..1", interactive = true)
            List<char[]> x;

            @Option(names = "-z")
            int z;

            @Parameters(index = "2")
            String[] remainder;
        }

        Streams streams = new Streams();
        try {
            Capture capture = new Capture("123");
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            try {
                cmd.parseArgs("-y", "-w", "456", "abc");
                fail("Expect exception");
            } catch (UnmatchedArgumentException ex) {
                assertEquals("Unknown options: '-y', '-w'", ex.getMessage());
            }
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testIssue2087InteractiveEchoOffOptionWithDefault() {
        class Issue2087a {
            @Option(names = "-a", interactive = true, echo = false, defaultValue = "DEFAULT") String a;
        }

        System.setProperty("picocli.trace", "DEBUG");
        Streams streams = new Streams();
        try {
            Capture capture = new Capture();

            Issue2087a app = new Issue2087a();
            new CommandLine(app).parseArgs();
            assertEquals("DEFAULT", app.a);

            assertEquals("", capture.out());
            assertThat(capture.err(), containsString("Applying defaultValue (*****(masked)) to field String picocli.InteractiveArgTest$1Issue2087a.a"));
            assertThat(capture.err(), containsString("Setting field String picocli.InteractiveArgTest$1Issue2087a.a to '*****(masked)' (was 'null') for"));
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testIssue2087InteractiveEchoOnOptionWithDefault() {
        class Issue2087b {
            @Option(names = "-a", interactive = true, echo = true, defaultValue = "DEFAULT") String a;
        }

        System.setProperty("picocli.trace", "DEBUG");
        Streams streams = new Streams();
        try {
            Capture capture = new Capture();

            Issue2087b app = new Issue2087b();
            new CommandLine(app).parseArgs();
            assertEquals("DEFAULT", app.a);

            assertEquals("", capture.out());
            assertThat(capture.err(), containsString("Applying defaultValue (DEFAULT) to field String picocli.InteractiveArgTest$1Issue2087b.a"));
            assertThat(capture.err(), containsString("Setting field String picocli.InteractiveArgTest$1Issue2087b.a to 'DEFAULT' (was 'null') for"));
        } finally {
            streams.reset();
        }
    }

    static class Issue2087DefaultProvider1 implements CommandLine.IDefaultValueProvider {
        public String defaultValue(ArgSpec argSpec) throws Exception {
            return "DEFAULT1";
        }
    }

    @Test
    public void testIssue2087InteractiveOptionEchoOffWithDefaultProvider() {
        @CommandLine.Command(defaultValueProvider = Issue2087DefaultProvider1.class)
        class Issue2087c {
            @Option(names = "-a", interactive = true, echo = false) String a;
        }
        System.setProperty("picocli.trace", "DEBUG");
        Streams streams = new Streams();
        try {
            Capture capture = new Capture();
            Issue2087c app = new Issue2087c();
            new CommandLine(app).parseArgs();
            assertEquals("DEFAULT1", app.a);

            assertEquals("", capture.out());
            assertThat(capture.err(), containsString("Applying defaultValue (*****(masked)) from picocli.InteractiveArgTest$Issue2087DefaultProvider1"));
            assertThat(capture.err(), containsString("Setting field String picocli.InteractiveArgTest$1Issue2087c.a to '*****(masked)' (was 'null') for"));
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testIssue2087InteractiveOptionEchoOnWithDefaultProvider() {
        @CommandLine.Command(defaultValueProvider = Issue2087DefaultProvider1.class)
        class Issue2087d {
            @Option(names = "-a", interactive = true, echo = true) String a;
        }
        System.setProperty("picocli.trace", "DEBUG");
        Streams streams = new Streams();
        try {
            Capture capture = new Capture();
            Issue2087d app = new Issue2087d();
            new CommandLine(app).parseArgs();
            assertEquals("DEFAULT1", app.a);

            assertEquals("", capture.out());
            assertThat(capture.err(), containsString("Applying defaultValue (DEFAULT1) from picocli.InteractiveArgTest$Issue2087DefaultProvider1"));
            assertThat(capture.err(), containsString("Setting field String picocli.InteractiveArgTest$1Issue2087d.a to 'DEFAULT1' (was 'null') for"));
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testIssue2087InteractiveEchoOnPositionalWithDefault() {
        class Issue2087e {
            @Parameters(interactive = true, echo = true, defaultValue = "DEFAULT") String a;
        }
        System.setProperty("picocli.trace", "DEBUG");
        Streams streams = new Streams();
        try {
            Capture capture = new Capture();
            Issue2087e app = new Issue2087e();
            new CommandLine(app).parseArgs();
            assertEquals("DEFAULT", app.a);

            assertEquals("", capture.out());
            assertThat(capture.err(), containsString("Applying defaultValue (DEFAULT) "));
            assertThat(capture.err(), containsString("Setting field String picocli.InteractiveArgTest$1Issue2087e.a to 'DEFAULT' (was 'null') for"));
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testIssue2087InteractiveEchoOffPositionalWithDefault() {
        class Issue2087g {
            @Parameters(interactive = true, echo = false, defaultValue = "DEFAULT") String a;
        }
        System.setProperty("picocli.trace", "DEBUG");
        Streams streams = new Streams();
        try {
            Capture capture = new Capture();
            Issue2087g app = new Issue2087g();
            new CommandLine(app).parseArgs();
            assertEquals("DEFAULT", app.a);

            assertEquals("", capture.out());
            assertThat(capture.err(), containsString("Applying defaultValue (*****(masked)) "));
            assertThat(capture.err(), containsString("Setting field String picocli.InteractiveArgTest$1Issue2087g.a to '*****(masked)' (was 'null') for"));
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testIssue2087InteractiveEchoOnPositionalWithDefaultProvider() {
        @CommandLine.Command(defaultValueProvider = Issue2087DefaultProvider1.class)
        class Issue2087f {
            @Parameters(interactive = true, echo = true) String a;
        }
        System.setProperty("picocli.trace", "DEBUG");
        Streams streams = new Streams();
        try {
            Capture capture = new Capture();
            Issue2087f app = new Issue2087f();
            new CommandLine(app).parseArgs();
            assertEquals("DEFAULT1", app.a);

            assertEquals("", capture.out());
            assertThat(capture.err(), containsString("Applying defaultValue (DEFAULT1) from picocli.InteractiveArgTest$Issue2087DefaultProvider1"));
            assertThat(capture.err(), containsString("Setting field String picocli.InteractiveArgTest$1Issue2087f.a to 'DEFAULT1' (was 'null') for"));
        } finally {
            streams.reset();
        }
    }

    @Test
    public void testIssue2087InteractiveEchoOffPositionalWithDefaultProvider() {
        @CommandLine.Command(defaultValueProvider = Issue2087DefaultProvider1.class)
        class Issue2087h {
            @Parameters(interactive = true, echo = false) String a;
        }
        System.setProperty("picocli.trace", "DEBUG");
        Streams streams = new Streams();
        try {
            Capture capture = new Capture();
            Issue2087h app = new Issue2087h();
            new CommandLine(app).parseArgs();
            assertEquals("DEFAULT1", app.a);

            assertEquals("", capture.out());
            assertThat(capture.err(), containsString("Applying defaultValue (*****(masked)) from picocli.InteractiveArgTest$Issue2087DefaultProvider1"));
            assertThat(capture.err(), containsString("Setting field String picocli.InteractiveArgTest$1Issue2087h.a to '*****(masked)' (was 'null') for"));
        } finally {
            streams.reset();
        }
    }

}
