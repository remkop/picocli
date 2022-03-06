package picocli.examples.module.simple;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModularCliTest {
    @Test public void testGetMessage() {
        CommandLine cmd = new CommandLine(new ModularCli());
        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw, true));
        cmd.execute();
        assertEquals(String.format("Hello modular world!%n"), sw.toString());
    }
}
