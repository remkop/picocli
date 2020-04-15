package picocli.examples.arggroup;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Map;

// Demo for https://stackoverflow.com/questions/61203155/picocli-is-it-possible-to-define-options-with-a-space-in-the-name
public class SO61203155 {

    @Command(name = "group-demo", mixinStandardHelpOptions = true,
            sortOptions = false)
    static class UsingGroups implements Runnable {

        static class MyGroup {
            @Option(names = "-VAR", description = "Option prefix. Must be followed by one of ARGUMENT1, ARGUMENT2 or BOOLEANARG")
            boolean ignored;

            static class InnerGroup {
                @Option(names = "ARGUMENT1", description = "An arg. Must be preceded by -VAR.")
                String arg1;

                @Option(names = "ARGUMENT2", description = "Another arg. Must be preceded by -VAR.")
                String arg2;

                @Option(names = "BOOLEANARG", arity = "1", description = "A boolean arg. Must be preceded by -VAR.")
                boolean arg3;
            }

            // exclusive: only one of these options can follow a -VAR option
            @ArgGroup(multiplicity = "1", exclusive = true)
            InnerGroup inner;
        }


        // non-exclusive means co-occuring, so if -VAR is specified,
        // then it must be followed by one of the InnerGroup options
        @ArgGroup(multiplicity = "0..*", exclusive = false)
        MyGroup group;

        @Override
        public void run() {
            // business logic here
        }

        public static void main(String[] args) {
            new CommandLine(new UsingGroups()).execute(args);
        }
    }


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
