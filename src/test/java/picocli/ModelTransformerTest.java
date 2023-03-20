package picocli;

import org.junit.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class ModelTransformerTest {

    @Command(name = "gen-manpage", description = "Generates man pages")
    static class GenerateManPageCommand implements Runnable {
        public void run() {}
    }

    @Command(name = "mycmd",
        subcommands = {AutoComplete.GenerateCompletion.class, GenerateManPageCommand.class},
        modelTransformer = MyTransformer.class,
        mixinStandardHelpOptions = true)
    static class MyCommand implements Runnable {
        public void run() {}
    }

    static class MyTransformer implements CommandLine.IModelTransformer {
        public CommandLine.Model.CommandSpec transform(CommandLine.Model.CommandSpec commandSpec) {
            CommandLine gen = commandSpec.removeSubcommand("gen-manpage");
            commandSpec.addSubcommand("generate-manpage", gen);
            return commandSpec;
        }
    }

    @Test
    public void testUsage() {
        StringWriter sw = new StringWriter();
        // Explicitly disable Ansi to make sure that the cached isJansiConsoleInstalled
        // value doesn't inadvertently cause the usage help to enable ansi.
        new CommandLine(new MyCommand()).usage(new PrintWriter(sw), Ansi.OFF);
        String expected = String.format("" +
            "Usage: mycmd [-hV] [COMMAND]%n" +
            "  -h, --help      Show this help message and exit.%n" +
            "  -V, --version   Print version information and exit.%n" +
            "Commands:%n" +
            "  generate-completion  Generate bash/zsh completion script for mycmd.%n" +
            "  generate-manpage     Generates man pages%n");
        assertEquals(expected, sw.toString());
    }
}
