package picocli;

import java.io.IOException;

import org.junit.Test;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static picocli.CommandLine.*;

public class AutoCompleteHyphenatedCommandTest {

    @Test
    public void testCompletionScript() throws IOException {
        String actual = AutoComplete.bash("rcmd", new CommandLine(new HyphenatedCommand()));
        String expected = format(AutoCompleteTest.loadTextFromClasspath("/hyphenated_completion.bash"), CommandLine.VERSION);
        assertEquals(expected, actual);
    }
}

@Command(
        name = "rcmd",
        mixinStandardHelpOptions = true,
        subcommands = {HyphenatedCommand.Sub1Command.class, HyphenatedCommand.Sub2Command.class}
)
class HyphenatedCommand {

    public static void main(String... args) {
        CommandLine command = new CommandLine(new HyphenatedCommand());
        command.parseWithHandler(new RunAll(), args);
    }

    @Command(name = "sub-1", mixinStandardHelpOptions = true)
    public static class Sub1Command implements Runnable{

        @Option(names = "option1")
        int option1;

        @Option(names = "flag1")
        boolean flag1;

        public void run() {
            System.out.println(String.format("option1=%d, flag1=%s", option1, flag1));
        }
    }

    @Command(name = "sub-2", mixinStandardHelpOptions = true)
    public static class Sub2Command implements Runnable{

        @Option(names = "option-2")
        int option2;

        @Option(names = "flag-2")
        boolean flag2;

        public void run() {
            System.out.println(String.format("option-2=%d, flag-2=%s", option2, flag2));
        }
    }
}