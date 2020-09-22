package picocli.examples.customhelp;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.IHelpFactory;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Parameters;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Demonstrates how to use a custom {@link IHelpFactory} and subclassing {@code Help}
 * to create a custom parameter list.
 *
 * See https://github.com/remkop/picocli/issues/1181
 */
public class CustomParamList {

    @Command(name = "CustomParamList", version = "CustomParamList 1.0", mixinStandardHelpOptions = true,
            parameterListHeading = "positional arguments:%n",
            optionListHeading = "optional arguments:%n",
            description = "Demonstrates how to create a custom left-aligned param list.")
    static class App {
        @Parameters(paramLabel = "Other", description = "Some positional parameter") String str;
        @Parameters(paramLabel = "Another", description = {
                "Some positional parameter with a very very long description that spans multiple lines",
                "And has multiple paragraphs"
        })
        String str2;
    }

    static class MyHelpFactory implements IHelpFactory {
        @Override
        public Help create(CommandSpec commandSpec, ColorScheme colorScheme) {
            return new Help(commandSpec, colorScheme) {
                @Override
                public String parameterList(List<PositionalParamSpec> positionalParams) {
                    int usageHelpWidth = commandSpec.usageMessage().width();
                    int longOptionsColumnWidth = longOptionsColumnWidth(createDefaultLayout());
                    int descriptionWidth = usageHelpWidth - 1 - longOptionsColumnWidth;
                    TextTable tt = TextTable.forColumns(colorScheme,
                            new Column(2,                0, Column.Overflow.TRUNCATE), // "*"
                            new Column(0,                0, Column.Overflow.SPAN), // "-c"
                            new Column(0,                0, Column.Overflow.TRUNCATE), // ","
                            new Column(longOptionsColumnWidth, 0, Column.Overflow.SPAN),  // " --create"
                            new Column(descriptionWidth, 4, Column.Overflow.WRAP)); // " Creates a ..."
                    tt.setAdjustLineBreaksForWideCJKCharacters(commandSpec.usageMessage().adjustLineBreaksForWideCJKCharacters());
                    Layout layout = new Layout(colorScheme, tt, createDefaultOptionRenderer(), createDefaultParameterRenderer());
                    return parameterList(positionalParams, layout, parameterLabelRenderer());
                }
            };
        }
        int longOptionsColumnWidth(Help.Layout layout) { // bit of a hack...
            try {
                Field table = Help.Layout.class.getDeclaredField("table");
                table.setAccessible(true);
                Help.TextTable tt = (Help.TextTable) table.get(layout);
                return tt.columns()[3].width;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new App());
        cmd.setHelpFactory(new MyHelpFactory());

        String expected = String.format("" +
                "Usage: CustomParamList [-hV] Other Another%n" +
                "Demonstrates how to create a custom left-aligned param list.%n" +
                "positional arguments:%n" +
                "  Other           Some positional parameter%n" +
                "  Another         Some positional parameter with a very very long description%n" +
                "                    that spans multiple lines%n" +
                "                  And has multiple paragraphs%n" +
                "optional arguments:%n" +
                "  -h, --help      Show this help message and exit.%n" +
                "  -V, --version   Print version information and exit.%n");
        String actual = cmd.getUsageMessage(Help.Ansi.OFF);
        if (!expected.equals(actual)) {
            throw new IllegalStateException(expected + " != " + actual);
        }

    }
}
