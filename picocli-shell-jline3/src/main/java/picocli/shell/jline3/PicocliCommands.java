package picocli.shell.jline3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.jline.builtins.Options.HelpException;
import org.jline.console.ArgDesc;
import org.jline.console.CmdDesc;
import org.jline.console.CommandRegistry;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.SystemCompleter;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.InfoCmp.Capability;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;

/**
 * Compiles SystemCompleter for command completion and implements a method commandDescription() that provides command descriptions
 * for JLine TailTipWidgets to be displayed in terminal status bar.
 * SystemCompleter implements the JLine 3 {@link Completer} interface. SystemCompleter generates completion
 * candidates for the specified command line based on the {@link CommandSpec} that this {@code PicocliCommands} was constructed with.
 *
 * @since 4.1.2
 */
public class PicocliCommands implements CommandRegistry {
    private String picocliCommandsName;

    /**
     * Command that clears the screen.
     * <p>
     * <b>WARNING:</b> This subcommand needs a JLine {@code Terminal} to clear the screen.
     * To accomplish this, construct the {@code CommandLine} with a {@code PicocliCommandsFactory},
     * and set the {@code Terminal} on that factory. For example:
     * <pre>
     * &#064;Command(subcommands = PicocliCommands.ClearScreen.class)
     * class MyApp //...
     *
     * PicocliCommandsFactory factory = new PicocliCommandsFactory();
     * CommandLine cmd = new CommandLine(new MyApp(), factory);
     * // create terminal
     * factory.setTerminal(terminal);
     * </pre>
     *
     * @since 4.6
     */
    @Command(name = "cls", aliases = "clear", mixinStandardHelpOptions = true,
            description = "Clears the screen", version = "1.0")
    public static class ClearScreen implements Callable<Void> {

        private final Terminal terminal;

        ClearScreen(Terminal terminal) { this.terminal = terminal; }

        public Void call() throws IOException {
            if (terminal != null) { terminal.puts(Capability.clear_screen); }
            return null;
        }
    }

    /**
     * Command factory that is necessary for applications that want the use the {@code ClearScreen} subcommand.
     * It can be chained with other factories.
     * <p>
     * <b>WARNING:</b> If the application uses the {@code ClearScreen} subcommand,  construct the {@code CommandLine}
     * with a {@code PicocliCommandsFactory}, and set the {@code Terminal} on that factory. Applications need
     * to call the {@code setTerminal} method with a {@code Terminal}; this will be passed to the {@code ClearScreen}
     * subcommand.
     *
     * For example:
     * <pre>
     * PicocliCommandsFactory factory = new PicocliCommandsFactory();
     * CommandLine cmd = new CommandLine(new MyApp(), factory);
     * // create terminal
     * factory.setTerminal(terminal);
     * </pre>
     *
     * Other factories can be chained by passing them in to the constructor like this:
     * <pre>
     * MyCustomFactory customFactory = createCustomFactory(); // your application custom factory
     * PicocliCommandsFactory factory = new PicocliCommandsFactory(customFactory); // chain the factories
     * </pre>
     *
     * @since 4.6
     */
    public static class PicocliCommandsFactory implements CommandLine.IFactory {
        private CommandLine.IFactory nextFactory;
        private Terminal terminal;

        public PicocliCommandsFactory() {
            // nextFactory and terminal are null
        }

        public PicocliCommandsFactory(IFactory nextFactory) {
            this.nextFactory = nextFactory;
            // nextFactory is set (but may be null) and terminal is null
        }

        @SuppressWarnings("unchecked")
        public <K> K create(Class<K> clazz) throws Exception {
            if (ClearScreen.class == clazz) { return (K) new ClearScreen(terminal); }
            if (nextFactory != null) { return nextFactory.create(clazz); }
            return CommandLine.defaultFactory().create(clazz);
        }

        public void setTerminal(Terminal terminal) {
            this.terminal = terminal;
            // terminal may be null, so check before using it in ClearScreen command
        }
    }

    private final CommandLine cmd;
    private final Set<String> commands;
    private final Map<String,String> aliasCommand = new HashMap<>();

    public PicocliCommands(CommandLine cmd) {
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

    private class PicocliCompleter extends ArgumentCompleter implements Completer {

        public PicocliCompleter() { super(NullCompleter.INSTANCE); }

        @Override
        public void complete(LineReader reader, ParsedLine commandLine, List<Candidate> candidates) {
            assert commandLine != null;
            assert candidates != null;
            String word = commandLine.word();
            List<String> words = commandLine.words();
            CommandLine sub = findSubcommandLine(words, commandLine.wordIndex());
            if (sub == null) {
                return;
            }
            if (word.startsWith("-")) {
                String buffer = word.substring(0, commandLine.wordCursor());
                int eq = buffer.indexOf('=');
                for (OptionSpec option : sub.getCommandSpec().options()) {
                    if (option.hidden()) continue;
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
                    if (!s.getCommandSpec().usageMessage().hidden()) {
                        addCandidates(candidates, Arrays.asList(s.getCommandSpec().aliases()));
                    }
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

    }

    private CommandLine findSubcommandLine(List<String> args, int lastIdx) {
        CommandLine out = cmd;
        for (int i = 0; i < lastIdx; i++) {
            if (!args.get(i).startsWith("-")) {
                out = findSubcommandLine(out, args.get(i));
                if (out == null) {
                    break;
                }
            }
        }
        return out;
    }

    private CommandLine findSubcommandLine(CommandLine cmdline, String command) {
        for (CommandLine s : cmdline.getSubcommands().values()) {
            if (s.getCommandName().equals(command) || Arrays.asList(s.getCommandSpec().aliases()).contains(command)) {
                return s;
            }
        }
        return null;
    }

    /**
     *
     * @param args
     * @return command description for JLine TailTipWidgets to be displayed in terminal status bar.
     */
    @Override
    public CmdDesc commandDescription(List<String> args) {
        CommandLine sub = findSubcommandLine(args, args.size());
        if (sub == null) {
            return null;
        }
        CommandSpec spec = sub.getCommandSpec();
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

    // For JLine >= 3.16.0
    @Override
    public Object invoke(CommandRegistry.CommandSession session, String command, Object... args) throws Exception {
        List<String> arguments = new ArrayList<>();
        arguments.add( command );
        arguments.addAll( Arrays.stream( args ).map( Object::toString ).collect( Collectors.toList() ) );
        cmd.execute( arguments.toArray( new String[0] ) );
        return null;
    }

    // @Override This method was removed in JLine 3.16.0; keep it in case this component is used with an older version of JLine
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

    // @Override This method was removed in JLine 3.16.0; keep it in case this component is used with an older version of JLine
    public CmdDesc commandDescription(String command) {
        return null;
    }

    /**
     * Returns the name shown for this collection of picocli commands in the usage help message.
     * If not set with {@link #name(String)}, this returns {@link CommandRegistry#name()}.
     * @return the name shown for this collection of picocli commands in the usage help message
     */
    @Override
    public String name() {
        if (picocliCommandsName != null) {
            return picocliCommandsName;
        }
        return CommandRegistry.super.name();
    }

    /**
     * Sets the name shown for this collection of picocli commands in the usage help message.
     * @param newName the new name to show
     */
    public void name(String newName) {
        picocliCommandsName = newName;
    }
}
