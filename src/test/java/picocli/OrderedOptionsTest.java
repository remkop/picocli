package picocli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

// https://github.com/remkop/picocli/issues/761
public class OrderedOptionsTest {

    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ProvideSystemProperty ansiOFF = new ProvideSystemProperty("picocli.ansi", "false");

    @Command(name = "rsync")
    static class Rsync implements Runnable {

        @Option(names = "--include")
        List<String> includes;

        @Option(names = "--exclude")
        List<String> excludes;

        @Spec CommandSpec spec;

        public void run() {
            ParseResult pr = spec.commandLine().getParseResult();
            List<ArgSpec> optionSpecs = pr.matchedArgs();
            // do something
        }
    }

    @Test
    public void testOrderWithParseResult() {
        CommandLine cmd = new CommandLine(new Rsync());
        ParseResult parseResult = cmd.parseArgs("--include", "a", "--exclude", "b", "--include", "c", "--exclude", "d");
        List<ArgSpec> argSpecs = parseResult.matchedArgs();
        assertEquals(4, argSpecs.size());
        assertEquals("--include", ((OptionSpec) argSpecs.get(0)).longestName());
        assertEquals("--exclude", ((OptionSpec) argSpecs.get(1)).longestName());
        assertEquals("--include", ((OptionSpec) argSpecs.get(2)).longestName());
        assertEquals("--exclude", ((OptionSpec) argSpecs.get(3)).longestName());

        List<OptionSpec> matchedOptions = parseResult.matchedOptions();
        assertEquals(4, matchedOptions.size());

        assertEquals("--include", matchedOptions.get(0).longestName());
        assertSame(matchedOptions.get(0), matchedOptions.get(2));
        assertEquals(Arrays.asList("a"), matchedOptions.get(0).typedValues().get(0));
        assertEquals(Arrays.asList("c"), matchedOptions.get(2).typedValues().get(1));

        assertEquals("--exclude", matchedOptions.get(1).longestName());
        assertSame(matchedOptions.get(1), matchedOptions.get(3));
        assertEquals(Arrays.asList("b"), matchedOptions.get(1).typedValues().get(0));
        assertEquals(Arrays.asList("d"), matchedOptions.get(3).typedValues().get(1));
    }
}
