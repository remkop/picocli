package picocli.examples.synopsis;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;

/**
 * See https://github.com/remkop/picocli/issues/1348
 */
@Command(name = "rename")
public class ParametersBeforeOptions implements Runnable {
    @Parameters
    File file;

    @Option(names = "to", required = true)
    File new_file;

    @Override
    public void run() {
        // business logic
    }

    public static void main(String... args) {
        new CommandLine(new ParametersBeforeOptions())
                .setHelpFactory(new ReverseSynopsisHelpFactory())
                .execute("-h");
    }
}

class ReverseSynopsisHelpFactory implements CommandLine.IHelpFactory {

    @Override
    public CommandLine.Help create(CommandSpec commandSpec, ColorScheme colorScheme) {
        return new CommandLine.Help(commandSpec, colorScheme) {
            @Override
            protected String makeSynopsisFromParts(int synopsisHeadingLength, Ansi.Text optionText, Ansi.Text groupsText, Ansi.Text endOfOptionsText, Ansi.Text positionalParamText, Ansi.Text commandText) {
                return super.makeSynopsisFromParts(synopsisHeadingLength, positionalParamText, groupsText, endOfOptionsText, optionText, commandText);
            }
        };
    }
}
