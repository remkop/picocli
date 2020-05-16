<p align="center"><img src="https://picocli.info/images/logo/horizontal-400x150.png" alt="picocli" height="150px"></p>


# Picocli Shell JLine3 - build interactive shells with ease

Picocli Shell JLine3 contains components and documentation for building
interactive shell command line applications with JLine 3 and picocli.

JLine and picocli complement each other very well and have little or none functional overlap.

JLine provides interactive shell functionality but has no built-in command line parsing functionality.
What it does provide is a tokenizer for splitting a single command line String into an array of command line argument Strings.

Given an array of Strings, picocli can execute a command or subcommand.
Combining these two libraries makes it easy to build powerful interactive shell applications.


## About JLine 3

[JLine 3](https://github.com/jline/jline3) is a well-known library for building interactive shell applications.
From the JLine [web site](https://github.com/jline/jline.github.io/blob/master/index.md):

> JLine is a Java library for handling console input. It is similar in functionality to [BSD editline](http://www.thrysoee.dk/editline/) and [GNU readline](http://www.gnu.org/s/readline/) but with additional features that bring it in par with [ZSH line editor](http://zsh.sourceforge.net/Doc/Release/Zsh-Line-Editor.html).

## About picocli
Picocli is a Java command line parser with both an annotations API and a programmatic API, featuring usage help with ANSI colors, autocomplete and nested subcommands.

The picocli user manual is [here](https://picocli.info), and the GitHub project is [here](https://github.com/remkop/picocli).

## Command Completer
`PicocliJLineCompleter` is a small component that generates completion candidates to allow users to
get command line TAB auto-completion for a picocli-based application running in a JLine 3 shell.

## PicocliCommands
`PicocliCommands` is a small component, analogous to JLine's `Builtins`, that takes a `picocli.CommandLine`
object with a picocli-based command hierarchy and creates a JLine `SystemCompleter` for all commands
in the hierarchy to set up command TAB completion in JLine.
In addition, it makes a `org.jline.builtins.Widgets.CmdDesc` object available for each command,
which allows a detailed description of the command and its options to be displayed
in the JLine `TailTipWidgets` terminal status bar.


## Demo

<a href="https://asciinema.org/a/284482?t=5" target="_blank"><img src="https://asciinema.org/a/284482.png" width="400" /></a>

JLine [Wiki](https://github.com/jline/jline3/wiki) and some more [Demos](https://github.com/jline/jline3/wiki/Demos).

## Example for JLine 3.13.2 and Picocli 4.1.2

The following example requires JLine 3.13.2 or higher.

```java
package picocli.shell.jline3.example;

import org.fusesource.jansi.AnsiConsole;
import org.jline.builtins.Builtins;
import org.jline.builtins.Completers.SystemCompleter;
import org.jline.builtins.Options.HelpException;
import org.jline.builtins.Widgets.CmdDesc;
import org.jline.builtins.Widgets.CmdLine;
import org.jline.builtins.Widgets.TailTipWidgets;
import org.jline.builtins.Widgets.TailTipWidgets.TipType;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
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
import java.util.Arrays;
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
                    "Example interactive shell with completion. " +
                            "Hit @|magenta <TAB>|@ to see available commands.",
                    "Type `@|bold,yellow keymap ^[s tailtip-toggle|@`, " +
                            "then hit @|magenta ALT-S|@ to toggle tailtips.",
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

    /**
     * Provide command descriptions for JLine TailTipWidgets
     * to be displayed in the status bar.
     */
    private static class DescriptionGenerator {
        Builtins builtins;
        PicocliCommands picocli;

        public DescriptionGenerator(Builtins builtins, PicocliCommands picocli) {
            this.builtins = builtins;
            this.picocli = picocli;
        }

        CmdDesc commandDescription(CmdLine line) {
            CmdDesc out = null;
            switch (line.getDescriptionType()) {
                case COMMAND:
                    String cmd = Parser.getCommand(line.getArgs().get(0));
                    if (builtins.hasCommand(cmd)) {
                        out = builtins.commandDescription(cmd);
                    } else if (picocli.hasCommand(cmd)) {
                        out = picocli.commandDescription(cmd);
                    }
                    break;
                default:
                    break;
            }
            return out;
        }
    }

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        try {
            // set up JLine built-in commands
            Path workDir = Paths.get("");
            Builtins builtins = new Builtins(workDir, null, null);
            builtins.rename(org.jline.builtins.Builtins.Command.TTOP, "top");
            builtins.alias("zle", "widget");
            builtins.alias("bindkey", "keymap");
            SystemCompleter systemCompleter = builtins.compileCompleters();
            // set up picocli commands
            CliCommands commands = new CliCommands();
            CommandLine cmd = new CommandLine(commands);
            PicocliCommands picocliCommands = new PicocliCommands(workDir, cmd);
            systemCompleter.add(picocliCommands.compileCompleters());
            systemCompleter.compile();
            Terminal terminal = TerminalBuilder.builder().build();
            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(systemCompleter)
                    .parser(new DefaultParser())
                    .variable(LineReader.LIST_MAX, 50)   // max tab completion candidates
                    .build();
            builtins.setLineReader(reader);
            commands.setReader(reader);
            DescriptionGenerator descriptionGenerator = new DescriptionGenerator(builtins, picocliCommands);
            new TailTipWidgets(reader, descriptionGenerator::commandDescription, 5, TipType.COMPLETER);

            String prompt = "prompt> ";
            String rightPrompt = null;

            // start the shell and process input until the user quits with Ctl-D
            String line;
            while (true) {
                try {
                    line = reader.readLine(prompt, rightPrompt, (MaskingCallback) null, null);
                    if (line.matches("^\\s*#.*")) {
                        continue;
                    }
                    ParsedLine pl = reader.getParser().parse(line, 0);
                    String[] arguments = pl.words().toArray(new String[0]);
                    String command = Parser.getCommand(pl.word());
                    if (builtins.hasCommand(command)) {
                        builtins.execute(command, Arrays.copyOfRange(arguments, 1, arguments.length)
                                , System.in, System.out, System.err);
                    } else {
                        new CommandLine(commands).execute(arguments);
                    }
                } catch (HelpException e) {
                    HelpException.highlight(e.getMessage(), HelpException.defaultStyle()).print(terminal);
                } catch (UserInterruptException e) {
                    // Ignore
                } catch (EndOfFileException e) {
                    return;
                } catch (Exception e) {
                    AttributedStringBuilder asb = new AttributedStringBuilder();
                    asb.append(e.getMessage(), AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
                    asb.toAttributedString().println(terminal);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
```

## Example for JLine 3.14.1 and Picocli 4.3+

Picocli 4.3.0 fixes an autocompletion issue with nested subcommands, thanks to a [pull request](https://github.com/remkop/picocli/pull/987) by one of the JLine authors.

This requires JLine 3.14.1, and things need to wired up differently with the new version of the `PicocliCommands` completer.
Here is the updated example:

```java
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
 * @since 4.3.2
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
            description = {"Command with some options to demonstrate TAB-completion.",
                    " (Note that enum values also get completed.)"},
            subcommands = {Nested.class, CommandLine.HelpCommand.class})
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

    @Command(name = "nested", mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
            description = "Hosts more sub-subcommands")
    static class Nested implements Runnable {
        public void run() {
            System.out.println("I'm a nested subcommand. I don't do much, but I have sub-subcommands!");
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Multiplies two numbers.")
        public void multiply(@Option(names = {"-l", "--left"}, required = true) int left,
                             @Option(names = {"-r", "--right"}, required = true) int right) {
            System.out.printf("%d * %d = %d%n", left, right, left * right);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Adds two numbers.")
        public void add(@Option(names = {"-l", "--left"}, required = true) int left,
                        @Option(names = {"-r", "--right"}, required = true) int right) {
            System.out.printf("%d + %d = %d%n", left, right, left + right);
        }

        @Command(mixinStandardHelpOptions = true, subcommands = {CommandLine.HelpCommand.class},
                description = "Subtracts two numbers.")
        public void subtract(@Option(names = {"-l", "--left"}, required = true) int left,
                             @Option(names = {"-r", "--right"}, required = true) int right) {
            System.out.printf("%d - %d = %d%n", left, right, left - right);
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
```
