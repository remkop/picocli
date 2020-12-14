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
import picocli.CommandLine.IFactory;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;
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
            footer = {"", "Press Ctrl-D to exit."},
            subcommands = {MyCommand.class, ClearScreen.class, ReadInteractive.class})
    static class CliCommands implements Runnable {
        final ConsoleReader reader;
        final PrintWriter out;
        
        @Spec
        private CommandSpec spec;

        CliCommands(ConsoleReader reader) {
            this.reader = reader;
            out = new PrintWriter(reader.getOutput());
        }

        public void run() {
            out.println(spec.commandLine().getUsageMessage());
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
    
    /**
     * Command that optionally reads and password interactively.
     */
    @Command(name = "pwd", mixinStandardHelpOptions = true,
    		description = "Interactivly reads a password", version = "1.0")
    static class ReadInteractive implements Callable<Void> {
    	
    	@Option(names = {"-p"}, parameterConsumer = InteractiveParameterConsumer.class)
    	private String password;

        @ParentCommand CliCommands parent;

        public Void call() throws Exception {
            if(password == null) {
                parent.out.println("No password prompted");
            } else {
                parent.out.println("Password is '" + password + "'");
            }
            return null;
        }
    }
    
    public static void main(String[] args) {
        try {
            ConsoleReader reader = new ConsoleReader();
            IFactory factory = new CustomFactory(new InteractiveParameterConsumer(reader));
            
            // set up the completion
            CliCommands commands = new CliCommands(reader);
            CommandLine cmd = new CommandLine(commands, factory);
            reader.addCompleter(new PicocliJLineCompleter(cmd.getCommandSpec()));

            // start the shell and process input until the user quits with Ctrl-D
            String line;
            while ((line = reader.readLine("prompt> ")) != null) {
                ArgumentList list = new WhitespaceArgumentDelimiter()
                    .delimit(line, line.length());
                new CommandLine(commands, factory)
                    .execute(list.getArguments());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
