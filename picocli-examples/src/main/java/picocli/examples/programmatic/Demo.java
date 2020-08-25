package picocli.examples.programmatic;

import picocli.CommandLine;

import static picocli.CommandLine.*;
import static picocli.CommandLine.Model.*;

public class Demo {

    public static void main(String[] args) {
        final CommandSpec rootSpec = CommandSpec.create();

        final String commandName = "determined-at-runtime-from-remote-source";
        rootSpec.addSubcommand(
                commandName,
                CommandSpec.wrapWithoutInspection(
                        new Runnable() {
                            @Override
                            public void run() {
                                CommandLine me = rootSpec.subcommands().get(commandName);
                                System.out.printf("Running %s...%n", commandName);

                                CommandSpec spec = me.getCommandSpec();
                                for (OptionSpec option : spec.options()) {
                                    System.out.printf("%s='%s'%n", option.longestName(), option.getValue());
                                }

                                // let's print this command's usage, just to see if that works...
                                me.usage(System.out);
                            }
                        })
                        .addOption(OptionSpec.builder("runtime-option-a").build()));

        System.exit(new CommandLine(rootSpec)
                //.setExecutionStrategy(new RunLast()) // this is already the default
                .execute(commandName));
    }
}
