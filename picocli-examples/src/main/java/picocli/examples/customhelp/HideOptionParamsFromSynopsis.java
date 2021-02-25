package picocli.examples.customhelp;

import picocli.CommandLine;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi.Text;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.IHelpFactory;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Option;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

// Shows usage help like this (without param label in the synopsis):
//
// Usage: <main class> [-p] [-o] [-u]
//  -o, --port=<port>   Listening port, default is 12345.
//  -p, --password      Password for the user.
//  -u, --user=<user>   The connecting user name.
//
// See HideOptionParams.java for how to hide the param name in the options list.

// See also https://github.com/remkop/picocli/issues/1335
//
public class HideOptionParamsFromSynopsis {
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
        CommandLine cmd = new CommandLine(new HideOptionParamsFromSynopsis());
        cmd.setHelpFactory(new IHelpFactory() {
            public Help create(final CommandSpec commandSpec, ColorScheme colorScheme) {
                return new Help(commandSpec, colorScheme) {
                    IParamLabelRenderer paramLabelRenderer;

                    public IParamLabelRenderer parameterLabelRenderer() {
                        return paramLabelRenderer == null ? super.parameterLabelRenderer() : paramLabelRenderer;
                    }

                    protected Text createDetailedSynopsisOptionsText(Collection<CommandLine.Model.ArgSpec> done, List<OptionSpec> optionList, Comparator<OptionSpec> optionSort, boolean clusterBooleanOptions) {
                        paramLabelRenderer = new IParamLabelRenderer() {
                            @Override
                            public Text renderParameterLabel(CommandLine.Model.ArgSpec argSpec, Ansi ansi, List<Ansi.IStyle> styles) {
                                return ansi.text("");
                            }
                            @Override public String separator() { return null; }
                        };
                        Text result = super.createDetailedSynopsisOptionsText(done, optionList, optionSort, clusterBooleanOptions);
                        paramLabelRenderer = null;
                        return result;
                    }
                };
            }
        });
        cmd.usage(System.out);
    }
}
