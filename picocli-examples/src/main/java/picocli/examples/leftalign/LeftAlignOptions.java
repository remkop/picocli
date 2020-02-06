package picocli.examples.leftalign;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.IHelpFactory;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

/**
 * The example below demonstrates how to left-align long options in the usage help.
 */
@Command(name = "layout-demo", mixinStandardHelpOptions = true, description = "option list layout demo")
public class LeftAlignOptions implements Callable<Integer> {

    @Option(names = "--large-option",        description = "large option") boolean largeOption;
    @Option(names = {"-l", "--long-option"}, description = "long option")  String longOption;

    @Override
    public Integer call() {
        // run the business logic
        // ...
        int exitCode = 0;
        return exitCode;
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new LeftAlignOptions());

        // customize the usage help to left-align long options
        cmd.setHelpFactory(createCustomizedUsageHelp());

        // instead of cmd.execute, applications can also call cmd.parseArgs
        System.exit(cmd.execute(args));
    }

    private static IHelpFactory createCustomizedUsageHelp() {
        return new IHelpFactory() {
            private static final int COLUMN_REQUIRED_OPTION_MARKER_WIDTH = 2;
            private static final int COLUMN_SHORT_OPTION_NAME_WIDTH = 2;
            private static final int COLUMN_OPTION_NAME_SEPARATOR_WIDTH = 2;
            private static final int COLUMN_LONG_OPTION_NAME_WIDTH = 22;

            private static final int INDEX_REQUIRED_OPTION_MARKER = 0;
            private static final int INDEX_SHORT_OPTION_NAME = 1;
            private static final int INDEX_OPTION_NAME_SEPARATOR = 2;
            private static final int INDEX_LONG_OPTION_NAME = 3;
            private static final int INDEX_OPTION_DESCRIPTION = 4;

            @Override
            public Help create(CommandSpec commandSpec, ColorScheme colorScheme) {
                return new Help(commandSpec, colorScheme) {
                    @Override
                    public Layout createDefaultLayout() {

                        // The default layout creates a TextTable with 5 columns, as follows:
                        // 0: empty text or (if configured) the requiredOptionMarker character
                        // 1: short option name
                        // 2: comma separator (if option has both short and long option)
                        // 3: long option name(s)
                        // 4: option description
                        //
                        // The code below creates a TextTable with 3 columns, as follows:
                        // 0: empty text or (if configured) the requiredOptionMarker character
                        // 1: all option names, comma-separated if necessary
                        // 2: option description

                        int optionNamesColumnWidth = COLUMN_SHORT_OPTION_NAME_WIDTH +
                                COLUMN_OPTION_NAME_SEPARATOR_WIDTH +
                                COLUMN_LONG_OPTION_NAME_WIDTH;

                        TextTable table = TextTable.forColumnWidths(colorScheme,
                                COLUMN_REQUIRED_OPTION_MARKER_WIDTH,
                                optionNamesColumnWidth,
                                commandSpec.usageMessage().width() - (optionNamesColumnWidth + COLUMN_REQUIRED_OPTION_MARKER_WIDTH));
                        Layout result = new Layout(colorScheme,
                                table,
                                createDefaultOptionRenderer(),
                                createDefaultParameterRenderer()) {
                            public void layout(ArgSpec argSpec, Ansi.Text[][] cellValues) {

                                // The default option renderer produces 5 Text values for each option.
                                // Below we combine the short option name, comma separator and long option name
                                // into a single Text object, and we pass 3 Text values to the TextTable.
                                for (Ansi.Text[] original : cellValues) {
                                    if (original[INDEX_OPTION_NAME_SEPARATOR].getCJKAdjustedLength() > 0) {
                                        original[INDEX_OPTION_NAME_SEPARATOR] = original[INDEX_OPTION_NAME_SEPARATOR].concat(" ");
                                    }
                                    Ansi.Text[] threeColumns = new Ansi.Text[] {
                                            original[INDEX_REQUIRED_OPTION_MARKER],
                                            original[INDEX_SHORT_OPTION_NAME]
                                                    .concat(original[INDEX_OPTION_NAME_SEPARATOR])
                                                    .concat(original[INDEX_LONG_OPTION_NAME]),
                                            original[INDEX_OPTION_DESCRIPTION],
                                    };
                                    table.addRowValues(threeColumns);
                                }
                            }
                        };
                        return result;
                    }
                };
            }
        };
    }
}
