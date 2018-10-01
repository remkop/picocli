package picocli.shell.jline2;

import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import picocli.AutoComplete;
import picocli.CommandLine.Model.CommandSpec;

import java.util.List;

/**
 * Implementation of the JLine 2 {@link Completer} interface that generates completion
 * candidates for the specified command line based on the {@link CommandSpec} that
 * this {@code PicocliJLineCompleter} was constructed with.
 *
 * @since 3.7
 */
public class PicocliJLineCompleter implements Completer {
    private final CommandSpec spec;

    /**
     * Constructs a new {@code PicocliJLineCompleter} for the given command spec.
     * @param spec the command specification to generate completions for. Must be non-{@code null}.
     */
    public PicocliJLineCompleter(CommandSpec spec) {
        if (spec == null) { throw new NullPointerException("spec"); }
        this.spec = spec;
    }

    /**
     * Populates the specified list with completion candidates for the specified buffer
     * based on the command specification that this shell was constructed with.
     *
     * @param buffer the command line string
     * @param cursor the position of the cursor in the command line string
     * @param candidates the list to populate with completion candidates that would produce
     *                   valid options, parameters or subcommands at the cursor position
     *                   in the command line
     * @return the cursor position in the buffer for which the completion will be relative,
     *          or {@code -1} if no completions are found
     */
    //@Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        // use the jline internal parser to split the line into tokens
        ArgumentCompleter.ArgumentList list =
                new ArgumentCompleter.WhitespaceArgumentDelimiter().delimit(buffer, cursor);

        // let picocli generate completion candidates for the token where the cursor is at
        return AutoComplete.complete(spec,
                list.getArguments(),
                list.getCursorArgumentIndex(),
                list.getArgumentPosition(),
                cursor,
                candidates);
    }
}
