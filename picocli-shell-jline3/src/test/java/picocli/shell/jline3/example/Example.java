package picocli.shell.jline3.example;

import org.fusesource.jansi.AnsiConsole;
import org.jline.builtins.Builtins;
import org.jline.builtins.Widgets.TailTipWidgets;
import org.jline.builtins.Widgets.TailTipWidgets.TipType;
import org.jline.keymap.KeyMap;
import org.jline.builtins.SystemRegistry;
import org.jline.builtins.SystemRegistryImpl;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.shell.jline3.PicocliCommands;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Example that demonstrates how to build an interactive shell with JLine3 and picocli.
 * @since 4.1.2
 */
public class Example {

    /**
     * Top-level command that just prints help.
     */
    @Command(name = "",
            description = {
                    "Example interactive shell with completion and autosuggestions. " +
                            "Hit @|magenta <TAB>|@ to see available commands.",
                            "Hit @|magenta ALT-S|@ to toggle tailtips.",
                    ""},
            footer = {"", "Press Ctl-D to exit."},
            subcommands = {
                    MyCommand.class, ClearScreen.class, CommandLine.HelpCommand.class})
    static class CliCommands implements Runnable {
        LineReaderImpl reader;
        PrintWriter out;

        CliCommands() {}

        public void setReader(LineReader reader){
            this.reader = (LineReaderImpl)reader;
            out = reader.getTerminal().writer();
        }

        public void run() {
            out.println(new CommandLine(this).getUsageMessage());
        }
    }

    /**
     * A command with some options to demonstrate completion.
     */
    @Command(name = "cmd", mixinStandardHelpOptions = true, version = "1.0",
            description = "Command with some options to demonstrate TAB-completion" +
                    " (note that enum values also get completed)",
            subcommands = CommandLine.HelpCommand.class)
    static class MyCommand implements Runnable {
        @Option(names = {"-v", "--verbose"},
                description = { "Specify multiple -v options to increase verbosity.",
                        "For example, `-v -v -v` or `-vvv`"})
        private boolean[] verbosity = {};

        @ArgGroup(exclusive = false)
        private MyDuration myDuration = new MyDuration();

        static class MyDuration {
            @Option(names = {"-d", "--duration"},
                    description = "The duration quantity.",
                    required = true)
            private int amount;

            @Option(names = {"-u", "--timeUnit"},
                    description = "The duration time unit.",
                    required = true)
            private TimeUnit unit;
        }

        @ParentCommand CliCommands parent;

        public void run() {
            if (verbosity.length > 0) {
                parent.out.printf("Hi there. You asked for %d %s.%n",
                        myDuration.amount, myDuration.unit);
            } else {
                parent.out.println("hi!");
            }
        }
    }

    /**
     * Command that clears the screen.
     */
    @Command(name = "cls", aliases = "clear", mixinStandardHelpOptions = true,
            description = "Clears the screen", version = "1.0")
    static class ClearScreen implements Callable<Void> {

        @ParentCommand CliCommands parent;

        public Void call() throws IOException {
            parent.reader.clearScreen();
            return null;
        }
    }

    private static Path workDir() {
        return Paths.get(System.getProperty("user.dir"));
    }

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        try {
            // set up JLine built-in commands
            Builtins builtins = new Builtins(Example::workDir, null, null);
            builtins.rename(org.jline.builtins.Builtins.Command.TTOP, "top");
            builtins.alias("zle", "widget");
            builtins.alias("bindkey", "keymap");
            // set up picocli commands
            CliCommands commands = new CliCommands();
            CommandLine cmd = new CommandLine(commands);
            PicocliCommands picocliCommands = new PicocliCommands(Example::workDir, cmd);

            Parser parser = new DefaultParser();
            Terminal terminal = TerminalBuilder.builder().build();

            SystemRegistry systemRegistry = new SystemRegistryImpl(parser, terminal, Example::workDir, null);
            systemRegistry.setCommandRegistries(builtins, picocliCommands);

            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(systemRegistry.completer())
                    .parser(parser)
                    .variable(LineReader.LIST_MAX, 50)   // max tab completion candidates
                    .build();
            builtins.setLineReader(reader);
            commands.setReader(reader);
            new TailTipWidgets(reader, systemRegistry::commandDescription, 5, TipType.COMPLETER);
            KeyMap<Binding> keyMap = reader.getKeyMaps().get("main");
            keyMap.bind(new Reference("tailtip-toggle"), KeyMap.alt("s"));

            String prompt = "prompt> ";
            String rightPrompt = null;

            // start the shell and process input until the user quits with Ctrl-D
            String line;
            while (true) {
                try {
                    systemRegistry.cleanUp();
                    line = reader.readLine(prompt, rightPrompt, (MaskingCallback) null, null);
                    systemRegistry.execute(line);
                } catch (UserInterruptException e) {
                    // Ignore
                } catch (EndOfFileException e) {
                    return;
                } catch (Exception e) {
                    systemRegistry.trace(e);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
