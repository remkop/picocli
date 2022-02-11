package picocli;

import org.junit.Ignore;
import org.junit.Test;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.IParameterConsumer;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;

import java.util.Stack;

import static org.junit.Assert.*;

public class Issue1549ArgGroupParamConsumer {
    static class MyCommand {
        static class TestConsumer implements IParameterConsumer {
            public void consumeParameters(Stack<String> args, ArgSpec argSpec, CommandSpec commandSpec) {
                argSpec.setValue(args.pop());
            }
        }

        @ArgGroup
        Group group;

        static class Group {
            @Option(names = "-foo"/*, parameterConsumer = TestConsumer.class*/)
            String foo;

            @Option(names = "-bar")
            String bar;
        }
    }

    @Ignore("https://github.com/remkop/picocli/issues/1549")
    @Test
    public void testIssue1549() {
//        try {
            new CommandLine(new MyCommand()).parseArgs("-foo", "value", "-bar", "value");
            fail("Expected exception");
//        } catch (CommandLine.MutuallyExclusiveArgsException ex) {
//            String msg = "Error: -foo=<foo>, -bar=<bar> are mutually exclusive (specify only one)";
//            assertEquals(msg, ex.getMessage());
//        }
    }

}
