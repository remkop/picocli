package picocli.examples.customhelp;

import picocli.CommandLine;
import picocli.CommandLine.INegatableOptionTransformer;
import picocli.CommandLine.Option;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Help.Ansi.Text;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.IHelpFactory;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;

import java.util.ArrayList;
import java.util.List;

// Shows usage help like this (without param label):
//
// Usage: <main class> [-p] [-o=<port>] [-u=<user>]
//  -o, --port          Listening port.
//  -p, --password      Password for the user.
//  -u, --user          The connecting user name.
//
// See https://github.com/remkop/picocli/issues/1271
//
public class HideOptionParams {
    @Option(names = {"-u", "--user"}, defaultValue = "${user.name}",
            description = "The connecting user name.")
    private String user;

    @Option(names = {"-p", "--password"}, interactive = true,
            description = "Password for the user.")
    private String password;

    @Option(names = {"-o", "--port"}, defaultValue = "12345",
            description = "Listening port, default is ${DEFAULT-VALUE}.")
    private int port;

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new HideOptionParams());
        cmd.setHelpFactory(new IHelpFactory() {
            public Help create(final CommandSpec commandSpec, ColorScheme colorScheme) {
                return new Help(commandSpec, colorScheme) {
                    @Override
                    public IOptionRenderer createDefaultOptionRenderer() {
                        return new IOptionRenderer() {
                            public Text[][] render(OptionSpec option,
                                                   IParamLabelRenderer ignored,
                                                   ColorScheme scheme) {
                                return makeOptionList(option, scheme);
                            }
                        };
                    }
                };
            }
        });
        cmd.usage(System.out);
    }

    private static Text[][] makeOptionList(OptionSpec option, ColorScheme scheme) {
        String shortOption = option.shortestName(); // assumes every option has a short option
        String longOption = option.longestName(); // assumes every option has a short and a long option

        if (option.negatable()) { // ok to omit if you don't have negatable options
            INegatableOptionTransformer transformer =
                    option.command().negatableOptionTransformer();
            shortOption = transformer.makeSynopsis(shortOption, option.command());
            longOption = transformer.makeSynopsis(longOption, option.command());
        }

        // assume one line of description text (may contain embedded %n line separators)
        String[] description = option.description();
        Text[] descriptionFirstLines = scheme.text(description[0]).splitLines();

        Text EMPTY = Ansi.OFF.text("");
        List<Text[]> result = new ArrayList<Text[]>();
        result.add(new Text[]{
                scheme.optionText(String.valueOf(
                        option.command().usageMessage().requiredOptionMarker())),
                scheme.optionText(shortOption),
                scheme.text(","), // we assume every option has a short and a long name
                scheme.optionText(longOption), // just the option name without parameter
                descriptionFirstLines[0]});
        for (int i = 1; i < descriptionFirstLines.length; i++) {
            result.add(new Text[]{EMPTY, EMPTY, EMPTY, EMPTY, descriptionFirstLines[i]});
        }
        // if @Command(showDefaultValues = true) was set, append line with default value
        if (option.command().usageMessage().showDefaultValues()) {
            Text defaultValue = scheme.text("  Default: " + option.defaultValueString(true));
            result.add(new Text[]{EMPTY, EMPTY, EMPTY, EMPTY, defaultValue});
        }
        return result.toArray(new Text[result.size()][]);
    }
}
