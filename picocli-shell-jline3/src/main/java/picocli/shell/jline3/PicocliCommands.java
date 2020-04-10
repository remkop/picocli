package picocli.shell.jline3;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jline.builtins.CommandRegistry;
import org.jline.builtins.Completers.SystemCompleter;
import org.jline.builtins.Options.HelpException;
import org.jline.builtins.Widgets.ArgDesc;
import org.jline.builtins.Widgets.CmdDesc;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.utils.AttributedString;
import picocli.CommandLine;
import picocli.CommandLine.Help;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Compiles SystemCompleter for command completion and implements a method commandDescription() that provides command descriptions
 * for JLine TailTipWidgets to be displayed in terminal status bar.
 * SystemCompleter implements the JLine 3 {@link Completer} interface. SystemCompleter generates completion
 * candidates for the specified command line based on the {@link CommandSpec} that this {@code PicocliCommands} was constructed with.
 *
 * @since 4.1.2
 */
public class PicocliCommands implements CommandRegistry {
    private final Supplier<Path> workDir;
    private final CommandLine cmd;
    private final Set<String> commands;
    private final Map<String,String> aliasCommand = new HashMap<>();

    public PicocliCommands(Path workDir, CommandLine cmd) {
        this(() -> workDir, cmd);
    }

    public PicocliCommands(Supplier<Path> workDir, CommandLine cmd) {
        this.workDir = workDir;
        this.cmd = cmd;
        commands = cmd.getCommandSpec().subcommands().keySet();
        for (String c: commands) {
            for (String a: cmd.getSubcommands().get(c).getCommandSpec().aliases()) {
                aliasCommand.put(a, c);
            }
        }
    }

    /**
     *
     * @param command
     * @return true if PicocliCommands contains command
     */
    public boolean hasCommand(String command) {
        return commands.contains(command) || aliasCommand.containsKey(command);
    }


    public SystemCompleter compileCompleters() {
        SystemCompleter out = new SystemCompleter();
        List<String> all = new ArrayList<>();
        all.addAll(commands);
        all.addAll(aliasCommand.keySet());
        out.add(all, new PicocliCompleter());
        return out;
    }

    private class PicocliCompleter implements Completer {

        public PicocliCompleter() {}

        @Override
        public void complete(LineReader reader, ParsedLine commandLine, List<Candidate> candidates) {
            assert commandLine != null;
            assert candidates != null;
            String word = commandLine.word();
            List<String> words = commandLine.words();
            CommandLine sub = cmd;
            for (int i = 0; i < commandLine.wordIndex(); i++) {
                if (!words.get(i).startsWith("-")) {
                    sub = findSubcommandLine(sub,words.get(i));
                    if (sub == null) {
                        return;
                    }
                }
            }
            if (word.startsWith("-")) {
                String buffer = word.substring(0, commandLine.wordCursor());
                int eq = buffer.indexOf('=');
                for (OptionSpec option : sub.getCommandSpec().options()) {
                    if (option.arity().max() == 0 && eq < 0) {
                        addCandidates(candidates, Arrays.asList(option.names()));
                    } else {
                        if (eq > 0) {
                            String opt = buffer.substring(0, eq);
                            if (Arrays.asList(option.names()).contains(opt) && option.completionCandidates() != null) {
                                addCandidates(candidates, option.completionCandidates(), buffer.substring(0, eq + 1), "", true);
                            }
                        } else {
                            addCandidates(candidates, Arrays.asList(option.names()), "", "=", false);
                        }
                    }
                }
            } else {
                addCandidates(candidates, sub.getSubcommands().keySet());
                for (CommandLine s : sub.getSubcommands().values()) {
                    addCandidates(candidates, Arrays.asList(s.getCommandSpec().aliases()));
                }
            }
        }

        private void addCandidates(List<Candidate> candidates, Iterable<String> cands) {
            addCandidates(candidates, cands, "", "", true);
        }

        private void addCandidates(List<Candidate> candidates, Iterable<String> cands, String preFix, String postFix, boolean complete) {
            for (String s : cands) {
                candidates.add(new Candidate(AttributedString.stripAnsi(preFix + s + postFix), s, null, null, null, null, complete));
            }
        }

        private CommandLine findSubcommandLine(CommandLine cmdline, String command) {
            for (CommandLine s : cmdline.getSubcommands().values()) {
                if (s.getCommandName().equals(command) || Arrays.asList(s.getCommandSpec().aliases()).contains(command)) {
                    return s;
                }
            }
            return null;
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
            if (o.arity().max() > 0) {
                key += "=" + o.paramLabel();
            }
            options.put(key, val);
        }
        return new CmdDesc(main, ArgDesc.doArgNames(Arrays.asList("")), options);
    }

    @Override
    public List<String> commandInfo(String command) {
        List<String> out = new ArrayList<>();
        CommandSpec spec = cmd.getSubcommands().get(command).getCommandSpec();
        Help cmdhelp = new picocli.CommandLine.Help(spec);
        String description = AttributedString.stripAnsi(spec.usageMessage().sectionMap().get("description").render(cmdhelp).toString());
        out.addAll(Arrays.asList(description.split("\\r?\\n")));
        return out;
    }

    @Override
    public Object execute(CommandRegistry.CommandSession session, String command, String[] args) throws Exception {
        List<String> arguments = new ArrayList<>();
        arguments.add(command);
        arguments.addAll(Arrays.asList(args));
        cmd.execute(arguments.toArray(new String[0]));
        return null;
    }

    @Override
    public Set<String> commandNames() {
        return commands;
    }

    @Override
    public Map<String, String> commandAliases() {
        return aliasCommand;
    }
}
