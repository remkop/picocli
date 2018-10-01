<p align="center"><img src="https://picocli.info/images/logo/horizontal-400x150.png" alt="picocli" height="150px"></p>


# Picocli Shell JLine2 - build interactive shells with ease

Picocli Shell JLine2 contains components and documentation for building
interactive shell command line applications with JLine 2 and picocli.

JLine and picocli complement each other very well and have little or none functional overlap.

JLine provides interactive shell functionality but has no built-in command line parsing functionality.
What it does provide is a tokenizer for splitting a single command line String into an array of command line argument Strings.

Given an array of Strings, picocli can execute a command or subcommand.
Combining these two libraries makes it easy to build powerful interactive shell applications.


## About JLine 2

[JLine 2](https://github.com/jline/jline2) is a well-known library for building interactive shell applications.
From the JLine [web site](https://github.com/jline/jline.github.io/blob/master/index.md):

> JLine is a Java library for handling console input. It is similar in functionality to [BSD editline](http://www.thrysoee.dk/editline/) and [GNU readline](http://www.gnu.org/s/readline/) but with additional features that bring it in par with [ZSH line editor](http://zsh.sourceforge.net/Doc/Release/Zsh-Line-Editor.html).

## About picocli
Picocli is a Java command line parser with both an annotations API and a programmatic API, featuring usage help with ANSI colors, autocomplete and nested subcommands.

The picocli user manual is [here](https://picocli.info), and the GitHub project is [here](https://github.com/remkop/picocli).

## Command Completer
`PicocliJLineCompleter` is a small component that generates completion candidates to allow users to
get command line TAB auto-completion for a picocli-based application running in a JLine 2 shell.

## Example

```java
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

```
