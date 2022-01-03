package picocli.tool.jline3;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

import static java.lang.System.exit;

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
class Options implements Callable<Integer> {
    @Override
    public Integer call() {
        // Return failure to demonstrate that `-h` does not fail: standard
        // options are handled by Picocli: this `call()` method is not
        // invoked by standard options
        return 2;
    }
}
