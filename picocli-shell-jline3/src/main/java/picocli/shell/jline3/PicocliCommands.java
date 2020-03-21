package picocli.shell.jline3;

import org.jline.builtins.Completers.OptionCompleter;
import org.jline.builtins.Completers.SystemCompleter;
import org.jline.builtins.Options.HelpException;
import org.jline.builtins.Widgets.ArgDesc;
import org.jline.builtins.Widgets.CmdDesc;
import org.jline.reader.Completer;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.utils.AttributedString;
import picocli.CommandLine;
import picocli.CommandLine.Help;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Compiles SystemCompleter for command completion and implements a method commandDescription() that provides command descriptions
 * for JLine TailTipWidgets to be displayed in terminal status bar.
 * SystemCompleter implements the JLine 3 {@link Completer} interface. SystemCompleter generates completion
 * candidates for the specified command line based on the {@link CommandSpec} that this {@code PicocliCommands} was constructed with.
 *
 * @since 4.1.2
 */
public class PicocliCommands {
    private final Supplier<Path> workDir;
    private final CommandLine cmd;
    private final List<String> commands;
    private final Map<String, String> aliasCommand;

    public PicocliCommands(Path workDir, CommandLine cmd) {
        this(() -> workDir, cmd);
    }

    public PicocliCommands(Supplier<Path> workDir, CommandLine cmd) {
        this.workDir = workDir;
        this.cmd = cmd;
        commands = new ArrayList<>(cmd.getCommandSpec().subcommands().keySet());
        aliasCommand = extractAliases(cmd);
    }

    private static Map<String, String> extractAliases(CommandLine cmd) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String name : cmd.getCommandSpec().subcommands().keySet()) {
            for (String alias : cmd.getSubcommands().get(name).getCommandSpec().aliases()) {
                result.put(alias, name);
            }
        }
        return result;
    }

    /**
     *
     * @param command
     * @return true if PicocliCommands contains command
     */
    public boolean hasCommand(String command) {
        return commands.contains(command) || aliasCommand.containsKey(command);
    }

    /**
     *
     * @return SystemCompleter for command completion
     */
    public SystemCompleter compileCompleters() {
        return compileCompleters(new SystemCompleter(), cmd);
    }

    private SystemCompleter compileCompleters(SystemCompleter completer, CommandLine cmd) {
        completer.addAliases(extractAliases(cmd));

        for (Map.Entry<String, CommandLine> entry : cmd.getSubcommands().entrySet()) {
            CommandSpec sub = entry.getValue().getCommandSpec();
            registerCompleters(completer, entry.getKey(), sub);

            // TODO support nested sub-subcommands (https://github.com/remkop/picocli/issues/969)
            //   Is the below sufficient?
            //   compileCompleters(completer, sub.commandLine());
        }
        return completer;
    }

    private void registerCompleters(SystemCompleter out, String commandName, CommandSpec spec) {
        List<String> options = new ArrayList<>();
        Map<String, List<String>> optionValues = new HashMap<>();
        for (OptionSpec o : spec.options()) {
            List<String> values = new ArrayList<>();
            if (o.completionCandidates() != null) {
                o.completionCandidates().forEach(values::add);
            }
            if (o.arity().max() == 0) {
                options.addAll(Arrays.asList(o.names()));
            } else {
                for (String n: o.names()) {
                    optionValues.put(n, values);
                }
            }
        }
        // TODO positional parameter completion
        // JLine OptionCompleter need to be improved with option descriptions and option value completion,
        // now it completes only strings.
        if (options.isEmpty() && optionValues.isEmpty()) {
            out.add(commandName, new ArgumentCompleter(new StringsCompleter(commandName), NullCompleter.INSTANCE));
        } else {
            out.add(commandName, new ArgumentCompleter(new StringsCompleter(commandName)
                     , new OptionCompleter(NullCompleter.INSTANCE, optionValues, options, 1)));
        }
    }

    /**
     *
     * @param command
     * @return command description for JLine TailTipWidgets to be displayed in terminal status bar.
     */
    public CmdDesc commandDescription(String command) {
        CommandSpec spec = cmd.getSubcommands().get(command).getCommandSpec();
        Help cmdhelp= new picocli.CommandLine.Help(spec);
        List<AttributedString> main = new ArrayList<>();
        Map<String, List<AttributedString>> options = new HashMap<>();
        String synopsis = AttributedString.stripAnsi(spec.usageMessage().sectionMap().get("synopsis").render(cmdhelp).toString());
        main.add(HelpException.highlightSyntax(synopsis.trim(), HelpException.defaultStyle()));
        // using JLine help highlight because the statement below does not work well...
        //        main.add(new AttributedString(spec.usageMessage().sectionMap().get("synopsis").render(cmdhelp).toString()));
        for (OptionSpec o : spec.options()) {
            String key = Arrays.stream(o.names()).collect(Collectors.joining(" "));
            List<AttributedString> val = new ArrayList<>();
            for (String d:  o.description()) {
                val.add(new AttributedString(d));
            }
            if (val.isEmpty()) {
                val.add(new AttributedString("")); // in order to avoid IndexOutOfBoundsException
                                                   // need to be fixed in JLine
            }
            if (o.arity().max() > 0 && key.matches(".*[a-zA-Z]{2,}$")) {
                key += "=" + o.paramLabel();
            }
            options.put(key, val);
        }
        return new CmdDesc(main, ArgDesc.doArgNames(Arrays.asList("")), options);
    }
}
