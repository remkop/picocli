package picocli.shell.jline3;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jline.builtins.Completers.OptionCompleter;
import org.jline.builtins.Completers.SystemCompleter;
import org.jline.builtins.Options.HelpException;
import org.jline.builtins.Widgets.ArgDesc;
import org.jline.builtins.Widgets.CmdDesc;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.utils.AttributedString;

import picocli.CommandLine;
import picocli.CommandLine.Help;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;

public class PicocliCommands {
    private final Supplier<Path> workDir;
    private final CommandLine cmd;
    private final List<String> commands;

    public PicocliCommands(Path workDir, CommandLine cmd) {
        this(() -> workDir, cmd);
    }

    public PicocliCommands(Supplier<Path> workDir, CommandLine cmd) {
        this.workDir = workDir;
        this.cmd = cmd;
        commands = new ArrayList<>(Arrays.asList(cmd.getCommandSpec().aliases()));
        commands.addAll(cmd.getCommandSpec().subcommands().keySet());

    }

    public boolean hasCommand(String command) {
        return commands.contains(command);
    }

    public SystemCompleter compileCompleters() {
        SystemCompleter out = new SystemCompleter();
        // with original completer...
        //        out.add(commands, new PicocliJLineCompleter(cmd.getCommandSpec()));
        //        return out;
        for (String s: commands) {
            CommandSpec spec = cmd.getSubcommands().get(s).getCommandSpec();
            List<String> options = new ArrayList<>();
            Map<String,List<String>> optionValues = new HashMap<>();
            for (OptionSpec o: spec.options()) {
                List<String> values = new ArrayList<>();
                if (o.completionCandidates() != null) {
                    o.completionCandidates().forEach(v -> values.add(v));
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
                out.add(s, new ArgumentCompleter(new StringsCompleter(s), NullCompleter.INSTANCE));
            } else {
                out.add(s, new ArgumentCompleter(new StringsCompleter(s)
                         , new OptionCompleter(NullCompleter.INSTANCE, optionValues, options, 1)));
            }
        }
        return out;
    }

    public CmdDesc commandDescription(String command) {
        CommandSpec spec = cmd.getSubcommands().get(command).getCommandSpec();
        Help cmdhelp= new picocli.CommandLine.Help(spec);
        List<AttributedString> main = new ArrayList<>();
        Map<String, List<AttributedString>> options = new HashMap<>();
        String synopsis = AttributedString.stripAnsi(spec.usageMessage().sectionMap().get("synopsis").render(cmdhelp).toString());
        main.add(HelpException.highlightSyntax(synopsis.trim(), HelpException.defaultStyle()));
        // using JLine help highlight because the statement below does not work well...
        //        main.add(new AttributedString(spec.usageMessage().sectionMap().get("synopsis").render(cmdhelp).toString()));
        for (OptionSpec o: spec.options()) {
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
