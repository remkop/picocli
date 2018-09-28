package picocli.jline2.completer;

import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

import java.util.List;

public class PicocliJLineCompleter implements Completer {
    private final CommandSpec spec;

    public PicocliJLineCompleter(CommandSpec spec) {
        this.spec = spec;
    }

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