package picocli.spring.boot.autoconfigure.example.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import picocli.CommandLine;
import picocli.spring.boot.autoconfigure.sample.MyCommand;
import picocli.spring.boot.autoconfigure.sample.MySpringApp;

import static org.junit.Assert.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 * Add the following dependency to your build.gradle:
 *
 * <pre>
 * dependencies {
 *     // ...
 *     testImplementation "org.springframework.boot:spring-boot-starter-test:$springBootVersion"
 * }
 * </pre>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = NONE, classes = MySpringApp.class)
public class ExampleTest {

    @Autowired
    CommandLine.IFactory factory;

    @Autowired
    MyCommand myCommand;

    @Test
    public void testParsingCommandLineArgs() {
        CommandLine.ParseResult parseResult = new CommandLine(myCommand, factory)
            .parseArgs("-x", "abc", "sub", "-y", "123");
        assertEquals("abc", myCommand.x);
        assertNull(myCommand.positionals);

        assertTrue(parseResult.hasSubcommand());
        CommandLine.ParseResult subResult = parseResult.subcommand();
        MyCommand.Sub sub = (MyCommand.Sub) subResult.commandSpec().userObject();
        assertEquals("123", sub.y);
        assertNull(sub.positionals);
    }

    @Test
    public void testUsageHelp() {
        String expected = String.format("" +
            "Usage: mycommand [-hV] [-x=<x>] [<positionals>...] [COMMAND]%n" +
            "      [<positionals>...]   positional params%n" +
            "  -h, --help               Show this help message and exit.%n" +
            "  -V, --version            Print version information and exit.%n" +
            "  -x=<x>                   optional option%n" +
            "Commands:%n" +
            "  sub%n");
        String actual = new CommandLine(myCommand, factory).getUsageMessage(CommandLine.Help.Ansi.OFF);
        assertEquals(expected, actual);
    }
}
