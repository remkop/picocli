package picocli.shell.jline3;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

import org.jline.console.impl.SystemRegistryImpl;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/**
 * Reproduces <a href="https://github.com/remkop/picocli/issues/2546">#2546</a>:
 * JLine output redirection ({@code command > file}) only worked for commands that write
 * to {@code System.out} directly, while output written via {@link CommandLine#getOut()}
 * kept going to the {@code System.out} that JLine had replaced when setting up the redirect.
 */
public class PicocliCommandsRedirectTest {

    @Command(name = "say", description = "Writes a line via CommandLine.getOut().")
    static class SayCommand implements Runnable {
        @Spec CommandSpec spec;

        public void run() {
            spec.commandLine().getOut().println("hi!");
        }
    }

    @Command(name = "", subcommands = SayCommand.class)
    static class RootCommand implements Runnable {
        public void run() { }
    }

    private PrintStream originalOut;
    private PrintStream originalErr;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        originalOut = System.out;
        originalErr = System.err;
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void testOutputFollowsJLineRedirect() throws Exception {
        ByteArrayOutputStream consoleBytes = new ByteArrayOutputStream();
        System.setOut(consolePrintStream(consoleBytes));

        File redirectTarget = new File(temporaryFolder.getRoot(), "cmd.out");
        // Explicit streams force a dumb terminal wrapping the (already replaced) System.out;
        // otherwise JLine may attach to the real console device on Windows, bypassing System.out.
        Terminal terminal = TerminalBuilder.builder().streams(System.in, System.out).dumb(true).build();
        try {
            CommandLine cmd = new CommandLine(new RootCommand());
            PicocliCommands picocliCommands = new PicocliCommands(cmd);
            SystemRegistryImpl registry = new SystemRegistryImpl(new DefaultParser(), terminal, workDir(), null);
            registry.setCommandRegistries(picocliCommands);

            // Run once without redirection: output must appear on the console.
            // As a side effect, CommandLine.getOut() caches the console System.out.
            registry.execute("say");
            assertTrue("expected 'hi!' on the console before redirection",
                    consoleContent(consoleBytes).contains("hi!"));

            // Now redirect to a file. Per issue #2546, the output must end up in the file,
            // not on the console via a writer cached before the redirect was set up.
            consoleBytes.reset();
            registry.execute("say > " + redirectTarget.getAbsolutePath().replace('\\', '/'));
            // The interactive shell calls cleanUp() before showing the next prompt;
            // this closes the redirect stream and restores the original System.out.
            registry.cleanUp();

            assertTrue("redirected output should be written to the file, file content: <"
                            + fileContent(redirectTarget) + ">, console content: <" + consoleContent(consoleBytes) + ">",
                    fileContent(redirectTarget).contains("hi!"));
            assertFalse("redirected output should not also appear on the console",
                    consoleContent(consoleBytes).contains("hi!"));
        } finally {
            terminal.close();
        }
    }

    @Test
    public void testOutputGoesToConsoleWhenNotRedirected() throws Exception {
        ByteArrayOutputStream consoleBytes = new ByteArrayOutputStream();
        System.setOut(consolePrintStream(consoleBytes));

        Terminal terminal = TerminalBuilder.builder().streams(System.in, System.out).dumb(true).build();
        try {
            CommandLine cmd = new CommandLine(new RootCommand());
            PicocliCommands picocliCommands = new PicocliCommands(cmd);
            SystemRegistryImpl registry = new SystemRegistryImpl(new DefaultParser(), terminal, workDir(), null);
            registry.setCommandRegistries(picocliCommands);

            registry.execute("say");
            registry.cleanUp();

            assertTrue("expected 'hi!' on the console when output is not redirected",
                    consoleContent(consoleBytes).contains("hi!"));
        } finally {
            terminal.close();
        }
    }

    private static Supplier<Path> workDir() {
        return () -> Paths.get(System.getProperty("user.dir"));
    }

    private static PrintStream consolePrintStream(ByteArrayOutputStream bytes) {
        try {
            return new PrintStream(bytes, true, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e); // UTF-8 is always supported
        }
    }

    private static String consoleContent(ByteArrayOutputStream bytes) {
        return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
    }

    private static String fileContent(File file) throws Exception {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }
}
