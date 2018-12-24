package picocli.shell.jline3.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.TerminalBuilder;
import org.jline.terminal.Terminal;
import org.jline.reader.MaskingCallback;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.shell.jline3.PicocliJLineCompleter;

/**
 * Example that demonstrates how to build an interactive shell with JLine3 and picocli.
 * @since 3.7
 */
public class Example {

    /**
     * Top-level command that just prints help.
     */
    @Command(name = "", description = "Example interactive shell with completion",
            footer = {"", "Press Ctl-D to exit."},
            subcommands = {MyCommand.class, ClearScreen.class})
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
                    " (note that enum values also get completed)")
    static class MyCommand implements Runnable {
        @Option(names = {"-v", "--verbose"})
        private boolean[] verbosity = {};

        @Option(names = {"-d", "--duration"})
        private int amount;

        @Option(names = {"-u", "--timeUnit"})
        private TimeUnit unit;

        @ParentCommand CliCommands parent;

        public void run() {
            if (verbosity.length > 0) {
                parent.out.printf("Hi there. You asked for %d %s.%n", amount, unit);
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

    public static void main(String[] args) {
        try {
            // set up the completion
            CliCommands commands = new CliCommands();
            CommandLine cmd = new CommandLine(commands);
            Terminal terminal = TerminalBuilder.builder().build();
            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(new PicocliJLineCompleter(cmd.getCommandSpec()))
                    .parser(new DefaultParser())
                    .build();
            commands.setReader(reader);
            String prompt = "prompt> ";
            String rightPrompt = null;

            // start the shell and process input until the user quits with Ctl-D
            String line;
            while (true) {    
                line = reader.readLine(prompt, rightPrompt, (MaskingCallback) null, null);
                CommandLine.run(commands, line.split("\\s+"));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
