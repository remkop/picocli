package picocli.codegen.aot.graalvm;

import org.junit.Test;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static org.junit.Assert.*;

public class Issue793Test {

    @Command
    static class Issue793Command {
        static class Mode {
            @Option(names = {"-e", "--encrypt"}, required = true) boolean encrypt;
            @Option(names = {"-d", "--decrypt"}, required = true) boolean decrypt;
        }

        @ArgGroup(exclusive = true, multiplicity = "1")
        Mode mode;

        @ArgGroup Composite composite;
    }

    static class Composite {
        @ArgGroup(exclusive = true) Exclusive exclusive;
        @ArgGroup(exclusive = false) Dependent dependent;
    }
    static class Exclusive {
        @Option(names = {"-a"}, required = true) boolean a;
        @Option(names = {"-b"}, required = true) boolean b;
    }
    static class Dependent {
        @Option(names = {"-f"}, required = true) boolean f;
        @Option(names = {"-g"}, required = true) boolean g;
    }

    @Test
    public void testReflectionConfig() throws Exception {
        CommandLine commandLine = new CommandLine(new Issue793Command());
        String actual = ReflectionConfigGenerator.generateReflectionConfig(commandLine.getCommandSpec());
        String expected = String.format("" +
                "[%n" +
                "  {%n" +
                "    \"name\" : \"picocli.codegen.aot.graalvm.Issue793Test$Composite\",%n" +
                "    \"allDeclaredConstructors\" : true,%n" +
                "    \"allPublicConstructors\" : true,%n" +
                "    \"allDeclaredMethods\" : true,%n" +
                "    \"allPublicMethods\" : true,%n" +
                "    \"fields\" : [%n" +
                "      { \"name\" : \"dependent\" },%n" +
                "      { \"name\" : \"exclusive\" }%n" +
                "    ]%n" +
                "  },%n" +
                "  {%n" +
                "    \"name\" : \"picocli.codegen.aot.graalvm.Issue793Test$Dependent\",%n" +
                "    \"allDeclaredConstructors\" : true,%n" +
                "    \"allPublicConstructors\" : true,%n" +
                "    \"allDeclaredMethods\" : true,%n" +
                "    \"allPublicMethods\" : true,%n" +
                "    \"fields\" : [%n" +
                "      { \"name\" : \"f\" },%n" +
                "      { \"name\" : \"g\" }%n" +
                "    ]%n" +
                "  },%n" +
                "  {%n" +
                "    \"name\" : \"picocli.codegen.aot.graalvm.Issue793Test$Exclusive\",%n" +
                "    \"allDeclaredConstructors\" : true,%n" +
                "    \"allPublicConstructors\" : true,%n" +
                "    \"allDeclaredMethods\" : true,%n" +
                "    \"allPublicMethods\" : true,%n" +
                "    \"fields\" : [%n" +
                "      { \"name\" : \"a\" },%n" +
                "      { \"name\" : \"b\" }%n" +
                "    ]%n" +
                "  },%n" +
                "  {%n" +
                "    \"name\" : \"picocli.codegen.aot.graalvm.Issue793Test$Issue793Command\",%n" +
                "    \"allDeclaredConstructors\" : true,%n" +
                "    \"allPublicConstructors\" : true,%n" +
                "    \"allDeclaredMethods\" : true,%n" +
                "    \"allPublicMethods\" : true,%n" +
                "    \"fields\" : [%n" +
                "      { \"name\" : \"composite\" },%n" +
                "      { \"name\" : \"mode\" }%n" +
                "    ]%n" +
                "  },%n" +
                "  {%n" +
                "    \"name\" : \"picocli.codegen.aot.graalvm.Issue793Test$Issue793Command$Mode\",%n" +
                "    \"allDeclaredConstructors\" : true,%n" +
                "    \"allPublicConstructors\" : true,%n" +
                "    \"allDeclaredMethods\" : true,%n" +
                "    \"allPublicMethods\" : true,%n" +
                "    \"fields\" : [%n" +
                "      { \"name\" : \"decrypt\" },%n" +
                "      { \"name\" : \"encrypt\" }%n" +
                "    ]%n" +
                "  }%n" +
                "]%n");
        assertEquals(expected, actual);
    }
}
