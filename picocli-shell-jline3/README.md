<p align="center"><img src="https://picocli.info/images/logo/horizontal-400x150.png" alt="picocli" height="150px"></p>


# Picocli Shell JLine3 - build interactive shells with ease

Picocli Shell JLine3 contains components and documentation for building
interactive shell command line applications with JLine 3 and picocli.

JLine and picocli complement each other very well and have little or none functional overlap.

JLine provides interactive shell functionality but has no built-in command line parsing functionality.
What it does provide is a tokenizer for splitting a single command line String into an array of command line argument Strings.

Given an array of Strings, picocli can execute a command or subcommand.
Combining these two libraries makes it easy to build powerful interactive shell applications.

Here is an example of what can be achieved:

[![asciicast](https://asciinema.org/a/284482.svg)](https://asciinema.org/a/284482)

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
It does not use JLine's `TailTipWidgets` terminal status bar and can be used with older versions of JLine.

## PicocliCommands
Completion with description.

`PicocliCommands` is a small component, analogous to JLine's `Builtins`, that takes a `picocli.CommandLine`
object with a picocli-based command hierarchy and creates a JLine `SystemCompleter` for all commands in the hierarchy to set up command TAB completion in JLine.
In addition, it makes a `org.jline.builtins.Widgets.CmdDesc` object available for each command,
which allows a detailed description of the command and its options to be displayed in the JLine `TailTipWidgets` terminal status bar.
This component requires JLine 3.14.1 or greater.

### PicocliCommands Version Compatibility

The JLine `TailTipWidgets` terminal status bar is still under development, and the JLine API has undergone some backwards incompatible changes.
As a result, applications that use the `PicocliCommands` component for completion must take care to use compatible versions.

The following versions of `jline` and `picocli-shell-jline3` are compatible:

| JLine Version  | Picocli Version |
| -------------- | --------------- |
| 3.13.2 - 3.14.0 | 4.1.2 - 4.2.0 |
| 3.14.1  | 4.3.0 - 4.3.2 |
| 3.15.0 -  | 4.4.0 -  |

Note: JLine v3.17.1 is not compatible as it is affected by [this bug](https://github.com/jline/jline3/issues/640).

See [examples for the older versions](https://github.com/remkop/picocli/wiki/JLine-3-Examples).

## Demo

<a href="https://asciinema.org/a/284482?t=5" target="_blank"><img src="https://asciinema.org/a/284482.png" width="400" /></a>

JLine [Wiki](https://github.com/jline/jline3/wiki) and some more [Demos](https://github.com/jline/jline3/wiki/Demos).

## Example

### Maven

```xml
<dependency>
    <groupId>info.picocli</groupId>
    <artifactId>picocli-shell-jline3</artifactId>
    <version>4.7.5</version>
</dependency>
```

### Older versions

See examples for older versions on the [wiki](https://github.com/remkop/picocli/wiki/JLine-3-Examples).

* [Example for JLine 3.13.2 and Picocli 4.1.2](https://github.com/remkop/picocli/wiki/JLine-3-Examples#example-for-jline-3132-and-picocli-412)
* [Example for JLine 3.14.1 and Picocli 4.3.0 - 4.3.2](https://github.com/remkop/picocli/wiki/JLine-3-Examples#example-for-jline-3141-and-picocli-430---432)
* [Example for JLine 3.15 and Picocli 4.4+](https://github.com/remkop/picocli/wiki/JLine-3-Examples#example-for-jline-315-and-picocli-44)

### JLine 3.16 and Picocli 4.4+ Example

```java
package picocli.shell.jline3.example;

import org.fusesource.jansi.AnsiConsole;
import org.jline.builtins.ConfigurationPath;
import org.jline.console.SystemRegistry;
import org.jline.console.impl.Builtins;
import org.jline.console.impl.SystemRegistryImpl;
import org.jline.keymap.KeyMap;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.widget.TailTipWidgets;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.shell.jline3.PicocliCommands;
import picocli.shell.jline3.PicocliCommands.PicocliCommandsFactory;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Example that demonstrates how to build an interactive shell with JLine3 and picocli.
 * This example requires JLine 3.16+ and picocli 4.4+.
 * <p>
 * The built-in {@code PicocliCommands.ClearScreen} command was introduced in picocli 4.6.
 * </p>
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
            footer = {"", "Press Ctrl-D to exit."},
            subcommands = {
                    MyCommand.class, PicocliCommands.ClearScreen.class, CommandLine.HelpCommand.class})
    static class CliCommands implements Runnable {
        PrintWriter out;

        CliCommands() {}

        public void setReader(LineReader reader){
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

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        try {
            Supplier<Path> workDir = () -> Paths.get(System.getProperty("user.dir"));
            // set up JLine built-in commands
            Builtins builtins = new Builtins(workDir, new ConfigurationPath(workDir.get(), workDir.get()), null);
            builtins.rename(Builtins.Command.TTOP, "top");
            builtins.alias("zle", "widget");
            builtins.alias("bindkey", "keymap");
            // set up picocli commands
            CliCommands commands = new CliCommands();

            PicocliCommandsFactory factory = new PicocliCommandsFactory();
            // Or, if you have your own factory, you can chain them like this:
            // MyCustomFactory customFactory = createCustomFactory(); // your application custom factory
            // PicocliCommandsFactory factory = new PicocliCommandsFactory(customFactory); // chain the factories

            CommandLine cmd = new CommandLine(commands, factory);
            PicocliCommands picocliCommands = new PicocliCommands(cmd);

            Parser parser = new DefaultParser();
            try (Terminal terminal = TerminalBuilder.builder().build()) {
                SystemRegistry systemRegistry = new SystemRegistryImpl(parser, terminal, workDir, null);
                systemRegistry.setCommandRegistries(builtins, picocliCommands);
                systemRegistry.register("help", picocliCommands);

                LineReader reader = LineReaderBuilder.builder()
                        .terminal(terminal)
                        .completer(systemRegistry.completer())
                        .parser(parser)
                        .variable(LineReader.LIST_MAX, 50)   // max tab completion candidates
                        .build();
                builtins.setLineReader(reader);
                commands.setReader(reader);
                factory.setTerminal(terminal);
                TailTipWidgets widgets = new TailTipWidgets(reader, systemRegistry::commandDescription, 5, TailTipWidgets.TipType.COMPLETER);
                widgets.enable();
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
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            AnsiConsole.systemUninstall();
        }
    }
}
```
