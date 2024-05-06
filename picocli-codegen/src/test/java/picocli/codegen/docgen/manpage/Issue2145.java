package picocli.codegen.docgen.manpage;

import org.junit.Test;
import picocli.CommandLine;

import static org.junit.Assert.*;

public class Issue2145 {

    @CommandLine.Command(
        name = "documentation",
        description = "Command to generate CLI documentation",
        subcommands = ManPageGenerator.class
    )
    static class Documentation {

    }

    @Test
    public void testManPageGenAsSubcommand() {
        int result = new CommandLine(new Documentation()).execute("gen-manpage", "-dout", "-v");
        assertEquals(0, result);
    }
}
