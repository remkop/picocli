package picocli.shell.jline2.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter.ArgumentList;
import jline.console.completer.ArgumentCompleter.WhitespaceArgumentDelimiter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.shell.jline2.PicocliJLineCompleter;

/**
 * Example that demonstrates how to build an interactive shell with JLine and picocli.
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
        final ConsoleReader reader;
        final PrintWriter out;

        CliCommands(ConsoleReader reader) {
            this.reader = reader;
            out = new PrintWriter(reader.getOutput());
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
            ConsoleReader reader = new ConsoleReader();
            reader.setPrompt("prompt> ");

            // set up the completion
            CliCommands commands = new CliCommands(reader);
            CommandLine cmd = new CommandLine(commands);
            reader.addCompleter(new PicocliJLineCompleter(cmd.getCommandSpec()));

            // start the shell and process input until the user quits with Ctl-D
            String line;
            while ((line = reader.readLine()) != null) {
                ArgumentList list =
                        new WhitespaceArgumentDelimiter().delimit(line, line.length());
                CommandLine.run(commands, list.getArguments());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
