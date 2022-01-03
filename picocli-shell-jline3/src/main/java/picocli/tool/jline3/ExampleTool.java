package picocli.tool.jline3;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.Callable;

import static java.lang.System.exit;
import static java.util.Collections.emptyList;
import static picocli.tool.jline3.Repl.runRepl;
import static picocli.tool.jline3.YourProcessing.process;

/**
 * "ExampleTool" demonstrates a command-line tool using Picocli and JLine3
 * together that follow standard UNIX/Linux practices: <ul>
 * <li>Provide colorful help or version based on command-line flags (no
 * further processing)</li>
 * <li>Process command-line as program input</li>
 * <li>Process <code>STDIN</code> (a pipe, shell redirection, etc) as
 * program input</li>
 * <li>Provide a REPL (shell) to process input interactively including
 * expected editing features at a prompt</li>
 * </ul>
 */
public class ExampleTool {
    public static void main(String[] args) {
        exit(new CommandLine(new Options()).execute(args));
    }
}

@Command(
    name = "example tool",
    mixinStandardHelpOptions = true,
    version = {"example tool 0-SNAPSHOT"}
)
final class Options implements Callable<Integer> {
    @Option(
        names = {"--prompt"},
        description = {"Change the interactive prompt from '> '."}
    )
    private String prompt = "> ";

    @Parameters(
        description = {"Command line arguments",
            "Edit this help as suitable."}
    )
    private List<String> arguments = emptyList();

    @Override
    public Integer call() {
        // Check if we are reading from a pipe or shell redirection
        final boolean isatty = null != System.console();

        // Return failure to demonstrate that `-h` does not fail: standard
        // options are handled by Picocli: this `call()` method is not
        // invoked by standard options
        if (!isatty) {
            // TODO: Bleh.  Nicer in more modern Java
            try (final BufferedReader lineReader = lineReader()) {
                while (true) {
                    final String line = lineReader.readLine();
                    if (null == line) break;
                    final Integer result = process(line);
                    if (0 != result)
                        return result;
                }
            } catch (final IOException e) {
                return 2;
            }
        } else if (arguments.isEmpty())
            return runRepl(prompt);
        else for (final String argument : arguments) {
                final Integer result = process(argument);
                if (0 != result)
                    return result;
            }

        return 0;
    }

    private static BufferedReader lineReader() {
        return new BufferedReader(new InputStreamReader(System.in));
    }
}

final class Repl {
    /**
     * This is an <em>absolutely minimal</em> REPL.
     *
     * @param prompt the prompt to show on left-side asking user for input
     *
     * @return the exit code for <code>main()</code>
     *
     * @todo Newer versions of Java permit defining this as a lambda
     */
    static Integer runRepl(final String prompt) {
        try (final Terminal terminal = TerminalBuilder.builder()
            .name("example tool")
            .build()) {
            final LineReader lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
            do {
                final String input;
                try {
                    input = lineReader.readLine(prompt);
                } catch (final UserInterruptException | EndOfFileException e) {
                    // Handling user banging on <kbd>Ctrl-C</kbd>
                    // (<code>UserInterruptException</code>), or ending
                    // input with <kbd>Ctrl-D</kbd> or <kbd>Ctrl-Z</kbd>
                    // (<code>EndOfFileException</code>) is a policy
                    // decision for your program.  However you handle
                    // these, you need a means to exit the "do-while" loop.
                    return 0;
                }
                // TODO: It is challenging in Java to return _both_ a status
                //  code for the command line, and to let caller handle
                //  exceptions
                final Integer result = process(input);
                // Another policy decision: Should invalid input kill the
                // REPL, or notify the user and continue?
                if (0 != result) return result;
            } while (true);
        } catch (final IOException e) {
            // TODO: It is challenging in Java to return _both_ a status code
            //  for the command line, and to let caller handle exceptions
            return 2;
        }
    }
}

/**
 * Your processing logic goes here. It could be human-edited text from the
 * REPL, it could be tests, it could be text passed from the command line, it
 * could be text read from <code>STDIN</code> via a pipe or shell
 * redirection.
 */
final class YourProcessing {
    // TODO: It is challenging in Java to return _all of_ a status code for
    //  the command line, output results, and to let caller handle exceptions
    static Integer process(final String input) {
        System.out.println(input); // Dummy example
        return 0;
    }
}
