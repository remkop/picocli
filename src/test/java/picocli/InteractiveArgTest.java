package picocli;

import org.junit.Test;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.UnmatchedArgumentException;

import java.io.*;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static java.lang.String.format;
import static org.junit.Assert.*;

public class InteractiveArgTest {

    @Test
    public void testInteractiveOptionReadsFromStdIn() {
        class App {
            @Option(names = "-x", description = {"Pwd", "line2"}, interactive = true) int x;
            @Option(names = "-z") int z;
        }

        PrintStream out = System.out;
        InputStream in = System.in;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));
            System.setIn(new ByteArrayInputStream("123".getBytes()));

            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parse("-x");

            assertEquals("Enter value for -x (Pwd): ", baos.toString());
            assertEquals(123, app.x);
            assertEquals(0, app.z);

            cmd.parse("-z", "678");

            assertEquals(0, app.x);
            assertEquals(678, app.z);
        } finally {
            System.setOut(out);
            System.setIn(in);
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

        PrintStream out = System.out;
        InputStream in = System.in;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));
            System.setIn(inputStream("123"));
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parse("-x", "-x");

            assertEquals("Enter value for -x (Pwd): Enter value for -x (Pwd): ", baos.toString());
            assertEquals(Arrays.asList(123, 123), app.x);
            assertEquals(0, app.z);

            cmd.parse("-z", "678");

            assertNull(app.x);
            assertEquals(678, app.z);
        } finally {
            System.setOut(out);
            System.setIn(in);
        }
    }

    ByteArrayInputStream inputStream(final String value) {
        return new ByteArrayInputStream(value.getBytes()) {
            int count;

            @Override
            public synchronized int read(byte[] b, int off, int len) {
                System.arraycopy(value.getBytes(), 0, b, off, value.length());
                return (count++ % 3) == 0 ? value.length() : -1;
            }
        };
    }

    @Test
    public void testInteractiveOptionAsListOfCharArrays() throws IOException {
        class App {
            @Option(names = "-x", description = {"Pwd", "line2"}, interactive = true)
            List<char[]> x;

            @Option(names = "-z")
            int z;
        }

        PrintStream out = System.out;
        InputStream in = System.in;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));
            System.setIn(inputStream("123"));
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parse("-x", "-x");

            assertEquals("Enter value for -x (Pwd): Enter value for -x (Pwd): ", baos.toString());
            assertEquals(2, app.x.size());
            assertArrayEquals("123".toCharArray(), app.x.get(0));
            assertArrayEquals("123".toCharArray(), app.x.get(1));
            assertEquals(0, app.z);

            cmd.parse("-z", "678");

            assertNull(app.x);
            assertEquals(678, app.z);
        } finally {
            System.setOut(out);
            System.setIn(in);
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

        PrintStream out = System.out;
        InputStream in = System.in;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));
            System.setIn(inputStream("123"));
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parse("-x");

            assertEquals("Enter value for -x (Pwd): ", baos.toString());
            assertArrayEquals("123".toCharArray(), app.x);
            assertEquals(0, app.z);
        } finally {
            System.setOut(out);
            System.setIn(in);
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

        PrintStream out = System.out;
        InputStream in = System.in;
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            System.setIn(inputStream("123"));
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parse("-x", "456", "abc");

            assertArrayEquals("456".toCharArray(), app.x);
            assertArrayEquals(new String[]{"abc"}, app.remainder);
        } finally {
            System.setOut(out);
            System.setIn(in);
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

        PrintStream out = System.out;
        InputStream in = System.in;
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            System.setIn(inputStream("123"));
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parse("-x", "456", "-x", "789", "abc");

            assertEquals(2, app.x.size());
            assertArrayEquals("456".toCharArray(), app.x.get(0));
            assertArrayEquals("789".toCharArray(), app.x.get(1));
            assertArrayEquals(new String[]{"abc"}, app.remainder);
        } finally {
            System.setOut(out);
            System.setIn(in);
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

        PrintStream out = System.out;
        InputStream in = System.in;
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            System.setIn(inputStream("123"));
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parse("-x", "-z", "456", "abc");

            assertArrayEquals("123".toCharArray(), app.x);
            assertEquals(456, app.z);
            assertArrayEquals(new String[]{"abc"}, app.remainder);
        } finally {
            System.setOut(out);
            System.setIn(in);
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

        PrintStream out = System.out;
        InputStream in = System.in;
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            System.setIn(inputStream("123"));
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parse("-x", "-z", "456", "abc");

            assertEquals(1, app.x.size());
            assertArrayEquals("123".toCharArray(), app.x.get(0));
            assertEquals(456, app.z);
            assertArrayEquals(new String[]{"abc"}, app.remainder);
        } finally {
            System.setOut(out);
            System.setIn(in);
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

        PrintStream out = System.out;
        InputStream in = System.in;
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            System.setIn(inputStream("123"));
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parse("-x", "-y", "456", "abc");

            assertArrayEquals("-y".toCharArray(), app.x);
            assertEquals(0, app.z);
            assertArrayEquals(new String[]{"456", "abc"}, app.remainder);
        } finally {
            System.setOut(out);
            System.setIn(in);
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

        PrintStream out = System.out;
        InputStream in = System.in;
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            System.setIn(inputStream("123"));
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parse("-x", "-y", "-x", "-w", "456", "abc");

            assertEquals(2, app.x.size());
            assertArrayEquals("-y".toCharArray(), app.x.get(0));
            assertArrayEquals("-w".toCharArray(), app.x.get(1));
            assertEquals(0, app.z);
            assertArrayEquals(new String[]{"456", "abc"}, app.remainder);
        } finally {
            System.setOut(out);
            System.setIn(in);
        }
    }

    @Test
    public void testInteractiveOptionReadsFromStdInMultiLinePrompt() {
        class App {
            @Option(names = "-x", description = {"Pwd%nline2", "ignored"}, interactive = true) int x;
            @Option(names = "-z") int z;
        }

        PrintStream out = System.out;
        InputStream in = System.in;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));
            System.setIn(new ByteArrayInputStream("123".getBytes()));

            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parse("-x", "-z", "987");

            String expectedPrompt = format("Enter value for -x (Pwd%nline2): ");
            assertEquals(expectedPrompt, baos.toString());
            assertEquals(123, app.x);
            assertEquals(987, app.z);
        } finally {
            System.setOut(out);
            System.setIn(in);
        }
    }

    @Test
    public void testInteractivePositionalReadsFromStdIn() {
        class App {
            @Parameters(index = "0", description = {"Pwd%nline2", "ignored"}, interactive = true) int x;
            @Parameters(index = "1") int z;
        }

        PrintStream out = System.out;
        InputStream in = System.in;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));
            System.setIn(new ByteArrayInputStream("123".getBytes()));

            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parse("987");

            String expectedPrompt = format("Enter value for position 0 (Pwd%nline2): ");
            assertEquals(expectedPrompt, baos.toString());
            assertEquals(123, app.x);
            assertEquals(987, app.z);
        } finally {
            System.setOut(out);
            System.setIn(in);
        }
    }

    @Test
    public void testInteractivePositional2ReadsFromStdIn() {
        class App {
            @Parameters(index = "0") int a;
            @Parameters(index = "1", description = {"Pwd%nline2", "ignored"}, interactive = true) int x;
            @Parameters(index = "2") int z;
        }

        PrintStream out = System.out;
        InputStream in = System.in;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));
            System.setIn(new ByteArrayInputStream("123".getBytes()));

            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parse("333", "987");

            String expectedPrompt = format("Enter value for position 1 (Pwd%nline2): ");
            assertEquals(expectedPrompt, baos.toString());
            assertEquals(333, app.a);
            assertEquals(123, app.x);
            assertEquals(987, app.z);
        } finally {
            System.setOut(out);
            System.setIn(in);
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

        PrintStream out = System.out;
        InputStream in = System.in;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));
            System.setIn(inputStream("password123"));

            Login login = new Login();
            CommandLine.call(login, "-u", "user123", "-p");

            String expectedPrompt = format("Enter value for --password (Password or passphrase): " +
                    "Hi user123, your password is hashed to 75K3eLr+dx6JJFuJ7LwIpEpOFmwGZZkRiB84PURz6U8=.%n");
            assertEquals(expectedPrompt, baos.toString());
            assertEquals("user123", login.user);
            assertArrayEquals("           ".toCharArray(), login.password);
        } finally {
            System.setOut(out);
            System.setIn(in);
        }
    }

    @Test
    public void testInteractivePositionalAsListOfCharArrays() throws IOException {
        class App {
            @Parameters(index = "0..1", description = {"Pwd", "line2"}, interactive = true)
            List<char[]> x;
            @Parameters(index = "2") int z;
        }

        PrintStream out = System.out;
        InputStream in = System.in;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));
            System.setIn(inputStream("123"));
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parse("999");

            assertEquals("Enter value for position 0 (Pwd): Enter value for position 1 (Pwd): ", baos.toString());
            assertEquals(2, app.x.size());
            assertArrayEquals("123".toCharArray(), app.x.get(0));
            assertArrayEquals("123".toCharArray(), app.x.get(1));
            assertEquals(999, app.z);
        } finally {
            System.setOut(out);
            System.setIn(in);
        }
    }

    @Test
    public void testInteractivePositionalAsCharArray() throws IOException {
        class App {
            @Parameters(index = "0", description = {"Pwd", "line2"}, interactive = true)
            char[] x;
            @Parameters(index = "1") int z;
        }

        PrintStream out = System.out;
        InputStream in = System.in;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));
            System.setIn(inputStream("123"));
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parse("9");

            assertEquals("Enter value for position 0 (Pwd): ", baos.toString());
            assertArrayEquals("123".toCharArray(), app.x);
            assertEquals(9, app.z);
        } finally {
            System.setOut(out);
            System.setIn(in);
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

        PrintStream out = System.out;
        InputStream in = System.in;
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            System.setIn(inputStream("123"));
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parse("456", "abc");

            assertArrayEquals("456".toCharArray(), app.x);
            assertArrayEquals(new String[]{"abc"}, app.remainder);
        } finally {
            System.setOut(out);
            System.setIn(in);
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

        PrintStream out = System.out;
        InputStream in = System.in;
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            System.setIn(inputStream("123"));
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            cmd.parse("456", "789", "abc");

            assertEquals(2, app.x.size());
            assertArrayEquals("456".toCharArray(), app.x.get(0));
            assertArrayEquals("789".toCharArray(), app.x.get(1));
            assertArrayEquals(new String[]{"abc"}, app.remainder);
        } finally {
            System.setOut(out);
            System.setIn(in);
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

        PrintStream out = System.out;
        InputStream in = System.in;
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            System.setIn(inputStream("123"));
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            try {
                cmd.parse("-y", "456", "abc");
                fail("Expect exception");
            } catch (UnmatchedArgumentException ex) {
                assertEquals("Unknown option: -y", ex.getMessage());
            }
        } finally {
            System.setOut(out);
            System.setIn(in);
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

        PrintStream out = System.out;
        InputStream in = System.in;
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            System.setIn(inputStream("123"));
            App app = new App();
            CommandLine cmd = new CommandLine(app);
            try {
                cmd.parse("-y", "-w", "456", "abc");
                fail("Expect exception");
            } catch (UnmatchedArgumentException ex) {
                assertEquals("Unknown options: -y, -w", ex.getMessage());
            }
        } finally {
            System.setOut(out);
            System.setIn(in);
        }
    }

}
