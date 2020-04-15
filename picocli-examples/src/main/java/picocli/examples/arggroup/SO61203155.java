package picocli.examples.arggroup;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Map;

// Demo for https://stackoverflow.com/questions/61203155/picocli-is-it-possible-to-define-options-with-a-space-in-the-name
public class SO61203155 {

@Command(name = "group-demo", mixinStandardHelpOptions = true,
        sortOptions = false)
static class UsingGroups implements Runnable {

    static class MyGroup {
        @Option(names = "-VAR", required = true,
          description = "Option prefix. Must be followed by one of ARGUMENT1, ARGUMENT2 or BOOLEANARG")
        boolean ignored;

        static class InnerGroup {
            @Option(names = "ARGUMENT1", description = "An arg. Must be preceded by -VAR.")
            String arg1;

            @Option(names = "ARGUMENT2", description = "Another arg. Must be preceded by -VAR.")
            String arg2;

            @Option(names = "BOOLEANARG", arity = "1",
              description = "A boolean arg. Must be preceded by -VAR.")
            Boolean arg3;
        }

        // exclusive: only one of these options can follow a -VAR option
        // multiplicity=1: InnerGroup must occur once
        @ArgGroup(multiplicity = "1", exclusive = true)
        InnerGroup inner;
    }


    // non-exclusive means co-occurring, so if -VAR is specified,
    // then it must be followed by one of the InnerGroup options
    @ArgGroup(multiplicity = "0..*", exclusive = false)
    List<MyGroup> groupOccurrences;

    @Override
    public void run() {
        // business logic here

        System.out.printf("You specified %d -VAR options.%n", groupOccurrences.size());
        for (MyGroup group : groupOccurrences) {
            System.out.printf("ARGUMENT1=%s, ARGUMENT2=%s, BOOLEANARG=%s%n",
                    group.inner.arg1, group.inner.arg2, group.inner.arg3);
        }
    }

    public static void main(String[] args) {
        new CommandLine(new UsingGroups()).execute(args);
    }
}


    @Command(separator = " ")
    static class Simple implements Runnable {

        enum MyOption {ARGUMENT1, ARGUMENT2, BOOLEANARG}

        @Option(names = "-VAR",
                description = "Variable options. Valid keys: ${COMPLETION-CANDIDATES}.")
        Map<MyOption, String> options;

        @Override
        public void run() {
            // business logic here
        }

        public static void main(String[] args) {
            new CommandLine(new Simple()).execute(args);
        }
    }
}
